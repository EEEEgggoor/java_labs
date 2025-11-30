package model;

import enclosure.Enclosure;
import java.util.List;

/**
 * Водоплавающее животное. Для расселения ищет вольер, подходящий для водоплавающих.
 */
public class Aquatic extends Animal {
    /**
     * Конструктор водоплавающего животного.
     *
     * @param name имя
     * @param weight вес
     * @param age возраст
     */
    public Aquatic(String name, double weight, int age) {
        super(name, weight, age);
    }

    /**
     * Реализация расселения: перебирает вольеры и пытается разместиться в первом подходящем.
     *
     * @param enclosures список доступных вольеров
     * @return true если размещено, иначе false
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
