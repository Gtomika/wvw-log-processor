package com.gaspar.logprocessor.service;

import com.gaspar.logprocessor.constants.JsonGenerator;
import com.gaspar.logprocessor.constants.LogExtension;
import com.gaspar.logprocessor.constants.Setting;
import com.gaspar.logprocessor.exception.SettingsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.swing.filechooser.FileSystemView;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SettingsService {

    public static final String NO_PATH_SET = "Nincs megadva";

    private final Path settingFile = Paths.get(
            FileSystemView.getFileSystemView().getDefaultDirectory().getPath(),
            "wvwlogprocessor",
            "settings.xml");

    private Properties settingsProperties;

    @PostConstruct
    public void init() {
        settingsProperties = new Properties();
        loadSettings();
    }

    public void loadSettings() {
        if(Files.exists(settingFile) && Files.isReadable(settingFile)) {
            try(InputStream is = new FileInputStream(settingFile.toFile())) {
                settingsProperties.loadFromXML(is);
                log.debug("Settings read from file: {}", settingFile);
            } catch (Exception e) {
                log.error("Failed to read settings XML file! Using default properties.", e);
                addDefaultSettings();
            }
        } else {
            log.info("Settings XML file doesn't exist or is not readable. Using default properties. Expected location: {}", settingFile);
            addDefaultSettings();
        }
        try {
            validateSettingsIntegrity();
        } catch (SettingsException e) {
            log.error("Settings are in an invalid state!", e);
        }
    }

    public void saveSettings() {
        try {
            Path settingsFolder = settingFile.getParent();
            if(Files.notExists(settingsFolder)) {
                Files.createDirectory(settingsFolder);
            }
            if(Files.notExists(settingFile)) {
                Files.createFile(settingFile);
            }
        } catch (IOException e) {
            log.error("Failed to create settings file before saving.", e);
        }

        try(FileOutputStream os = new FileOutputStream(settingFile.toFile())) {
            settingsProperties.storeToXML(os, "WvW Log Processor settings");
            log.debug("Settings saved to file: {}", settingFile);
        } catch (Exception e) {
            log.error("Failed to save settings!", e);
        }
    }

    private void addDefaultSettings() {
        addSetting(Setting.SOURCE_FOLDER, NO_PATH_SET);
        addSetting(Setting.SOURCE_DELETE_SOURCES, false);
        addSetting(Setting.SOURCE_LOG_EXTENSIONS, Set.of(LogExtension.EVTC, LogExtension.ZEVTC));
        addSetting(Setting.ENGINE_JSON_GENERATOR, JsonGenerator.DPS_REPORT_API);
        addSetting(Setting.ENGINE_DPS_REPORT_SAVE_PERMALINKS, true);
        addSetting(Setting.ENGINE_ELITE_INSIGHT_PATH, NO_PATH_SET);
        addSetting(Setting.TARGET_FOLDER, NO_PATH_SET);
        addSetting(Setting.ENGIN_ELITE_INSIGHT_KEEP_BIG_JSON, false);
    }

    private void validateSettingsIntegrity() throws SettingsException {
        if(settingsProperties == null) {
            throw new IllegalStateException("Settings are null.");
        }
        for(Setting setting: Setting.values()) {
            if(settingsProperties.getProperty(setting.name()) == null) {
                throw new SettingsException("Setting " + setting.name() + " not found, illegal settings state.");
            }
        }
    }

    public <T> void addSetting(Setting setting, T value) {
        settingsProperties.setProperty(setting.name(), value.toString());
    }

    public <T> T getSetting(Setting setting, Function<String, T> converter) {
        String settingString = settingsProperties.getProperty(setting.name());
        return converter.apply(settingString);
    }

    public void validateSettings() throws SettingsException {
        //validate source settings
        if(getSetting(Setting.SOURCE_FOLDER, Function.identity()).equals(NO_PATH_SET)) {
            throw new SettingsException("Nincs beállítva a log fájlok mappája!");
        }
        if(getSetting(Setting.SOURCE_LOG_EXTENSIONS, EXTENSION_CONVERTER).isEmpty()) {
            throw new SettingsException("Egy log kiterjesztés sincs bekapcsolva!");
        }

        //validate engine
        switch (getSetting(Setting.ENGINE_JSON_GENERATOR, JsonGenerator::valueOf)) {
            case LOCAL_ELITE_INSIGHT:
                if(getSetting(Setting.ENGINE_ELITE_INSIGHT_PATH, Function.identity()).equals(NO_PATH_SET)) {
                    throw new SettingsException("Nincs beállítva az Elite Insight .exe helye!");
                }
                break;
            case DPS_REPORT_API:
                //no need to validate anything
                break;
        }

        //validate target
        if(getSetting(Setting.TARGET_FOLDER, Function.identity()).equals(NO_PATH_SET)) {
            throw new SettingsException("Nincs beállítva az eredményfájlok mappája!");
        }
    }

    public static final Function<String, Set<LogExtension>> EXTENSION_CONVERTER = s -> {
        s = s.replace("[", "").replace("]", "").replace(" ", "");
        return Arrays.stream(s.split(","))
                .map(LogExtension::valueOf)
                .collect(Collectors.toSet());
    };

}
