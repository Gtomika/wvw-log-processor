package com.gaspar.logprocessor.gui.panel;

import com.gaspar.logprocessor.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class SourcePanel extends JPanel {

    private final SettingsService settingsService;

}
