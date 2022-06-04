package com.gaspar.logprocessor.gui.panel;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
public class SettingsTabsPanel extends JPanel {

    private CardLayout layout;

    @PostConstruct
    public void init() {
        layout = new CardLayout();
        setLayout(layout);
    }

}
