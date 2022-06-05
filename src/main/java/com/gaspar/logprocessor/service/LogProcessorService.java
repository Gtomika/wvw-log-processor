package com.gaspar.logprocessor.service;

import com.gaspar.logprocessor.constants.GuiConstants;
import com.gaspar.logprocessor.constants.LogExtension;
import com.gaspar.logprocessor.constants.Setting;
import com.gaspar.logprocessor.exception.SettingsException;
import com.gaspar.logprocessor.gui.panel.BottomBarPanel;
import com.gaspar.logprocessor.runnable.LogProcessorRunnable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.swing.*;
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
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogProcessorService {

    private final SettingsService settingsService;
    private final JsonService jsonService;
    private final BottomBarPanel bottomBarPanel;

    private int logAmount;
    private int processedAmount;

    public void processLogs() {
        try {
            processLogsInternal();
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
            //launch background threads
            int numThreads = Math.min(Runtime.getRuntime().availableProcessors(), logAmount);
            log.info("Will use {} threads to process logs...", numThreads);

            bottomBarPanel.getProgressBar().setMinimum(0);
            bottomBarPanel.getProgressBar().setMaximum(logAmount);
            bottomBarPanel.getProgressBar().setValue(0);
            bottomBarPanel.getProgressBar().setString(getProgressBarString(0));

            processedAmount = 0;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            for(Path logFile: logs) {
                var task = new LogProcessorRunnable(logFile, this, jsonService);
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
        }).start();
    }

    private List<Path> listLogFiles() throws IOException {
        var logFilesList = new ArrayList<Path>();
        String sourceFolder = settingsService.getSetting(Setting.SOURCE_FOLDER, Function.identity());
        Set<LogExtension> extensions = settingsService.getSetting(Setting.SOURCE_LOG_EXTENSIONS, SettingsService.EXTENSION_CONVERTER);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(sourceFolder))) {
            for (Path path : stream) {
                if (isLogFile(path, extensions)) {
                    log.debug("Found log file: {}", path);
                    logFilesList.add(path);
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
        log.debug("Invoking 'onTaskFinished'...");
        processedAmount++;
        bottomBarPanel.getProgressBar().setValue(processedAmount);
        bottomBarPanel.getProgressBar().setString(getProgressBarString(processedAmount));
        bottomBarPanel.getProgressBar().revalidate();
        bottomBarPanel.getProgressBar().repaint();
        bottomBarPanel.revalidate();
        bottomBarPanel.repaint();
    }

    private void onAllTasksFinished() {
        log.info("All threads finished, executor stopped.");

        final JDialog resultDialog = new JDialog();
        resultDialog.setTitle("A feldolgozás befejeződött");

        JPanel resultPanel = new JPanel();
        resultPanel.setBorder(GuiConstants.BORDER_MARGIN);

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
    }

    private String getProgressBarString(int count) {
        return count + "/" + logAmount + " log feldolgozva...";
    }

}
