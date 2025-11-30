package view.gui;

import config.Settings;
import controller.ZooController;
import db.DatabaseManager;
import enclosure.Enclosure;
import experLogger.CollectionExperiment;
import experLogger.OperationLogger;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import model.Animal;
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
