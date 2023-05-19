package com.rxs.tutorsearch.models;

import com.rxs.tutorsearch.database.models.Tutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TutorData {
    private Map<Long, Tutor> tutorDatas = new HashMap<>();

    public void createTutorData(long userId) {
        Tutor tutor = new Tutor();
        tutor.setTutorId(userId);
        tutor.setTutorReviewsScore(0.0);
        tutor.setTutorReviewsCount(0);
        tutorDatas.put(userId, tutor);
    }

    public void setTutorName(long userId, String name) {
        Tutor tutor = tutorDatas.get(userId);
        tutor.setTutorName(name);
    }

    public void setTutorDescription(long userId, String description) {
        Tutor tutor = tutorDatas.get(userId);
        tutor.setTutorDescription(description);
    }

    public void setTutorPriceHour(long userId, int priceHour) {
        Tutor tutor = tutorDatas.get(userId);
        tutor.setTutorPriceHour(priceHour);
    }

    public String getTutorName(long userId) {
        return tutorDatas.get(userId).getTutorName();
    }

    public String getTutorDescription(long userId) {
        return tutorDatas.get(userId).getTutorDescription();
    }

    public int getTutorPriceHour(long userId) {
        return tutorDatas.get(userId).getTutorPriceHour();
    }

    public Tutor getTutorData(long userId) {
        return tutorDatas.get(userId);
    }
}
