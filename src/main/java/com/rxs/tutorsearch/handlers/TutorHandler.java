package com.rxs.tutorsearch.handlers;

import com.rxs.tutorsearch.database.Database;
import com.rxs.tutorsearch.database.models.Category;
import com.rxs.tutorsearch.database.models.User;
import com.rxs.tutorsearch.models.CustomMessage;
import com.rxs.tutorsearch.models.TutorData;
import com.rxs.tutorsearch.models.UserData;
import com.rxs.tutorsearch.states.BotState;
import com.rxs.tutorsearch.utils.MarkupUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

@Component
public class TutorHandler implements UpdateHandler {
    private final UserData userData;
    private final MarkupUtils markupUtils;
    private final Database database;
    private final TutorData tutorData;

    public TutorHandler(UserData userData, MarkupUtils markupUtils, Database database, TutorData tutorData) {
        this.userData = userData;
        this.markupUtils = markupUtils;
        this.database = database;
        this.tutorData = tutorData;
    }

    @Override
    public List<CustomMessage> handleUpdate(Update update) {
        BotState botState = userData.getBotState(getChatId(update));
        switch (botState) {
            case TUTOR_NAME_STATE -> {
                tutorData.createTutorData(getChatId(update));
                tutorData.setTutorName(getChatId(update), update.getMessage().getText());
                userData.setBotState(getChatId(update), BotState.TUTOR_SUBJECT_STATE);
                return List.of(CustomMessage.createSendMessage(update, "➡\uFE0F Выберите предметную область", markupUtils.getCategoriesMarkup()));
            }
            case TUTOR_SUBJECT_STATE -> {
                Category category = database.getCategoryRepository().findById(update.getCallbackQuery().getData()).get();
                category.setTutorIds(Optional.ofNullable(category.getTutorIds()).orElse("")
                        + update.getCallbackQuery().getMessage().getChatId() + ", ");
                database.getCategoryRepository().save(category);
                userData.setBotState(getChatId(update), BotState.TUTOR_DESCRIPTION_STATE);
                return List.of(CustomMessage.createEditMessageText(update, "➡\uFE0F Введите описание профиля", markupUtils.getClearMarkup()));
            }
            case TUTOR_DESCRIPTION_STATE -> {
                tutorData.setTutorDescription(getChatId(update), update.getMessage().getText());
                userData.setBotState(getChatId(update), BotState.TUTOR_PRICE_STATE);
                return List.of(CustomMessage.createSendMessage(update, "➡\uFE0F Выберите стоимость услуги за час", markupUtils.getPriceMarkup()));
            }
            case TUTOR_PRICE_STATE -> {
                User admin = ((List<User>) database.getUserRepository().findAll()).stream()
                        .filter(user -> user.getUserRole().equals("admin")).toList().get(0);
                tutorData.setTutorPriceHour(getChatId(update), Integer.parseInt(update.getCallbackQuery().getData()));
                database.getTutorRepository().save(tutorData.getTutorData(getChatId(update)));
                userData.setBotState(getChatId(update), BotState.TUTOR_WAIT_STATE);
                return List.of(
                        CustomMessage.createSendMessage(
                                admin.getUserId(),
                                "\uD83C\uDF00 Проверка заявки ID" + update.getCallbackQuery().getMessage().getChatId() + "\n" +
                                        "Ник: @" + update.getCallbackQuery().getFrom().getUserName() + "\n" +
                                        "Имя: " + tutorData.getTutorName(getChatId(update)) + "\n" +
                                        "Описание:\n" + tutorData.getTutorDescription(getChatId(update)) + "\n" +
                                        "Цена услуги: " + tutorData.getTutorPriceHour(getChatId(update)) + "₽",
                                markupUtils.getAcceptDenyMarkup()
                        ),
                        CustomMessage.createEditMessageText(
                                update,
                                "⏰ Ожидайте подтверждения заявки",
                                markupUtils.getClearMarkup()
                        )
                );
            }
            case TUTOR_WAIT_STATE -> {
                if (update.hasCallbackQuery()) {
                    switch (update.getCallbackQuery().getData()) {
                        case "BALANCE" -> {
                            userData.setBotState(getChatId(update), BotState.TUTOR_CHOOSE_VALUE_BALANCE_STATE);
                            User user = database.getUserRepository().findById(getChatId(update)).get();
                            int balance = user.getUserBalance();
                            return List.of(
                                    CustomMessage.createEditMessageText(
                                            update,
                                            "\uD83D\uDCB0 Баланс: " + balance + "₽\n➡\uFE0F Выберите сумму для вывода",
                                            markupUtils.getPriceMarkupWithBack()
                                    )
                            );
                        }
                        case "UPDATE" -> {
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
                        case "RESTART" -> {
                            userData.setBotState(getChatId(update), BotState.ROLE_STATE);
                            return List.of(
                                    CustomMessage.createSendMessage(
                                            update,
                                            "➡\uFE0F Выберите свою роль",
                                            markupUtils.getChooseRoleMarkup()
                                    )
                            );
                        }
                        case "ACCEPT" -> {
                            String msgText = update.getCallbackQuery().getMessage().getText();
                            String studentTgName = msgText.substring(msgText.indexOf("@") + 1, msgText.indexOf("\nД"));
                            long studentId = database.getChatIdWithTgName(studentTgName);
                            String tutorTgName = database.getUserRepository().findById(getChatId(update)).get().getTgName();
                            return List.of(
                                    CustomMessage.createEditMessageText(
                                            update,
                                            update.getCallbackQuery().getMessage().getText() + "\n" +
                                                    "Проведите занятие с пользователем @" + studentTgName + " в указанное время" +
                                                    "и получите оплату за занятие после его подтверждения",
                                            markupUtils.getClearMarkup()
                                    ),
                                    CustomMessage.createSendMessage(
                                            studentId,
                                            "Репетитор @" + tutorTgName + " одобрил занятие\n" +
                                                    "После проведения занятия не забудьте подтвердить оплату!",
                                            markupUtils.getAcceptMoneyMarkup()
                                    )
                            );
                        }
                        case "DENY" -> {
                            String msgText = update.getCallbackQuery().getMessage().getText();
                            String studentTgName = msgText.substring(msgText.indexOf("@") + 1, msgText.indexOf("\nД"));
                            long studentId = database.getChatIdWithTgName(studentTgName);
                            return List.of(
                                    CustomMessage.createEditMessageText(
                                            update,
                                            "❌ Занятие отклонено",
                                            markupUtils.getClearMarkup()
                                    ),
                                    CustomMessage.createSendMessage(
                                            studentId,
                                            "❌ Репетитор отклонил занятие",
                                            markupUtils.getBackMarkup()
                                    )
                            );
                        }
                    }
                }
                return null;
            }
            case TUTOR_CHOOSE_VALUE_BALANCE_STATE -> {
                if (update.hasCallbackQuery()) {
                    String data = update.getCallbackQuery().getData();
                    switch (data) {
                        case "BACK" -> {
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
                        default -> {
                            userData.setBotState(getChatId(update), BotState.TUTOR_WAIT_CARD_NUMBER_STATE);
                            User user = database.getUserRepository().findById(getChatId(update)).get();
                            int balance = user.getUserBalance();
                            int pickValue = Integer.parseInt(update.getCallbackQuery().getData());
                            if (balance >= pickValue) {
                                user.setUserBalance(balance - pickValue);
                                database.getUserRepository().save(user);
                                return List.of(
                                        CustomMessage.createEditMessageText(
                                                update,
                                                "➡\uFE0F Введите номер карты",
                                                markupUtils.getClearMarkup()
                                        )
                                );
                            } else {
                                userData.setBotState(getChatId(update), BotState.TUTOR_WAIT_STATE);
                                return List.of(
                                        CustomMessage.createEditMessageText(
                                                update,
                                                "❌Недостаточно средств❌\n\n\uD83D\uDCB0 Баланс: " + balance + "₽\n⏰ Ожидайте заявок на занятие",
                                                markupUtils.getMainTutorMarkup()
                                        )
                                );
                            }
                        }
                    }
                }
                return null;
            }
            case TUTOR_WAIT_CARD_NUMBER_STATE -> {
                if (update.hasMessage()) {
                    userData.setBotState(getChatId(update), BotState.TUTOR_WAIT_STATE);
                    User user = database.getUserRepository().findById(getChatId(update)).get();
                    int balance = user.getUserBalance();
                    return List.of(
                            CustomMessage.createSendMessage(
                                    update,
                                    "✅Вывод прошел успешно✅\n\n\uD83D\uDCB0 Баланс: " + balance + "₽\n⏰ Ожидайте заявок на занятие",
                                    markupUtils.getMainTutorMarkup()
                            )
                    );
                }
            }
        }
        return null;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.TUTOR_STATE;
    }
}
