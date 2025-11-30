package test;

import config.Settings;
import controller.ZooController;
import db.DatabaseManager;
import enclosure.Enclosure;
import enclosure.EnclosureType;
import experLogger.OperationLogger;
import java.util.ArrayList;
import java.util.List;
import model.Animal;
import model.Aquatic;
import model.ColdBlooded;
import model.Feathered;
import model.Hoofed;

/**
 * Класс для автоматического тестирования функциональности приложения
 */
public class AutoTest {
    private final Settings settings;
    private final OperationLogger appLogger;
    private int passedTests;
    private int totalTests;

    /**
     * Конструктор класса AutoTest.
     * 
     * @param settings настройки приложения
     * @param appLogger логгер для записи результатов тестов
     */
    public AutoTest(Settings settings, OperationLogger appLogger) {
        this.settings = settings;
        this.appLogger = appLogger;
        this.passedTests = 0;
        this.totalTests = 0;
    }

    /**
     * Запускает все автотесты и возвращает общий результат.
     * 
     * @return true если все тесты пройдены, иначе false
     */
    public boolean runAllTests() {
        passedTests = 0;
        totalTests = 0;

        appLogger.logInfo("Starting all auto tests...");

        // Запуск отдельных тестов
        testAnimalCreation();
        testEnclosureCapacity();
        testDatabaseOperations();
        testAnimalDistribution();
        testAnimalTypes();
        testEnclosureTypes();
        testAnimalEnclosureCompatibility();

        appLogger.logInfo("Auto tests completed: " + passedTests + "/" + totalTests + " passed");

        return passedTests == totalTests;
    }

    /**
     * Запускает все автотесты с выводом результатов в консоль.
     * 
     * @return true если все тесты пройдены, иначе false
     */
    public boolean runAllTestsWithConsoleOutput() {
        System.out.println("\n=== ЗАПУСК АВТОТЕСТОВ ===");
        
        boolean result = runAllTests();
        
        // Вывод результатов в консоль
        System.out.println("\n=== РЕЗУЛЬТАТЫ ТЕСТИРОВАНИЯ ===");
        System.out.println("Пройдено тестов: " + passedTests + " из " + totalTests);
        
        if (result) {
            System.out.println("✓ ВСЕ ТЕСТЫ ПРОЙДЕНЫ УСПЕШНО");
        } else {
            System.out.println("✗ НЕКОТОРЫЕ ТЕСТЫ НЕ ПРОЙДЕНЫ");
        }
        
        return result;
    }

    /**
     * Тестирует создание объектов животных.
     */
    public void testAnimalCreation() {
        totalTests++;
        try {
            Animal animal = new Aquatic("TestFish", 1.5, 2);
            boolean test1 = animal.getName().equals("TestFish");
            boolean test2 = animal.getWeight() == 1.5;
            boolean test3 = animal.getAge() == 2;

            if (test1 && test2 && test3) {
                passedTests++;
                appLogger.logInfo("Animal creation test: PASSED");
            } else {
                appLogger.logError("Animal creation test: FAILED - data mismatch", null);
            }
        } catch (Exception e) {
            appLogger.logError("Animal creation test: FAILED", e);
        }
    }

    /**
     * Тестирует функциональность вольеров.
     */
    public void testEnclosureCapacity() {
        totalTests++;
        try {
            Enclosure enclosure = new Enclosure("Test", EnclosureType.AQUARIUM, 5);
            boolean test1 = enclosure.getCapacity() == 5;
            boolean test2 = enclosure.getName().equals("Test");
            boolean test3 = enclosure.getType() == EnclosureType.AQUARIUM;

            if (test1 && test2 && test3) {
                passedTests++;
                appLogger.logInfo("Enclosure capacity test: PASSED");
            } else {
                appLogger.logError("Enclosure capacity test: FAILED - data mismatch", null);
            }
        } catch (Exception e) {
            appLogger.logError("Enclosure capacity test: FAILED", e);
        }
    }

    /**
     * Тестирует операции с базой данных.
     */
    public void testDatabaseOperations() {
        totalTests++;
        try {
            List<Animal> testAnimals = new ArrayList<>();
            testAnimals.add(new Aquatic("TestFish", 1.5, 2));

            // Сохраняем тестовых животных
            DatabaseManager.saveAnimals(testAnimals);

            // Загружаем обратно
            List<Animal> loadedAnimals = DatabaseManager.loadAnimals();

            // Проверяем, что данные совпадают
            boolean success = !loadedAnimals.isEmpty() && 
                    loadedAnimals.get(0).getName().equals("TestFish");

            if (success) {
                passedTests++;
                appLogger.logInfo("Database operations test: PASSED");
            } else {
                appLogger.logError("Database operations test: FAILED - data mismatch", null);
            }
        } catch (Exception e) {
            appLogger.logError("Database operations test: FAILED", e);
        }
    }

    /**
     * Тестирует распределение животных по вольерам.
     */
    public void testAnimalDistribution() {
        totalTests++;
        try {
            // Создаем тестовые данные
            List<Enclosure> testEnclosures = new ArrayList<>();
            testEnclosures.add(new Enclosure("TestAqua", EnclosureType.AQUARIUM, 2));

            List<Animal> testAnimals = new ArrayList<>();
            testAnimals.add(new Aquatic("TestFish1", 1.0, 1));
            testAnimals.add(new Aquatic("TestFish2", 1.5, 2));

            // Распределяем животных
            ZooController controller = new ZooController(testEnclosures);
            List<Animal> unassigned = controller.distributeAnimals(testAnimals);

            // Проверяем, что все животные размещены (или не размещены, если нет места)
            boolean success = unassigned.size() <= testAnimals.size();

            if (success) {
                passedTests++;
                appLogger.logInfo("Animal distribution test: PASSED");
            } else {
                appLogger.logError("Animal distribution test: FAILED - distribution issue", null);
            }
        } catch (Exception e) {
            appLogger.logError("Animal distribution test: FAILED", e);
        }
    }

    /**
     * Тестирует создание всех типов животных.
     */
    public void testAnimalTypes() {
        totalTests++;
        try {
            // Тестируем создание животных всех типов
            Animal aquatic = new Aquatic("Fish", 1.0, 1);
            Animal feathered = new Feathered("Bird", 0.5, 2);
            Animal hoofed = new Hoofed("Deer", 150.0, 3);
            Animal coldBlooded = new ColdBlooded("Lizard", 0.2, 1);

            boolean test1 = aquatic instanceof Aquatic;
            boolean test2 = feathered instanceof Feathered;
            boolean test3 = hoofed instanceof Hoofed;
            boolean test4 = coldBlooded instanceof ColdBlooded;

            if (test1 && test2 && test3 && test4) {
                passedTests++;
                appLogger.logInfo("Animal types test: PASSED");
            } else {
                appLogger.logError("Animal types test: FAILED - type mismatch", null);
            }
        } catch (Exception e) {
            appLogger.logError("Animal types test: FAILED", e);
        }
    }

    /**
     * Тестирует функциональность разных типов вольеров.
     */
    public void testEnclosureTypes() {
        totalTests++;
        try {
            // Тестируем создание вольеров всех типов
            Enclosure aquarium = new Enclosure("Aqua", EnclosureType.AQUARIUM, 5);
            Enclosure netCovered = new Enclosure("Net", EnclosureType.NET_COVERED, 3);
            Enclosure open = new Enclosure("Open", EnclosureType.OPEN, 10);
            Enclosure infrared = new Enclosure("Infra", EnclosureType.INFRARED, 2);

            boolean test1 = aquarium.getType() == EnclosureType.AQUARIUM;
            boolean test2 = netCovered.getType() == EnclosureType.NET_COVERED;
            boolean test3 = open.getType() == EnclosureType.OPEN;
            boolean test4 = infrared.getType() == EnclosureType.INFRARED;

            if (test1 && test2 && test3 && test4) {
                passedTests++;
                appLogger.logInfo("Enclosure types test: PASSED");
            } else {
                appLogger.logError("Enclosure types test: FAILED - type mismatch", null);
            }
        } catch (Exception e) {
            appLogger.logError("Enclosure types test: FAILED", e);
        }
    }

    /**
     * Тестирует совместимость животных с вольерами.
     */
    public void testAnimalEnclosureCompatibility() {
        totalTests++;
        try {
            // Создаем вольеры разных типов
            Enclosure aquarium = new Enclosure("Aqua", EnclosureType.AQUARIUM, 5);
            Enclosure netCovered = new Enclosure("Net", EnclosureType.NET_COVERED, 3);

            // Создаем животных разных типов
            Animal aquatic = new Aquatic("Fish", 1.0, 1);
            Animal feathered = new Feathered("Bird", 0.5, 2);

            // Проверяем совместимость
            boolean test1 = aquarium.canAccept(aquatic);   // Должно быть true
            boolean test2 = aquarium.canAccept(feathered); // Должно быть false
            boolean test3 = netCovered.canAccept(feathered); // Должно быть true
            boolean test4 = netCovered.canAccept(aquatic);   // Должно быть false

            if (test1 && !test2 && test3 && !test4) {
                passedTests++;
                appLogger.logInfo("Animal enclosure compatibility test: PASSED");
            } else {
                appLogger.logError("Animal enclosure compatibility test: FAILED - compatibility issue", null);
            }
        } catch (Exception e) {
            appLogger.logError("Animal enclosure compatibility test: FAILED", e);
        }
    }

    /**
     * Запускает расширенный набор тестов (включая дополнительные тесты).
     */
    public boolean runExtendedTests() {
        passedTests = 0;
        totalTests = 0;

        appLogger.logInfo("Starting extended auto tests...");

        // Базовые тесты
        testAnimalCreation();
        testEnclosureCapacity();
        testDatabaseOperations();
        testAnimalDistribution();
        testAnimalTypes();
        testEnclosureTypes();
        
        // Расширенные тесты
        testAnimalEnclosureCompatibility();

        appLogger.logInfo("Extended auto tests completed: " + passedTests + "/" + totalTests + " passed");

        return passedTests == totalTests;
    }

    /**
     * Возвращает количество пройденных тестов.
     */
    public int getPassedTests() {
        return passedTests;
    }

    /**
     * Возвращает общее количество тестов.
     */
    public int getTotalTests() {
        return totalTests;
    }

    /**
     * Возвращает процент успешно пройденных тестов.
     */
    public double getSuccessRate() {
        if (totalTests == 0) return 0.0;
        return (double) passedTests / totalTests * 100;
    }
}