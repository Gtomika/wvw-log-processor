package com.gaspar.logprocessor.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

@Slf4j
public abstract class PathSelectorUtils {

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

    @Nullable
    public static String selectExeFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Elite Insight .exe helye");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".exe");
            }

            @Override
            public String getDescription() {
                return ".exe";
            }
        });

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().toString();
        } else {
            log.debug("User cancelled file chooser.");
            return null;
        }
    }

    @Nullable
    public static String selectEliteInsightConfFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Mellékelt Elite Insight konfiguráció helye");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".conf");
            }

            @Override
            public String getDescription() {
                return ".conf";
            }
        });

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().toString();
        } else {
            log.debug("User cancelled file chooser.");
            return null;
        }
    }

}
