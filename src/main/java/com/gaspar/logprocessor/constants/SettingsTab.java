package com.gaspar.logprocessor.constants;

import com.gaspar.logprocessor.gui.panel.EnginePanel;
import com.gaspar.logprocessor.gui.panel.SourcePanel;
import com.gaspar.logprocessor.gui.panel.TargetPanel;
import lombok.Getter;

import javax.swing.*;

@Getter
public enum SettingsTab {

    /**
     * Tab about the source log files.
     */
    SOURCE("Forrásfájlok", SourcePanel.class),

    /**
     * Tab about method of JSON extraction and cleaning.
     */
    ENGINE("Feldolgozás módja", EnginePanel.class),

    /**
     * Tab about target folder for cleaned log files.
     */
    TARGET("Eredményfájlok", TargetPanel.class);

    private final String tabName;
    private final Class<? extends JPanel> panelBean;

    SettingsTab(String tabName, Class<? extends JPanel> panelBean) {
        this.tabName = tabName;
        this.panelBean = panelBean;
    }
}
