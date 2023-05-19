package com.rxs.tutorsearch.handlers;

import com.rxs.tutorsearch.models.CustomMessage;
import com.rxs.tutorsearch.states.BotState;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface UpdateHandler {
    List<CustomMessage> handleUpdate(Update update);
    BotState getHandlerName();

    default long getChatId(Update update) {
        try {
            return update.getMessage().getChatId();
        } catch (Exception e) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
    }
}
