package com.gaspar.logprocessor.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import javax.swing.*;

@Slf4j
public abstract class FolderSelectorUtils {

    @Nullable
    public static String selectFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Válassz mappát");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().toString();
        } else {
            log.debug("User cancelled file chooser.");
            return null;
        }
    }

}
