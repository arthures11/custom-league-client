package com.hawolt.ui.settings;

import com.hawolt.client.settings.SettingsObject;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import com.hawolt.util.panel.ChildUIComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;

public class SettingUIComponent extends ChildUIComponent {
    private static final Font textFont = new Font("Arial", Font.PLAIN, 12);
    private static final Font tagFont = new Font("Arial", Font.BOLD, 20);
    private final EventListenerList onSave = new EventListenerList();
    private final EventListenerList onClose = new EventListenerList();

    private SettingUIComponent (LayoutManager layout, SettingsObject settings, String key) {
        super(layout);
    }

    public void save() {
        Object[] listeners = onSave.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ActionListener.class) {
                ((ActionListener)listeners[i+1]).actionPerformed(null);
            }
        }
    }

    public void close() {
        Object[] listeners = onClose.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ActionListener.class) {
                ((ActionListener)listeners[i+1]).actionPerformed(null);
            }
        }
    }

    private void addOnSaveActionListener(ActionListener listener) {
        onSave.add(ActionListener.class, listener);
    }

    private void addOnQuitActionListener(ActionListener listener) {
        onClose.add(ActionListener.class, listener);
    }

    public static ChildUIComponent createTagComponent(String name) {
        ChildUIComponent result = new ChildUIComponent(new BorderLayout());
        result.setPreferredSize(new Dimension(10, 32));

        ChildUIComponent labelContainer = new ChildUIComponent(new FlowLayout());
        result.add(labelContainer, BorderLayout.WEST);

        JLabel label = new JLabel(name);
        label.setFont(tagFont);
        labelContainer.add(label);

        return result;
    }

    public static SettingUIComponent createPathComponent(String name, SettingsObject settings, String key) {
        SettingUIComponent result = new SettingUIComponent(new BorderLayout(), settings, key);
        result.setPreferredSize(new Dimension(10, 64));

        ChildUIComponent pathContainer = new ChildUIComponent(new FlowLayout());
        result.add(pathContainer, BorderLayout.SOUTH);

        ChildUIComponent labelContainer = new ChildUIComponent(new FlowLayout());
        labelContainer.setBorder(new EmptyBorder(0, 16, 0, 0));
        result.add(labelContainer, BorderLayout.WEST);

        JLabel label = new JLabel(name);
        label.setFont(textFont);
        labelContainer.add(label);

        JTextField pathField = new JTextField("");
        pathField.setText(settings.getString(key));
        pathField.setPreferredSize(new Dimension(800, 30));
        pathContainer.add(pathField, BorderLayout.SOUTH);

        JButton searchButton = new JButton("Open");
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
                settings.put(key, path);
            } catch (InvalidPathException | NullPointerException e) {}
        });

        result.addOnQuitActionListener(listener -> {
            pathField.setText(settings.getString(key));
        });

        return result;
    }
}