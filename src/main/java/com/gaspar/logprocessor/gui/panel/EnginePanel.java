package com.gaspar.logprocessor.gui.panel;

import com.gaspar.logprocessor.constants.GuiConstants;
import com.gaspar.logprocessor.constants.JsonGenerator;
import com.gaspar.logprocessor.constants.Setting;
import com.gaspar.logprocessor.service.SettingsService;
import com.gaspar.logprocessor.utils.PathSelectorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

@Component
@Slf4j
@RequiredArgsConstructor
public class EnginePanel extends JPanel {

    private final SettingsService settingsService;

    private JPanel dpsReportPanel;
    private JRadioButton dpsReportRadio;

    private JPanel elInsPanel;
    private JRadioButton elInsRadio;

    @PostConstruct
    public void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(GuiConstants.BORDER_MARGIN);
        addDpsReportPanel();
        add(Box.createRigidArea(new Dimension(GuiConstants.GAP, GuiConstants.GAP)));
        addEliteInsightPanel();

        JsonGenerator generator = settingsService.getSetting(Setting.ENGINE_JSON_GENERATOR, JsonGenerator::valueOf);
        switch (generator) {
            case LOCAL_ELITE_INSIGHT:
                elInsRadio.setSelected(true);
                onElInsClicked(true);
                break;
            case DPS_REPORT_API:
                dpsReportRadio.setSelected(true);
                onDpsReportClicked(true);
                break;
        }
    }

    private void addDpsReportPanel() {
        dpsReportRadio = new JRadioButton("dps.report API használata");
        dpsReportRadio.addActionListener(e -> onDpsReportClicked(dpsReportRadio.isSelected()));
        add(dpsReportRadio);

        dpsReportPanel = new JPanel();
        dpsReportPanel.setLayout(new BoxLayout(dpsReportPanel, BoxLayout.Y_AXIS));

        JCheckBox permalinkCheckbox = new JCheckBox("Permalinkek mentése (egy szövegfájlba az erdemények mellé)");
        permalinkCheckbox.addActionListener(e -> {
            log.debug("User selected value for permalink saving: {}", permalinkCheckbox.isSelected());
            settingsService.addSetting(Setting.ENGINE_DPS_REPORT_SAVE_PERMALINKS, permalinkCheckbox.isSelected());
        });
        dpsReportPanel.add(permalinkCheckbox);

        add(dpsReportPanel);
    }

    private void onDpsReportClicked(boolean checked) {
        if(checked) {
            log.debug("User selected dps.report API as JSON generation method.");
            setAllComponentsEnabled(dpsReportPanel, true);
            elInsRadio.setSelected(false);
            setAllComponentsEnabled(elInsPanel, false);
            settingsService.addSetting(Setting.ENGINE_JSON_GENERATOR, JsonGenerator.DPS_REPORT_API);
        }
    }

    private void addEliteInsightPanel() {
        elInsRadio = new JRadioButton("Helyi Elite Insights parser használata");
        elInsRadio.addActionListener(e -> onElInsClicked(elInsRadio.isSelected()));
        add(elInsRadio);

        elInsPanel = new JPanel();
        elInsPanel.setLayout(new BoxLayout(elInsPanel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Elite Insight .exe helye");
        elInsPanel.add(label);

        String elInsPath = settingsService.getSetting(Setting.ENGINE_ELITE_INSIGHT_PATH, Function.identity());
        JLabel pathLabel = new JLabel(elInsPath);
        pathLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(GuiConstants.BORDER_MARGIN, BorderFactory.createLineBorder(Color.black, 1)),
                GuiConstants.BORDER_MARGIN
        ));
        elInsPanel.add(pathLabel);

        JButton browse = new JButton("Tallózás");
        browse.addActionListener(e -> {
            String path = PathSelectorUtils.selectExeFile();
            if(path != null) {
                log.debug("User selected new path to EliteInsight: {}", path);
                pathLabel.setText(path);
                settingsService.addSetting(Setting.ENGINE_ELITE_INSIGHT_PATH, path);
            }
        });
        elInsPanel.add(browse);

        boolean keepBigJson = settingsService.getSetting(Setting.ENGIN_ELITE_INSIGHT_KEEP_BIG_JSON, Boolean::valueOf);
        JCheckBox keepBigJsonCheckbox = new JCheckBox("Nem tisztított JSON fájlok megtartása (az eredeti logok mellett lesznek)");
        keepBigJsonCheckbox.setSelected(keepBigJson);
        keepBigJsonCheckbox.addActionListener(e -> {
            log.info("User selected new value for keeping big JSON: {}", keepBigJsonCheckbox.isSelected());
            settingsService.addSetting(Setting.ENGIN_ELITE_INSIGHT_KEEP_BIG_JSON, keepBigJsonCheckbox.isSelected());
        });
        elInsPanel.add(keepBigJsonCheckbox);

        add(elInsPanel);
    }

    private void onElInsClicked(boolean checked) {
        if(checked) {
            log.debug("User selected local ELite Insights as JSON generation method.");
            setAllComponentsEnabled(dpsReportPanel, false);
            dpsReportRadio.setSelected(false);
            setAllComponentsEnabled(elInsPanel, true);
            settingsService.addSetting(Setting.ENGINE_JSON_GENERATOR, JsonGenerator.LOCAL_ELITE_INSIGHT);
        }
    }

    private void setAllComponentsEnabled(JPanel panel, boolean enabled) {
        for(java.awt.Component component: panel.getComponents()) {
            component.setEnabled(enabled);
        }
    }

}
