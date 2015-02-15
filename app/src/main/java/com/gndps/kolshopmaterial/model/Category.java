package com.gndps.kolshopmaterial.model;

public class Category {
    int id;
    String name;

    public Category() {

    }

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, int id) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Category [id=" + id + ", name=" + name + "]";
    }
}
