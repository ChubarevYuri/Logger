package ru.tpi2;

public abstract class Row {

    /**
     * представление заголовка
     */
    public abstract String getHead();

    public int getHeadLinesCount() {
        if (getHead() == null || getHead().isEmpty()) {
            return 0;
        } else {
            int result = 1;
            for (char c : getHead().toCharArray()) {
                if (c == '\n') {
                    result++;
                }
            }
            return result;
        }
    }

    /**
     * представление данных
     */
    public abstract String getTail();
}
