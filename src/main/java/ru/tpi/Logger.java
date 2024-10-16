package ru.tpi2;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Logger extends Base {
    protected final ArrayList<Thread> threads = new ArrayList<>();

    public Logger(String path, String name, Charset charset) {
        super(path, name, charset);
    }

    public Logger(String path, String name) {
        this(path, name, StandardCharsets.UTF_8);
    }

    private Level level = Level.DEBUG;
    /**
     * минимальный уровень логирования
     */
    public Level getLevel() {
        synchronized (lock) {
            return level;
        }
    }
    /**
     * минимальный уровень логирования
     */
    public void setLevel(Level level) {
        if (level == null) {
            throw new NullPointerException();
        }
        synchronized (lock) {
            this.level = level;
        }
    }

    /**
     * запись сообщения в файл
     *
     * @param level   уровень сообщения, если ниже getLevel(), то сообщение не будет записано
     * @param message сообщение для записи в лог
     */
    public void log(Level level, String message) {
        if (level == null) {
            return;
        }
        if (level == Level.FATAL) {
            synchronized (threads) {
                send(level, message);
            }
        } else {
            Thread th = new Thread(() -> {
                boolean queue = false;
                Thread ct = Thread.currentThread();
                while (!queue) {
                    synchronized (threads) {
                        if (threads.getFirst().equals(ct)) {
                            queue = true;
                        }
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) { }
                }
                send(level, message);
                synchronized (threads) {
                    threads.remove(ct);
                }
                System.out.println(message);
            });
            synchronized (threads) {
                threads.add(th);
            }
            th.start();
        }
    }

    private void send(Level level, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        message = message.replace("\\", "\\\\");
        message = message.replace("\n", "\\n");
        message = message.replace("\r", "\\r");
        message = message.replace("\t", "\\t");
        message = String.format("%s %-7s %s",
                new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS").format(new Date()),
                level,
                message);
        try {
            synchronized (lock) {
                if (this.level.compareTo(level) > 0) {
                    return;
                }
                writeLine(message);
                sizeControl(0);
            }
        } catch (Exception ignored) { }
    }
}