package com.rxs.tutorsearch.utils;

import com.rxs.tutorsearch.database.Database;
import com.rxs.tutorsearch.database.models.Tutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MarkupUtils {
    private final Database database;

    public MarkupUtils(Database database) {
        this.database = database;
    }

    public InlineKeyboardMarkup getChooseRoleMarkup() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton studentBtn = InlineKeyboardButton.builder()
                .text("Я клиент")
                .callbackData("ROLE_STUDENT")
                .build();
        rows.add(List.of(studentBtn));

        InlineKeyboardButton tutorBtn = InlineKeyboardButton.builder()
                .text("Я репетитор")
                .callbackData("ROLE_TUTOR")
                .build();
        rows.add(List.of(tutorBtn));

        InlineKeyboardButton adminBtn = InlineKeyboardButton.builder()
                .text("Я администратор")
                .callbackData("ROLE_ADMIN")
                .build();
        rows.add(List.of(adminBtn));

        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getCategoriesMarkup() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> oneRow = new ArrayList<>();
        List<String> categories = Database.CATEGORIES_NAMES;
        for (int i = 0; i < categories.size(); i++) {
            if (i % 3 == 0) {
                rows.add(oneRow);
                oneRow = new ArrayList<>();
            }
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(categories.get(i));
            button.setCallbackData(categories.get(i));
            oneRow.add(button);
        }
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getPriceMarkup() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> oneRow = new ArrayList<>();
        int price = 500;
        while (price <= 5000) {
            if (price % 1000 == 0) {
                rows.add(oneRow);
                oneRow = new ArrayList<>();
            }
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf(price));
            button.setCallbackData(String.valueOf(price));
            oneRow.add(button);
            price += 500;
        }
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getPriceMarkupWithBack() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> oneRow = new ArrayList<>();
        int price = 500;
        while (price <= 5000) {
            if (price % 1000 == 0) {
                rows.add(oneRow);
                oneRow = new ArrayList<>();
            }
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf(price));
            button.setCallbackData(String.valueOf(price));
            oneRow.add(button);
            price += 500;
        }
        InlineKeyboardButton backBtn = InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("BACK")
                .build();
        rows.add(List.of(backBtn));
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getMainTutorMarkup() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton updateBtn = InlineKeyboardButton.builder()
                .text("Обновить баланс")
                .callbackData("UPDATE")
                .build();
        rows.add(List.of(updateBtn));

        InlineKeyboardButton balanceBtn = InlineKeyboardButton.builder()
                .text("Вывести деньги")
                .callbackData("BALANCE")
                .build();
        rows.add(List.of(balanceBtn));

        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getMainStudentMarkup() {
        InlineKeyboardButton findBtn = InlineKeyboardButton.builder()
                .text("Найти репетитора")
                .callbackData("FIND_TUTOR")
                .build();

        InlineKeyboardButton balanceBtn = InlineKeyboardButton.builder()
                .text("Пополнить баланс")
                .callbackData("BALANCE")
                .build();

        return new InlineKeyboardMarkup(List.of(List.of(findBtn), List.of(balanceBtn)));
    }

    public InlineKeyboardMarkup getTutorsListMarkup(String category) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        String tutorIdsString = Optional.ofNullable(database.getCategoryRepository().findById(category).get().getTutorIds()).orElse("");
        if (!tutorIdsString.equals("")) {
            List<Long> tutorIds = Arrays.stream(tutorIdsString.split(", "))
                    .map(Long::parseLong)
                    .toList();
            for (long tutorId : tutorIds) {
                Optional<Tutor> checkTutor = database.getTutorRepository().findById(tutorId);
                Tutor tutor = checkTutor.orElse(null);
                if (tutor != null) {
                    InlineKeyboardButton tutorBtn = new InlineKeyboardButton();
                    tutorBtn.setText(tutor.getTutorName() + " I " +
                            tutor.getTutorPriceHour() + "₽/час I " +
                            tutor.getTutorReviewsScore() + "\uD83C\uDF1F(" +
                            tutor.getTutorReviewsCount() + ")");
                    tutorBtn.setCallbackData(String.valueOf(tutorId));
                    rows.add(List.of(tutorBtn));
                }
            }
        }

        InlineKeyboardButton backBtn = InlineKeyboardButton.builder()
                .text("Главное меню")
                .callbackData("MAIN_MENU")
                .build();
        rows.add(List.of(backBtn));

        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getTutorProfileMarkup() {
        InlineKeyboardButton registerBtn = InlineKeyboardButton.builder()
                .text("Записаться на занятие")
                .callbackData("REGISTER")
                .build();
        InlineKeyboardButton backBtn = InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("BACK")
                .build();
        return new InlineKeyboardMarkup(List.of(List.of(registerBtn), List.of(backBtn)));
    }

    public InlineKeyboardMarkup getBackMarkup() {
        InlineKeyboardButton backBtn = InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("BACK")
                .build();
        return new InlineKeyboardMarkup(List.of(List.of(backBtn)));
    }

    public InlineKeyboardMarkup getAcceptMoneyMarkup() {
        InlineKeyboardButton acceptMoneyBtn = InlineKeyboardButton.builder()
                .text("Подтвердить оплату✅")
                .callbackData("ACCEPT_MONEY")
                .build();
        return new InlineKeyboardMarkup(List.of(List.of(acceptMoneyBtn)));
    }

    public InlineKeyboardMarkup getReviewsMarkup() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            InlineKeyboardButton btn = InlineKeyboardButton.builder()
                    .text(String.valueOf(i))
                    .callbackData(String.valueOf(i))
                    .build();
            rows.add(List.of(btn));
        }
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getBadTutorMarkup() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> oneRow = new ArrayList<>();

        InlineKeyboardButton badBtn = new InlineKeyboardButton();
        badBtn.setText("Перезапустить бота");
        badBtn.setCallbackData("RESTART");
        oneRow.add(badBtn);
        rows.add(oneRow);

        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getAcceptDenyMarkup() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> oneRow = new ArrayList<>();

        InlineKeyboardButton acceptBtn = new InlineKeyboardButton();
        acceptBtn.setText("Подтвердить✅");
        acceptBtn.setCallbackData("ACCEPT");
        oneRow.add(acceptBtn);

        InlineKeyboardButton cancelBtn = new InlineKeyboardButton();
        cancelBtn.setText("Отклонить❌");
        cancelBtn.setCallbackData("DENY");
        oneRow.add(cancelBtn);

        rows.add(oneRow);

        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup getClearMarkup() {
        return new InlineKeyboardMarkup(new ArrayList<>());
    }
}
