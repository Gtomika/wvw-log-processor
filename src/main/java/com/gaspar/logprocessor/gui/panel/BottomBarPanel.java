package com.gaspar.logprocessor.gui.panel;

import com.gaspar.logprocessor.gui.GuiConstants;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
public class BottomBarPanel extends JPanel {

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
        JPanel wrapper = new JPanel();
        wrapper.add(processButton);
        add(wrapper, BorderLayout.LINE_END);
    }

}
