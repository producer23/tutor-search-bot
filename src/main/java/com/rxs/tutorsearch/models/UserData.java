package com.rxs.tutorsearch.models;

import com.rxs.tutorsearch.states.BotState;
import jakarta.persistence.Entity;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
public class UserData {
    private Map<Long, BotState> userBotStates = new HashMap<>();
    private Map<Long, Long> userTutorRegistrations = new HashMap<>();
    private Map<Long, String> userCategorySelects = new HashMap<>();

    public void setBotState(long userId, BotState botState) {
        userBotStates.put(userId, botState);
    }

    public void setTutorRegistration(long userId, long tutorId) {
        userTutorRegistrations.put(userId, tutorId);
    }

    public void setCategorySelect(long userId, String category) {
        userCategorySelects.put(userId, category);
    }

    public BotState getBotState(long userId) {
        BotState botState = userBotStates.get(userId);
        if (botState == null) {
            botState = BotState.START_STATE;
        }
        return botState;
    }

    public long getTutorRegistration(long userId) {
        return userTutorRegistrations.get(userId);
    }

    public String getCategorySelect(long userId) {
        return userCategorySelects.get(userId);
    }
}
