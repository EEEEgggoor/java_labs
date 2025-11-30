package view.gui;

import model.Animal;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Табличная модель для отображения списка животных в JTable.
 */
public class AnimalTableModel extends AbstractTableModel {

    private List<Animal> animals;
    private final String[] columns = {"№", "Имя", "Тип", "Возраст", "Вес"};

    /**
     * Создаёт модель таблицы животных.
     *
     * @param animals список животных
     */
    public AnimalTableModel(List<Animal> animals) {
        this.animals = animals;
    }

    /**
     * Обновляет список животных.
     */
    public void setAnimals(List<Animal> animals) {
        this.animals = animals;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return animals.size();
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
        Animal a = animals.get(row);
        return switch (col) {
            case 0 -> row + 1;
            case 1 -> a.getName();
            case 2 -> a.getClass().getSimpleName();
            case 3 -> a.getAge();
            case 4 -> a.getWeight();
            default -> "";
        };
    }
}
