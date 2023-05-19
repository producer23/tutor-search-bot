package com.rxs.tutorsearch.database.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "categories")
public class Category {
    @Id
    private String categoryName;
    private String tutorIds;
}
