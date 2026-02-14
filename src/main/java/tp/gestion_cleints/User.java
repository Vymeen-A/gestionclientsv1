package tp.gestion_cleints;

public class User {
    private String username;
    private Role role;
    private String fullName;
    private String email;
    private String phone;
    private String profilePhoto;
    private boolean active = true;
    private String lastLogin;
    private int failedAttempts;
    private String lockoutUntil;

    public User(String username, Role role) {
        this.username = username;
        this.role = role;
    }

    public User(String username, Role role, String fullName, String email, String phone, String profilePhoto) {
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.profilePhoto = profilePhoto;
    }

    public User(String username, Role role, String fullName, String email, String phone, String profilePhoto,
            boolean active, String lastLogin, int failedAttempts, String lockoutUntil) {
        this(username, role, fullName, email, phone, profilePhoto);
        this.active = active;
        this.lastLogin = lastLogin;
        this.failedAttempts = failedAttempts;
        this.lockoutUntil = lockoutUntil;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName != null ? fullName : username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isAccountant() {
        return role == Role.ADMIN || role == Role.ACCOUNTANT;
    }

    public boolean isReadOnly() {
        return role == Role.READ_ONLY;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public String getLockoutUntil() {
        return lockoutUntil;
    }

    public void setLockoutUntil(String lockoutUntil) {
        this.lockoutUntil = lockoutUntil;
    }

    public boolean isLocked() {
        if (lockoutUntil == null)
            return false;
        try {
            java.time.LocalDateTime until = java.time.LocalDateTime.parse(lockoutUntil);
            return until.isAfter(java.time.LocalDateTime.now());
        } catch (Exception e) {
            return false;
        }
    }
}
