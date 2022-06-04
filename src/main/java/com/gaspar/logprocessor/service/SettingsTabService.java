package com.gaspar.logprocessor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;

@Service
@Slf4j
public class SettingsTabService {

    private JPanel parent;
    private CardLayout cardLayout;

    public void registerLayout(CardLayout cardLayout, JPanel parent) {
        this.parent = parent;
        this.cardLayout = cardLayout;
        log.debug("CardLayout registered as settings tab layout.");
    }

    public void showTab(String name) {
        if(cardLayout != null && parent != null) {
            cardLayout.show(parent, name);
            log.debug("Showing settings tab '{}'", name);
        } else {
            log.warn("No CardLayout registered as settings tab layout, ignoring event...");
        }
    }

}
