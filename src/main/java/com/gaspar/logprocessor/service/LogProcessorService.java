package com.gaspar.logprocessor.service;

import com.gaspar.logprocessor.constants.GuiConstants;
import com.gaspar.logprocessor.constants.JsonGenerator;
import com.gaspar.logprocessor.constants.LogExtension;
import com.gaspar.logprocessor.constants.Setting;
import com.gaspar.logprocessor.exception.SettingsException;
import com.gaspar.logprocessor.gui.panel.BottomBarPanel;
import com.gaspar.logprocessor.model.CleanedWvwLog;
import com.gaspar.logprocessor.runnable.DpsReportLogProcessorRunnable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

//stateful service!
@Service
@Slf4j
@RequiredArgsConstructor
public class LogProcessorService {

    private final SettingsService settingsService;
    private final DpsReportJsonCreatorService dpsReportJsonCreatorService;
    private final EliteInsightJsonCreatorService eliteInsightJsonCreatorService;
    private final BottomBarPanel bottomBarPanel;

    private int logAmount;
    private int processedAmount;
    private int failedAmount;
    private volatile boolean inProgress;

    public void processLogs() {
        try {
            if(inProgress) {
                JOptionPane.showMessageDialog(null, "Már folyamatban van egy feldolgozás.", "Hiba", JOptionPane.ERROR_MESSAGE);
            } else {
                inProgress = true;
                //clear in any case
                dpsReportJsonCreatorService.getPermalinks().clear();
                processLogsInternal();
            }
        } catch (Exception e) {
            log.error("Unexpected exception while processing logs!", e);
            JOptionPane.showMessageDialog(null, "Hiba a log fájlok feldolgozásakor: " + e.getMessage(),
                    "Hiba", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processLogsInternal() throws Exception {
        try {
            settingsService.validateSettings();
        } catch (SettingsException e) {
            log.info("Settings are invalid and the processing can't start: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Hibás beállítások", JOptionPane.ERROR_MESSAGE);
            return;
        }
        //settings are valid, processing can start
        var logs = listLogFiles();
        if(logs.isEmpty()) {
            log.info("No logs found in source folder...");
            JOptionPane.showMessageDialog(null, "Egy beállításoknak megfelelő log fájl sincs a megadott mappában!",
                    "Nincsenek logok", JOptionPane.WARNING_MESSAGE);
            return;
        }
        logAmount = logs.size();
        log.info("Found {} logs, starting processing...", logAmount);

        new Thread(() -> {
            bottomBarPanel.getProgressBar().setMinimum(0);
            bottomBarPanel.getProgressBar().setMaximum(logAmount);
            bottomBarPanel.getProgressBar().setValue(0);
            bottomBarPanel.getProgressBar().setString(getProgressBarString(0));

            processedAmount = 0;
            failedAmount = 0;

            JsonGenerator generator = settingsService.getSetting(Setting.ENGINE_JSON_GENERATOR, JsonGenerator::valueOf);
            switch (generator) {
                case DPS_REPORT_API:
                    log.debug("Selected log processing method is dps.report API");
                    processFilesWithDpsReport(logs);
                    break;
                case LOCAL_ELITE_INSIGHT:
                    log.debug("Selected log processing method is local Elite Insight parser.");
                    processFilesWithLocalEliteInsight(logs);
            }
        }).start();
    }

    private void processFilesWithDpsReport(List<Path> logFiles) {
        //launch background threads
        int numThreads = Math.min(Runtime.getRuntime().availableProcessors(), logAmount);
        log.info("Will use {} threads to process logs...", numThreads);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for(Path logFile: logFiles) {
            var task = new DpsReportLogProcessorRunnable(logFile, this, dpsReportJsonCreatorService);
            executor.execute(task);
        }

        executor.shutdown();
        boolean finished = false;
        try {
            finished = executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            log.error("Interrupted", e);
        }
        if(finished) {
            SwingUtilities.invokeLater(() -> {
                bottomBarPanel.getProgressBar().setValue(0);
                bottomBarPanel.getProgressBar().setString("Nincs aktív feldolgozás");
            });
            onAllTasksFinished();
        } else {
            log.warn("Executor failed to finish in time!");
            throw new RuntimeException("Executor timed out.");
        }
    }

    private void processFilesWithLocalEliteInsight(List<Path> logFiles) {
        Consumer<List<String>> onSuccess = (jsons) -> {
            if(jsons.size() != logFiles.size()) {
                log.error("Incorrect number of JSON strings returned ({}), expected {}", jsons.size(), logFiles.size());
                onAllTasksFinished();
                return;
            }
            processedAmount = logAmount;
            failedAmount = 0;
            log.debug("Successful processing of all logs with local Elite insight parser");
            for(int i = 0; i < jsons.size(); i++) {
                Path logPath = logFiles.get(i);
                String json = jsons.get(i);
                try {
                    log.debug("Cleaning and writing to file the following log: {}", logPath);
                    CleanedWvwLog cleanedWvwLog = eliteInsightJsonCreatorService.cleanJson(json, logPath);
                    eliteInsightJsonCreatorService.writeCleanedLogToFile(cleanedWvwLog, logPath);
                } catch (Exception e) {
                    log.error("Failed to process log file: {}", logPath, e);
                }
            }
            onAllTasksFinished();
        };
        Runnable onFail = () -> {
            log.error("Failed to process log files with local Elite Insight parser");
            onAllTasksFinished();
        };
        eliteInsightJsonCreatorService.getLogJsonFromEliteInsight(logFiles, onSuccess, onFail);
    }

    private List<Path> listLogFiles() throws IOException {
        var logFilesList = new ArrayList<Path>();
        String sourceFolder = settingsService.getSetting(Setting.SOURCE_FOLDER, Function.identity());
        Set<LogExtension> extensions = settingsService.getSetting(Setting.SOURCE_LOG_EXTENSIONS, SettingsService.EXTENSION_CONVERTER);
        int minSizeOriginal = settingsService.getSetting(Setting.SOURCE_MIN_SIZE_MB, Integer::parseInt);
        int minSize = minSizeOriginal * 1024 * 1024; //to bytes

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(sourceFolder))) {
            for (Path path : stream) {
                if (isLogFile(path, extensions)) {
                    log.debug("Found log file: {}", path);
                    long logfileSize = Files.size(path);
                    if(minSizeOriginal == SettingsService.MIN_SIZE_DISABLED || logfileSize >= minSize) {
                        logFilesList.add(path);
                        log.debug("Log file of acceptable size, will be processed.");
                    } else {
                        log.debug("Log file is not of accepted size, will be ignored.");
                    }
                }
            }
        }
        return logFilesList;
    }

    private static boolean isLogFile(Path path, Set<LogExtension> extensions) {
        if(Files.isDirectory(path)) return false;
        for(LogExtension extension: extensions) {
            if(path.getFileName().toString().endsWith(extension.getExtension())) {
                return true;
            }
        }
        return false;
    }

    public synchronized void onTaskFinished() {
        processedAmount++;
        bottomBarPanel.getProgressBar().setValue(processedAmount + failedAmount);
        bottomBarPanel.getProgressBar().setString(getProgressBarString(processedAmount + failedAmount));
        bottomBarPanel.getProgressBar().revalidate();
        bottomBarPanel.getProgressBar().repaint();
        bottomBarPanel.revalidate();
        bottomBarPanel.repaint();
    }

    public synchronized void onTaskFailed() {
        failedAmount++;
        bottomBarPanel.getProgressBar().setValue(processedAmount + failedAmount);
        bottomBarPanel.getProgressBar().setString(getProgressBarString(processedAmount + failedAmount));
        bottomBarPanel.getProgressBar().revalidate();
        bottomBarPanel.getProgressBar().repaint();
        bottomBarPanel.revalidate();
        bottomBarPanel.repaint();
    }

    private void onAllTasksFinished() {
        writePermalinksIfNeeded();

        final JDialog resultDialog = new JDialog();
        resultDialog.setTitle("A feldolgozás befejeződött");

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(GuiConstants.BORDER_MARGIN);

        JLabel label = new JLabel(processedAmount + "/" + logAmount + " log sikeresen feldolgozva.");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultPanel.add(label);

        JPanel buttons = new JPanel();
        JButton ok = new JButton("OK");
        ok.addActionListener(e -> resultDialog.dispose());
        buttons.add(ok);
        JButton browse = new JButton("Eredménymappa megnyitása");
        browse.addActionListener(e -> {
            String targetFolder = settingsService.getSetting(Setting.TARGET_FOLDER, Function.identity());
            try {
                resultDialog.dispose();
                Runtime.getRuntime().exec("explorer.exe /select," + targetFolder);
            } catch (IOException exc) {
                log.error("Failed to open folder.", exc);
            }
        });
        buttons.add(browse);
        resultPanel.add(buttons);

        JOptionPane resultPane = new JOptionPane(resultPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
                null, new Object[]{}, null);
        resultDialog.setContentPane(resultPane);
        resultDialog.pack();
        resultDialog.setLocationRelativeTo(null);
        resultDialog.setAlwaysOnTop(true);
        resultDialog.setVisible(true);
        resultDialog.setModal(true);

        inProgress = false;
    }

    private void writePermalinksIfNeeded() {
        JsonGenerator generator = settingsService.getSetting(Setting.ENGINE_JSON_GENERATOR, JsonGenerator::valueOf);
        boolean savePermalinks = settingsService.getSetting(Setting.ENGINE_DPS_REPORT_SAVE_PERMALINKS, Boolean::valueOf);
        if(generator == JsonGenerator.DPS_REPORT_API && savePermalinks) {
            String targetFolder = settingsService.getSetting(Setting.TARGET_FOLDER, Function.identity());
            Path permalinkPath = Paths.get(targetFolder, "permalinks.txt");
            try {
                Files.deleteIfExists(permalinkPath);
                Files.createFile(permalinkPath);
                String permalinkString = String.join("\n", dpsReportJsonCreatorService.getPermalinks());
                Files.writeString(permalinkPath, permalinkString);
                log.info("Saved permalinks to file: {}", permalinkPath);
            } catch (Exception e) {
                log.error("Failed to write permalinks to file", e);
                JOptionPane.showMessageDialog(null, "Nem sikerült a permalinkek elmentése: " + e.getMessage(),
                        "Hiba", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            log.debug("No need to save permalinks now.");
        }
    }

    private String getProgressBarString(int count) {
        return count + "/" + logAmount + " log feldolgozva...";
    }

}
