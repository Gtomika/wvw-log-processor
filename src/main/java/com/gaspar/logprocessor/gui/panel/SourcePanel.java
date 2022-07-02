package com.gaspar.logprocessor.gui.panel;

import com.gaspar.logprocessor.constants.GuiConstants;
import com.gaspar.logprocessor.constants.LogExtension;
import com.gaspar.logprocessor.constants.Setting;
import com.gaspar.logprocessor.service.SettingsService;
import com.gaspar.logprocessor.utils.PathSelectorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.io.IOException;
import java.util.function.Function;

@Component
@Slf4j
@RequiredArgsConstructor
public class SourcePanel extends JPanel {

    private final SettingsService settingsService;

    @PostConstruct
    public void init() {
        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(layout);
        addSourceFolderSelector();
        separator();
        addDeleteSourcesCheckbox();
        separator();
        addExtensionSelectors();
        separator();
        addMinSizeSelector();
    }

    private void separator() {
        JPanel sep = new JPanel();
        sep.setSize(new Dimension(GuiConstants.GAP,GuiConstants.GAP));
        sep.setMaximumSize(new Dimension(GuiConstants.GAP,GuiConstants.GAP));
        add(sep);
    }

    private void addSourceFolderSelector() {
        JLabel label = new JLabel("Log fájlok mappája");
        label.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        add(label);

        String sourcePath = settingsService.getSetting(Setting.SOURCE_FOLDER, Function.identity());
        JLabel folderLabel = new JLabel(sourcePath);
        folderLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        folderLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(GuiConstants.BORDER_MARGIN, BorderFactory.createLineBorder(Color.black, 1)),
                GuiConstants.BORDER_MARGIN
        ));
        add(folderLabel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JButton browse = new JButton("Tallózás");
        browse.addActionListener(e -> {
            String path = PathSelectorUtils.selectFolder();
            if(path != null) {
                log.debug("User selected new source folder: {}", path);
                folderLabel.setText(path);
                settingsService.addSetting(Setting.SOURCE_FOLDER, path);
            }
        });
        buttons.add(browse);

        JButton show = new JButton("Mappa megnyitása");
        show.addActionListener(e -> {
            String sourcePathSelected = settingsService.getSetting(Setting.SOURCE_FOLDER, Function.identity());
            if(!sourcePathSelected.equals(SettingsService.NO_PATH_SET)) {
                try {
                    Runtime.getRuntime().exec("explorer.exe /select," + sourcePathSelected);
                } catch (IOException exc) {
                    log.error("Failed to open folder.", exc);
                }
            }
        });
        buttons.add(show);

        add(buttons);
    }

    private void addDeleteSourcesCheckbox() {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
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
        label.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        add(label);

        var initSelectedExtensions = settingsService.getSetting(Setting.SOURCE_LOG_EXTENSIONS, SettingsService.EXTENSION_CONVERTER);

        for(LogExtension extension: LogExtension.values()) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            checkBox.setText(extension.getExtension());
            final LogExtension localExtension = extension;
            checkBox.addActionListener(e -> {
                boolean include = checkBox.isSelected();
                log.debug("Log file extension '{}' included: {}", localExtension.getExtension(), include);
                var selectedExtensions = settingsService.getSetting(Setting.SOURCE_LOG_EXTENSIONS, SettingsService.EXTENSION_CONVERTER);
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

    private void addMinSizeSelector() {
        String minSize = settingsService.getSetting(Setting.SOURCE_MIN_SIZE_MB, Function.identity());
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        JLabel label = new JLabel("Minimális log méret (mB), \"-1\" esetén nincs minimális méret.");
        JTextField field = new JTextField(minSize);
        field.setEditable(true);
        field.setPreferredSize(new Dimension(50, 20));
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                save(e.getDocument());
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                save(e.getDocument());
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                save(e.getDocument());
            }

            private void save(Document document) {
                try {
                    String text = document.getText(0, document.getLength());
                    settingsService.addSetting(Setting.SOURCE_MIN_SIZE_MB, text);
                } catch (BadLocationException e) {
                    log.error("Error, bad location for getting text from document", e);
                }
            }
        });
        panel.add(label);
        panel.add(field);
        add(panel);
    }

}
