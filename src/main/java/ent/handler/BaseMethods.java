package ent.handler;


import ent.Bot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.io.File;

import static java.lang.Math.toIntExact;

@Component
@RequiredArgsConstructor
public class BaseMethods {
    protected Message message;
    protected CallbackQuery callbackQuery;
    protected String mText;
    protected User user;
    protected Long chatId;
    protected final Bot bot;

    protected void prepare(Update update) {
        if (update.hasCallbackQuery()) message = update.getCallbackQuery().getMessage();
        else message = update.getMessage();
        callbackQuery = update.getCallbackQuery();
        mText = message.getText();
        chatId = message.getChatId();
        user = message.getFrom();
    }

    protected SendMessage msgObject(long chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId + "", text);
        sendMessage.enableHtml(true);
        return sendMessage;
    }

    protected EditMessageText eMsgObject(Long chatId, Update update, String text) {
        long message_id;
        if (update.hasCallbackQuery()) message_id = update.getCallbackQuery().getMessage().getMessageId();
        else message_id = update.getMessage().getMessageId();
        EditMessageText sendMessage = new EditMessageText();
        sendMessage.setText(text);
        sendMessage.setMessageId(toIntExact(message_id));
        sendMessage.setChatId(chatId.toString());
        sendMessage.enableHtml(true);
        return sendMessage;
    }

    protected SendPhoto ePhoto(Long chatId, String caption, String path) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(new InputFile(new File(path)));
        sendPhoto.setChatId(chatId.toString());
        sendPhoto.setCaption(caption);
        sendPhoto.setParseMode("HTML");
        return sendPhoto;
    }

    protected void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = msgObject(chatId, text);
        bot.executeMessage(sendMessage);
    }

    protected void sendMessage(Long chatId, String text, ReplyKeyboardMarkup markup) {
        SendMessage sendMessage = msgObject(chatId, text);
        sendMessage.setReplyMarkup(markup);
        bot.executeMessage(sendMessage);
    }

    protected void sendMessage(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage sendMessage = msgObject(chatId, text);
        sendMessage.setReplyMarkup(markup);
        bot.executeMessage(sendMessage);
    }

    protected void sendMessage(Long chatId, String text, ReplyKeyboardRemove markup) {
        SendMessage sendMessage = msgObject(chatId, text);
        sendMessage.setReplyMarkup(markup);
        bot.executeMessage(sendMessage);
    }
}
