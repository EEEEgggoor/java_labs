package model;

import enclosure.Enclosure;
import java.util.List;

/**
 * Абстрактный класс, представляющий животное в модели зоопарка.
 */
public abstract class Animal {
    /** Вес животного в килограммах. */
    protected double weight;
    /** Возраст животного в годах. */
    protected int age;
    /** Читабельное имя животного. */
    protected String name;

    /**
     * Конструктор животного.
     */
    public Animal(String name, double weight, int age) {
        this.name = name;
        this.weight = weight;
        this.age = age;
    }

    /**
     * Получить имя животного.
     */
    public String getName(){ return name; }

    /**
     * Получить вес животного.
     */
    public double getWeight(){ return weight; }

    /**
     * Получить возраст животного.
     */
    public int getAge(){ return age; }

    /**
     * Попытаться расселить это животное в подходящий вольер из списка.
     */
    public abstract boolean move(List<Enclosure> enclosures);

    @Override
    public String toString() {
        return String.format("%s(name=%s, age=%d, weight=%.1f)",
            this.getClass().getSimpleName(), name, age, weight);
    }
}
