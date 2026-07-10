package Models;

import java.sql.Date;
import java.sql.Timestamp;

public class SalesEntry {

    private int id;
    private String entryType;
    private String description;
    private double amount;
    private int enteredBy;
    private String enteredByName; // display ke liye
    private Date entryDate;
    private String notes;
    private Timestamp createdAt;

    public SalesEntry() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getEnteredBy() {
        return enteredBy;
    }

    public void setEnteredBy(int enteredBy) {
        this.enteredBy = enteredBy;
    }

    public String getEnteredByName() {
        return enteredByName;
    }

    public void setEnteredByName(String enteredByName) {
        this.enteredByName = enteredByName;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}