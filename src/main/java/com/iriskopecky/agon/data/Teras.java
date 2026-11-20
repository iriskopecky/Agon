package com.iriskopecky.agon.data;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Comparator;

@Entity
public class Teras {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Account owner;
    private boolean publicFacing;
    private String name;
    private Instant creationDate;

    public Teras(Account owner, String name) {
        this.owner = owner;
        this.name = name;
        this.publicFacing = false;
        this.creationDate = Instant.now();
    }

    public Teras() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublicFacing() {
        return publicFacing;
    }

    public void setPublicFacing(boolean publicFacing) {
        this.publicFacing = publicFacing;
    }

    public Account getOwner() {
        return owner;
    }

    public Long getId() {
        return id;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public static class TerasCreationComparator implements Comparator<Teras> {
        @Override
        public int compare(Teras o1, Teras o2) {
            return o1.getCreationDate().compareTo(o2.getCreationDate());
        }
    }

    public static class TerasAlphabeticalComparator implements Comparator<Teras> {
        @Override
        public int compare(Teras o1, Teras o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
