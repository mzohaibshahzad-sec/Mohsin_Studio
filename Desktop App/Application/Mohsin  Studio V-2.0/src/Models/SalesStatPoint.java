package Models;

public class SalesStatPoint {

    private String label;       // date (yyyy-MM-dd) ya month (yyyy-MM)
    private double shopSales;
    private double ordersRevenue;

    public SalesStatPoint() {
    }

    public SalesStatPoint(String label, double shop, double orders) {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getShopSales() {
        return shopSales;
    }

    public void setShopSales(double shopSales) {
        this.shopSales = shopSales;
    }

    public double getOrdersRevenue() {
        return ordersRevenue;
    }

    public void setOrdersRevenue(double ordersRevenue) {
        this.ordersRevenue = ordersRevenue;
    }

    // TableView aur charts ke liye combined total
    public double getTotal() {
        return shopSales + ordersRevenue;
    }
}