package model;

import enclosure.Enclosure;
import java.util.List;

/**
 * Хладнокровное животное (рептилии, амфибии).
 */
public class ColdBlooded extends Animal {
    public ColdBlooded(String name, double weight, int age) {
        super(name, weight, age);
    }

    /**
     * Ищет вольер с инфракрасным/специализированным освещением.
     *
     * @param enclosures список вольеров
     * @return true если размещено
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
