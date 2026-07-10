package Security;

public class PasswordUtil {

    // Ab hashing nahi hoti - client ki request pe simple/plain rakha gaya hai.
    // Password seedha as-is store/compare hota hai.
    public static String hashPassword(String plainPassword) {
        return plainPassword;
    }

    // Login ke waqt - seedha plain text compare karta hai
    public static boolean verifyPassword(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) return false;
        return plainPassword.equals(storedPassword);
    }
}