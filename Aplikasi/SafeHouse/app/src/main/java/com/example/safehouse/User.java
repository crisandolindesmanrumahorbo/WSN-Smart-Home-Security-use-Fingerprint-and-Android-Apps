package com.example.safehouse;

public class User {
    public String name, email, id;
    public int status;

    public User(){

    }

    public User(String name, String email, String phone,int status) {
        this.name = name;
        this.email = email;
        this.id = phone;
        this.status = status;
    }
}
