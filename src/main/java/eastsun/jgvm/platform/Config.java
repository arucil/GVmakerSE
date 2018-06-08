package eastsun.jgvm.platform;

import com.nikhaldimann.inieditor.IniEditor;
import eastsun.jgvm.util.ColorUtil;

import java.io.IOException;

public final class Config {
    private Config() {}

    private static int steps;
    private static int delay;
    private static boolean isDelayEnabled;

    private static int backgroundColor;
    private static int foregroundColor;
    private static int pixelScale;

    private static Exception configLoadingException;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        IniEditor ini = new IniEditor();
        try {
            ini.load("jgvm.ini");
        } catch (IOException e) {
            configLoadingException = e;
            return;
        }

        try {
            steps = Integer.parseInt(ini.get("Run", "steps"));
        } catch (NullPointerException | NumberFormatException e) {
            configLoadingException = e;
            return;
        }

        try {
            delay = Integer.parseInt(ini.get("Run", "delay"));
        } catch (NullPointerException | NumberFormatException e) {
            configLoadingException = e;
            return;
        }

        try {
            isDelayEnabled = Boolean.parseBoolean(ini.get("Run", "delayEnabled"));
        } catch (NullPointerException | NumberFormatException e) {
            configLoadingException = e;
            return;
        }

        try {
            backgroundColor = ColorUtil.parse(ini.get("UI", "backgroundColor"));
        } catch (NullPointerException | IllegalArgumentException e) {
            configLoadingException = e;
            return;
        }

        try {
            foregroundColor = ColorUtil.parse(ini.get("UI", "foregroundColor"));
        } catch (NullPointerException | IllegalArgumentException e) {
            configLoadingException = e;
            return;
        }

        try {
            pixelScale = Integer.parseInt(ini.get("UI", "pixelScale"));
        } catch (NullPointerException | NumberFormatException e) {
            configLoadingException = e;
        }
    }

    public static Exception getConfigLoadingException() {
        return configLoadingException;
    }

    public static int getSteps() {
        return steps;
    }

    public static int getDelay() {
        return delay;
    }

    public static boolean isDelayEnabled() {
        return isDelayEnabled;
    }

    public static int getBackgroundColor() {
        return backgroundColor;
    }

    public static int getForegroundColor() {
        return foregroundColor;
    }

    public static int getPixelScale() {
        return pixelScale;
    }
}
