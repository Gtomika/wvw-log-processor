package com.gaspar.logprocessor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaspar.logprocessor.constants.Setting;
import com.gaspar.logprocessor.model.CleanedWvwLog;
import com.gaspar.logprocessor.utils.WvwLogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractJsonService {

    protected final ObjectMapper mapper;
    protected final SettingsService settingsService;

    public CleanedWvwLog cleanJson(String json, Path logFile) throws IOException {
        var targets = WvwLogUtils.extractTargets(json);
        CleanedWvwLog cleanedWvwLog = mapper.readValue(json, CleanedWvwLog.class);
        cleanedWvwLog.setTargets(targets);
        return cleanedWvwLog;
    }

    public void writeCleanedLogToFile(CleanedWvwLog cleanedWvwLog, Path logFile) throws IOException {
        String jsonName = WvwLogUtils.logNameWithoutExtension(logFile.getFileName().toString()) + "_cleaned.json";
        Path targetFolder = settingsService.getSetting(Setting.TARGET_FOLDER, Paths::get);
        Path cleanedJsonFile = targetFolder.resolve(jsonName);
        Files.deleteIfExists(cleanedJsonFile);
        Files.createFile(cleanedJsonFile);
        Files.writeString(cleanedJsonFile, mapper.writeValueAsString(cleanedWvwLog));
        log.debug("Log {} successfully cleaned and written to file: {}", logFile, cleanedJsonFile);
    }
}
