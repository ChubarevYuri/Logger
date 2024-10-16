package ru.tpi2;

import java.io.IOException;
import java.nio.charset.Charset;

public class Table<T extends Row> extends Base {

    public Table(String path, String name, Charset charset) {
        super(path, name, charset);
    }

    public Table(String path, String name) {
        super(path, name);
    }

    public void add(T row) {
        if (row == null) { return; }
        synchronized (lock) {
            try {
                if (!getFile().exists() || !getFile().isFile() || getFile().length() <= 0) {
                    writeLine(row.getHead());
                }
                writeLine(row.getTail());
                sizeControl(row.getHeadLinesCount());
            } catch (IOException ignored) { }
        }
    }
}
