package com.iriskopecky.agon.data;

import jakarta.persistence.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String hash;
    private String username;
    @OneToMany(mappedBy = "owner")
    private List<Teras> terata = new ArrayList<>();

    public Account(String username, String hash) {
        this.hash = hash;
        this.username = username;
    }

    public Account() {};

    public Long getId() {return id;}
    public String getUsername() {return username;}
    public String getHash() {return hash;}
}
