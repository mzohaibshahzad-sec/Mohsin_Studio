package Utils;

import javafx.scene.Scene;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.util.prefs.Preferences;

public class ThemeManager {

    public static final String LIGHT_CSS = "/resources/app-light.css";
    public static final String DARK_CSS = "/resources/app-dark.css";

    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private static final String PREF_KEY = "theme_mode";

    private static String currentTheme = prefs.get(PREF_KEY, "light"); // default light

    public static boolean isDarkMode() {
        return "dark".equals(currentTheme);
    }

    public static String getCurrentCssPath() {
        return isDarkMode() ? DARK_CSS : LIGHT_CSS;
    }

    public static void toggleTheme(Scene scene) {
        currentTheme = isDarkMode() ? "light" : "dark";
        prefs.put(PREF_KEY, currentTheme);
        applyTheme(scene);
    }

    public static void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(ThemeManager.class.getResource(getCurrentCssPath()).toExternalForm());
    }

    // Smooth fade transition jab theme switch ho
    public static void applyThemeWithTransition(Scene scene, javafx.scene.Parent root) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.85);
        fadeOut.setOnFinished(e -> {
            applyTheme(scene);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), root);
            fadeIn.setFromValue(0.85);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }
}