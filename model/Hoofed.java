package model;

import enclosure.Enclosure;
import java.util.List;

/**
 * Копытное животное (например, олень, козёл).
 */
public class Hoofed extends Animal {
    public Hoofed(String name, double weight, int age) {
        super(name, weight, age);
    }

    /**
     * Расселение в подходящий открытый вольер.
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
