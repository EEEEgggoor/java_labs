package controller;

import enclosure.Enclosure;
import java.util.ArrayList;
import java.util.List;
import model.Animal;

/**
 * Контроллер, реализующий логику расселения партии животных по доступным вольерам.
 */
public class ZooController {
    /** Список доступных вольеров (локальная копия). */
    private final List<Enclosure> enclosures;

    /**
     * Создаёт контроллер с набором вольеров.
     */
    public ZooController(List<Enclosure> enclosures) {
        this.enclosures = new ArrayList<>(enclosures);
    }

    /**
     * Попытаться расселить каждое животное из пришедшей партии.
     */
    public List<Animal> distributeAnimals(List<Animal> incomingBatch) {
        List<Animal> unassigned = new ArrayList<>();
        for (Animal a : incomingBatch) {
            boolean placed = a.move(enclosures);
            if (!placed) {
                unassigned.add(a);
            }
        }
        return unassigned;
    }
}
