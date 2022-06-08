package com.gaspar.logprocessor.gui;

import com.gaspar.logprocessor.constants.GuiConstants;
import com.gaspar.logprocessor.gui.panel.RootPanel;
import com.gaspar.logprocessor.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class LogProcessorFrame extends JFrame {

    private final RootPanel rootPanel;
    private final SettingsService settingsService;

    @PostConstruct
    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log.error("Look and feel is not available.");
        }
        setTitle("WvW Log Feldolgozó");
        setSize(GuiConstants.FRAME_DEFAULT_SIZE);
        setMaximumSize(GuiConstants.FRAME_DEFAULT_SIZE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        addFrameListeners();

        add(rootPanel);

        pack();
        setVisible(true);
        log.info("Main window created.");
    }

    private void addFrameListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                log.info("Main window is being closed.");
                settingsService.saveSettings();
                super.windowClosing(e);
            }
        });
    }

}
