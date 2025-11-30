package controller;

import enclosure.Enclosure;
import java.util.LinkedList;
import java.util.List;
import model.Animal;

/**
 * Контроллер, реализующий логику расселения партии животных по доступным вольерам.
 */
public class ZooController_Link {
    /** Список доступных вольеров (локальная копия). */
    private final List<Enclosure> enclosures_link;

    /**
     * Создаёт контроллер с набором вольеров.
     */
    public ZooController_Link(List<Enclosure> enclosures_link) {
        this.enclosures_link = new LinkedList<>(enclosures_link);
    }

    /**
     * Попытаться расселить каждое животное из пришедшей партии.
     */
    public List<Animal> distributeAnimals(List<Animal> incomingBatch) {
        List<Animal> unassigned = new LinkedList<>();
        for (Animal a : incomingBatch) {
            boolean placed = a.move(enclosures_link);
            if (!placed) {
                unassigned.add(a);
            }
        }
        return unassigned;
    }
}
