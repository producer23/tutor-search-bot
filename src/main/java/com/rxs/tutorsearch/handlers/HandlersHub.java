package com.rxs.tutorsearch.handlers;

import com.rxs.tutorsearch.models.CustomMessage;
import com.rxs.tutorsearch.states.BotState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HandlersHub {
    private final Map<BotState, UpdateHandler> updateHandlers = new HashMap<>();

    public HandlersHub(List<UpdateHandler> updateHandlers) {
        updateHandlers.forEach(handler -> this.updateHandlers.put(handler.getHandlerName(), handler));
    }

    public List<CustomMessage> handleUpdate(BotState currentState, Update update) {
        return findUpdateHandler(currentState).handleUpdate(update);
    }

    private UpdateHandler findUpdateHandler(BotState currentState) {
        log.info("HANDLERS HUB : " + currentState);
        return switch (currentState) {
            case START_STATE, ROLE_STATE -> updateHandlers.get(BotState.START_STATE);
            case ADMIN_STATE -> updateHandlers.get(BotState.ADMIN_STATE);
            case TUTOR_STATE,
                    TUTOR_NAME_STATE,
                    TUTOR_SUBJECT_STATE,
                    TUTOR_DESCRIPTION_STATE,
                    TUTOR_PRICE_STATE,
                    TUTOR_WAIT_STATE,
                    TUTOR_CHOOSE_VALUE_BALANCE_STATE,
                    TUTOR_WAIT_CARD_NUMBER_STATE -> updateHandlers.get(BotState.TUTOR_STATE);
            case STUDENT_STATE,
                    STUDENT_CHOOSE_VALUE_BALANCE_STATE,
                    STUDENT_SUBJECT_STATE,
                    STUDENT_TUTORS_LIST_STATE,
                    STUDENT_TUTOR_PROFILE_STATE,
                    STUDENT_WAIT_TIME_STATE,
                    STUDENT_WAIT_TUTOR_ANSWER_STATE,
                    STUDENT_WAIT_REVIEW_STATE -> updateHandlers.get(BotState.STUDENT_STATE);
        };
    }

}
