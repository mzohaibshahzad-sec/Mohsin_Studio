package Models;

import java.util.ArrayList;
import java.util.List;

public class Package {

    private int id;
    private String packageName;
    private String description;
    private double price;
    private String category;
    private boolean isActive;
    private String services;   // format: "Photography:5000,Videography:8000" (naam:perDayPrice)
    private double discount;   // percentage: 10 = 10% off

    public Package() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getServices() { return services; }
    public void setServices(String services) { this.services = services; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    // Discount ke baad final price
    public double getFinalPrice() {
        if (discount > 0) {
            return price - (price * discount / 100.0);
        }
        return price;
    }

    /**
     * Services parse karta hai — dono formats support karta hai:
     * Old format: "Photography,Videography"
     * New format: "Photography:5000,Videography:8000"
     * Returns: [ ["Photography", "5000"], ["Videography", "8000"] ]
     * Old format mein price "" hogi
     */
    public List<String[]> getParsedServices() {
        List<String[]> result = new ArrayList<>();
        if (services == null || services.trim().isEmpty()) return result;
        for (String entry : services.split(",")) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            if (entry.contains(":")) {
                String[] parts = entry.split(":");
                String svcName = parts[0].trim();
                String perDay = parts.length > 1 ? parts[1].trim() : "";
                String days = parts.length > 2 ? parts[2].trim() : "1";
                result.add(new String[]{ svcName, perDay, days });
            } else {
                result.add(new String[]{ entry, "", "1" });
            }
        }
        return result;
    }

    /** Sirf service names (comma separated) — backward compat ke liye */
    public String getServiceNamesOnly() {
        if (services == null) return "";
        StringBuilder sb = new StringBuilder();
        for (String[] s : getParsedServices()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(s[0]);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return packageName + " - Rs. " + String.format("%.0f", getFinalPrice());
    }
}