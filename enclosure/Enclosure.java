package enclosure;

import java.util.ArrayList;
import java.util.List;
import model.Animal;
import model.Aquatic;
import model.ColdBlooded;
import model.Feathered;
import model.Hoofed;

/**
 * Класс, представляющий вольер в зоопарке.
 */
public class Enclosure {
    /** Тип вольера. */
    private final EnclosureType type;
    /** Читабельное имя вольера. */
    private final String name;
    /** Максимальное количество животных в вольере. */
    private final int capacity;
    /** Текущий список животных (внутренний). */
    private final List<Animal> animals = new ArrayList<>();

    /**
     * Создаёт новый вольер.
     */
    public Enclosure(String name, EnclosureType type, int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be >= 0");
        }
        this.type = type;
        this.name = name;
        this.capacity = capacity;
    }

    /**
     * Получить тип вольера.
     *
     * @return тип
     */
    public EnclosureType getType(){ return type; }

    /**
     * Получить имя вольера.
     *
     * @return имя
     */
    public String getName(){ return name; }


    /**
     * Получить максимальную вместимость вольера.
     *
     * @return максимальное количество животных
     */
    public int getCapacity(){ return capacity; }
    

    /**
     * Проверяет, подходит ли этот вольер для данного животного по типу/классу
     */
    public boolean canAccept(Animal a) {
        return switch (type) {
            case AQUARIUM -> a instanceof Aquatic;
            case NET_COVERED -> a instanceof Feathered;
            case OPEN -> a instanceof Hoofed;
            case INFRARED -> a instanceof ColdBlooded;
            default -> false;
        };
    }

    /**
     * Попытаться добавить животное в вольер.
     */
    public boolean addAnimal(Animal a) {
        if (animals.size() >= capacity) return false;
        animals.add(a);
        return true;
    }

    /**
     * Получить копию списка животных, содержащихся в вольере.
     * Возвращается новая коллекция для защиты внутреннего состояния.
     *
     * @return копия списка животных
     */
    public List<Animal> getAnimals() {
        return new ArrayList<>(animals);
    }

    @Override
    public String toString() {
        return String.format("Enclosure[%s - %s] capacity=%d current=%d",
            name, type, capacity, animals.size());
    }
}
