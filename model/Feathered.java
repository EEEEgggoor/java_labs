package model;

import enclosure.Enclosure;
import java.util.List;

/**
 * Пернатое животное (птица).
 */
public class Feathered extends Animal {
    public Feathered(String name, double weight, int age) {
        super(name, weight, age);
    }

    /**
     * Перебирает вольеры и пытается разместиться в первом вольере, который принимает пернатых.
     *
     * @param enclosures список вольеров
     * @return true при успешном размещении
     */
    @Override
    public boolean move(List<Enclosure> enclosures) {
        for (Enclosure e : enclosures) {
            if (e.canAccept(this) && e.addAnimal(this)) {
                return true;
            }
        }
        return false;
    }
}
