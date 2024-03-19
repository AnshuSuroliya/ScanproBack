package com.mavericks.scanpro.services;

public class ValidationServiceImpl {

    private String nameRegex ="^(?=.*[A-Za-z])[A-Za-z0-9_]*$";

    public boolean validateName(String name) {
        return name.matches(nameRegex);
    }
}
