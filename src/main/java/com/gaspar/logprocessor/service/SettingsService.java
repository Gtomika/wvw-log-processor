package com.gaspar.logprocessor.service;

import com.gaspar.logprocessor.constants.JsonGenerator;
import com.gaspar.logprocessor.constants.LogExtension;
import com.gaspar.logprocessor.constants.Setting;
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
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

@Service
@Slf4j
public class SettingsService {

    public static final String NO_PATH_SET = "-";

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
                addDefaultProperties();
            }
        } else {
            log.info("Settings XML file doesn't exist or is not readable. Using default properties. Expected location: {}", settingFile);
            addDefaultProperties();
        }
        try {
            validateSettings();
        } catch (IllegalStateException e) {
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

    private void addDefaultProperties() {
        addSetting(Setting.SOURCE_FOLDER, NO_PATH_SET);
        addSetting(Setting.SOURCE_DELETE_SOURCES, false);
        addSetting(Setting.SOURCE_LOG_EXTENSIONS, List.of(LogExtension.EVTC, LogExtension.ZEVTC));
        addSetting(Setting.ENGINE_JSON_GENERATOR, JsonGenerator.DPS_REPORT_API);
        addSetting(Setting.ENGINE_DPS_REPORT_SAVE_PERMALINKS, true);
        addSetting(Setting.TARGET_FOLDER, NO_PATH_SET);
    }

    private void validateSettings() throws IllegalStateException {
        if(settingsProperties == null) {
            throw new IllegalStateException("Settings are null.");
        }
        for(Setting setting: Setting.values()) {
            if(settingsProperties.getProperty(setting.name()) == null) {
                throw new IllegalStateException("Setting " + setting.name() + " not found, illegal settings state.");
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

}
