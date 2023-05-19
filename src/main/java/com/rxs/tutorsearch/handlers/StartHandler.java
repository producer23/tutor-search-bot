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
public class StartHandler implements UpdateHandler {
    private final UserData userData;
    private final MarkupUtils markupUtils;
    private final Database database;

    public StartHandler(UserData userData, MarkupUtils markupUtils, Database database) {
        this.userData = userData;
        this.markupUtils = markupUtils;
        this.database = database;
    }

    @Override
    public List<CustomMessage> handleUpdate(Update update) {
        BotState botState = userData.getBotState(getChatId(update));
        switch (botState) {
            case START_STATE -> {
                userData.setBotState(getChatId(update), BotState.ROLE_STATE);
                return List.of(CustomMessage.createSendMessage(update, "➡\uFE0F Выберите свою роль", markupUtils.getChooseRoleMarkup()));
            }
            case ROLE_STATE -> {
                if (update.hasCallbackQuery()) {
                    String data = update.getCallbackQuery().getData();
                    switch (data) {
                        case "ROLE_STUDENT" -> {
                            if (database.getUserRepository().findById(getChatId(update)).isEmpty()) {
                                database.getUserRepository().save(new User(getChatId(update), update.getCallbackQuery().getFrom().getUserName(), "student", 0));
                            }
                            userData.setBotState(getChatId(update), BotState.STUDENT_STATE);
                            User user = database.getUserRepository().findById(getChatId(update)).get();
                            int balance = user.getUserBalance();
                            return List.of(CustomMessage.createEditMessageText(update,
                                    "Приветствую, клиент!\n\uD83D\uDCB0 Баланс: " + balance + "₽",
                                    markupUtils.getMainStudentMarkup()));
                        }

                        case "ROLE_TUTOR" -> {
                            if (database.getTutorRepository().findById(getChatId(update)).isEmpty()) {
                                database.getUserRepository().save(new User(getChatId(update), update.getCallbackQuery().getFrom().getUserName(), "tutor", 0));
                                userData.setBotState(getChatId(update), BotState.TUTOR_NAME_STATE);
                                return List.of(
                                        CustomMessage.createEditMessageText(
                                                update,
                                                "➡\uFE0F Введите ваше имя",
                                                markupUtils.getClearMarkup()
                                        )
                                );
                            } else {
                                userData.setBotState(getChatId(update), BotState.TUTOR_WAIT_STATE);
                                User user = database.getUserRepository().findById(getChatId(update)).get();
                                int balance = user.getUserBalance();
                                return List.of(
                                        CustomMessage.createEditMessageText(
                                                update,
                                                "\uD83D\uDCB0 Баланс: " + balance + "₽\n⏰ Ожидайте заявок на занятие",
                                                markupUtils.getMainTutorMarkup()
                                        )
                                );
                            }
                        }

                        case "ROLE_ADMIN" -> {
                            if (database.getUserRepository().findById(getChatId(update)).isPresent()) {
                                if (database.getUserRepository().findById(getChatId(update)).get().getUserRole().equals("admin")) {
                                    userData.setBotState(getChatId(update), BotState.ADMIN_STATE);
                                    return List.of(CustomMessage.createEditMessageText(update, "\uD83C\uDF89Администратор успешно авторизован\uD83C\uDF89", markupUtils.getClearMarkup()));
                                } else {
                                    return List.of(CustomMessage.createEditMessageText(update, "❌Вы не администратор❌\n\n➡\uFE0F Выберите свою роль", markupUtils.getChooseRoleMarkup()));
                                }
                            } else {
                                return List.of(CustomMessage.createEditMessageText(update, "❌Вы не администратор❌\n\n➡\uFE0F Выберите свою роль", markupUtils.getChooseRoleMarkup()));
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.START_STATE;
    }
}
