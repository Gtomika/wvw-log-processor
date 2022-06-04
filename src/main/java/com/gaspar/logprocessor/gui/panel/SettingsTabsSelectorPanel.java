package com.gaspar.logprocessor.gui.panel;

import com.gaspar.logprocessor.constants.GuiConstants;
import com.gaspar.logprocessor.constants.SettingsTab;
import com.gaspar.logprocessor.service.SettingsTabService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class SettingsTabsSelectorPanel extends JPanel {

    private final SettingsTabService settingsTabService;

    private FlowLayout layout;

    @PostConstruct
    public void init() {
        layout = new FlowLayout(FlowLayout.LEFT);
        layout.setHgap(GuiConstants.GAP);
        addTabSelectorButtons();
    }

    private void addTabSelectorButtons() {
        for(SettingsTab tab: SettingsTab.values()) {
            JButton tabButton = new JButton(tab.getTabName());
            tabButton.setName(tab.getTabName());
            tabButton.setMargin(GuiConstants.MARGIN);
            tabButton.addActionListener(e -> {
                String name = tabButton.getName();
                settingsTabService.showTab(name);
            });
            add(tabButton);
        }
    }

}
