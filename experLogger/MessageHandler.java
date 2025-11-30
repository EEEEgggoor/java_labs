package experLogger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Класс для централизованной записи сообщений и ошибок в лог-файл.
 * Поддерживает счётчик ошибок и автоматическое закрытие ресурса.
 * 
 * @author Generated
 * @version 2.0
 */
public class MessageHandler implements AutoCloseable {
    private final BufferedWriter writer;
    private final PrintWriter pw;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private int errorCount = 0;

    /**
     * Открывает лог-файл для записи.
     * @param filename путь к файлу логов
     * @param append если true, файл будет открыт в режиме дополнения
     * @throws IOException при ошибке открытия файла
     */
    public MessageHandler(String filename, boolean append) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(filename, append));
        this.pw = new PrintWriter(writer);
    }

    /**
     * Записать информационную строку с временной меткой.
     * Метод синхронизирован для потокобезопасности.
     */
    public synchronized void logInfo(String message) {
        String ts = LocalDateTime.now().format(dtf);
        pw.println(ts + " INFO: " + message);
        pw.flush();
    }

    /**
     * Записать сырое сообщение (без временной метки)
     */
    public synchronized void logRaw(String message) {
        pw.println(message);
        pw.flush();
    }

    /**
     * Зарегистрировать и записать ошибку
     */
    public synchronized void logError(String contextMessage, Throwable t) {
        errorCount++;
        String ts = LocalDateTime.now().format(dtf);
        if (t != null) {
            pw.println(ts + " ERROR: " + contextMessage + " - " + t.getMessage());
            t.printStackTrace(pw);
        } else {
            pw.println(ts + " ERROR: " + contextMessage);
        }
        pw.flush();
    }

    /**
     * Получить количество зарегистрированных ошибок
     */
    public synchronized int getErrorCount() {
        return errorCount;
    }

    /**
     * Закрыть ресурс (файл). Автоматически вызывается для try-with-resources
     */
    @Override
    public synchronized void close() throws IOException {
        try {
            pw.flush();
        } finally {
            pw.close();
            writer.close();
        }
    }
}