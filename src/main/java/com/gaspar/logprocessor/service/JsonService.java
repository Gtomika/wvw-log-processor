package com.gaspar.logprocessor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaspar.logprocessor.constants.JsonGenerator;
import com.gaspar.logprocessor.constants.Setting;
import com.gaspar.logprocessor.model.CleanedWvwLog;
import com.gaspar.logprocessor.utils.WvwLogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class JsonService {

    private final DpsReportJsonCreatorService dpsRepJsonService;
    private final EliteInsightJsonCreatorService elInsJsonService;
    private final SettingsService settingsService;
    private final ObjectMapper mapper;

    //async
    public void generateJsonAsync(Path logFile, BiConsumer<String, Path> onSuccess, Consumer<Path> onFail) {
        JsonGenerator generator = settingsService.getSetting(Setting.ENGINE_JSON_GENERATOR, JsonGenerator::valueOf);
        switch (generator) {
            case LOCAL_ELITE_INSIGHT:
                log.debug("Getting JSON from local Elite Insight parser: {}", logFile);
                elInsJsonService.getLogJsonFromEliteInsight(logFile, onSuccess, onFail);
                break;
            case DPS_REPORT_API:
                log.debug("Getting JSON from dps.report API: {}", logFile);
                dpsRepJsonService.uploadLogToDpsReport(logFile, onSuccess, onFail);
                break;
            default:
                log.error("Unknown JSON generator: {}", generator);
                throw new RuntimeException("Unknown JSON generator");
        }
    }

    public CleanedWvwLog cleanJson(String json, Path logFile) throws IOException {
        var targets = WvwLogUtils.extractTargets(json);
        CleanedWvwLog cleanedWvwLog = mapper.readValue(json, CleanedWvwLog.class);
        cleanedWvwLog.setTargets(targets);
        return cleanedWvwLog;
    }

    public void writeCleanedLogToFile(CleanedWvwLog cleanedWvwLog, Path logFile) throws IOException {
        String jsonName = fileNameWithoutExtension(logFile.getFileName().toString()) + "_cleaned.json";
        Path targetFolder = settingsService.getSetting(Setting.TARGET_FOLDER, Paths::get);
        Path cleanedJsonFile = targetFolder.resolve(jsonName);
        Files.deleteIfExists(cleanedJsonFile);
        Files.createFile(cleanedJsonFile);
        Files.writeString(cleanedJsonFile, mapper.writeValueAsString(cleanedWvwLog));
        log.debug("Log {} successfully cleaned and written to file: {}", logFile, cleanedJsonFile);
    }

    private String fileNameWithoutExtension(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }

}
