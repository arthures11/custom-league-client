package com.hawolt.util.audio;

import com.hawolt.logger.Logger;

import javax.sound.sampled.*;
import javax.sound.sampled.Control.Type;
import javax.sound.sampled.Mixer.Info;
import java.util.ArrayList;
import java.util.List;

public class AudioEngineMasterOutput {

    public static void setMasterOutputVolume(float value) {
        Line line = getMasterOutputLine();
        if (line == null) {
            Logger.error("Master output port not found");
            throw new RuntimeException();
        }
        boolean opened = open(line);
        try {
            FloatControl control = getVolumeControl(line);
            if (control == null) {
                Logger.error("Volume control not found in master port: " + toString(line));
                throw new RuntimeException();
            }
            control.setValue(value);
        } finally {
            if (opened) line.close();
        }
    }

    public static Line getMasterOutputLine() {
        for (Mixer mixer : getMixers()) {
            for (Line line : getAvailableOutputLines(mixer)) {
                if (line.getLineInfo().toString().contains("SPEAKER")) return line;
            }
        }
        return null;
    }

    public static FloatControl getVolumeControl(Line line) {
        if (!line.isOpen())
            Logger.error("Line is closed: " + toString(line));
        return (FloatControl) findControl(FloatControl.Type.VOLUME, line.getControls());
    }

    private static Control findControl(Type type, Control... controls) {
        if (controls == null) return null;
        for (Control control : controls) {
            if (control.getType().equals(type)) return control;
            if (control instanceof CompoundControl compoundControl) {
                Control member = findControl(type, compoundControl.getMemberControls());
                if (member != null) return member;
            }
        }
        return null;
    }

    public static List<Mixer> getMixers() {
        Info[] infos = AudioSystem.getMixerInfo();
        List<Mixer> mixers = new ArrayList<>(infos.length);
        for (Info info : infos) {
            Mixer mixer = AudioSystem.getMixer(info);
            mixers.add(mixer);
        }
        return mixers;
    }

    public static List<Line> getAvailableOutputLines(Mixer mixer) {
        return getAvailableLines(mixer, mixer.getTargetLineInfo());
    }

    private static List<Line> getAvailableLines(Mixer mixer, Line.Info[] lineInfos) {
        List<Line> lines = new ArrayList<>(lineInfos.length);
        for (Line.Info lineInfo : lineInfos) {
            Line line;
            line = getLineIfAvailable(mixer, lineInfo);
            if (line != null) lines.add(line);
        }
        return lines;
    }

    public static Line getLineIfAvailable(Mixer mixer, Line.Info lineInfo) {
        try {
            return mixer.getLine(lineInfo);
        } catch (LineUnavailableException ex) {
            return null;
        }
    }

    public static boolean open(Line line) {
        if (line.isOpen()) return false;
        try {
            line.open();
        } catch (LineUnavailableException ex) {
            return false;
        }
        return true;
    }

    public static String toString(Control control) {
        if (control == null) return null;
        return control + " (" + control.getType().toString() + ")";
    }

    public static String toString(Line line) {
        if (line == null) return null;
        Line.Info info = line.getLineInfo();
        return info.toString();
    }

    public static String toString(Mixer mixer) {
        if (mixer == null) return null;
        StringBuilder sb = new StringBuilder();
        Mixer.Info info = mixer.getMixerInfo();
        sb.append(info.getName());
        sb.append(" (").append(info.getDescription()).append(")");
        sb.append(mixer.isOpen() ? " [open]" : " [closed]");
        return sb.toString();
    }

}
