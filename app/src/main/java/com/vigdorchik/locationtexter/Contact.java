package com.vigdorchik.locationtexter;

public class Contact {

    private String name;
    private String number;

    public Contact(String name, String number) {
        setName(name);
        setNumber(number);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
