package com.iriskopecky.agon.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String hash;
    private String username;

    public Account(String username, String hash) {
        this.hash = hash;
        this.username = username;
    }

    public Account() {};

    public Long getId() {return id;}
    public String getUsername() {return username;}
    public String getHash() {return hash;}
}
