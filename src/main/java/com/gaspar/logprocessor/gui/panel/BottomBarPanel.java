package com.gaspar.logprocessor.gui.panel;

import com.gaspar.logprocessor.constants.GuiConstants;
import com.gaspar.logprocessor.service.LogProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
@RequiredArgsConstructor
public class BottomBarPanel extends JPanel {

    private final LogProcessorService logProcessorService;

    private BorderLayout layout;

    @PostConstruct
    public void init() {
        layout = new BorderLayout();
        setLayout(layout);
        addProcessButton();
    }

    private void addProcessButton() {
        JButton processButton = new JButton("Feldolgozás indítása");
        processButton.setMargin(GuiConstants.MARGIN);
        processButton.addActionListener(e -> logProcessorService.processLogs());
        JPanel wrapper = new JPanel();
        wrapper.add(processButton);
        add(wrapper, BorderLayout.LINE_END);
    }

}
