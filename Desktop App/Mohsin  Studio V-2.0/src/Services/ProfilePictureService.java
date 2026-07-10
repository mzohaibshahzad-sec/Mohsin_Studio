package Services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ProfilePictureService {

    // Profile pictures yahan save hoti hain: ~/MohsinStudioProfilePictures/
    private static String getStorageFolderPath() {
        String userHome = System.getProperty("user.home");
        String folderPath = userHome + File.separator + "MohsinStudioProfilePictures";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folderPath;
    }

    // Selected image file ko copy kar ke permanent storage mein rakhta hai, naya path return karta hai
    public static String saveProfilePicture(File sourceFile, int userId) {
        try {
            String extension = getExtension(sourceFile.getName());
            String fileName = "user_" + userId + "_" + System.currentTimeMillis() + "." + extension;
            String destPath = getStorageFolderPath() + File.separator + fileName;

            Files.copy(sourceFile.toPath(), Path.of(destPath), StandardCopyOption.REPLACE_EXISTING);

            return destPath;

        } catch (IOException e) {
            System.out.println("Error saving profile picture: " + e.getMessage());
            return null;
        }
    }

    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "png";
    }
}