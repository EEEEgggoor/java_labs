package test;

import controller.RandomDataGenerator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Animal;

/**
 * Потоки теперь генерируют и сразу добавляют одно животное в общий список (sharedList).
 * Локальные списки не используются — каждая генерация сразу обрабатывается и вставляется.
 */
public class MultithreadRandomFill extends JFrame {

    private enum CollisionStrategy { SKIP, OVERWRITE, MERGE }

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new String[]{"Ключ", "Класс", "Имя", "Возраст/Другое"}, 0);

    private final JTextArea logArea = new JTextArea(8, 40);

    // общий список — инициализируется при старте (ArrayList или LinkedList)
    private List<Animal> sharedList = null;

    // список активных производителей (Thread'ов)
    private final List<Thread> producerThreads = Collections.synchronizedList(new ArrayList<>());

    // таймер для обновления GUI (javax.swing.Timer, работает в EDT)
    private javax.swing.Timer refreshTimer;

    private volatile boolean running = false;

    public MultithreadRandomFill() {
        super("Многопоточная генерация — немедленная запись в общий список");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 620);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        SpinnerNumberModel producersModel = new SpinnerNumberModel(4, 1, 50, 1);
        SpinnerNumberModel intervalModel = new SpinnerNumberModel(400, 50, 5000, 50);

        JSpinner producersSpinner = new JSpinner(producersModel);
        JSpinner intervalSpinner = new JSpinner(intervalModel);

        JComboBox<CollisionStrategy> strategyBox = new JComboBox<>(CollisionStrategy.values());
        // тип локального списка (убран, не нужен для немедленной записи) — но оставим настройку для режима generateAnimals
        JComboBox<String> genModeBox = new JComboBox<>(new String[]{"generateAnimals(1)", "fillAnimals(target,1)"}); 
        // тип общего списка, который будет shared репозиторием
        JComboBox<String> sharedListTypeBox = new JComboBox<>(new String[]{"ArrayList (shared)", "LinkedList (shared)"});

        JCheckBox useGenerateLinked = new JCheckBox("generateAnimals использовать как LinkedList", false);

        JButton startBtn = new JButton("Старт");
        JButton stopBtn  = new JButton("Стоп");
        JButton clearBtn = new JButton("Очистить общий список");
        stopBtn.setEnabled(false);

        top.add(new JLabel("Потоки:")); top.add(producersSpinner);
        top.add(new JLabel("Интервал (мс):")); top.add(intervalSpinner);
        top.add(new JLabel("Стратегия:")); top.add(strategyBox);
        top.add(new JLabel("Режим генерации:")); top.add(genModeBox);
        top.add(new JLabel("Общий список:")); top.add(sharedListTypeBox);
        top.add(useGenerateLinked);
        top.add(startBtn);
        top.add(stopBtn);
        top.add(clearBtn);

        JTable table = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(table);

        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(950, 180));

        getContentPane().setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(logScroll, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> {
            if (running) return;
            running = true;

            tableModel.setRowCount(0);
            logArea.setText("");
            synchronized (producerThreads) { producerThreads.clear(); }

            // создаём общий список в зависимости от выбора
            String sharedSel = (String) sharedListTypeBox.getSelectedItem();
            if (sharedSel != null && sharedSel.startsWith("LinkedList")) {
                sharedList = new LinkedList<>();
            } else {
                sharedList = new ArrayList<>();
            }

            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);

            int producers = (Integer) producersSpinner.getValue();
            int interval = (Integer) intervalSpinner.getValue();
            CollisionStrategy strategy = (CollisionStrategy) strategyBox.getSelectedItem();
            String genMode = (String) genModeBox.getSelectedItem();
            boolean genLinked = useGenerateLinked.isSelected();

            log("СТАРТ: потоков=" + producers +
                    ", интервал=" + interval + "мс" +
                    ", стратегия=" + strategy +
                    ", режим=" + genMode +
                    ", общий список=" + sharedSel +
                    ", generateUsesLinked=" + genLinked);

            // Таймер для обновления таблицы (EDT)
            refreshTimer = new javax.swing.Timer(700, ev -> refreshTable());
            refreshTimer.setInitialDelay(0);
            refreshTimer.start();

            // стартуем производители как Thread — каждый создаёт 1 животное и сразу вставляет в sharedList
            for (int i = 0; i < producers; i++) {
                final int id = i;
                Thread t = new Thread(() ->
                        producerImmediateAddLoop("Поток-" + id, interval, strategy, genMode, genLinked));
                t.setName("Producer-" + i);
                producerThreads.add(t);
                t.start();
            }
        });

        stopBtn.addActionListener(e -> {
            if (!running) return;
            running = false;
            log("Запрошена остановка — прерываем потоки...");
            // прерываем все
            synchronized (producerThreads) {
                for (Thread t : producerThreads) t.interrupt();
            }
            // ждём завершения с таймаутом
            synchronized (producerThreads) {
                for (Thread t : producerThreads) {
                    try { t.join(1500); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                }
                producerThreads.clear();
            }
            if (refreshTimer != null) { refreshTimer.stop(); refreshTimer = null; }

            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            log("Остановка завершена.");
        });

        clearBtn.addActionListener(e -> {
            if (sharedList != null) {
                synchronized (sharedList) { sharedList.clear(); }
            }
            log("Общий список очищен.");
            refreshTable();
        });
    }

    /**
     * Каждый поток генерирует по одному животному и сразу пытается вставить его в sharedList
     * (без предварительного накопления в локальном списке).
     *
     * genMode:
     * - "generateAnimals(1)" — вызываем RandomDataGenerator.generateAnimals(1, genLinked) и берём элемент
     * - "fillAnimals(target,1)" — генерируем временный список размера 1 и берём элемент (не передаём sharedList напрямую,
     *   чтобы иметь возможность корректно применить стратегию)
     */
    private void producerImmediateAddLoop(String producerName, int intervalMs,
                                          CollisionStrategy strategy, String genMode, boolean genLinked) {
        Random rnd = new Random();

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Генерируем ровно одно животное (не накапливаем пачки)
                Animal a;
                if ("generateAnimals(1)".equals(genMode)) {
                    List<Animal> tmp = RandomDataGenerator.generateAnimals(1, genLinked);
                    a = tmp.isEmpty() ? null : tmp.get(0);
                } else {
                    // fillAnimals requires a target list; используем временный список из 1 элемента
                    List<Animal> temp = new ArrayList<>();
                    try {
                        RandomDataGenerator.fillAnimals(temp, 1);
                    } catch (Throwable ex) {
                        // fallback на generateAnimals если fillAnimals не сработал
                        List<Animal> tmp = RandomDataGenerator.generateAnimals(1, genLinked);
                        a = tmp.isEmpty() ? null : tmp.get(0);
                        // обработка ниже
                        if (a == null) { Thread.sleep(100); continue; }
                    }
                    a = temp.isEmpty() ? null : temp.get(0);
                }

                if (a == null) {
                    Thread.sleep(50);
                    continue;
                }

                String key = detectKey(a);
                if (key == null) key = UUID.randomUUID().toString();

                // Синхронизированная вставка — один поток в критической секции за раз
                synchronized (sharedList) {
                    int idx = findIndexByKey(sharedList, key);
                    switch (strategy) {
                        case SKIP:
                            if (idx == -1) {
                                sharedList.add(a);
                                log(producerName + " добавил (SKIP) [" + key + "] -> " + shortRepr(a));
                            } else {
                                log(producerName + " пропуск (SKIP) — ключ уже есть: " + key);
                            }
                            break;
                        case OVERWRITE:
                            if (idx == -1) sharedList.add(a);
                            else sharedList.set(idx, a);
                            log(producerName + " записал (OVERWRITE) [" + key + "] -> " + shortRepr(a));
                            break;
                        case MERGE:
                            if (idx == -1) {
                                sharedList.add(a);
                            } else {
                                Animal old = sharedList.get(idx);
                                Animal merged = mergeAnimals(old, a);
                                sharedList.set(idx, merged);
                            }
                            log(producerName + " обработал (MERGE) [" + key + "] -> " + shortRepr(a));
                            break;
                    }
                } // synchronized

                // небольшая задержка + рандом
                long sleep = intervalMs + rnd.nextInt(Math.max(10, intervalMs / 3));
                Thread.sleep(sleep);

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable t) {
                log(producerName + " ошибка: " + t);
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
        }
        log(producerName + " завершён.");
    }

    // возвращает индекс найденного элемента с ключом или -1
    private int findIndexByKey(List<Animal> list, String key) {
        for (int i = 0; i < list.size(); i++) {
            Animal a = list.get(i);
            String k = detectKey(a);
            if (k != null && k.equals(key)) return i;
        }
        return -1;
    }

    private String detectKey(Animal a) {
        try {
            Method m = findMethodIgnoreCase(a.getClass(), "getName");
            if (m != null && Modifier.isPublic(m.getModifiers())) {
                Object v = m.invoke(a);
                if (v != null) return a.getClass().getSimpleName() + ":" + v;
            }
        } catch (Throwable ignored) {}
        return a.getClass().getSimpleName() + ":" + a.toString();
    }

    /**
     * Попытка объединить два Animal: если можем — переписываем protected поле name через рефлексию,
     * иначе возвращаем новый (newA).
     */
    private Animal mergeAnimals(Animal oldA, Animal newA) {
        try {
            Method getOld = findMethodIgnoreCase(oldA.getClass(), "getName");
            Method getNew = findMethodIgnoreCase(newA.getClass(), "getName");

            String oldName = (getOld != null) ? String.valueOf(getOld.invoke(oldA)) : null;
            String newName = (getNew != null) ? String.valueOf(getNew.invoke(newA)) : null;
            String mergedName = (oldName == null ? "" : oldName) + "/" + (newName == null ? "" : newName);

            // ищем поле name в иерархии классов
            Field nameField = null;
            Class<?> cls = oldA.getClass();
            while (cls != null) {
                try { nameField = cls.getDeclaredField("name"); break; }
                catch (NoSuchFieldException ex) { cls = cls.getSuperclass(); }
            }
            if (nameField != null) {
                nameField.setAccessible(true);
                nameField.set(oldA, mergedName);
                return oldA;
            }
        } catch (Throwable ignored) {}

        return newA;
    }

    private Method findMethodIgnoreCase(Class<?> cls, String name, Class<?>... params) {
        for (Method m : cls.getMethods()) {
            if (m.getName().equalsIgnoreCase(name) && Arrays.equals(m.getParameterTypes(), params)) return m;
        }
        return null;
    }

    private String shortRepr(Object o) {
        if (o == null) return "null";
        String s = o.toString();
        return s.length() > 100 ? s.substring(0, 100) + "..." : s;
    }

    private void refreshTable() {
        // вызывается в EDT (Timer)
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            if (sharedList == null) return;
            synchronized (sharedList) {
                for (Animal a : sharedList) {
                    String key = detectKey(a);
                    String name = tryInvokeString(a, "getName");
                    String other = tryInvokeString(a, "getAge");
                    tableModel.addRow(new Object[]{key, a.getClass().getSimpleName(), name, other});
                }
            }
        });
    }

    private String tryInvokeString(Object obj, String method) {
        try {
            Method m = findMethodIgnoreCase(obj.getClass(), method);
            if (m != null) {
                Object v = m.invoke(obj);
                return v == null ? "" : String.valueOf(v);
            }
        } catch (Throwable ignored) {}
        return "";
    }

    private void log(String s) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + new Date() + "] " + s + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MultithreadRandomFill window = new MultithreadRandomFill();
            window.setVisible(true);
        });
    }
}
