import Controllers.LoginScreen;
import Controllers.Dashboard;
import Database.SalesEntryDAO;
import Services.BackupService;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        // App ka icon set karna (taskbar/title bar ke liye)
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(getClass().getResourceAsStream("/resources/images/logo.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Logo load nahi ho saka: " + e.getMessage());
        }


        // Auto-backup jab app band ho
        primaryStage.setOnCloseRequest(event -> {
            try {
                System.out.println("App band ho rahi hai - backup le raha hun...");
                java.util.List<Models.SalesEntry> todayEntries = SalesEntryDAO.getAllTodayEntries();
                if (!todayEntries.isEmpty()) {
                    String backupPath = BackupService.backupTodaySales(todayEntries);
                    if (backupPath != null) {
                        System.out.println("Backup ho gaya: " + backupPath);
                    } else {
                        System.out.println("Backup nahi hua - koi masla aaya.");
                    }
                } else {
                    System.out.println("Aaj ki koi entry nahi thi - backup skip.");
                }
            } catch (Exception e) {
                System.out.println("Backup error on close: " + e.getMessage());
            }
        });

        new LoginScreen(primaryStage).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}