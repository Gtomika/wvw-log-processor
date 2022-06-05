package com.gaspar.logprocessor.gui.panel;

import com.gaspar.logprocessor.constants.GuiConstants;
import com.gaspar.logprocessor.constants.Setting;
import com.gaspar.logprocessor.service.SettingsService;
import com.gaspar.logprocessor.utils.PathSelectorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

@Component
@Slf4j
@RequiredArgsConstructor
public class TargetPanel extends JPanel {

    private final SettingsService settingsService;

    @PostConstruct
    public void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(GuiConstants.BORDER_MARGIN);
        addTargetFolderSelector();
    }

    private void addTargetFolderSelector() {
        JLabel label = new JLabel("Eredményfájlok mappája");
        add(label);

        String targetPath = settingsService.getSetting(Setting.TARGET_FOLDER, Function.identity());
        JLabel folderLabel = new JLabel(targetPath);
        folderLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(GuiConstants.BORDER_MARGIN, BorderFactory.createLineBorder(Color.black, 1)),
                GuiConstants.BORDER_MARGIN
        ));
        add(folderLabel);

        JButton browse = new JButton("Tallózás");
        browse.addActionListener(e -> {
            String path = PathSelectorUtils.selectFolder();
            if(path != null) {
                log.debug("User selected new target folder: {}", path);
                folderLabel.setText(path);
                settingsService.addSetting(Setting.TARGET_FOLDER, path);
            }
        });
        add(browse);
    }

}
