package com.gaspar.logprocessor.gui.panel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
@RequiredArgsConstructor
public class RootPanel extends JPanel {

    private final SettingsTabsSelectorPanel settingsTabsSelectorPanel;
    private final SettingsTabsPanel settingsTabsPanel;
    private final BottomBarPanel bottomBarPanel;

    private BorderLayout layout;

    @PostConstruct
    public void init() {
        layout = new BorderLayout();
        setLayout(layout);
        addTabSelectorPanel();
        addTabPanel();
        addBottomBar();
    }

    private void addTabSelectorPanel() {
        add(settingsTabsSelectorPanel, BorderLayout.PAGE_START);
    }

    private void addTabPanel() {
        add(settingsTabsPanel, BorderLayout.CENTER);
    }

    private void addBottomBar() {
        add(bottomBarPanel, BorderLayout.PAGE_END);
    }

}
