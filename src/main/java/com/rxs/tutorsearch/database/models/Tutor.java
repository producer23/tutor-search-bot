package com.rxs.tutorsearch.database.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "tutors")
public class Tutor {
    @Id
    private Long tutorId;
    private String tutorName;
    private String tutorDescription;
    private Integer tutorPriceHour;
    private Integer tutorReviewsCount;
    private Double tutorReviewsScore;
}
