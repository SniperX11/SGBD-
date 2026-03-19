package com.sgbd.model;

import java.time.LocalDate;

/**
 * Model class representing an Employee (child entity linked to a Department).
 */
public class Employee {

    private int id;
    private String name;
    private String email;
    private double salary;
    private LocalDate hireDate;
    private int departmentId;

    public Employee() {}

    public Employee(int id, String name, String email, double salary,
                    LocalDate hireDate, int departmentId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.salary = salary;
        this.hireDate = hireDate;
        this.departmentId = departmentId;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
}
