package experLogger;

import controller.RandomDataGenerator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import model.Animal;

/**
 * CollectionExperiment — полный класс, который проводит эксперимент над коллекциями:
 * - заполняет коллекцию операциями add и логирует каждую операцию;
 * - затем выполняет set для 10% элементов (индексы случайные) и логирует set;
 * - использует ваш RandomDataGenerator для генерации Animal;
 * - использует OperationLogger (который в свою очередь использует MessageHandler) для записи логов.
 *
 * Логи записываются в файлы:
 *   ArrayList_10.log ... ArrayList_100000.log
 *   LinkedList_10.log ... LinkedList_100000.log
 *
 * Примечание: класс лежит в «default» package (без package declaration) чтобы корректно видеть OperationLogger,
 * если OperationLogger тоже в default package. Если у вас другая структура пакетов — добавьте соответствующий пакет.
 */
public class CollectionExperiment {
    private final Random rng = new Random(12345); // фиксированный seed для воспроизводимости

    /**
     * Запуск эксперимента для ArrayList размера size.
     */
    public void runArrayListExperiment(int size) {
        List<Animal> list = new ArrayList<>(size);
        String fname = "ArrayList_" + size + ".log";
        runExperimentOnList(list, fname, "ArrayList", size);
    }

    /**
     * Запуск эксперимента для LinkedList размера size.
     */
    public void runLinkedListExperiment(int size) {
        List<Animal> list = new LinkedList<>();
        String fname = "LinkedList_" + size + ".log";
        runExperimentOnList(list, fname, "LinkedList", size);
    }

    /**
     * Общая логика эксперимента для заданной реализации List<Animal>
     */
    private void runExperimentOnList(List<Animal> list, String fileName, String collectionName, int size) {
        try (OperationLogger logger = new OperationLogger(fileName, collectionName)) {
            List<Animal> generated = RandomDataGenerator.generateAnimals(size, false);

            for (int i = 0; i < generated.size(); i++) {
                Animal a = generated.get(i);
                long t0 = System.nanoTime();
                list.add(a);
                long t1 = System.nanoTime();
                long elapsed = t1 - t0;
                logger.logOperation("add", i + 1, elapsed);
            }

            int modifyCount = Math.max(1, size / 10);
            for (int k = 0; k < modifyCount; k++) {
                if (list.isEmpty()) break;
                int idx = rng.nextInt(list.size());
                Animal replacement = RandomDataGenerator.generateAnimals(1, false).get(0);
                long t0 = System.nanoTime();
                list.set(idx, replacement);
                long t1 = System.nanoTime();
                long elapsed = t1 - t0;
                logger.logOperation("set", idx + 1, elapsed);
            }

           
            int errors = logger.getErrorCount();
            if (errors > 0) {
                System.err.println("During experiment " + collectionName + " found errors: " + errors);
            }
        } catch (IOException e) {
            System.err.println("Не удалось создать/открыть лог-файл '" + fileName + "': " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Ошибка во время эксперимента для " + collectionName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}


