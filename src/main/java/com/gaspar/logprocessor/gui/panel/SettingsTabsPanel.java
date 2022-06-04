package com.gaspar.logprocessor.gui.panel;

import com.gaspar.logprocessor.constants.SettingsTab;
import com.gaspar.logprocessor.service.SettingsTabService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class SettingsTabsPanel extends JPanel {

    private final SettingsTabService settingsTabService;
    private final ApplicationContext applicationContext;

    private CardLayout layout;

    @PostConstruct
    public void init() {
        layout = new CardLayout();
        setLayout(layout);
        //service will manage this card layout
        settingsTabService.registerLayout(layout, this);
        addTabPanels();
    }

    private void addTabPanels() {
        for(SettingsTab tab: SettingsTab.values()) {
            try {
                JPanel tabPanel = applicationContext.getBean(tab.getPanelBean());
                add(tabPanel, tab.getTabName());
                log.debug("Settings tab with name '{}' has been added.", tab.getTabName());
            } catch (BeansException e) {
                log.error("The settings tab '{}' has no registered panel bean! It should be: {}",
                        tab.getTabName(), tab.getPanelBean().getCanonicalName());
            }
        }
    }

}
