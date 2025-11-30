import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import config.Settings;
import db.DatabaseManager;
import experLogger.OperationLogger;
import view.gui.*;

/**
 * Главный класс приложения.
 * Отвечает за инициализацию и запуск приложения
 */
public class App {
    private static Settings settings;
    private static OperationLogger Logger;
    private static List<model.Animal> animals = new ArrayList<>();
    private static List<enclosure.Enclosure> enclosures = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        settings = new Settings();
        
        try {
            Logger = new OperationLogger("app.log", "ZooApplication", true);
            
            DatabaseManager.initializeLogger(Logger);
            
            Logger.logInfo("Program started by user: " + settings.getUsername());
            
            MenuGui.launch(settings, Logger, animals, enclosures);


            // MenuView menuView = new MenuView(settings, Logger, animals, enclosures, scanner);
            // menuView.start();
            
            
        } catch (Exception e) {
            System.err.println("Фатальная ошибка: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (Logger != null) {
                Logger.logInfo("Program finished");
                Logger.close();
            }
            scanner.close();
        }
    }
}