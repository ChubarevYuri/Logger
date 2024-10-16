package ru.tpi2;

import java.util.Objects;

public class Level {
    private byte level;
    private Level(byte level) {
        this.level = level;
    }

    public static Level DEBUG = new Level((byte)0);
    public static Level SETTING = new Level((byte)1);
    public static Level INFO = new Level((byte)2);
    public static Level WARNING = new Level((byte)3);
    public static Level ERROR = new Level((byte)4);
    public static Level FATAL = new Level((byte)5);

    @Override
    public String toString() {
        return switch (this.level) {
            case 0 -> "DEBUG";
            case 1 -> "SETTING";
            case 2 -> "INFO";
            case 3 -> "WARNING";
            case 4 -> "ERROR";
            case 5 -> "FATAL";
            default -> "UNKNOWN";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return level == ((Level) o).level;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(level);
    }

    public int compareTo(Level o) {
        if (o == null) {
            throw new NullPointerException();
        }
        return this.level - o.level;
    }

    public static Level valueOf(String value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return switch (value.toLowerCase()) {
            case "0", "d", "debug" -> DEBUG;
            case "1", "s", "setting" -> SETTING;
            case "2", "i", "info" -> INFO;
            case "3", "w", "warning" -> WARNING;
            case "4", "e", "error" -> ERROR;
            case "5", "f", "fatal" -> FATAL;
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }
}
