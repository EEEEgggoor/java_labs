package experLogger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * OperationLogger теперь используется для общего логирования приложения
 */
public class OperationLogger implements AutoCloseable {
    private final MessageHandler msgHandler;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final String collectionName;
    private final LocalDateTime startTime;

    private final List<Long> addTimes = new ArrayList<>();
    private final List<Long> setTimes = new ArrayList<>();
    private long totalOpsTimeNanos = 0L;


    public OperationLogger(String filename, String collectionName, boolean append) throws IOException {
        this.msgHandler = new MessageHandler(filename, append);
        this.collectionName = collectionName;
        this.startTime = LocalDateTime.now();
        writeHeader();
    }

    /**
     * Конструктор
     */
    public OperationLogger(String filename, String collectionName) throws IOException {
        this(filename, collectionName, false);
    }

    private void writeHeader() {
        msgHandler.logRaw("Start program: " + startTime.format(dtf));
        msgHandler.logRaw(collectionName);
    }

    /**
     * Лог одной операции (add или set). id — индекс/ключ (1-based в логе).
     * timeNanos — измеренное время операции в наносекундах.
     */
    public void logOperation(String opType, int id, long timeNanos) {
        try {
            msgHandler.logRaw(String.format("%s, ID = %d, %d", opType, id, timeNanos));
            totalOpsTimeNanos += timeNanos;
            if ("add".equals(opType)) addTimes.add(timeNanos);
            else if ("set".equals(opType)) setTimes.add(timeNanos);
        } catch (Exception e) {
            msgHandler.logError("Ошибка при логировании операции " + opType + " ID=" + id, e);
        }
    }

    /**
     * Метод для логирования информационных сообщений
     */
    public void logInfo(String message) {
        msgHandler.logInfo(message);
    }

    /**
     * Метод для логирования ошибок
     */
    public void logError(String message, Throwable t) {
        msgHandler.logError(message, t);
    }

    /**
     * Метод для логирования отладочной информации
     */
    public void logDebug(String message) {
        String timestamp = LocalDateTime.now().format(dtf);
        msgHandler.logRaw(timestamp + " DEBUG: " + message);
    }

    private void writeStatsFor(String prefix, List<Long> times) {
        long total = 0L;
        for (Long t : times) total += t;
        long median = calculateMedian(times);
        long avg = times.isEmpty() ? 0L : total / times.size();

        msgHandler.logRaw("");
        msgHandler.logRaw(prefix + "TotalCount = " + times.size());
        msgHandler.logRaw(prefix + "TotalTime = " + total);
        msgHandler.logRaw(prefix + "MedianTime = " + median);
        msgHandler.logRaw(prefix + "AverageTime = " + avg);
    }

    private long calculateMedian(List<Long> times) {
        if (times.isEmpty()) return 0L;
        List<Long> copy = new ArrayList<>(times);
        Collections.sort(copy);
        int m = copy.size();
        if (m % 2 == 1) return copy.get(m / 2);
        else return (copy.get(m / 2 - 1) + copy.get(m / 2)) / 2;
    }

    private void writeFooter() {
        msgHandler.logRaw("");
        writeStatsFor("add", addTimes);
        msgHandler.logRaw("");
        writeStatsFor("set", setTimes);
        msgHandler.logRaw("");
        msgHandler.logRaw("TotalOperationsTimeNanos = " + totalOpsTimeNanos);
        msgHandler.logRaw("Finish program: " + LocalDateTime.now().format(dtf));
    }

    /**
     * Получить количество ошибок, зарегистрированных MessageHandler-ом.
     */
    public int getErrorCount() {
        return msgHandler.getErrorCount();
    }

    @Override
    public void close() {
        // При закрытии записываем footer и закрываем MessageHandler
        try {
            writeFooter();
        } catch (Exception e) {
            // если при записи футера что-то пошло не так — сохранить это как ошибку
            msgHandler.logError("Ошибка при записи финального блока лога", e);
        } finally {
            try {
                msgHandler.close();
            } catch (IOException e) {
                // если закрытие не удалось — увеличить счётчик ошибок и записать
                msgHandler.logError("Ошибка при закрытии MessageHandler", e);
            }
        }
    }
}