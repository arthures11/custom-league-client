package com.hawolt.ui.settings;


import com.hawolt.generic.util.Network;
import com.hawolt.io.RunLevel;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.*;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.ui.github.Github;
import com.hawolt.util.audio.AudioEngine;
import com.hawolt.util.other.StaticConstant;
import com.hawolt.util.settings.SettingService;
import com.hawolt.util.settings.SettingType;
import com.hawolt.virtual.misc.DynamicObject;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Objects;

public class SettingUIComponent extends ChildUIComponent {
    private static final Font textFont = new Font("Arial", Font.PLAIN, 12);
    private static final Font tagFont = new Font("Arial", Font.BOLD, 20);
    private static final String[] friendHandlingOptions = {"User choice", "Auto accept", "Auto reject"};
    private final EventListenerList onSave = new EventListenerList();
    private final EventListenerList onClose = new EventListenerList();

    private SettingUIComponent(LayoutManager layout) {
        super(layout);
    }

    private static ChildUIComponent createDefaultFlowLayout() {
        ChildUIComponent result = new ChildUIComponent(new FlowLayout());
        result.setBorder(new EmptyBorder(0, 16, 0, 0));
        return result;
    }

    private static ChildUIComponent createDefaultGridLayout(int rows, int cols, int hGap, int vGap) {
        ChildUIComponent result = new ChildUIComponent(new GridLayout(rows, cols, hGap, vGap));
        result.setBorder(new EmptyBorder(0, 16, 0, 0));
        return result;
    }

    public static ChildUIComponent createTagComponent(String name) {
        ChildUIComponent result = new ChildUIComponent(new BorderLayout());
        result.setPreferredSize(new Dimension(10, 32));

        ChildUIComponent labelContainer = new ChildUIComponent(new FlowLayout());
        result.add(labelContainer, BorderLayout.WEST);

        JLabel label = new JLabel(name);
        label.setFont(tagFont);
        label.setForeground(Color.WHITE);
        labelContainer.add(label);

        return result;
    }

    public static SettingUIComponent createPathComponent(String name, SettingService settingService, String key) {
        DynamicObject settings = settingService.getClientSettings();

        SettingUIComponent result = new SettingUIComponent(new BorderLayout());
        result.setPreferredSize(new Dimension(10, 64));

        ChildUIComponent pathContainer = new ChildUIComponent(new FlowLayout());
        result.add(pathContainer, BorderLayout.SOUTH);

        ChildUIComponent labelContainer = createDefaultFlowLayout();
        result.add(labelContainer, BorderLayout.WEST);

        JLabel label = createLabel(name);
        labelContainer.add(label);

        JTextField pathField = new JTextField("");
        pathField.setText(settings.getByKeyOrDefault(key, "C:\\Riot Games\\League of Legends"));
        pathField.setPreferredSize(new Dimension(800, 30));
        pathContainer.add(pathField, BorderLayout.SOUTH);

        LFlatButton searchButton = new LFlatButton("Open", LTextAlign.CENTER, HighlightType.COMPONENT);
        searchButton.setRounding(ColorPalette.CARD_ROUNDING);
        searchButton.setPreferredSize(new Dimension(80, 30));
        searchButton.addActionListener(listener -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setCurrentDirectory(new File(pathField.getText()));

            int i = fileChooser.showOpenDialog(null);
            if (i == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                pathField.setText(path);
            }
        });
        pathContainer.add(searchButton, BorderLayout.SOUTH);

        result.addOnSaveActionListener(listener -> {
            String path = pathField.getText();
            try {
                Paths.get(path);
                settingService.write(SettingType.CLIENT, key, path);
            } catch (InvalidPathException | NullPointerException e) {
                Logger.warn(e.getMessage());
            }
        });

        result.addOnQuitActionListener(listener -> {
            pathField.setText(settings.getByKeyOrDefault(key, "C:\\Riot Games\\League of Legends"));
        });

        return result;
    }

    public static SettingUIComponent createAutoFriendComponent(String name, SettingService settingService, String key) {
        DynamicObject settings = settingService.getClientSettings();
        SettingUIComponent result = new SettingUIComponent(new BorderLayout());

        ChildUIComponent autoFriendContainer = createDefaultFlowLayout();
        result.add(autoFriendContainer, BorderLayout.WEST);

        JLabel label = createLabel(name);
        autoFriendContainer.add(label);

        LComboBox<String> autoFriend = new LComboBox<>(friendHandlingOptions);
        autoFriend.setSelectedItem(settings.getByKeyOrDefault(key, "User choice"));
        autoFriendContainer.add(autoFriend);

        result.addOnSaveActionListener(listener -> {
            try {
                settingService.write(SettingType.CLIENT, key, Objects.requireNonNull(autoFriend.getSelectedItem()).toString());
            } catch (InvalidPathException | NullPointerException e) {
                Logger.warn(e.getMessage());
            }
        });

        result.addOnQuitActionListener(listener -> {
            autoFriend.setSelectedItem(settings.getByKeyOrDefault(key, "User choice"));
        });

        return result;
    }

    public static SettingUIComponent createVolumeComponent(String name, SettingService settingService, String key1, String key2) {
        DynamicObject settings = settingService.getClientSettings();
        AudioEngine.setGain(-80f + (8.6f * ((float) settingService.getClientSettings().getClientVolumeGain() / 10)));
        SettingUIComponent result = new SettingUIComponent(new BorderLayout());

        ChildUIComponent labelContainer = createDefaultGridLayout(2, 0, 0, 42);
        result.add(labelContainer, BorderLayout.WEST);

        ChildUIComponent audioContainer = createDefaultGridLayout(2, 0, 0, 42);
        result.add(audioContainer, BorderLayout.CENTER);

        JLabel label = createLabel(name);
        labelContainer.add(label);

        JPanel volumeGain = new JPanel(new BorderLayout());
        JSlider gainSlider = createVolumeSlider(settingService.getClientSettings().getClientVolumeGain());
        gainSlider.addChangeListener(listener -> {
            float gain = -80f + (8.6f * ((float) gainSlider.getValue() / 10));
            AudioEngine.setGain(gain);
        });
        volumeGain.add(gainSlider);
        audioContainer.add(volumeGain);

        JLabel labelMixer = createLabel(name + " Mixer");
        labelContainer.add(labelMixer);

        JPanel volumeMixer = new JPanel(new BorderLayout());
        LSlider mixerSlider = createVolumeSlider(settingService.getClientSettings().getClientVolumeMixer());
        //mixerSlider.setBackground(new Color(0,0,0,0));
        mixerSlider.addChangeListener(listener -> {
            AudioEngine.setMasterOutput(mixerSlider.getValue() / 100f);
        });
        volumeMixer.add(mixerSlider);
        audioContainer.add(volumeMixer);

        result.addOnSaveActionListener(listener -> {
            try {
                settingService.write(SettingType.CLIENT, key1, gainSlider.getValue());
                settingService.write(SettingType.CLIENT, key2, mixerSlider.getValue());
            } catch (InvalidPathException | NullPointerException e) {
                Logger.warn(e.getMessage());
            }
        });

        result.addOnQuitActionListener(listener -> {
            mixerSlider.setValue(settings.getByKeyOrDefault(key1, 100));
            mixerSlider.setValue(settings.getByKeyOrDefault(key2, 100));
        });

        return result;
    }

    public static SettingUIComponent createAboutComponent(String name) {
        SettingUIComponent result = new SettingUIComponent(new BorderLayout());
        ChildUIComponent aboutContainer = new ChildUIComponent(new FlowLayout());
        result.add(aboutContainer, BorderLayout.WEST);

        ChildUIComponent iconContainer = createDefaultFlowLayout();
        aboutContainer.add(iconContainer, BorderLayout.WEST);

        ChildUIComponent labelContainer = createDefaultGridLayout(1, 0, 10, 0);
        aboutContainer.add(labelContainer, BorderLayout.WEST);

        ChildUIComponent versionContainer = createDefaultGridLayout(1, 0, 10, 0);
        aboutContainer.add(versionContainer, BorderLayout.WEST);

        JLabel label = createLabel(name);
        try {
            BufferedImage logo = ImageIO.read(RunLevel.get("logo.png"));
            label.setIcon(new ImageIcon(logo));
        } catch (IOException e) {
            Logger.error("Failed to load {} logo", StaticConstant.PROJECT);
        }
        iconContainer.add(label);

        JSONArray contributorsList = Github.getContributorsList();
        double c = Math.ceil((double) contributorsList.length() / 5);
        int pos = 0;
        for (int i = 0; i < c; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < contributorsList.length() / c; j++) {
                JSONObject contributor = contributorsList.getJSONObject(pos);
                pos++;
                builder.append(contributor.getString("login")).append("\n");
            }
            LTextArea contributors = createTextArea(builder.toString());
            labelContainer.add(contributors);
        }

        String currentInfo = "Current version (Release date):\n\n" + Github.getCurrentVersion() + "\n" + Github.getCurrentReleaseDate();
        LTextArea currentVersion = createTextArea(currentInfo);
        versionContainer.add(currentVersion);
        if (!Github.getCurrentVersion().equals(Github.getLatestVersion())) {
            String latestInfo = "Latest version (Release date):\n\n" + Github.getLatestVersion() + "\n" + Github.getLatestReleaseDate();
            LTextArea latestVersion = createTextArea(latestInfo);
            versionContainer.add(latestVersion);
        }
        return result;
    }

    public static SettingUIComponent createHelpComponent() {
        SettingUIComponent result = new SettingUIComponent(new BorderLayout());

        ChildUIComponent discordContainer = createDefaultFlowLayout();
        result.add(discordContainer, BorderLayout.WEST);

        JLabel h = createLabel("Come ask on:");
        discordContainer.add(h, BorderLayout.WEST);

        LFlatButton help = new LFlatButton("Discord", LTextAlign.CENTER, HighlightType.COMPONENT);
        help.addActionListener(listener -> {
            try {
                Network.browse("https://discord.gg/UcGhC9dcHk");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        discordContainer.add(help);

        return result;
    }

    public static SettingUIComponent createKnownBugComponent() {
        SettingUIComponent result = new SettingUIComponent(new BorderLayout());

        ChildUIComponent knownBugContainer = createDefaultFlowLayout();
        result.add(knownBugContainer, BorderLayout.WEST);

        JSONArray issues = Github.getIssues();
        StringBuilder builder = new StringBuilder();
        builder.append("Currently known bugs:\n\n");
        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = issues.getJSONObject(i);
            String title = issue.getString("title");
            if (title.startsWith("[BUG]")) {
                builder.append(title).append("\n");
            }
        }
        LTextArea bugs = createTextArea(builder.toString());
        knownBugContainer.add(bugs);

        return result;
    }

    public static SettingUIComponent createSubmitBugComponent() {
        SettingUIComponent result = new SettingUIComponent(new BorderLayout());

        ChildUIComponent submitBugContainer = createDefaultFlowLayout();
        result.add(submitBugContainer, BorderLayout.WEST);

        JLabel b = createLabel("Found a bug?");
        submitBugContainer.add(b);

        LFlatButton bug = new LFlatButton("Submit bug", LTextAlign.CENTER, HighlightType.COMPONENT);
        bug.addActionListener(listener -> {
            JFrame temp = new JFrame();
            int confirm = JOptionPane.showConfirmDialog(temp, "This will open a bug issue on Github. You'll need an account to continue. Do you want to continue?",
                    "Submit bug?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION)
                Github.submitBug();
            temp.dispose();
        });
        submitBugContainer.add(bug);

        return result;
    }

    public static SettingUIComponent createComboBoxComponent(String name, SettingService settingService, String key, LComboBox comboBox) {
        DynamicObject settings = settingService.getClientSettings();

        SettingUIComponent result = new SettingUIComponent(new BorderLayout());
        result.setPreferredSize(new Dimension(10, 64));

        ChildUIComponent comboBoxContainer = new ChildUIComponent(new FlowLayout());
        result.add(comboBoxContainer, BorderLayout.SOUTH);

        ChildUIComponent labelContainer = new ChildUIComponent(new FlowLayout());
        labelContainer.setBorder(new EmptyBorder(0, 16, 0, 0));
        result.add(labelContainer, BorderLayout.WEST);

        JLabel label = new JLabel(name);
        label.setFont(textFont);
        label.setForeground(Color.WHITE);
        labelContainer.add(label);

        comboBoxContainer.add(comboBox, BorderLayout.SOUTH);

        result.addOnSaveActionListener(listener -> {
            int value = comboBox.getSelectedIndex();
            try {
                settingService.write(SettingType.CLIENT, key, value);
            } catch (InvalidPathException | NullPointerException e) {
                Logger.warn(e.getMessage());
            }
        });

        result.addOnQuitActionListener(listener -> {
            comboBox.setSelectedIndex(settings.getByKeyOrDefault(key, 0));
        });

        return result;
    }

    private static JLabel createLabel(String name) {
        JLabel result = new JLabel(name);
        result.setFont(textFont);
        result.setForeground(Color.WHITE);
        return result;
    }

    private static LTextArea createTextArea(String text) {
        LTextArea result = new LTextArea();
        result.setText(text);
        return result;
    }

    private static LSlider createVolumeSlider(int start) {
        LSlider result = new LSlider(JSlider.HORIZONTAL, 0, 100, start);
        result.setMajorTickSpacing(10);
        result.setMinorTickSpacing(1);
        result.setPaintTicks(true);
        result.setPaintLabels(true);
        return result;
    }

    public void save() {
        Object[] listeners = onSave.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                ((ActionListener) listeners[i + 1]).actionPerformed(null);
            }
        }
    }

    public void close() {
        Object[] listeners = onClose.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                ((ActionListener) listeners[i + 1]).actionPerformed(null);
            }
        }
    }

    private void addOnSaveActionListener(ActionListener listener) {
        onSave.add(ActionListener.class, listener);
    }

    private void addOnQuitActionListener(ActionListener listener) {
        onClose.add(ActionListener.class, listener);
    }
}
