package view.gui;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Диалоговое окно для добавления нового животного.
 */
public class AddAnimalDialog extends JDialog {

    /**
     * Создаёт диалог добавления животного и добавляет его в список.
     *
     * @param parent родительское окно
     * @param animals список животных
     */
    public AddAnimalDialog(JFrame parent, List<Animal> animals) {
        super(parent, "Добавить животное", true);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        JComboBox<String> type = new JComboBox<>(new String[]{
                "Aquatic", "Feathered", "Hoofed", "ColdBlooded"
        });

        JTextField name = new JTextField();
        JTextField weight = new JTextField();
        JTextField age = new JTextField();

        panel.add(new JLabel("Тип:"));
        panel.add(type);
        panel.add(new JLabel("Имя:"));
        panel.add(name);
        panel.add(new JLabel("Вес:"));
        panel.add(weight);
        panel.add(new JLabel("Возраст:"));
        panel.add(age);

        JButton addBtn = new JButton("Добавить");
        addBtn.addActionListener(e -> {
            try {
                double w = Double.parseDouble(weight.getText());
                int a = Integer.parseInt(age.getText());

                Animal an = switch (type.getSelectedIndex()) {
                    case 0 -> new Aquatic(name.getText(), w, a);
                    case 1 -> new Feathered(name.getText(), w, a);
                    case 2 -> new Hoofed(name.getText(), w, a);
                    case 3 -> new ColdBlooded(name.getText(), w, a);
                    default -> null;
                };

                animals.add(an);
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка ввода!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(addBtn, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}
