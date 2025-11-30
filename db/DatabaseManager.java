package db;

import enclosure.Enclosure;
import enclosure.EnclosureType;
import experLogger.OperationLogger;
import java.io.*;
import java.util.*;
import model.Animal;
import model.Aquatic;
import model.ColdBlooded;
import model.Feathered;
import model.Hoofed;

/**
 * Менеджер базы данных для сохранения и загрузки животных и вольеров
 */
public class DatabaseManager {
    private static final String ANIMALS_DB = "animals.db";
    private static final String ENCLOSURES_DB = "enclosures.db";
    private static OperationLogger logger;

    /**
     * Инициализирует логгер для операций с базой данных.
     */
    public static void initializeLogger(OperationLogger appLogger) {
        logger = appLogger;
    }

    /**
     * Сохраняет список животных в файл базы данных.
     */
    public static void saveAnimals(List<Animal> animals) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ANIMALS_DB))) {
            for (Animal animal : animals) {
                writer.println(serializeAnimal(animal));
            }
            if (logger != null) {
                logger.logInfo("Saved " + animals.size() + " animals to database");
            }
        } catch (IOException e) {
            if (logger != null) {
                logger.logError("Failed to save animals to database", e);
            }
        }
    }

    /**
     * Загружает список животных из файла базы данных.
     */
    public static List<Animal> loadAnimals() {
        List<Animal> animals = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ANIMALS_DB))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Animal animal = deserializeAnimal(line);
                if (animal != null) {
                    animals.add(animal);
                }
            }
            if (logger != null) {
                logger.logInfo("Loaded " + animals.size() + " animals from database");
            }
        } catch (IOException e) {
            if (logger != null) {
                logger.logError("Failed to load animals from database", e);
            }
        }
        return animals;
    }

    /**
     * Сохраняет список вольеров в файл базы данных.
     */
    public static void saveEnclosures(List<Enclosure> enclosures) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ENCLOSURES_DB))) {
            for (Enclosure enclosure : enclosures) {
                writer.println(serializeEnclosure(enclosure));
            }
            if (logger != null) {
                logger.logInfo("Saved " + enclosures.size() + " enclosures to database");
            }
            System.out.println("✓ Вольеры сохранены в базу данных: " + enclosures.size() + " вольеров");
        } catch (IOException e) {
            if (logger != null) {
                logger.logError("Failed to save enclosures to database", e);
            }
            System.out.println("✗ Ошибка сохранения вольеров: " + e.getMessage());
        }
    }

    /**
     * Загружает список вольеров из файла базы данных.
     */
    public static List<Enclosure> loadEnclosures() {
        List<Enclosure> enclosures = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ENCLOSURES_DB))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Enclosure enclosure = deserializeEnclosure(line);
                if (enclosure != null) {
                    enclosures.add(enclosure);
                }
            }
            if (logger != null) {
                logger.logInfo("Loaded " + enclosures.size() + " enclosures from database");
            }
        } catch (IOException e) {
            if (logger != null) {
                logger.logError("Failed to load enclosures from database", e);
            }
        }
        return enclosures;
    }

    private static String serializeAnimal(Animal animal) {
        return String.format("%s|%s|%.2f|%d", 
            animal.getClass().getSimpleName(),
            animal.getName(),
            animal.getWeight(),
            animal.getAge());
    }

    private static Animal deserializeAnimal(String data) {
        try {
            String[] parts = data.split("\\|");
            if (parts.length == 4) {
                String type = parts[0];
                String name = parts[1];
                double weight = Double.parseDouble(parts[2]);
                int age = Integer.parseInt(parts[3]);
                
                switch (type) {
                    case "Aquatic": return new Aquatic(name, weight, age);
                    case "Feathered": return new Feathered(name, weight, age);
                    case "Hoofed": return new Hoofed(name, weight, age);
                    case "ColdBlooded": return new ColdBlooded(name, weight, age);
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.logError("Error deserializing animal: " + data, e);
            }
        }
        return null;
    }

    private static String serializeEnclosure(Enclosure enclosure) {
        return String.format("%s|%s|%d",
            enclosure.getName(),
            enclosure.getType().name(),
            enclosure.getCapacity());
    }

    private static Enclosure deserializeEnclosure(String data) {
        try {
            String[] parts = data.split("\\|");
            if (parts.length == 3) {
                String name = parts[0];
                EnclosureType type = EnclosureType.valueOf(parts[1]);
                int capacity = Integer.parseInt(parts[2]);
                return new Enclosure(name, type, capacity);
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.logError("Error deserializing enclosure: " + data, e);
            }
        }
        return null;
    }
}