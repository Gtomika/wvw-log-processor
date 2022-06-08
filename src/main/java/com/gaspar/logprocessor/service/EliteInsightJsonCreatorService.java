package com.gaspar.logprocessor.service;

import com.gaspar.logprocessor.constants.Setting;
import com.gaspar.logprocessor.utils.WvwLogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class EliteInsightJsonCreatorService {

    private final SettingsService settingsService;

    public void getLogJsonFromEliteInsight(Path logFile, BiConsumer<String, Path> onSuccess, Consumer<Path> onFail) {
        Path eliteInsightPath = settingsService.getSetting(Setting.ENGINE_ELITE_INSIGHT_PATH, Paths::get);
        log.debug("Elite Insight parser is expected at: {}", eliteInsightPath);

        new Thread(() -> {
            try {
                Path configPath = new File(EliteInsightJsonCreatorService.class.getProtectionDomain().getCodeSource().getLocation()
                        .toURI()).toPath();
                log.debug("Config file is expected at {}", configPath);

                Process eliteInsightProcess = new ProcessBuilder(eliteInsightPath.toString(),
                        "-c", configPath.toString(), logFile.toString())
                        .start();
                //wait for finish
                eliteInsightProcess.onExit().get();

                //result should be here
                String sourcePathString = settingsService.getSetting(Setting.SOURCE_FOLDER, Function.identity());
                String logName = WvwLogUtils.logNameWithoutExtension(logFile.getFileName().toString());
                Path bigJsonPath = Paths.get(sourcePathString, logName + "_detailed_wvw_kill.json");
                Path eliteInsightLogPath = Paths.get(sourcePathString, logName + ".log"); //elite insight log
                log.debug("Result JSON is expected to be at {}", bigJsonPath);

                if(Files.notExists(bigJsonPath)) {
                    log.error("Result file is not at the expected location");
                    onFail.accept(logFile);
                    return;
                }

                String json = Files.readString(bigJsonPath);

                //delete other files if needed
                boolean keepBigJson = settingsService.getSetting(Setting.ENGIN_ELITE_INSIGHT_KEEP_BIG_JSON, Boolean::valueOf);
                if(!keepBigJson) {
                    Files.deleteIfExists(bigJsonPath);
                }
                //elite insights log is always deleted
                Files.deleteIfExists(eliteInsightLogPath);

                onSuccess.accept(json, logFile);
            } catch (Exception e) {
                log.error("Elite Insight process failed for log {}", logFile, e);
                onFail.accept(logFile);
            }
        }).start();
    }

}
