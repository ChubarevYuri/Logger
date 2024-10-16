package ru.tpi;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Base {
    protected final Object lock = new Object();
    protected final String path;
    protected final String name;
    protected final Charset charset;
    protected final static char END_LINE = '\n';
    protected final static long VAL_TO_BYTES = 1024 * 1024;
    protected static final ArrayList<String> USED_FILES = new ArrayList<>();
    protected final boolean work;

    public Base(String path, String name, Charset charset) {
        synchronized (USED_FILES) {
            this.path = path;
            this.name = name;
            this.charset = charset;
            work = !USED_FILES.contains(getPath().toString());
            if (work) {
                USED_FILES.add(getPath().toString());
            }
        }
    }

    public Base(String path, String name) {
        this(path, name, StandardCharsets.UTF_8);
    }

    protected Path getPath() {
        return Paths.get(this.path + "/" + this.name + ".log");
    }
    protected File getFile() {
        return getPath().toFile();
    }

    private Path getTemporalPath() {
        return Paths.get(this.path + "/~" + this.name + ".log");
    }
    private File getTemporalFile() {
        return getTemporalPath().toFile();
    }

    private int size = 1;
    /**
     * размер файла логов в MB
     */
    public int getSize() {
        synchronized (lock) {
            return size;
        }
    }
    /**
     * размер файла логов в MB
     */
    public void setSize(int size) {
        if (size <= 0) {
            size = 1;
        }
        synchronized (lock) {
            this.size = size;
        }
    }

    protected void writeLine(String line) throws IOException {
        if (!work) {return;}
        synchronized (lock) {
            PrintStream out = new PrintStream(new FileOutputStream(getFile(), true));
            if (getFile().length() > 0) {
                out.write(("" + END_LINE).getBytes(charset));
            }
            out.write(line.getBytes(charset));
            out.close();
        }
    }

    protected void sizeControl(int savedFirstLinesCount) throws IOException {
        if (!work) {return;}
        if (savedFirstLinesCount <= 0) {
            savedFirstLinesCount = 0;
        }
        synchronized (lock) {
            while (getFile().length() > (long) getSize() * VAL_TO_BYTES) {
                int linesWrite = 0;
                if (Runtime.getRuntime().freeMemory() > (long) getSize() * VAL_TO_BYTES * 2) {
                    List<String> content = Files.readAllLines(getPath(), charset);
                    StringBuilder result = null;
                    for (String line : content) {
                        if (linesWrite != savedFirstLinesCount) {
                            if (result == null) {
                                result = new StringBuilder(line);
                            } else {
                                result.append(END_LINE).append(line);
                            }
                        }
                        linesWrite++;
                    }
                    Files.writeString(getPath(), Objects.requireNonNullElse(result, ""), charset);
                } else {
                    if (getTemporalFile().exists()) {
                        if (!getTemporalFile().delete()) { return; }
                    }
                    boolean first = true;
                    BufferedReader reader =
                            new BufferedReader(
                                new InputStreamReader(
                                    new FileInputStream(getFile()), charset));
                    PrintWriter writer = new PrintWriter(getTemporalFile(), charset);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isEmpty()) {
                            if (linesWrite != savedFirstLinesCount) {
                                if (!first) {
                                    writer.print("" + END_LINE);
                                }
                                writer.print(line);
                                first = false;
                            }
                            linesWrite++;
                        }
                    }
                    reader.close();
                    writer.close();
                    if (!getFile().delete()) { return; }
                    if (!getTemporalFile().renameTo(getFile())) { return; }
                }
            }
        }
    }

}
