package com.rxs.tutorsearch.handlers;

import com.rxs.tutorsearch.database.Database;
import com.rxs.tutorsearch.database.models.Tutor;
import com.rxs.tutorsearch.database.models.User;
import com.rxs.tutorsearch.models.CustomMessage;
import com.rxs.tutorsearch.models.UserData;
import com.rxs.tutorsearch.states.BotState;
import com.rxs.tutorsearch.utils.MarkupUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class StudentHandler implements UpdateHandler {
    private final UserData userData;
    private final MarkupUtils markupUtils;
    private final Database database;

    public StudentHandler(UserData userData, MarkupUtils markupUtils, Database database) {
        this.userData = userData;
        this.markupUtils = markupUtils;
        this.database = database;
    }

    @Override
    public List<CustomMessage> handleUpdate(Update update) {
        BotState botState = userData.getBotState(getChatId(update));
        switch (botState) {
            case STUDENT_STATE -> {
                if (update.hasCallbackQuery()) {
                    switch (update.getCallbackQuery().getData()) {
                        case "FIND_TUTOR" -> {
                            userData.setBotState(getChatId(update), BotState.STUDENT_SUBJECT_STATE);
                            return List.of(
                                    CustomMessage.createEditMessageText(
                                            update,
                                            "➡\uFE0F Выберите предметную область",
                                            markupUtils.getCategoriesMarkup()
                                    )
                            );
                        }
                        case "BALANCE" -> {
                            userData.setBotState(getChatId(update), BotState.STUDENT_CHOOSE_VALUE_BALANCE_STATE);
                            return List.of(
                                    CustomMessage.createEditMessageText(
                                            update,
                                            "➡\uFE0F Выберите сумму для пополнения",
                                            markupUtils.getPriceMarkupWithBack()
                                    )
                            );
                        }
                    }
                }
            }
            case STUDENT_CHOOSE_VALUE_BALANCE_STATE -> {
                if (update.hasCallbackQuery()) {
                    String data = update.getCallbackQuery().getData();
                    switch (data) {
                        case "BACK" -> {
                            userData.setBotState(getChatId(update), BotState.STUDENT_STATE);
                            User user = database.getUserRepository().findById(getChatId(update)).get();
                            int balance = user.getUserBalance();
                            return List.of(
                                    CustomMessage.createEditMessageText(
                                            update,
                                            "\uD83D\uDCB0 Баланс: " + balance + "₽",
                                            markupUtils.getMainStudentMarkup()
                                    )
                            );
                        }
                        default -> {
                            userData.setBotState(getChatId(update), BotState.STUDENT_STATE);
                            User user = database.getUserRepository().findById(getChatId(update)).get();
                            int balance = user.getUserBalance() + Integer.parseInt(update.getCallbackQuery().getData());
                            user.setUserBalance(balance);
                            database.getUserRepository().save(user);
                            return List.of(
                                    CustomMessage.createEditMessageText(
                                            update,
                                            "✅Пополнение прошло успешно✅\n\n\uD83D\uDCB0 Баланс: " + balance + "₽",
                                            markupUtils.getMainStudentMarkup()
                                    )
                            );
                        }
                    }
                }
            }
            case STUDENT_SUBJECT_STATE -> {
                if (update.hasCallbackQuery()) {
                    userData.setBotState(getChatId(update), BotState.STUDENT_TUTORS_LIST_STATE);
                    String category = update.getCallbackQuery().getData();
                    userData.setCategorySelect(getChatId(update), category);
                    return List.of(
                            CustomMessage.createEditMessageText(
                                    update,
                                    "➡\uFE0F Выберите репетитора",
                                    markupUtils.getTutorsListMarkup(category)
                            )
                    );
                }
            }
            case STUDENT_TUTORS_LIST_STATE -> {
                if (update.hasCallbackQuery()) {
                    String data = update.getCallbackQuery().getData();
                    switch (data) {
                        case "MAIN_MENU" -> {
                            userData.setBotState(getChatId(update), BotState.STUDENT_STATE);
                            User user = database.getUserRepository().findById(getChatId(update)).get();
                            int balance = user.getUserBalance();
                            return List.of(
                                    CustomMessage.createEditMessageText(
                                            update,
                                            "\uD83D\uDCB0 Баланс: " + balance + "₽",
                                            markupUtils.getMainStudentMarkup()
                                    )
                            );
                        }
                        default -> {
                            userData.setBotState(getChatId(update), BotState.STUDENT_TUTOR_PROFILE_STATE);
                            Tutor tutor = database.getTutorRepository().findById(Long.valueOf(data)).get();
                            userData.setTutorRegistration(getChatId(update), tutor.getTutorId());
                            return List.of(
                                    CustomMessage.createEditMessageText(
                                            update,
                                            tutor.getTutorName().toUpperCase() + "\n" +
                                                    tutor.getTutorDescription() + "\n" +
                                                    "\uD83D\uDCB0 Стоимость: " + tutor.getTutorPriceHour() + "/час\n" +
                                                    "\uD83C\uDF1F Оценка: " + tutor.getTutorReviewsScore() + "\uD83C\uDF1F (" +
                                                    tutor.getTutorReviewsCount() + ")\n",
                                            markupUtils.getTutorProfileMarkup()
                                    )
                            );
                        }
                    }
                }
            }
            case STUDENT_TUTOR_PROFILE_STATE -> {
                if (update.hasCallbackQuery()) {
                    String data = update.getCallbackQuery().getData();
                    switch (data) {
                        case "REGISTER" -> {
                            User user = database.getUserRepository().findById(getChatId(update)).get();
                            int balance = user.getUserBalance();
                            Tutor tutor = database.getTutorRepository().findById(userData.getTutorRegistration(getChatId(update))).get();
                            if (balance >= tutor.getTutorPriceHour()) {
                                userData.setBotState(getChatId(update), BotState.STUDENT_WAIT_TIME_STATE);
                                return List.of(
                                        CustomMessage.createEditMessageText(
                                                update,
                                                "➡\uFE0F Введите желаюмую дату и время",
                                                markupUtils.getClearMarkup()
                                        )
                                );
                            } else {
                                userData.setBotState(getChatId(update), BotState.STUDENT_STATE);
                                return List.of(
                                        CustomMessage.createEditMessageText(
                                                update,
                                                "❌Недостаточно средств❌\n\n\uD83D\uDCB0 Баланс: " + balance + "₽",
                                                markupUtils.getMainStudentMarkup()
                                        )
                                );
                            }
                        }
                        case "BACK" -> {
                            userData.setBotState(getChatId(update), BotState.STUDENT_TUTORS_LIST_STATE);
                            return List.of(
                                    CustomMessage.createEditMessageText(
                                            update,
                                            "➡\uFE0F Выберите репетитора",
                                            markupUtils.getTutorsListMarkup(userData.getCategorySelect(getChatId(update)))
                                    )
                            );
                        }
                    }
                }
            }
            case STUDENT_WAIT_TIME_STATE -> {
                if (update.hasMessage()) {
                    userData.setBotState(getChatId(update), BotState.STUDENT_WAIT_TUTOR_ANSWER_STATE);
                    return List.of(
                            CustomMessage.createSendMessage(
                                    update,
                                    "⏰ Ожидайте подтверждения репетитора",
                                    markupUtils.getClearMarkup()
                            ),
                            CustomMessage.createSendMessage(
                                    userData.getTutorRegistration(getChatId(update)),
                                    "\uD83D\uDCA0 Заявка на занятие от @" + update.getMessage().getFrom().getUserName() +
                                            "\nДата и время: " + update.getMessage().getText(),
                                    markupUtils.getAcceptDenyMarkup()
                            )
                    );
                }
            }
            case STUDENT_WAIT_TUTOR_ANSWER_STATE -> {
                if (update.hasCallbackQuery()) {
                    String data = update.getCallbackQuery().getData();
                    switch (data) {
                        case "ACCEPT_MONEY" -> {
                            userData.setBotState(getChatId(update), BotState.STUDENT_WAIT_REVIEW_STATE);
                            Tutor tutor = database.getTutorRepository().findById(userData.getTutorRegistration(getChatId(update))).get();
                            User userTutor = database.getUserRepository().findById(userData.getTutorRegistration(getChatId(update))).get();
                            userTutor.setUserBalance(userTutor.getUserBalance() + tutor.getTutorPriceHour());
                            database.getUserRepository().save(userTutor);
                            User userStudent = database.getUserRepository().findById(getChatId(update)).get();
                            userStudent.setUserBalance(userStudent.getUserBalance() - tutor.getTutorPriceHour());
                            database.getUserRepository().save(userStudent);
                            return List.of(
                                    CustomMessage.createEditMessageText(
                                            update,
                                            "\uD83C\uDF89 Поздравляю с успешным занятием!\nОцените работу репетитора",
                                            markupUtils.getReviewsMarkup()
                                    ),
                                    CustomMessage.createSendMessage(
                                            userData.getTutorRegistration(getChatId(update)),
                                            "Пользователь @" + userStudent.getTgName() + " подтвердил оплату✅",
                                            markupUtils.getClearMarkup()
                                    )
                            );
                        }
                        case "BACK" -> {
                            userData.setBotState(getChatId(update), BotState.STUDENT_TUTORS_LIST_STATE);
                            return List.of(
                                    CustomMessage.createEditMessageText(
                                            update,
                                            "➡\uFE0F Выберите репетитора",
                                            markupUtils.getTutorsListMarkup(userData.getCategorySelect(getChatId(update)))
                                    )
                            );
                        }
                    }
                }
            }
            case STUDENT_WAIT_REVIEW_STATE -> {
                if (update.hasCallbackQuery()) {
                    userData.setBotState(getChatId(update), BotState.STUDENT_STATE);
                    Tutor tutor = database.getTutorRepository().findById(userData.getTutorRegistration(getChatId(update))).get();
                    double newScore = (tutor.getTutorReviewsScore() * Double.parseDouble(tutor.getTutorReviewsCount().toString()) +
                            Double.parseDouble(update.getCallbackQuery().getData())) / (Double.parseDouble(tutor.getTutorReviewsCount().toString()) + 1.0);
                    tutor.setTutorReviewsScore(newScore);
                    tutor.setTutorReviewsCount(tutor.getTutorReviewsCount() + 1);
                    database.getTutorRepository().save(tutor);

                    User user = database.getUserRepository().findById(getChatId(update)).get();
                    int balance = user.getUserBalance();
                    return List.of(
                            CustomMessage.createEditMessageText(
                                    update,
                                    "\uD83D\uDCB0 Баланс: " + balance + "₽",
                                    markupUtils.getMainStudentMarkup()
                            )
                    );
                }
            }
        }
        return null;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.STUDENT_STATE;
    }
}
