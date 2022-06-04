package com.gaspar.logprocessor.gui.panel;

import com.gaspar.logprocessor.gui.GuiConstants;
import com.gaspar.logprocessor.gui.SettingsTab;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
public class SettingsTabsSelectorPanel extends JPanel {

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
            tabButton.setMargin(GuiConstants.MARGIN);
            add(tabButton);
        }
    }

}
