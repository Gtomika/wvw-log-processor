package com.gaspar.logprocessor.runnable;

import com.gaspar.logprocessor.model.CleanedWvwLog;
import com.gaspar.logprocessor.service.DpsReportJsonCreatorService;
import com.gaspar.logprocessor.service.LogProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

@Slf4j
@RequiredArgsConstructor
public class DpsReportLogProcessorRunnable implements Runnable {

    //original log file's path
    private final Path logPath;
    private final LogProcessorService logProcessorService;
    private final DpsReportJsonCreatorService jsonService;

    @Override
    public void run() {
        final Semaphore semaphore = new Semaphore(0);
        jsonService.generateJsonAsync(logPath, (json, logFile) -> { //on success
            try {
                CleanedWvwLog cleanedWvwLog = jsonService.cleanJson(json, logFile);
                jsonService.writeCleanedLogToFile(cleanedWvwLog, logFile);
                log.debug("Processing of log '{}' is finished.", logPath);
                SwingUtilities.invokeLater(logProcessorService::onTaskFinished);
                semaphore.release();
            } catch (Exception e) {
                log.error("Failed to process log file: {}", logFile, e);
                SwingUtilities.invokeLater(logProcessorService::onTaskFailed);
                semaphore.release();
            }
        }, (logFile) -> { //on fail
            log.error("Failed to process log file: {}", logFile);
            SwingUtilities.invokeLater(logProcessorService::onTaskFailed);
            semaphore.release();
        });
        //this thread needs to block until async callback is finished
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            log.error("Interrupted while processing log file");
            SwingUtilities.invokeLater(logProcessorService::onTaskFailed);
        }
    }

}
