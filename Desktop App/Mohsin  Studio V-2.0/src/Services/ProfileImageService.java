package Services;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**
 * Profile pictures ko disk pe save/replace/delete karta hai.
 * Save location: ~/MohsinStudioProfiles/user_<id>.<ext>
 * DB mein sirf yeh path string save hoti hai (Users.profile_picture_path column).
 */
public class ProfileImageService {

    private static final String FOLDER_NAME = "MohsinStudioProfiles";

    private static Path getFolder() {
        Path folder = Paths.get(System.getProperty("user.dir"), FOLDER_NAME);
        try {
            if (!Files.exists(folder)) Files.createDirectories(folder);
        } catch (IOException e) {
            System.out.println("Error creating profile folder: " + e.getMessage());
        }
        return folder;
    }


    public static String saveProfileImage(int userId, File sourceFile) {
        if (sourceFile == null || !sourceFile.exists()) return null;

        try {
            String originalName = sourceFile.getName();
            String ext = "jpg";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex < originalName.length() - 1) {
                ext = originalName.substring(dotIndex + 1).toLowerCase();
            }

            // Purani profile image (kisi bhi extension ki) delete kardo
            deleteExistingImages(userId);

            Path folder = getFolder();
            Path target = folder.resolve("user_" + userId + "." + ext);
            Files.copy(sourceFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            return target.toAbsolutePath().toString();

        } catch (IOException e) {
            System.out.println("Error saving profile image: " + e.getMessage());
            return null;
        }
    }

    /** User ki purani profile images (har extension) delete karta hai */
    private static void deleteExistingImages(int userId) {
        Path folder = getFolder();
        String prefix = "user_" + userId + ".";
        File[] files = folder.toFile().listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.getName().startsWith(prefix)) {
                f.delete();
            }
        }
    }

    /** Diya gaya path valid file hai ya nahi check karta hai (fetch ke waqt use hota hai) */
    public static boolean imageExists(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        return new File(path).exists();
    }
}