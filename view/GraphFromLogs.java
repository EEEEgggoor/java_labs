package view;


import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.event.ActionListener;


/**
 * JavaSwingLogGrapher (GraphFromLogs)
 *
 * Программа для чтения логов производительности коллекций и построения
 * графиков зависимостей среднего и суммарного времени от числа элементов.
 */
public class GraphFromLogs extends JFrame {
    /**
     * Выпадающий список для выбора первой коллекции (левая кривa).
     * Пример значения: "ArrayList".
     */
    private final JComboBox<String> comboFirst = new JComboBox<>();

    /**
     * Выпадающий список для выбора второй коллекции (правая кривa).
     * Пример значения: "LinkedList".
     */
    private final JComboBox<String> comboSecond = new JComboBox<>();

    /**
     * Выпадающий список выбора операции, для которой отображаются метрики.
     * Поддерживаемые значения: "add" и "set".
     */
    private final JComboBox<String> opCombo = new JComboBox<>(new String[]{"add", "set"});

    /**
     * Чекбокс — показывать ли среднее время (average) на графике.
     */
    private final JCheckBox cbAverage = new JCheckBox("Average time");

    /**
     * Чекбокс — показывать ли суммарное время (total) на графике.
     */
    private final JCheckBox cbTotal = new JCheckBox("Total time");

    /**
     * Панель, ответственная за отрисовку графика.
     */
    private final ChartPanel chartPanel = new ChartPanel();

    /**
     * Основная структура данных с распарсенными метриками.
     * Формат: Map<collectionName, TreeMap<N, Map<metricKey, Long>>>.
     * - collectionName: "ArrayList", "LinkedList" и т.п.
     * - N: количество элементов (10, 100, 1000...)
     * - metricKey: "addTotalTime", "addAverageTime", "setTotalTime" и т.д.
     */
    private final Map<String, TreeMap<Integer, Map<String, Long>>> data = new HashMap<>();

    /**
     * Конструктор. Создаёт GUI, компоненты управления и привязывает слушатели.
     * Начальные элементы управления (до загрузки данных) отключены.
     */
    public GraphFromLogs() {
        super("Log Grapher — Java Swing");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadBtn = new JButton("Load log");
        loadBtn.addActionListener(e -> loadLogFile());
        top.add(loadBtn);

        top.add(new JLabel("Collection 1:"));
        top.add(comboFirst);
        top.add(new JLabel("Collection 2:"));
        top.add(comboSecond);
        top.add(new JLabel("Operation:"));
        top.add(opCombo);

        cbAverage.setSelected(true);
        cbTotal.setSelected(true);
        ActionListener redrawListener = e -> redraw();
        comboFirst.addActionListener(redrawListener);
        comboSecond.addActionListener(redrawListener);
        opCombo.addActionListener(redrawListener);
        cbAverage.addActionListener(redrawListener);
        cbTotal.addActionListener(redrawListener);

        JButton exportBtn = new JButton("Export PNG");
        exportBtn.addActionListener(e -> chartPanel.exportPNG());
        top.add(cbAverage);
        top.add(cbTotal);
        top.add(exportBtn);

        add(top, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);

        // initial state
        comboFirst.setEnabled(false);
        comboSecond.setEnabled(false);
        cbAverage.setEnabled(false);
        cbTotal.setEnabled(false);
        opCombo.setEnabled(false);
        exportBtn.setEnabled(false);
    }

    /**
     * Открывает диалог для выбора файла лога и парсит выбранный файл.
     * После парсинга обновляет списки коллекций и перерисовывает график.
     */
    private void loadLogFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt", "log"));
        int r = chooser.showOpenDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        parseLogFile(f);
        List<String> cols = new ArrayList<>(data.keySet());
        Collections.sort(cols);
        comboFirst.removeAllItems();
        comboSecond.removeAllItems();
        for (String s : cols) {
            comboFirst.addItem(s);
            comboSecond.addItem(s);
        }
        if (!cols.isEmpty()) {
            comboFirst.setSelectedIndex(0);
            comboSecond.setSelectedIndex(Math.min(1, cols.size()-1));
        }
        comboFirst.setEnabled(true);
        comboSecond.setEnabled(true);
        cbAverage.setEnabled(true);
        cbTotal.setEnabled(true);
        opCombo.setEnabled(true);
        chartPanel.enableExport(true);
        redraw();
    }

    /**
     * Парсит лог-файл, содержащий один или несколько блоков результатов.
     * Формат блока ожидается примерно такой:
     * <pre>
     * CollectionName
     * add, ID = 1, 469
     * ...
     * addTotalCount = 100
     * addTotalTime = 80576
     * addAverageTime = 805
     * </pre>
     * Метод извлекает пары key = value и определяет N по ключам типа
     * addTotalCount / setTotalCount.
     *
     * @param file файл лога
     */
    private void parseLogFile(File file) {
        data.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String currentCollection = null;
            Map<String, Long> currentMetrics = null;
            Integer currentN = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    if (currentCollection != null && currentN != null && currentMetrics != null) {
                        storeRun(currentCollection, currentN, currentMetrics);
                    }
                    currentCollection = null;
                    currentMetrics = null;
                    currentN = null;
                    continue;
                }

                if (line.startsWith("Start program") || line.startsWith("Finish program") || line.startsWith("TotalOperationsTimeNanos")) {
                    continue;
                }

                if (!line.contains("=") && !line.contains(",") && !line.contains(" ") && !line.endsWith("time") ) {
                    if (currentCollection != null && currentN != null && currentMetrics != null) {
                        storeRun(currentCollection, currentN, currentMetrics);
                    }
                    currentCollection = line;
                    currentMetrics = new HashMap<>();
                    currentN = null;
                    continue;
                }

                if (line.contains("=")) {
                    String[] parts = line.split("=");
                    if (parts.length >= 2) {
                        String key = parts[0].trim();
                        String valStr = parts[1].trim();
                        int sp = valStr.indexOf(' ');
                        if (sp > 0) valStr = valStr.substring(0, sp);
                        try {
                            long val = Long.parseLong(valStr);
                            if (currentMetrics == null) currentMetrics = new HashMap<>();
                            currentMetrics.put(key, val);
                            if ((key.equalsIgnoreCase("addTotalCount") || key.equalsIgnoreCase("removeTotalCount") || key.equalsIgnoreCase("setTotalCount")) && val > 0) {
                                currentN = (int) val;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    continue;
                }
            }
            if (currentCollection != null && currentN != null && currentMetrics != null) {
                storeRun(currentCollection, currentN, currentMetrics);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage());
        }
    }

    /**
     * Сохраняет блок метрик для указанной коллекции и размера N в основную структуру данных.
     * Метод нормализует ключи метрик (убирает пробелы) перед сохранением.
     *
     * @param collection имя коллекции (ключ верхнего уровня)
     * @param n         размер выборки (количество элементов)
     * @param metrics   карта метрик, полученная из лога
     */
    private void storeRun(String collection, int n, Map<String, Long> metrics) {
        TreeMap<Integer, Map<String, Long>> map = data.computeIfAbsent(collection, k -> new TreeMap<>());
        Map<String, Long> normalized = new HashMap<>();
        for (Map.Entry<String, Long> e : metrics.entrySet()) {
            normalized.put(e.getKey().replaceAll("\s+", ""), e.getValue());
        }
        map.put(n, normalized);
    }

    /**
     * Обновляет отображение графика, подтягивая текущие значения из UI
     * (выбранные коллекции, операция, чекбоксы) и передавая их в ChartPanel.
     */
    private void redraw() {
        String c1 = (String) comboFirst.getSelectedItem();
        String c2 = (String) comboSecond.getSelectedItem();
        String op = (String) opCombo.getSelectedItem();
        boolean showAvg = cbAverage.isSelected();
        boolean showTotal = cbTotal.isSelected();
        if (c1 == null || c2 == null) return;
        TreeMap<Integer, Map<String, Long>> d1 = data.get(c1);
        TreeMap<Integer, Map<String, Long>> d2 = data.get(c2);
        chartPanel.setData(c1, d1, c2, d2, op, showAvg, showTotal);
    }

    /**
     * Возвращает компонент панели графика — полезно, если нужно встроить его в
     * своё окно вместо создания отдельного экземпляра GraphFromLogs.
     *
     * @return компонент с графиком
     */
    public Component getChartComponent() { return chartPanel; }

    /**
     * Заменяет внутреннюю структуру данных внешней и обновляет UI.
     *
     * @param externalData структура Map<collection, TreeMap<N, Map<metricKey,Long>>>
     */
    public void setParsedData(Map<String, TreeMap<Integer, Map<String, Long>>> externalData) {
        this.data.clear();
        this.data.putAll(externalData);

        java.util.List<String> cols = new ArrayList<>(data.keySet());
        Collections.sort(cols);
        comboFirst.removeAllItems();
        comboSecond.removeAllItems();
        for (String s : cols) {
            comboFirst.addItem(s);
            comboSecond.addItem(s);
        }
        if (!cols.isEmpty()) {
            comboFirst.setSelectedIndex(0);
            comboSecond.setSelectedIndex(Math.min(1, cols.size()-1));
        }
        comboFirst.setEnabled(true);
        comboSecond.setEnabled(true);
        cbAverage.setEnabled(true);
        cbTotal.setEnabled(true);
        opCombo.setEnabled(true);
        chartPanel.enableExport(true);
        redraw();
    }



    /**
     * Запускает окно и сразу передаёт разобранные данные (если переданы).
     * Выполняется в EDT.
     *
     * @param externalData      внешняя структура данных (может быть null)
     * @param selectCollection1 имя коллекции, которую выбрать в первом комбобоксе (может быть null)
     * @param selectCollection2 имя коллекции, которую выбрать во втором комбобоксе (может быть null)
     * @param op                начальная операция ("add" или "set")
     * @param showAvg           начальное состояние чекбокса average
     * @param showTotal         начальное состояние чекбокса total
     */
    public static void showWindowWithData(Map<String, TreeMap<Integer, Map<String, Long>>> externalData,
                                          String selectCollection1, String selectCollection2,
                                          String op, boolean showAvg, boolean showTotal) {
        SwingUtilities.invokeLater(() -> {
            GraphFromLogs w = new GraphFromLogs();
            if (externalData != null) {
                w.setParsedData(externalData);
                if (selectCollection1 != null) w.comboFirst.setSelectedItem(selectCollection1);
                if (selectCollection2 != null) w.comboSecond.setSelectedItem(selectCollection2);
                if (op != null) w.opCombo.setSelectedItem(op);
                w.cbAverage.setSelected(showAvg);
                w.cbTotal.setSelected(showTotal);
                w.redraw();
            }
            w.setVisible(true);
        });
    }

    /**
     * Открывает окно и автоматически сканирует текущую директорию (".").
     */
    public static void showWindowAutoScan() {
        showWindowAutoScan(".");
    }

    /**
     * Сканирует указанную директорию и извлекает метрики из файлов, имена которых
     * соответствуют шаблону ArrayList_{N}.log или LinkedList_{N}.log.
     * После сканирования открывает окно с найденными данными.
     *
     * @param dirPath путь к директории для сканирования
     */
    public static void showWindowAutoScan(String dirPath) {
        Map<String, TreeMap<Integer, Map<String, Long>>> parsed = autoScanLogs(dirPath);
        showWindowWithData(parsed, null, null, "add", true, true);
    }

    /**
     * Реализация автосканирования: перебирает файлы в директории и парсит подходящие.
     * Возвращает структуру Map<collection, TreeMap<N, metrics>>.
     *
     * @param dirPath путь к директории
     * @return собранные метрики
     */
    private static Map<String, TreeMap<Integer, Map<String, Long>>> autoScanLogs(String dirPath) {
        Map<String, TreeMap<Integer, Map<String, Long>>> result = new HashMap<>();
        File dir = new File(dirPath);
        if (!dir.isDirectory()) return result;
        Pattern p = Pattern.compile("^(ArrayList|LinkedList)_(\\d+)\\.log$");
        File[] files = dir.listFiles();
        if (files == null) return result;
        for (File f : files) {
            Matcher m = p.matcher(f.getName());
            if (!m.matches()) continue;
            String coll = m.group(1);
            int n;
            try { n = Integer.parseInt(m.group(2)); } catch (NumberFormatException ex) { continue; }
            Map<String, Long> metrics = parseMetricsFromFile(f);
            if (metrics.isEmpty()) continue;
            TreeMap<Integer, Map<String, Long>> tm = result.computeIfAbsent(coll, k -> new TreeMap<>());
            tm.put(n, metrics);
        }
        return result;
    }

    /**
     * Парсит файл и возвращает карту метрик (key -> value) найденных в файле.
     * Метод ищет строки с символом '=' и пытается считать число в правой части.
     *
     * @param f файл лога
     * @return карта метрик
     */
    private static Map<String, Long> parseMetricsFromFile(File f) {
        Map<String, Long> metrics = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.contains("=")) {
                    String[] parts = line.split("=");
                    if (parts.length >= 2) {
                        String key = parts[0].trim().replaceAll("\s+", "");
                        String valStr = parts[1].trim().split(" ")[0];
                        try {
                            long v = Long.parseLong(valStr);
                            metrics.put(key, v);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        } catch (IOException ignored) {}
        return metrics;
    }

    /**
     * Стандартная точка входа для запуска в режиме разработки/отладки.
     * Создаёт окно и показывает его (без автосканирования).
     */
    public static void main(String[] args) {
        // оставлен для обратной совместимости
        SwingUtilities.invokeLater(() -> {
            GraphFromLogs w = new GraphFromLogs();
            w.setVisible(true);
        });
    }

    // --- ChartPanel ---
    /**
     * Вложенная панель, отвечающая за отрисовку сетки, линий и меток графика.
     * Эта панель не знает об источнике данных — ей передаются подмножества
     * общей структуры data через метод setData(...).
     */
    static class ChartPanel extends JPanel {
        /**
         * Имя первой коллекции (для легенды и заголовка).
         */
        private String name1, name2, op = "add";

        /**
         * Данные для первой и второй коллекции: TreeMap<N, metrics>.
         */
        private TreeMap<Integer, Map<String, Long>> data1, data2;

        /**
         * Флаги отображения серий.
         */
        private boolean showAvg = true, showTotal = true;

        /**
         * Разрешён ли экспорт PNG (включается после загрузки данных).
         */
        private boolean exportEnabled = false;

        /**
         * Конструктор панели, просто задаёт фон.
         */
        public ChartPanel() {
            setBackground(Color.WHITE);
        }

        /**
         * Включает/выключает возможность экспорта.
         *
         * @param v true — экспорт разрешён
         */
        public void enableExport(boolean v) { this.exportEnabled = v; }

        /**
         * Устанавливает данные и параметры отрисовки и вызывает repaint().
         *
         * @param name1    имя первой коллекции
         * @param d1       данные первой коллекции
         * @param name2    имя второй коллекции
         * @param d2       данные второй коллекции
         * @param op       операция ("add" или "set")
         * @param showAvg  показывать среднее
         * @param showTotal показывать суммарное
         */
        public void setData(String name1, TreeMap<Integer, Map<String, Long>> d1,
                            String name2, TreeMap<Integer, Map<String, Long>> d2,
                            String op, boolean showAvg, boolean showTotal) {
            this.name1 = name1; this.name2 = name2; this.data1 = d1; this.data2 = d2;
            this.op = op; this.showAvg = showAvg; this.showTotal = showTotal;
            repaint();
        }

        /**
         * Экспортирует текущее изображение панели в PNG-файл через диалог выбора.
         */
        public void exportPNG() {
            if (!exportEnabled) return;
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("chart.png"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File out = chooser.getSelectedFile();
            BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            paintComponent(g2);
            g2.dispose();
            try {
                javax.imageio.ImageIO.write(img, "png", out);
                JOptionPane.showMessageDialog(this, "Saved to " + out.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
            }
        }

        /**
         * Основной метод рисования: сетка, оси, подписи, линии и точки.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padLeft = 70, padRight = 20, padTop = 30, padBottom = 70;
            int gw = w - padLeft - padRight;
            int gh = h - padTop - padBottom;

            // draw title
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
            String title = "Operation: " + op + " — " + (name1 == null ? "(no data)" : name1) + " vs " + (name2 == null ? "(no data)" : name2);
            g2.drawString(title, padLeft, 20);

            // background for plot area
            g2.setColor(new Color(245, 245, 245));
            g2.fillRect(padLeft, padTop, gw, gh);
            g2.setColor(Color.BLACK);
            g2.drawRect(padLeft, padTop, gw, gh);

            // prepare X (sorted unique N from union of both sets)
            Set<Integer> xset = new TreeSet<>();
            if (data1 != null) xset.addAll(data1.keySet());
            if (data2 != null) xset.addAll(data2.keySet());
            if (xset.isEmpty()) {
                g2.drawString("No numeric data to plot. Load a log file with runs.", padLeft + 10, padTop + 20);
                g2.dispose();
                return;
            }
            List<Integer> xs = new ArrayList<>(xset);

            // determine Y range from selected metrics
            double ymin = Double.POSITIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
            for (int n : xs) {
                if (showAvg) {
                    Long v1 = getMetric(data1, n, op + "AverageTime");
                    Long v2 = getMetric(data2, n, op + "AverageTime");
                    if (v1 != null) { ymin = Math.min(ymin, v1); ymax = Math.max(ymax, v1); }
                    if (v2 != null) { ymin = Math.min(ymin, v2); ymax = Math.max(ymax, v2); }
                }
                if (showTotal) {
                    Long v1 = getMetric(data1, n, op + "TotalTime");
                    Long v2 = getMetric(data2, n, op + "TotalTime");
                    if (v1 != null) { ymin = Math.min(ymin, v1); ymax = Math.max(ymax, v1); }
                    if (v2 != null) { ymin = Math.min(ymin, v2); ymax = Math.max(ymax, v2); }
                }
            }
            if (ymin == Double.POSITIVE_INFINITY || ymax == Double.NEGATIVE_INFINITY) {
                g2.drawString("Selected operation/metric not present in data.", padLeft + 10, padTop + 20);
                g2.dispose();
                return;
            }
            double ypad = (ymax - ymin) * 0.1;
            if (ypad == 0) ypad = Math.max(1.0, ymax*0.1);
            ymin = Math.max(0, ymin - ypad);
            ymax = ymax + ypad;

            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
            int yTicks = 6;
            for (int i = 0; i <= yTicks; i++) {
                double frac = i / (double) yTicks;
                int y = padTop + gh - (int) (frac * gh);
                double val = ymin + (1 - frac) * (ymax - ymin);
                g2.setColor(new Color(220, 220, 220));
                g2.drawLine(padLeft, y, padLeft + gw, y);
                g2.setColor(Color.BLACK);
                String lbl = String.format("%.0f", val);
                g2.drawString(lbl, 8, y + 4);
            }

            int xCount = xs.size();
            for (int i = 0; i < xCount; i++) {
                int x = padLeft + (int) ((i / (double) (xCount - 1)) * gw);
                int y = padTop + gh + 15;
                g2.setColor(new Color(200,200,200));
                g2.drawLine(x, padTop, x, padTop + gh);
                g2.setColor(Color.BLACK);
                String lbl = String.valueOf(xs.get(i));
                FontMetrics fm = g2.getFontMetrics();
                int lw = fm.stringWidth(lbl);
                g2.drawString(lbl, Math.max(padLeft, x - lw/2), y + 12);
            }

            double xscale = (xCount == 1) ? 0 : (double) gw / (xCount - 1);
            java.util.List<Point> pts1Avg = new ArrayList<>(), pts1Tot = new ArrayList<>();
            java.util.List<Point> pts2Avg = new ArrayList<>(), pts2Tot = new ArrayList<>();

            for (int i = 0; i < xs.size(); i++) {
                int n = xs.get(i);
                int x = padLeft + (int) (i * xscale);
                Long aAvg = getMetric(data1, n, op + "AverageTime");
                Long aTot = getMetric(data1, n, op + "TotalTime");
                Long bAvg = getMetric(data2, n, op + "AverageTime");
                Long bTot = getMetric(data2, n, op + "TotalTime");
                if (aAvg != null) pts1Avg.add(new Point(x, valToY(aAvg, ymin, ymax, padTop, gh)));
                if (aTot != null) pts1Tot.add(new Point(x, valToY(aTot, ymin, ymax, padTop, gh)));
                if (bAvg != null) pts2Avg.add(new Point(x, valToY(bAvg, ymin, ymax, padTop, gh)));
                if (bTot != null) pts2Tot.add(new Point(x, valToY(bTot, ymin, ymax, padTop, gh)));
            }

            Stroke solid = new BasicStroke(2f);
            Stroke dashed = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8f,6f}, 0);

            int legendY = padTop + 10;
            int ly = legendY;
            int lx = padLeft + 10;

            if (showAvg) {
                g2.setStroke(solid);
                g2.setColor(Color.BLUE);
                drawPolyline(g2, pts1Avg);
                drawPoints(g2, pts1Avg, Color.BLUE);
                g2.drawString(name1 + " average", lx, ly);
                lx += 140;
                g2.setStroke(dashed);
                g2.setColor(Color.BLUE.darker());
                drawPolyline(g2, pts2Avg);
                drawPoints(g2, pts2Avg, Color.BLUE.darker());
                g2.drawString(name2 + " average", lx, ly);
                lx += 180;
            }
            if (showTotal) {
                ly += 16; lx = padLeft + 10;
                g2.setStroke(solid);
                g2.setColor(Color.RED);
                drawPolyline(g2, pts1Tot);
                drawPoints(g2, pts1Tot, Color.RED);
                g2.drawString(name1 + " total", lx, ly);
                lx += 140;
                g2.setStroke(dashed);
                g2.setColor(Color.RED.darker());
                drawPolyline(g2, pts2Tot);
                drawPoints(g2, pts2Tot, Color.RED.darker());
                g2.drawString(name2 + " total", lx, ly);
            }

            g2.dispose();
        }

        /**
         * Преобразует значение метрики в пиксельную координату Y с учётом
         * текущего диапазона ymin..ymax и полей (padTop и высоты gh).
         */
        private int valToY(long v, double ymin, double ymax, int padTop, int gh) {
            double frac = (v - ymin) / (ymax - ymin);
            if (Double.isNaN(frac)) frac = 0;
            int y = padTop + gh - (int) (frac * gh);
            return y;
        }

        /**
         * Пытается получить значение метрики из карты для заданного N и ключа.
         * Метод пробует несколько вариантов ключа для робастности.
         *
         * @param d   TreeMap с метриками
         * @param n   размер выборки
         * @param key искомый ключ (например "addAverageTime")
         * @return значение метрики или null
         */
        private Long getMetric(TreeMap<Integer, Map<String, Long>> d, int n, String key) {
            if (d == null) return null;
            Map<String, Long> m = d.get(n);
            if (m == null) return null;
            List<String> tries = Arrays.asList(key, key.toLowerCase(), key.replaceAll("time","Time"), key.replaceAll("Time","time"));
            for (String t : tries) {
                if (m.containsKey(t)) return m.get(t);
            }
            for (String k : m.keySet()) {
                String kk = k.toLowerCase();
                if (kk.contains(opKey(op)) && kk.contains(key.toLowerCase().replace(op.toLowerCase(), ""))) {
                    return m.get(k);
                }
            }
            return null;
        }

        /**
         * Преобразование имени операции к ключу поиска (в текущем коде — просто lower-case).
         */
        private String opKey(String op) { return op.toLowerCase(); }

        /**
         * Рисует полилинию между заданными точками.
         */
        private void drawPolyline(Graphics2D g2, java.util.List<Point> pts) {
            if (pts.size() < 2) return;
            for (int i = 0; i < pts.size()-1; i++) {
                Point a = pts.get(i), b = pts.get(i+1);
                g2.draw(new Line2D.Double(a.x, a.y, b.x, b.y));
            }
        }

        /**
         * Рисует маркеры точек (кружки) на указанных координатах.
         */
        private void drawPoints(Graphics2D g2, java.util.List<Point> pts, Color c) {
            g2.setColor(c);
            for (Point p : pts) {
                g2.fillOval(p.x-3, p.y-3, 6, 6);
            }
        }
    }
}