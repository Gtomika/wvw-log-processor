package com.gaspar.logprocessor.service;

import com.gaspar.logprocessor.constants.LogExtension;
import com.gaspar.logprocessor.constants.Setting;
import com.gaspar.logprocessor.exception.SettingsException;
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
        log.info("Found {} logs, starting processing...", logs.size());

        //launch background threads
        int numThreads = Math.min(Runtime.getRuntime().availableProcessors(), logs.size());
        log.info("Will use {} threads to process logs...", numThreads);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        executor.shutdown();
        boolean finished = executor.awaitTermination(1, TimeUnit.DAYS);
        if(finished) {
            log.info("All threads finished, executor stopped, displaying results...");
        } else {
            log.warn("Executor failed to finish in time!");
            throw new RuntimeException("Executor timed out.");
        }
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

}
