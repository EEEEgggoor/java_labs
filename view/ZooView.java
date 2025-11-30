package view;

import enclosure.Enclosure;
import java.util.List;
import model.Animal;

/**
 * Простейшее консольное представление (View) для демонстрации состояния вольеров
 */
public class ZooView {
    
    /**
     * Показать список вольеров и животных в каждом вольере в консоль.
     *
     * @param enclosures список вольеров для вывода
     */
    public void showEnclosures(List<Enclosure> enclosures) {
        System.out.println("\n=== СОДЕРЖИМОЕ ВОЛЬЕРОВ ===");
        
        if (enclosures.isEmpty()) {
            System.out.println("Вольеры отсутствуют.");
            return;
        }
        
        for (Enclosure e : enclosures) {
            System.out.println(e);
            List<Animal> animalsInEnclosure = e.getAnimals();
            if (animalsInEnclosure.isEmpty()) {
                System.out.println("  - Вольер пуст");
            } else {
                for (Animal a : animalsInEnclosure) {
                    System.out.println("  - " + a);
                }
            }
            System.out.println();
        }
    }

    /**
     * Показать детальную информацию о вольерах.
     */
    public void showEnclosuresDetailed(List<Enclosure> enclosures) {
        System.out.println("\n=== ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О ВОЛЬЕРАХ ===");
        
        if (enclosures.isEmpty()) {
            System.out.println("Вольеры отсутствуют.");
            return;
        }
        
        for (int i = 0; i < enclosures.size(); i++) {
            Enclosure e = enclosures.get(i);
            System.out.printf("%d. %s\n", i + 1, e);
            System.out.printf("   Тип: %s\n", e.getType());
            System.out.printf("   Вместимость: %d животных\n", e.getCapacity());
            System.out.printf("   Текущее количество: %d животных\n", e.getAnimals().size());
            System.out.printf("   Свободных мест: %d\n", e.getCapacity() - e.getAnimals().size());
            
            List<Animal> animals = e.getAnimals();
            if (animals.isEmpty()) {
                System.out.println("   Животные: нет");
            } else {
                System.out.println("   Животные:");
                for (Animal a : animals) {
                    System.out.printf("     - %s\n", a);
                }
            }
            System.out.println();
        }
    }

    /**
     * Показать животных, которые не были размещены.
     *
     * @param unassigned список неразмещённых животных
     */
    public void showUnassigned(List<Animal> unassigned) {
        System.out.println("=== НЕРАЗМЕЩЁННЫЕ ЖИВОТНЫЕ ===");
        if (unassigned.isEmpty()) {
            System.out.println("Все животные успешно размещены.");
        } else {
            System.out.println("Не удалось разместить " + unassigned.size() + " животных:");
            for (Animal a : unassigned) {
                System.out.println("  - " + a);
            }
        }
    }
}