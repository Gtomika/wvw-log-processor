package com.gaspar.logprocessor.gui;

import lombok.Getter;

@Getter
public enum SettingsTab {

    /**
     * Tab about the source log files.
     */
    SOURCE("Forrásfájlok"),

    /**
     * Tab about method of JSON extraction and cleaning.
     */
    ENGINE("Feldolgozás módja"),

    /**
     * Tab about target folder for cleaned log files.
     */
    TARGET("Eredményfájlok");

    private final String tabName;

    SettingsTab(String tabName) {
        this.tabName = tabName;
    }
}
