package com.gaspar.logprocessor.gui.panel;

import com.gaspar.logprocessor.constants.GuiConstants;
import com.gaspar.logprocessor.constants.LogExtension;
import com.gaspar.logprocessor.constants.Setting;
import com.gaspar.logprocessor.service.SettingsService;
import com.gaspar.logprocessor.utils.FolderSelectorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class SourcePanel extends JPanel {

    private final SettingsService settingsService;

    @PostConstruct
    public void init() {
        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setBorder(GuiConstants.BORDER_MARGIN);
        setLayout(layout);
        addSourceFolderSelector();
        separator();
        addDeleteSourcesCheckbox();
        separator();
        addExtensionSelectors();
    }

    private void separator() {
        JPanel sep = new JPanel();
        sep.setSize(new Dimension(GuiConstants.GAP,GuiConstants.GAP));
        sep.setMaximumSize(new Dimension(GuiConstants.GAP,GuiConstants.GAP));
        add(sep);
    }

    private void addSourceFolderSelector() {
        JLabel label = new JLabel("Log fájlok mappája");
        add(label);

        String sourcePath = settingsService.getSetting(Setting.SOURCE_FOLDER, Function.identity());
        JLabel folderLabel = new JLabel(sourcePath);
        folderLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(GuiConstants.BORDER_MARGIN, BorderFactory.createLineBorder(Color.black, 1)),
                GuiConstants.BORDER_MARGIN
        ));
        add(folderLabel);

        JButton browse = new JButton("Tallózás");
        browse.addActionListener(e -> {
            String path = FolderSelectorUtils.selectFolder();
            if(path != null) {
                log.debug("User selected new source folder: {}", path);
                folderLabel.setText(path);
                settingsService.addSetting(Setting.SOURCE_FOLDER, path);
            }
        });
        add(browse);
    }

    private void addDeleteSourcesCheckbox() {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setText("Logfájlok törlése feldolgozás után");
        checkBox.addActionListener(e -> {
            boolean delete = checkBox.isSelected();
            log.debug("User selected value for deleting source files: {}", delete);
            settingsService.addSetting(Setting.SOURCE_DELETE_SOURCES, delete);
        });
        add(checkBox);
    }

    private void addExtensionSelectors() {
        JLabel label = new JLabel("Log fájl kiterjesztések");
        add(label);

        var initSelectedExtensions = settingsService.getSetting(Setting.SOURCE_LOG_EXTENSIONS, extensionConverter);

        for(LogExtension extension: LogExtension.values()) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setText(extension.getExtension());
            final LogExtension localExtension = extension;
            checkBox.addActionListener(e -> {
                boolean include = checkBox.isSelected();
                log.debug("Log file extension '{}' included: {}", localExtension.getExtension(), include);
                var selectedExtensions = settingsService.getSetting(Setting.SOURCE_LOG_EXTENSIONS, extensionConverter);
                if(include) {
                    selectedExtensions.add(localExtension);
                } else {
                    selectedExtensions.remove(localExtension);
                }
                log.debug("Extensions after change: {}", selectedExtensions);
                settingsService.addSetting(Setting.SOURCE_LOG_EXTENSIONS, selectedExtensions);
            });
            checkBox.setSelected(initSelectedExtensions.contains(extension));
            add(checkBox);
        }
    }

    private static Function<String, Set<LogExtension>> extensionConverter = s -> {
        s = s.replace("[", "").replace("]", "").replace(" ", "");
        return Arrays.stream(s.split(","))
                .map(LogExtension::valueOf)
                .collect(Collectors.toSet());
    };

}
