package Models;

public class User {

    private int id;
    private String fullName;
    private String username;
    private String passwordHash;
    private String role; // CEO, CO_FOUNDER, CLERK
    private String email;
    private String phone;
    private String profilePicturePath;
    private boolean isActive;
    private boolean isLocked;
    private int loginAttempts;

    public User() {
    }

    public User(int id, String fullName, String username, String passwordHash,
                 String role, String email, String phone, boolean isActive,
                 boolean isLocked, int loginAttempts) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.isActive = isActive;
        this.isLocked = isLocked;
        this.loginAttempts = loginAttempts;
    }

    // ===== Getters and Setters =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public int getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    // ===== Helper methods for role checking =====

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isCEO() {
        return "CEO".equals(role);
    }

    // Admin ya CEO dono - "top level" access ke liye
    public boolean isAdminOrCEO() {
        return isAdmin() || isCEO();
    }

    public boolean isCoFounder() {
        return "CO_FOUNDER".equals(role);
    }

    public boolean isClerk() {
        return "CLERK".equals(role);
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

}