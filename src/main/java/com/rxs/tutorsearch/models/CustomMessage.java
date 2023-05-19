package com.rxs.tutorsearch.models;

import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Data
public class CustomMessage {
    private String type;
    private Update update;
    private long answerChatId = -1;
    private String text;
    private InlineKeyboardMarkup inlineKeyboardMarkup;

    public static CustomMessage createSendMessage(long chatId, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        CustomMessage customMessage = new CustomMessage();
        customMessage.setType("send");
        customMessage.setAnswerChatId(chatId);
        customMessage.setText(text);
        customMessage.setInlineKeyboardMarkup(inlineKeyboardMarkup);
        return customMessage;
    }

    public static CustomMessage createSendMessage(Update update, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        CustomMessage customMessage = new CustomMessage();
        customMessage.setType("send");
        customMessage.setUpdate(update);
        customMessage.setText(text);
        customMessage.setInlineKeyboardMarkup(inlineKeyboardMarkup);
        return customMessage;
    }

    public static CustomMessage createEditMessageText(Update update, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        CustomMessage customMessage = new CustomMessage();
        customMessage.setType("edit");
        customMessage.setUpdate(update);
        customMessage.setText(text);
        customMessage.setInlineKeyboardMarkup(inlineKeyboardMarkup);
        return customMessage;
    }

    public SendMessage generateSendMessage() {
        SendMessage sendMessage = new SendMessage();
        if (answerChatId == -1) {
            sendMessage.setChatId(generateChatId(update));
        } else {
            sendMessage.setChatId(answerChatId);
        }
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    public EditMessageText generateEditMessageText() {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(generateChatId(update));
        editMessage.setMessageId(generateMessageId(update));
        editMessage.setText(text);
        editMessage.setReplyMarkup(inlineKeyboardMarkup);
        return editMessage;
    }


    private static long generateChatId(Update update) {
        try {
            return update.getMessage().getChatId();
        } catch (Exception e) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
    }

    private static int generateMessageId(Update update) {
        try {
            return update.getMessage().getMessageId();
        } catch (Exception e) {
            return update.getCallbackQuery().getMessage().getMessageId();
        }
    }
}
