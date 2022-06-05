package com.gaspar.logprocessor.runnable;

import com.gaspar.logprocessor.service.JsonService;
import com.gaspar.logprocessor.service.LogProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
public class LogProcessorRunnable implements Runnable {

    //original log file's path
    private final Path logPath;
    private final LogProcessorService logProcessorService;
    private final JsonService jsonService;

    @Override
    public void run() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 5000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //finished
        log.debug("Processing of log '{}' is finished.", logPath);
        SwingUtilities.invokeLater(logProcessorService::onTaskFinished);
    }

}
