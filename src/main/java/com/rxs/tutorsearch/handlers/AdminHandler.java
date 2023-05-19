package com.rxs.tutorsearch.handlers;

import com.rxs.tutorsearch.database.Database;
import com.rxs.tutorsearch.database.models.User;
import com.rxs.tutorsearch.models.CustomMessage;
import com.rxs.tutorsearch.models.UserData;
import com.rxs.tutorsearch.states.BotState;
import com.rxs.tutorsearch.utils.MarkupUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
@Component
public class AdminHandler implements UpdateHandler {
    private final UserData userData;
    private final MarkupUtils markupUtils;
    private final Database database;

    public AdminHandler(UserData userData, MarkupUtils markupUtils, Database database) {
        this.userData = userData;
        this.markupUtils = markupUtils;
        this.database = database;
    }

    @Override
    public List<CustomMessage> handleUpdate(Update update) {
        BotState botState = userData.getBotState(getChatId(update));
        log.info("ADMIN HANDLER : " + botState);
        switch (botState) {
            case ADMIN_STATE -> {
                if (update.hasCallbackQuery()) {
                    String data = update.getCallbackQuery().getData();
                    switch (data) {
                        case "ACCEPT" -> {
                            String msgText = update.getCallbackQuery().getMessage().getText();
                            String tutorIdText = msgText.substring(msgText.indexOf("ID") + 2, msgText.indexOf("\nНик"));
                            long tutorId = Long.parseLong(tutorIdText);
                            User tutor = database.getUserRepository().findById(tutorId).get();
                            int balance = tutor.getUserBalance();
                            return List.of(
                                    CustomMessage.createSendMessage(
                                            tutorId,
                                            "Ваша заявка принята✅\n\uD83D\uDCB0 Баланс: " + balance + "₽\n⏰ Ожидайте заявок на занятие",
                                            markupUtils.getMainTutorMarkup()),
                                    CustomMessage.createEditMessageText(
                                            update,
                                            "✅ ОДОБРЕН\n" + update.getCallbackQuery().getMessage().getText(),
                                            markupUtils.getClearMarkup())
                            );
                        }

                        case "DENY" -> {
                            long tutorId = deleteTutor(update);
                            return List.of(
                                    CustomMessage.createSendMessage(tutorId,
                                            "Ваша заявка отклонена❌\nПерезапустите бота",
                                            markupUtils.getBadTutorMarkup()),
                                    CustomMessage.createEditMessageText(update,
                                            "❌ ОТКЛОНЕН\n" + update.getCallbackQuery().getMessage().getText(),
                                            markupUtils.getClearMarkup())
                            );
                        }
                    }
                }
            }
        }
        return null;
    }

    private long deleteTutor(Update update) {
        String msgText = update.getCallbackQuery().getMessage().getText();
        String tutorIdText = msgText.substring(msgText.indexOf("ID") + 2, msgText.indexOf("\nНик"));
        long tutorId = Long.parseLong(tutorIdText);
        if (database.getTutorRepository().findById(tutorId).isPresent()) {
            database.getTutorRepository().delete(database.getTutorRepository().findById(tutorId).get());
        }
        database.deleteTutorFromCategories(tutorId);
        return tutorId;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.ADMIN_STATE;
    }
}
