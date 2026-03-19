package com.sgbd.model;

/**
 * Model class representing a Department (parent entity).
 */
public class Department {

    private int id;
    private String name;
    private String location;
    private double budget;

    public Department() {}

    public Department(int id, String name, String location, double budget) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.budget = budget;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    /** Used by JTable / JComboBox renderers. */
    @Override
    public String toString() {
        return name;
    }
}
