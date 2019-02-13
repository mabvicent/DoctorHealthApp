package com.example.doctorhealthapp.model;

public class User {

    private String email,password,name,phone,specilation,hospital ;

    public User() {
    }

    public User(String email, String password, String name, String phone,String specilation,String hospital) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.specilation=specilation;
        this.hospital=hospital;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecilation() {
        return specilation;
    }

    public void setSpecilation(String specilation) {
        this.specilation = specilation;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }
}
