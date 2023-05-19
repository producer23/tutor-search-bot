package com.rxs.tutorsearch.database.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "users")
public class User {
    @Id
    private Long userId;
    private String tgName;
    private String userRole;
    private Integer userBalance;

    public User() {

    }

    public User(Long userId, String tgName, String userRole, Integer userBalance) {
        this.userId = userId;
        this.tgName = tgName;
        this.userRole = userRole;
        this.userBalance = userBalance;
    }
}
