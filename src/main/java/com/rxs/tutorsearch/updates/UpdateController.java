package com.rxs.tutorsearch.updates;

import com.rxs.tutorsearch.handlers.HandlersHub;
import com.rxs.tutorsearch.models.CustomMessage;
import com.rxs.tutorsearch.models.UserData;
import com.rxs.tutorsearch.states.BotState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UpdateController {
    private TelegramBot telegramBot;
    private HandlersHub handlersHub;
    private UserData userData;

    public UpdateController(HandlersHub handlersHub, UserData userData) {
        this.handlersHub = handlersHub;
        this.userData = userData;
    }

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void handleUpdate(Update update) {
        List<CustomMessage> answerMessages = new ArrayList<>();
        if ((update.hasMessage() && update.getMessage().hasText())) {
            answerMessages = handlersHub.handleUpdate(userData.getBotState(update.getMessage().getChatId()), update);
        } else if (update.hasCallbackQuery()) {
            log.info("UPDATE CONTROLLER : " + userData.getBotState(update.getCallbackQuery().getMessage().getChatId()));
            answerMessages = handlersHub.handleUpdate(userData.getBotState(update.getCallbackQuery().getMessage().getChatId()), update);
        }
        if (answerMessages != null) {
            for (CustomMessage answerMessage : answerMessages) {
                switch (answerMessage.getType()) {
                    case "send" -> setView(answerMessage.generateSendMessage());
                    case "edit" -> setView(answerMessage.generateEditMessageText());
                }
            }
        }
    }

    public void setView(SendMessage message) {
        try {
            telegramBot.execute(message);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void setView(EditMessageText message) {
        try {
            telegramBot.execute(message);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
