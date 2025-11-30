package controller;

import enclosure.Enclosure;
import enclosure.EnclosureType;
import model.Animal;
import model.Aquatic;
import model.Feathered;
import model.Hoofed;
import model.ColdBlooded;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Утилитный класс для генерации случайных Enclosure/Animal.
 * Методы возвращают List<T> — при желании можно указать реализацию LinkedList (useLinked=true)
 * или ArrayList (useLinked=false).
 *
 * Пример использования:
 *   List<Enclosure> enclosures = RandomDataGenerator.generateEnclosures(5, false); // ArrayList
 *   List<Animal> batch = RandomDataGenerator.generateAnimals(10, true); // LinkedList
 */
public class RandomDataGenerator {
    private static final Random RNG = new Random();

    private static final String[] AQUA_NAMES = {"рыба", "карась", "щука", "сом", "тунец"};
    private static final String[] FEATHER_NAMES = {"воробей", "голубь", "петух", "утка", "ласточка"};
    private static final String[] HOOF_NAMES = {"лошадь", "осел", "коза", "корова", "олень"};
    private static final String[] COLD_NAMES = {"ящерица", "змей", "геккон", "тритон", "угорь"};
    private static final String[] ENC_PREFIX = {"Aqua", "Net", "Open", "Infra", "Cage"};

    /**
     * Генерирует коллекцию вольеров.
     *
     * @param count количество вольеров
     * @param useLinked если true — возвращается LinkedList, иначе ArrayList
     * @return список Enclosure
     */
    public static List<Enclosure> generateEnclosures(int count, boolean useLinked) {
        List<Enclosure> list = useLinked ? new LinkedList<>() : new ArrayList<>();
        EnclosureType[] types = EnclosureType.values();
        for (int i = 0; i < count; i++) {
            EnclosureType t = types[RNG.nextInt(types.length)];
            String name = choosePrefixForType(t) + "-" + (i + 1);
            int capacity = 1 + RNG.nextInt(8); // вместимость 1..8
            list.add(new Enclosure(name, t, capacity));
        }
        return list;
    }

    /**
     * Заполняет уже имеющийся список вольеров (использует переданную реализацию списка).
     *
     * @param target список для заполнения
     * @param count количество добавляемых вольеров
     */
    public static void fillEnclosures(List<Enclosure> target, int count) {
        EnclosureType[] types = EnclosureType.values();
        for (int i = 0; i < count; i++) {
            EnclosureType t = types[RNG.nextInt(types.length)];
            String name = choosePrefixForType(t) + "-" + (target.size() + 1);
            int capacity = 1 + RNG.nextInt(8);
            target.add(new Enclosure(name, t, capacity));
        }
    }

    /**
     * Генерирует коллекцию животных (разных подклассов).
     *
     * @param count количество животных
     * @param useLinked если true — возвращается LinkedList, иначе ArrayList
     * @return список Animal
     */
    public static List<Animal> generateAnimals(int count, boolean useLinked) {
        List<Animal> list = useLinked ? new LinkedList<>() : new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(randomAnimal());
        }
        return list;
    }

    /**
     * Заполняет переданный список животными (использует реализацию списка caller-а).
     *
     * @param target список для заполнения
     * @param count сколько добавить
     */
    public static void fillAnimals(List<Animal> target, int count) {
        for (int i = 0; i < count; i++) {
            target.add(randomAnimal());
        }
    }

    /* ------------------ helpers ------------------ */

    private static Animal randomAnimal() {
        int kind = RNG.nextInt(4);
        return switch (kind) {
            case 0 -> randomAquatic();
            case 1 -> randomFeathered();
            case 2 -> randomHoofed();
            default -> randomColdBlooded();
        };
    }

    private static Aquatic randomAquatic() {
        String name = AQUA_NAMES[RNG.nextInt(AQUA_NAMES.length)];
        double weight = round(0.05 + RNG.nextDouble() * 10.0); // 0.05..10.05
        int age = 1 + RNG.nextInt(10);
        return new Aquatic(name, weight, age);
    }

    private static Feathered randomFeathered() {
        String name = FEATHER_NAMES[RNG.nextInt(FEATHER_NAMES.length)];
        double weight = round(0.05 + RNG.nextDouble() * 5.0); // 0.05..5.05
        int age = 1 + RNG.nextInt(8);
        return new Feathered(name, weight, age);
    }

    private static Hoofed randomHoofed() {
        String name = HOOF_NAMES[RNG.nextInt(HOOF_NAMES.length)];
        double weight = round(20.0 + RNG.nextDouble() * 400.0); // 20..420 kg
        int age = 1 + RNG.nextInt(20);
        return new Hoofed(name, weight, age);
    }

    private static ColdBlooded randomColdBlooded() {
        String name = COLD_NAMES[RNG.nextInt(COLD_NAMES.length)];
        double weight = round(0.01 + RNG.nextDouble() * 20.0); // 0.01..20
        int age = 1 + RNG.nextInt(12);
        return new ColdBlooded(name, weight, age);
    }

    private static String choosePrefixForType(EnclosureType t) {
        return switch (t) {
            case AQUARIUM -> "Aqua";
            case NET_COVERED -> "Net";
            case OPEN -> "Open";
            case INFRARED -> "Infra";
            default -> "Enc";
        };
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
