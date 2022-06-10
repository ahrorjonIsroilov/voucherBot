package ent.button;


import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;
import java.util.List;

@Component
public class MarkupBoards {

    private final ReplyKeyboardMarkup board = new ReplyKeyboardMarkup();

    public ReplyKeyboardMarkup adminPanel() {
        KeyboardButton settings = new KeyboardButton("Sozlamalar ⚙️");
        KeyboardButton employees = new KeyboardButton("Xodimlar 👨🏻‍🔧");
        KeyboardButton sellers = new KeyboardButton("Sotuvchilar 🕵🏻‍♂️");
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add(settings);
        row2.add(employees);
        row2.add(sellers);
        board.setKeyboard(Arrays.asList(row1, row2));
        board.setResizeKeyboard(true);
        board.setSelective(true);
        return board;
    }

    public ReplyKeyboardMarkup adminSettingPanel() {
        KeyboardButton voucherSettings = new KeyboardButton("Admin tayinlash ➕");
        KeyboardButton addSeller = new KeyboardButton("Xodim qo'shing ➕");
        KeyboardButton registerEmployee = new KeyboardButton("Sotuvchi qo'shing ➕");
        KeyboardButton back = new KeyboardButton("Orqaga 🔙");
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        row1.add(voucherSettings);
        row3.add(back);
        row2.addAll(Arrays.asList(addSeller, registerEmployee));
        board.setKeyboard(Arrays.asList(row1, row2, row3, row4));
        board.setResizeKeyboard(true);
        board.setSelective(true);
        return board;
    }

    public ReplyKeyboardMarkup sellerPanel() {
        KeyboardButton sell = new KeyboardButton("Sotish 🔰");
        KeyboardRow row1 = new KeyboardRow();
        row1.add(sell);
        board.setKeyboard(List.of(row1));
        board.setResizeKeyboard(true);
        board.setSelective(true);
        return board;
    }

    public ReplyKeyboardMarkup back() {
        KeyboardButton back = new KeyboardButton("Orqaga 🔙");
        KeyboardRow row1 = new KeyboardRow();
        row1.add(back);
        board.setKeyboard(List.of(row1));
        board.setResizeKeyboard(true);
        board.setSelective(true);
        return board;
    }
}
