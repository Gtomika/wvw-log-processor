package com.gaspar.logprocessor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaspar.logprocessor.constants.Setting;
import com.gaspar.logprocessor.utils.WvwLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class EliteInsightJsonCreatorService extends AbstractJsonService {

    public EliteInsightJsonCreatorService(ObjectMapper mapper, SettingsService settingsService) {
        super(mapper, settingsService);
    }

    public void generateJsonAsync(List<Path> logFiles, Consumer<List<String>> onSuccess, Runnable onFail) {
        log.debug("Getting JSON from local Elite Insight parser for {} logs", logFiles.size());
        getLogJsonFromEliteInsight(logFiles, onSuccess, onFail);
    }

    public void getLogJsonFromEliteInsight(List<Path> logFiles, Consumer<List<String>> onSuccess, Runnable onFail) {
        Path eliteInsightPath = settingsService.getSetting(Setting.ENGINE_ELITE_INSIGHT_PATH, Paths::get);
        log.debug("Elite Insight parser is expected at: {}", eliteInsightPath);

        new Thread(() -> {
            try {
                Path configPath = settingsService.getSetting(Setting.ENGINE_ELITE_INSIGHT_CONF_PATH, Paths::get);
                boolean configExists = Files.exists(configPath);
                log.debug("Config file is expected at {}. Exists: {}", configPath, configExists);
                if(!configExists) {
                    log.error("Config file is not found at expected place: {}", configPath);
                    onFail.run();
                    return;
                }

                List<String> commands = new ArrayList<>();
                commands.add(eliteInsightPath.toString()); //which program to execute
                commands.add("-c"); //specify config path
                commands.add(configPath.toString());
                for(Path logFile: logFiles) { //specify all log files
                    commands.add(logFile.toString());
                }
                log.debug("Command to run Elite Insight is: {}", String.join(" ", commands));
                Process eliteInsightProcess = new ProcessBuilder(commands).start();
                //wait for finish
                eliteInsightProcess.onExit().get();

                List<String> jsons = new ArrayList<>();
                //result should be here
                String sourcePathString = settingsService.getSetting(Setting.SOURCE_FOLDER, Function.identity());
                for(Path logFile: logFiles) {
                    String logName = WvwLogUtils.logNameWithoutExtension(logFile.getFileName().toString());
                    Path bigJsonPath = Paths.get(sourcePathString, logName + "_detailed_wvw_kill.json");
                    Path eliteInsightLogPath = Paths.get(sourcePathString, logName + ".log"); //elite insight log
                    log.debug("Result JSON is expected to be at {}", bigJsonPath);

                    if(Files.notExists(bigJsonPath)) {
                        log.error("Result file is not at the expected location");
                        onFail.run();
                        return;
                    }

                    String json = Files.readString(bigJsonPath);
                    jsons.add(json);

                    //delete other files if needed
                    boolean keepBigJson = settingsService.getSetting(Setting.ENGINE_ELITE_INSIGHT_KEEP_BIG_JSON, Boolean::valueOf);
                    if(!keepBigJson) {
                        Files.deleteIfExists(bigJsonPath);
                    }
                    //elite insights log is always deleted
                    Files.deleteIfExists(eliteInsightLogPath);
                }
                onSuccess.accept(jsons);
            } catch (Exception e) {
                log.error("Elite Insight process failed for logs", e);
                onFail.run();
            }
        }).start();
    }
}
