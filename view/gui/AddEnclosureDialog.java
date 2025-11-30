package view.gui;

import enclosure.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Диалоговое окно для создания нового вольера.
 */
public class AddEnclosureDialog extends JDialog {

    /**
     * Создаёт диалог добавления нового вольера.
     *
     * @param parent родительское окно
     * @param enclosures список вольеров
     */
    public AddEnclosureDialog(JFrame parent, List<Enclosure> enclosures) {
        super(parent, "Создать вольер", true);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        JTextField name = new JTextField();
        JComboBox<String> type = new JComboBox<>(new String[]{
                "AQUARIUM", "NET_COVERED", "OPEN", "INFRARED"
        });
        JTextField capacity = new JTextField();

        panel.add(new JLabel("Имя:"));
        panel.add(name);
        panel.add(new JLabel("Тип:"));
        panel.add(type);
        panel.add(new JLabel("Вместимость:"));
        panel.add(capacity);

        JButton addBtn = new JButton("Создать");
        addBtn.addActionListener(e -> {
            try {
                int cap = Integer.parseInt(capacity.getText());
                EnclosureType et = EnclosureType.valueOf(type.getSelectedItem().toString());

                enclosures.add(new Enclosure(name.getText(), et, cap));
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
