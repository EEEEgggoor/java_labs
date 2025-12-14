package view.gui;

import config.Settings;
import controller.ZooController;
import db.DatabaseManager;
import enclosure.Enclosure;
import experLogger.CollectionExperiment;
import experLogger.OperationLogger;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.*;
import model.Animal;
import network.NetworkClient;
import test.AutoTest;
import test.MultithreadRandomFill;
import view.GraphFromLogs;

/**
 * Главное графическое окно приложения Zoo Manager.
 * Управляет таблицами, диалогами и вызывает основные сценарии работы.
 */
public class MenuGui extends JFrame {

    private final Settings settings;
    private final OperationLogger appLogger;
    private List<Animal> animals;
    private List<Enclosure> enclosures;

    private final AnimalTableModel animalTableModel;
    private final EnclosureTableModel enclosureTableModel;
    private final JTable animalsTable;
    private final JTable enclosuresTable;

    private final AutoTest autoTest;

    // Network client — подключается сразу в конструкторе
    private final NetworkClient netClient;

    /**
     * Создаёт главное окно GUI.
     *
     * @param settings настройки пользователя
     * @param appLogger логгер приложения
     * @param animals список животных
     * @param enclosures список вольеров
     */
    public MenuGui(Settings settings, OperationLogger appLogger,
                   List<Animal> animals, List<Enclosure> enclosures) {

        super("Zoo Manager — GUI");

        this.settings = settings;
        this.appLogger = appLogger;
        this.animals = animals;
        this.enclosures = enclosures;

        this.autoTest = new AutoTest(settings, appLogger);

        this.animalTableModel = new AnimalTableModel(animals);
        this.enclosureTableModel = new EnclosureTableModel(enclosures);

        this.animalsTable = new JTable(animalTableModel);
        this.enclosuresTable = new JTable(enclosureTableModel);

        // Инициализация сетевого клиента (подключение при старте)
        // Хост и порт можно вынести в Settings при желании
        this.netClient = new NetworkClient("127.0.0.1", 9001);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(950, 600));

        initUI();
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Создаёт интерфейс главного окна:
     * — кнопки меню
     * — вкладки с таблицами
     * — обработчики событий
     */
    private void initUI() {
        JPanel leftPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnShow = new JButton("Показать данные");
        JButton btnDistribute = new JButton("Расселить животных");
        JButton btnLoad = new JButton("Загрузить из БД");
        JButton btnSave = new JButton("Сохранить в БД");
        JButton btnAddAnimal = new JButton("Добавить животное");
        JButton btnRemoveAnimal = new JButton("Удалить животное");
        JButton btnAddEnclosure = new JButton("Создать вольер");
        JButton btnExperiments = new JButton("Эксперименты");
        JButton btnGraph = new JButton("График логов");
        JButton btnMultithread = new JButton("Многопоток");
        JButton btnExit = new JButton("Выход");

        // ---- Новые сетевые кнопки ----
        JButton btnUploadAnimalsFile = new JButton("Загрузить животных (файл)");
        JButton btnUploadAnimalsMem = new JButton("Загрузить животных (память)");
        JButton btnDownloadAnimalsFile = new JButton("Выгрузить животных (в файл)");
        JButton btnDownloadAnimalsMem = new JButton("Выгрузить животных (в память)");

        JButton btnUploadEnclosuresFile = new JButton("Загрузить вольеры (файл)");
        JButton btnUploadEnclosuresMem = new JButton("Загрузить вольеры (память)");
        JButton btnDownloadEnclosuresFile = new JButton("Выгрузить вольеры (в файл)");
        JButton btnDownloadEnclosuresMem = new JButton("Выгрузить вольеры (в память)");

        leftPanel.add(btnShow);
        leftPanel.add(btnDistribute);
        leftPanel.add(btnLoad);
        leftPanel.add(btnSave);
        leftPanel.add(btnAddAnimal);
        leftPanel.add(btnRemoveAnimal);
        leftPanel.add(btnAddEnclosure);
        leftPanel.add(btnExperiments);
        leftPanel.add(btnGraph);
        leftPanel.add(btnMultithread);
        if (settings.isRoot()) {
            JButton btnDebug = new JButton("Отладка (root)");
            JButton btnAutoTests = new JButton("Автотесты (root)");
            JButton btnExtTests = new JButton("Расширенные тесты");
            leftPanel.add(btnDebug);
            leftPanel.add(btnAutoTests);
            leftPanel.add(btnExtTests);

            btnDebug.addActionListener(e -> runDebug());
            btnAutoTests.addActionListener(e -> runAutoTests());
            btnExtTests.addActionListener(e -> runExtendedTests());
        }

        // Добавляем новые сетевые кнопки в панель (после стандартных кнопок)
        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        leftPanel.add(btnUploadAnimalsFile);
        leftPanel.add(btnUploadAnimalsMem);
        leftPanel.add(btnDownloadAnimalsFile);
        leftPanel.add(btnDownloadAnimalsMem);

        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        leftPanel.add(btnUploadEnclosuresFile);
        leftPanel.add(btnUploadEnclosuresMem);
        leftPanel.add(btnDownloadEnclosuresFile);
        leftPanel.add(btnDownloadEnclosuresMem);

        leftPanel.add(btnExit);

        // Таблицы во вкладках
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Животные", new JScrollPane(animalsTable));
        tabs.addTab("Вольеры", new JScrollPane(enclosuresTable));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(leftPanel, BorderLayout.WEST);
        getContentPane().add(tabs, BorderLayout.CENTER);

        // Обработчики
        btnShow.addActionListener(e -> {
            refreshTables();
            JOptionPane.showMessageDialog(this,
                    "Животных: " + animals.size() +
                    "\n Вольеров: " + enclosures.size());
        });

        btnDistribute.addActionListener(e -> {
            distributeAnimals();
            refreshTables();
        });

        btnLoad.addActionListener(e -> {
            loadFromDatabase();
            refreshTables();
        });

        btnSave.addActionListener(e -> saveToDatabase());

        btnAddAnimal.addActionListener(e -> {
            new AddAnimalDialog(this, animals);
            refreshTables();
        });

        btnRemoveAnimal.addActionListener(e -> {
            int row = animalsTable.getSelectedRow();
            if (row >= 0) {
                animals.remove(row);
                refreshTables();
            }
        });

        btnAddEnclosure.addActionListener(e -> {
            new AddEnclosureDialog(this, enclosures);
            refreshTables();
        });

        btnExperiments.addActionListener(e -> runCollectionExperiments());
        btnGraph.addActionListener(e -> GraphFromLogs.showWindowAutoScan());

        btnMultithread.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    MultithreadRandomFill demoWindow = new MultithreadRandomFill();
                    demoWindow.setVisible(true);
                } catch (Throwable ex) {
                    JOptionPane.showMessageDialog(this,
                            "Не удалось открыть окно многопоточного демо:\n" + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            });
        });

        btnExit.addActionListener(e -> dispose());

        // ===== Обработчики сетевых кнопок =====

        // 1) Upload animals from DB files (сначала сохраняем в БД, чтобы файл был актуален)
        btnUploadAnimalsFile.addActionListener(e -> {
            new Thread(() -> {
                try {
                    // убедимся, что локальные .db актуальны
                    saveToDatabase();
                    Path animalsDb = Path.of("animals.db");
                    Path enclosuresDb = Path.of("enclosures.db");
                    if (!Files.exists(animalsDb) || !Files.exists(enclosuresDb)) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                                "Файлы БД не найдены. Сначала сохраните в БД.", "Ошибка", JOptionPane.ERROR_MESSAGE));
                        return;
                    }
                    netClient.uploadFile(animalsDb, "ANIMALS");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Файл animals.db успешно загружен на сервер."));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Ошибка загрузки файла: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        });

        // 2) Upload animals from memory (список объектов)
        btnUploadAnimalsMem.addActionListener(e -> {
            new Thread(() -> {
                try {
                    // отправляем текущий список animals (List<Animal>)
                    netClient.uploadObject(animals, "ANIMALS");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Список животных отправлен на сервер (из памяти)."));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Ошибка загрузки из памяти: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        });

        // 3) Download animals -> file (перезаписывает локальный animals.db)
        btnDownloadAnimalsFile.addActionListener(e -> {
            new Thread(() -> {
                try {
                    Path animalsDb = Path.of("animals.db");
                    netClient.downloadFile(animalsDb, "ANIMALS");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Файл animals.db загружен с сервера."));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Ошибка выгрузки в файл: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        });

        // 4) Download animals -> memory (заменяем коллекцию в приложении)
        btnDownloadAnimalsMem.addActionListener(e -> {
            new Thread(() -> {
                try {
                    Object obj = netClient.downloadObject("ANIMALS");
                    if (obj instanceof List) {
                        //noinspection unchecked
                        this.animals = (List<Animal>) obj;
                        animalTableModel.setAnimals(this.animals);
                        SwingUtilities.invokeLater(() -> {
                            refreshTables();
                            JOptionPane.showMessageDialog(this, "Список животных загружен в память из сервера.");
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                                "Полученные данные имеют неверный формат.", "Ошибка", JOptionPane.ERROR_MESSAGE));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Ошибка выгрузки в память: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        });

        // ===== Аналогично для вольеров =====

        btnUploadEnclosuresFile.addActionListener(e -> {
            new Thread(() -> {
                try {
                    saveToDatabase();
                    Path enclosuresDb = Path.of("enclosures.db");
                    if (!Files.exists(enclosuresDb)) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                                "Файл enclosures.db не найден. Сначала сохраните в БД.", "Ошибка", JOptionPane.ERROR_MESSAGE));
                        return;
                    }
                    netClient.uploadFile(enclosuresDb, "ENCLOSURES");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Файл enclosures.db успешно загружен на сервер."));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Ошибка загрузки файла: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        });

        btnUploadEnclosuresMem.addActionListener(e -> {
            new Thread(() -> {
                try {
                    netClient.uploadObject(enclosures, "ENCLOSURES");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Список вольеров отправлен на сервер (из памяти)."));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Ошибка загрузки из памяти: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        });

        btnDownloadEnclosuresFile.addActionListener(e -> {
            new Thread(() -> {
                try {
                    Path enclosuresDb = Path.of("enclosures.db");
                    netClient.downloadFile(enclosuresDb, "ENCLOSURES");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Файл enclosures.db загружен с сервера."));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Ошибка выгрузки в файл: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        });

        btnDownloadEnclosuresMem.addActionListener(e -> {
            new Thread(() -> {
                try {
                    Object obj = netClient.downloadObject("ENCLOSURES");
                    if (obj instanceof List) {
                        //noinspection unchecked
                        this.enclosures = (List<Enclosure>) obj;
                        enclosureTableModel.setEnclosures(this.enclosures);
                        SwingUtilities.invokeLater(() -> {
                            refreshTables();
                            JOptionPane.showMessageDialog(this, "Список вольеров загружен в память из сервера.");
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                                "Полученные данные имеют неверный формат.", "Ошибка", JOptionPane.ERROR_MESSAGE));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Ошибка выгрузки в память: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        });
    }

    /**
     * Запускает распределение животных по вольерам.
     */
    private void distributeAnimals() {
        ZooController c = new ZooController(enclosures);
        List<Animal> unassigned = c.distributeAnimals(animals);

        JOptionPane.showMessageDialog(this,
                "Расселено: " + (animals.size() - unassigned.size()) +
                "\nНе помещено: " + unassigned.size());
    }

    /**
     * Загружает данные из БД.
     */
    private void loadFromDatabase() {
        animals = DatabaseManager.loadAnimals();
        enclosures = DatabaseManager.loadEnclosures();

        animalTableModel.setAnimals(animals);
        enclosureTableModel.setEnclosures(enclosures);
    }

    /**
     * Сохраняет данные в БД.
     */
    private void saveToDatabase() {
        DatabaseManager.saveAnimals(animals);
        DatabaseManager.saveEnclosures(enclosures);
    }

    /**
     * Выполняет эксперименты с коллекциями.
     */
    private void runCollectionExperiments() {
        CollectionExperiment exp = new CollectionExperiment();
        for (int n : new int[]{10, 100, 1000}) {
            exp.runArrayListExperiment(n);
            exp.runLinkedListExperiment(n);
        }
        JOptionPane.showMessageDialog(this, "Эксперименты выполнены, данные в логах.");
    }

    /**
     * Записывает диагностическую информацию в лог.
     */
    private void runDebug() {
        appLogger.logDebug("Debug info: animals=" + animals.size() +
                ", enclosures=" + enclosures.size());
    }

    /**
     * Запускает автотесты.
     */
    private void runAutoTests() {
        autoTest.runAllTests();
        JOptionPane.showMessageDialog(this,
                "Автотесты: " + autoTest.getPassedTests() + "/" + autoTest.getTotalTests());
    }

    /**
     * Запускает расширенные автотесты.
     */
    private void runExtendedTests() {
        autoTest.runExtendedTests();
        JOptionPane.showMessageDialog(this,
                "Расширенные тесты: " + autoTest.getPassedTests() + "/" + autoTest.getTotalTests());
    }

    /**
     * Обновляет таблицы после изменения данных.
     */
    private void refreshTables() {
        animalTableModel.fireTableDataChanged();
        enclosureTableModel.fireTableDataChanged();
    }

    /**
     * Запускает GUI-приложение после аутентификации.
     */
    public static void launch(Settings settings, OperationLogger logger,
                              List<Animal> animals, List<Enclosure> enclosures) {
        SwingUtilities.invokeLater(() -> {
            MenuGui gui = new MenuGui(settings, logger, animals, enclosures);

            if (gui.authenticate()) gui.setVisible(true);
        });
    }

    /**
     * Окно аутентификации.
     *
     * @return true — если пользователь ввёл правильные логин и пароль
     */
    private boolean authenticate() {
        JPanel panel = new JPanel(new GridLayout(0, 1));

        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();

        panel.add(new JLabel("Логин:"));
        panel.add(user);
        panel.add(new JLabel("Пароль:"));
        panel.add(pass);

        int res = JOptionPane.showConfirmDialog(
                this, panel, "Вход", JOptionPane.OK_CANCEL_OPTION);

        if (res != JOptionPane.OK_OPTION) return false;

        boolean ok = user.getText().equals(settings.getUsername()) &&
                     new String(pass.getPassword()).equals(settings.getPassword());

        if (!ok)
            JOptionPane.showMessageDialog(this, "Неверные данные", "Ошибка", JOptionPane.ERROR_MESSAGE);

        return ok;
    }
}
