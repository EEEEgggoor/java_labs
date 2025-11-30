package view;

import config.Settings;
import controller.ZooController;
import db.DatabaseManager;
import enclosure.Enclosure;
import enclosure.EnclosureType;
import experLogger.OperationLogger;
import java.util.List;
import java.util.Scanner;
import model.Animal;
import model.Aquatic;
import model.ColdBlooded;
import model.Feathered;
import model.Hoofed;
import test.AutoTest;

/**
 * Класс для управления всем приложением, включая аутентификацию, автотесты и меню
 */
public class MenuView {
    private final Settings settings;
    private final OperationLogger appLogger;
    private List<Animal> animals;
    private List<Enclosure> enclosures;
    private final Scanner scanner;
    private final ZooView zooView;
    private final AutoTest autoTest;

    /**
     * Конструктор класса MenuView.
     */
    public MenuView(Settings settings, OperationLogger appLogger, 
                   List<Animal> animals, List<Enclosure> enclosures, Scanner scanner) {
        this.settings = settings;
        this.appLogger = appLogger;
        this.animals = animals;
        this.enclosures = enclosures;
        this.scanner = scanner;
        this.zooView = new ZooView();
        this.autoTest = new AutoTest(settings, appLogger);
    }

    /**
     * Запускает основную логику приложения.
     * Включает аутентификацию, автотесты и главное меню.
     */
    public void start() {
        
        // Аутентификация пользователя
        if (!authenticate()) {
            appLogger.logError("Authentication failed", null);
            return;
        }
        
        System.out.println("Добро пожаловать - " + settings.getUsername());
        
        // Автоматический запуск автотестов если включено в настройках
        if (settings.isAutoTestMode()) {
            runAutoTestsOnStartup();
        }
        
        // Запуск главного меню
        showMainMenu();
    }

    /**
     * Проводит аутентификацию пользователя.
     */
    private boolean authenticate() {
        System.out.println("=== АУТЕНТИФИКАЦИЯ ===");
        System.out.print("Логин: ");
        String username = scanner.nextLine();
        System.out.print("Пароль: ");
        String password = scanner.nextLine();
        
        boolean authenticated = username.equals(settings.getUsername()) && 
                            password.equals(settings.getPassword());
        
        if (authenticated) {
            appLogger.logInfo("User authenticated: " + username);
        } else {
            // Выводим сообщение в консоль
            System.out.println("Ошибка аутентификации: неверный логин или пароль!");
            appLogger.logError("Authentication failed for user: " + username, 
                            new SecurityException("Invalid credentials"));
        }
        
        return authenticated;
    }

    /**
     * Запускает автотесты при старте программы.
     */
    private void runAutoTestsOnStartup() {
        System.out.println("\n=== ЗАПУСК АВТОТЕСТОВ ПРИ СТАРТЕ ===");
        appLogger.logInfo("Running auto tests on startup...");

        boolean allPassed = autoTest.runAllTests();

        System.out.println("\nРезультаты автотестов при старте: " + 
            autoTest.getPassedTests() + "/" + autoTest.getTotalTests() + " тестов пройдено");

        if (allPassed) {
            System.out.println("✓ Все автотесты пройдены успешно");
            appLogger.logInfo("All startup auto tests passed");
        } else {
            System.out.println("✗ Некоторые автотесты не пройдены");
            appLogger.logInfo("Startup auto tests completed with failures: " + 
                autoTest.getPassedTests() + "/" + autoTest.getTotalTests());
        }
        
        System.out.printf("Процент успеха: %.1f%%\n", autoTest.getSuccessRate());
    }

    /**
     * Отображает главное меню и обрабатывает выбор пользователя.
     */
    private void showMainMenu() {
        while (true) {
            System.out.println("\n=== ГЛАВНОЕ МЕНЮ ===");
            System.out.println("1. Показать животных и вольеры");
            System.out.println("2. Расселить животных");
            System.out.println("3. Загрузить из базы данных");
            System.out.println("4. Сохранить в базу данных");
            System.out.println("5. Добавить животное");
            System.out.println("6. Удалить животное");
            System.out.println("7. Создать вольер");
            System.out.println("8. Эксперименты с коллекциями");
            System.out.println("9. Вывести график экспериментов с коллекциями");

            
            // Дополнительные пункты меню для root пользователей
            if (settings.isRoot()) {
                System.out.println("10. Отладка");
                System.out.println("11. Автотесты");
                System.out.println("12. Расширенные тесты");
            }
            
            System.out.println("0. Выход");
            System.out.print("Выберите пункт: ");
            
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
            } catch (Exception e) {
                System.out.println("Ошибка ввода. Пожалуйста, введите число.");
                scanner.nextLine(); // clear invalid input
                continue;
            }
            
            switch (choice) {
                case 1: showData(); break;
                case 2: distributeAnimals(); break;
                case 3: loadFromDatabase(); break;
                case 4: saveToDatabase(); break;
                case 5: addAnimal(); break;
                case 6: removeAnimal(); break;
                case 7: addEnclosure(); break;
                case 8: runCollectionExperiments(); break;
                case 9: GraphFromLogs.showWindowAutoScan(); break;
                case 10: 
                    if (settings.isRoot()) runDebug(); 
                    else System.out.println("Доступ запрещен. Требуются права root.");
                    break;
                case 14: 
                    if (settings.isRoot()) runAutoTests(); 
                    else System.out.println("Доступ запрещен. Требуются права root.");
                    break;
                case 12:
                    if (settings.isRoot()) runExtendedTests();
                    else System.out.println("Доступ запрещен. Требуются права root.");
                    break;
                case 0: 
                    System.out.println("Выход из программы...");
                    return;
                default: 
                    System.out.println("Неверный выбор. Пожалуйста, выберите пункт из меню.");
            }
        }
    }

    /**
     * Создает новый вольер через пользовательский ввод.
     */
    private void addEnclosure() {
        System.out.println("\n=== СОЗДАНИЕ НОВОГО ВОЛЬЕРА ===");
        
        // Ввод имени вольера
        System.out.print("Введите имя вольера: ");
        String name = scanner.nextLine();
        
        if (name.trim().isEmpty()) {
            System.out.println("Имя не может быть пустым. Операция отменена.");
            return;
        }
        
        // Выбор типа вольера
        System.out.println("Выберите тип вольера:");
        System.out.println("1. Аквариум (AQUARIUM) - для водоплавающих животных");
        System.out.println("2. Вольер с сеткой (NET_COVERED) - для птиц");
        System.out.println("3. Открытый вольер (OPEN) - для копытных животных");
        System.out.println("4. Вольер с ИК-освещением (INFRARED) - для хладнокровных животных");
        System.out.print("Ваш выбор (1-4): ");
        
        int typeChoice;
        try {
            typeChoice = scanner.nextInt();
            scanner.nextLine(); // consume newline
        } catch (Exception e) {
            System.out.println("Ошибка ввода. Операция отменена.");
            scanner.nextLine();
            return;
        }
        
        EnclosureType enclosureType;
        switch (typeChoice) {
            case 1:
                enclosureType = EnclosureType.AQUARIUM;
                break;
            case 2:
                enclosureType = EnclosureType.NET_COVERED;
                break;
            case 3:
                enclosureType = EnclosureType.OPEN;
                break;
            case 4:
                enclosureType = EnclosureType.INFRARED;
                break;
            default:
                System.out.println("Неверный выбор типа. Операция отменена.");
                return;
        }
        
        // Ввод вместимости
        System.out.print("Введите вместимость вольера (количество животных): ");
        int capacity;
        try {
            capacity = scanner.nextInt();
            scanner.nextLine(); // consume newline
        } catch (Exception e) {
            System.out.println("Ошибка ввода вместимости. Операция отменена.");
            scanner.nextLine();
            return;
        }
        
        if (capacity <= 0) {
            System.out.println("Вместимость должна быть положительным числом. Операция отменена.");
            return;
        }
        
        if (capacity > 100) {
            System.out.println("Вместимость слишком большая. Рекомендуется не более 100 животных.");
            System.out.print("Продолжить? (y/n): ");
            String confirm = scanner.nextLine();
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("Операция отменена.");
                return;
            }
        }
        
        // Создание вольера
        try {
            Enclosure newEnclosure = new Enclosure(name, enclosureType, capacity);
            enclosures.add(newEnclosure);
            
            System.out.println("✓ Вольер '" + name + "' успешно создан!");
            System.out.println("  Тип: " + enclosureType);
            System.out.println("  Вместимость: " + capacity + " животных");
            
            appLogger.logInfo("Created enclosure: " + newEnclosure);
            
        } catch (Exception e) {
            System.out.println("✗ Ошибка при создании вольера: " + e.getMessage());
            appLogger.logError("Failed to create enclosure: " + name, e);
        }
    }


    /**
     * Отображает текущие данные о животных и вольерах.
     */
    private void showData() {
        System.out.println("\n=== ОБЗОР ДАННЫХ ===");
        
        // Показываем общую статистику
        System.out.printf("Всего животных: %d\n", animals.size());
        System.out.printf("Всего вольеров: %d\n", enclosures.size());
        
        // Детальная информация о вольерах
        zooView.showEnclosuresDetailed(enclosures);
        
        // Список всех животных
        System.out.println("\n=== ВСЕ ЖИВОТНЫЕ ===");
        if (animals.isEmpty()) {
            System.out.println("Животные отсутствуют.");
        } else {
            for (int i = 0; i < animals.size(); i++) {
                Animal animal = animals.get(i);
                System.out.printf("%d. %s\n", i + 1, animal);
            }
        }
        
        // Логирование просмотра данных
        appLogger.logInfo("Data viewed: " + animals.size() + " animals, " + 
                        enclosures.size() + " enclosures");
    }

    /**
     * Распределяет животных по вольерам.
     */
    private void distributeAnimals() {
        if (animals.isEmpty()) {
            System.out.println("Нет животных для распределения.");
            return;
        }
        
        if (enclosures.isEmpty()) {
            System.out.println("Нет вольеров для распределения.");
            return;
        }
        
        ZooController controller = new ZooController(enclosures);
        List<Animal> unassigned = controller.distributeAnimals(animals);
        
        zooView.showEnclosures(enclosures);
        zooView.showUnassigned(unassigned);
        
        // Логирование результатов распределения
        appLogger.logInfo("Animals distribution completed: " + 
                         (animals.size() - unassigned.size()) + " placed, " + 
                         unassigned.size() + " unassigned");
    }

    /**
     * Загружает данные из базы данных.
     */
    private void loadFromDatabase() {
        List<Animal> loadedAnimals = DatabaseManager.loadAnimals();
        List<Enclosure> loadedEnclosures = DatabaseManager.loadEnclosures();
        
        if (!loadedAnimals.isEmpty() || !loadedEnclosures.isEmpty()) {
            animals = loadedAnimals;
            enclosures = loadedEnclosures;
            System.out.println("Данные загружены из базы данных: " + 
                             animals.size() + " животных, " + 
                             enclosures.size() + " вольеров");
            
            appLogger.logInfo("Data loaded from database: " + animals.size() + " animals, " + 
                             enclosures.size() + " enclosures");
        } else {
            System.out.println("Не удалось загрузить данные или база данных пуста.");
        }
    }

    /**
     * Сохраняет данные в базу данных.
     */
    private void saveToDatabase() {
        if (animals.isEmpty() && enclosures.isEmpty()) {
            System.out.println("Нет данных для сохранения.");
            return;
        }
        
        DatabaseManager.saveAnimals(animals);
        DatabaseManager.saveEnclosures(enclosures);
        System.out.println("Данные сохранены в базу данных: " + 
                         animals.size() + " животных, " + 
                         enclosures.size() + " вольеров");
        
        appLogger.logInfo("Data saved to database: " + animals.size() + " animals, " + 
                         enclosures.size() + " enclosures");
    }

    /**
     * Добавляет новое животное через пользовательский ввод.
     */
    private void addAnimal() {
        System.out.println("\n=== ДОБАВЛЕНИЕ НОВОГО ЖИВОТНОГО ===");
        
        // Выбор типа животного
        System.out.println("Выберите тип животного:");
        System.out.println("1. Водоплавающее (Aquatic)");
        System.out.println("2. Пернатое (Feathered)");
        System.out.println("3. Копытное (Hoofed)");
        System.out.println("4. Хладнокровное (ColdBlooded)");
        System.out.print("Ваш выбор (1-4): ");
        
        int typeChoice;
        try {
            typeChoice = scanner.nextInt();
            scanner.nextLine(); // consume newline
        } catch (Exception e) {
            System.out.println("Ошибка ввода. Операция отменена.");
            scanner.nextLine();
            return;
        }
        
        if (typeChoice < 1 || typeChoice > 4) {
            System.out.println("Неверный выбор типа. Операция отменена.");
            return;
        }
        
        // Ввод данных животного
        System.out.print("Введите имя животного: ");
        String name = scanner.nextLine();
        
        if (name.trim().isEmpty()) {
            System.out.println("Имя не может быть пустым. Операция отменена.");
            return;
        }
        
        System.out.print("Введите вес животного: ");
        double weight;
        try {
            weight = scanner.nextDouble();
        } catch (Exception e) {
            System.out.println("Ошибка ввода веса. Операция отменена.");
            scanner.nextLine();
            return;
        }
        
        if (weight <= 0) {
            System.out.println("Вес должен быть положительным числом. Операция отменена.");
            scanner.nextLine();
            return;
        }
        
        System.out.print("Введите возраст животного: ");
        int age;
        try {
            age = scanner.nextInt();
            scanner.nextLine(); // consume newline
        } catch (Exception e) {
            System.out.println("Ошибка ввода возраста. Операция отменена.");
            scanner.nextLine();
            return;
        }
        
        if (age < 0) {
            System.out.println("Возраст не может быть отрицательным. Операция отменена.");
            return;
        }
        
        // Создание животного в зависимости от выбранного типа
        Animal animal = null;
        switch (typeChoice) {
            case 1:
                animal = new Aquatic(name, weight, age);
                break;
            case 2:
                animal = new Feathered(name, weight, age);
                break;
            case 3:
                animal = new Hoofed(name, weight, age);
                break;
            case 4:
                animal = new ColdBlooded(name, weight, age);
                break;
        }
        
        if (animal != null) {
            animals.add(animal);
            System.out.println("Животное '" + name + "' успешно добавлено!");
            appLogger.logInfo("Added animal: " + animal);
        }
    }

    /**
     * Удаляет животное по имени.
     */
    private void removeAnimal() {
        if (animals.isEmpty()) {
            System.out.println("Нет животных для удаления.");
            return;
        }
        
        System.out.println("\n=== УДАЛЕНИЕ ЖИВОТНОГО ===");
        System.out.print("Введите имя животного для удаления: ");
        String name = scanner.nextLine();
        
        if (name.trim().isEmpty()) {
            System.out.println("Имя не может быть пустым.");
            return;
        }
        
        boolean removed = animals.removeIf(animal -> 
            animal.getName().equalsIgnoreCase(name.trim()));
        
        if (removed) {
            System.out.println("Животное '" + name + "' успешно удалено!");
            appLogger.logInfo("Removed animal: " + name);
        } else {
            System.out.println("Животное с именем '" + name + "' не найдено.");
            appLogger.logInfo("Attempt to remove non-existent animal: " + name);
        }
    }

    /**
     * Запускает эксперименты с коллекциями.
     */
    private void runCollectionExperiments() {
        System.out.println("\n=== ЗАПУСК ЭКСПЕРИМЕНТОВ С КОЛЛЕКЦИЯМИ ===");
        
        experLogger.CollectionExperiment exp = new experLogger.CollectionExperiment();
        int[] sizes = {10, 100, 1000};
        
        for (int s : sizes) {
            System.out.println("Запуск экспериментов для размера: " + s);
            exp.runArrayListExperiment(s);
            exp.runLinkedListExperiment(s);
        }
        
        System.out.println("Эксперименты завершены. Результаты сохранены в лог-файлы.");
        appLogger.logInfo("Collection experiments completed");
    }

    /**
     * Запускает режим отладки.
     */
    private void runDebug() {
        if (!settings.isDebugMode()) {
            System.out.println("Режим отладки отключен в настройках.");
            return;
        }
        
        System.out.println("\n=== РЕЖИМ ОТЛАДКИ ===");
        appLogger.logDebug("Debug mode activated by user");
        
        // Отладочная информация о текущем состоянии
        appLogger.logDebug("Current application state:");
        appLogger.logDebug("- Animals count: " + animals.size());
        appLogger.logDebug("- Enclosures count: " + enclosures.size());
        appLogger.logDebug("- User: " + settings.getUsername());
        appLogger.logDebug("- User group: " + settings.getUserGroup());
        
        // Детальная информация о вольерах
        for (int i = 0; i < enclosures.size(); i++) {
            Enclosure enclosure = enclosures.get(i);
            appLogger.logDebug(String.format("Enclosure %d: %s, Type: %s, Capacity: %d, Current: %d", 
                i + 1, enclosure.getName(), enclosure.getType(), 
                enclosure.getCapacity(), enclosure.getAnimals().size()));
        }
        
        // Детальная информация о животных
        for (int i = 0; i < Math.min(animals.size(), 10); i++) { // Ограничим вывод первыми 10 животными
            Animal animal = animals.get(i);
            appLogger.logDebug(String.format("Animal %d: %s, Type: %s, Age: %d, Weight: %.2f", 
                i + 1, animal.getName(), animal.getClass().getSimpleName(), 
                animal.getAge(), animal.getWeight()));
        }
        
        if (animals.size() > 10) {
            appLogger.logDebug("... and " + (animals.size() - 10) + " more animals");
        }
        
        System.out.println("Отладочная информация записана в лог.");
    }

    /**
     * Запускает автоматические тесты.
     */
    private void runAutoTests() {
        boolean allPassed = autoTest.runAllTestsWithConsoleOutput();
        
        if (allPassed) {
            appLogger.logInfo("All auto tests passed: " + 
                autoTest.getPassedTests() + "/" + autoTest.getTotalTests());
        } else {
            appLogger.logInfo("Auto tests completed with failures: " + 
                autoTest.getPassedTests() + "/" + autoTest.getTotalTests());
        }
    }

    /**
     * Запускает расширенные автотесты (только для root).
     */
    private void runExtendedTests() {
        if (!settings.isRoot()) {
            System.out.println("Расширенные тесты доступны только для пользователей с правами root.");
            return;
        }
        
        System.out.println("\n=== ЗАПУСК РАСШИРЕННЫХ АВТОТЕСТОВ ===");
        boolean allPassed = autoTest.runExtendedTests();
        
        System.out.println("\n=== РЕЗУЛЬТАТЫ РАСШИРЕННОГО ТЕСТИРОВАНИЯ ===");
        System.out.println("Пройдено тестов: " + autoTest.getPassedTests() + " из " + autoTest.getTotalTests());
        System.out.printf("Процент успеха: %.1f%%\n", autoTest.getSuccessRate());
        
        if (allPassed) {
            System.out.println("✓ ВСЕ РАСШИРЕННЫЕ ТЕСТЫ ПРОЙДЕНЫ УСПЕШНО");
            appLogger.logInfo("All extended auto tests passed");
        } else {
            System.out.println("✗ НЕКОТОРЫЕ РАСШИРЕННЫЕ ТЕСТЫ НЕ ПРОЙДЕНЫ");
            appLogger.logInfo("Extended auto tests completed with failures");
        }
    }
}