package view.gui;

import enclosure.Enclosure;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Табличная модель для отображения списка вольеров.
 */
public class EnclosureTableModel extends AbstractTableModel {

    private List<Enclosure> enclosures;
    private final String[] columns = {"№", "Имя", "Тип", "Вместимость", "Текущих"};

    /**
     * Создаёт модель таблицы вольеров.
     */
    public EnclosureTableModel(List<Enclosure> enclosures) {
        this.enclosures = enclosures;
    }

    /**
     * Обновляет список вольеров.
     */
    public void setEnclosures(List<Enclosure> enclosures) {
        this.enclosures = enclosures;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return enclosures.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        Enclosure e = enclosures.get(row);
        return switch (col) {
            case 0 -> row + 1;
            case 1 -> e.getName();
            case 2 -> e.getType().toString();
            case 3 -> e.getCapacity();
            case 4 -> e.getAnimals().size();
            default -> "";
        };
    }
}
