package com.gaspar.logprocessor.gui.panel;

import com.gaspar.logprocessor.constants.GuiConstants;
import com.gaspar.logprocessor.service.LogProcessorService;
import lombok.Getter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
public class BottomBarPanel extends JPanel {

    private final LogProcessorService logProcessorService;

    @Getter
    private JProgressBar progressBar;

    public BottomBarPanel(@Lazy LogProcessorService logProcessorService) {
        this.logProcessorService = logProcessorService;
    }

    @PostConstruct
    public void init() {
        setLayout(new BorderLayout());
        addProcessButton();
        addProgressBar();
    }

    private void addProcessButton() {
        JButton processButton = new JButton("Feldolgozás indítása");
        processButton.setMargin(GuiConstants.MARGIN);
        processButton.addActionListener(e -> logProcessorService.processLogs());
        JPanel wrapper = new JPanel();
        wrapper.add(processButton);
        add(wrapper, BorderLayout.LINE_END);
    }

    private void addProgressBar() {
        JPanel wrapper = new JPanel();
        wrapper.setBorder(GuiConstants.BORDER_MARGIN);
        progressBar = new JProgressBar();
        progressBar.setString("Nincs aktív feldogozás");
        progressBar.setStringPainted(true);
        wrapper.add(progressBar);
        add(wrapper, BorderLayout.LINE_START);
    }

}
