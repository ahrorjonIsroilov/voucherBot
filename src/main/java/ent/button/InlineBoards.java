package ent.button;

import ent.entity.Transaction;
import ent.entity.auth.AuthUser;
import ent.entity.auth.Role;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InlineBoards {
    private final InlineKeyboardMarkup board = new InlineKeyboardMarkup();

    public InlineKeyboardMarkup yesNo() {
        InlineKeyboardButton send = new InlineKeyboardButton("Confirm ✅");
        send.setCallbackData("accept");
        InlineKeyboardButton decline = new InlineKeyboardButton("Cancel ❌");
        decline.setCallbackData("decline");
        board.setKeyboard(Collections.singletonList(getRow(send, decline)));
        return board;
    }

    public InlineKeyboardMarkup assignAdmin(String id) {
        InlineKeyboardButton send = new InlineKeyboardButton("Admin etib tayinlash 🎖");
        send.setCallbackData("assignA." + id);
        board.setKeyboard(Arrays.asList(getRow(send), getRow(close())));
        return board;
    }

    public InlineKeyboardMarkup doTransaction(Transaction t) {
        InlineKeyboardButton transactionButton = new InlineKeyboardButton("Tranzaksiyani bajarish ✅");
        transactionButton.setCallbackData("transaction." + t.getEmployeeId() + "." + t.getSellerId() + "." + t.getAmount());
        InlineKeyboardButton abort = new InlineKeyboardButton("Bekor qilish ✖️");
        abort.setCallbackData("abort_transaction");
        board.setKeyboard(Arrays.asList(getRow(transactionButton), getRow(abort)));
        return board;
    }

    public InlineKeyboardMarkup manageUserBalance(AuthUser user) {
        InlineKeyboardButton changePrice = new InlineKeyboardButton("Vaucherni tahrirlash 🎁");
        changePrice.setCallbackData("balance." + user.getId());
        InlineKeyboardButton close = new InlineKeyboardButton("✖️");
        close.setCallbackData("close");
        InlineKeyboardButton block = new InlineKeyboardButton();
        if (user.getBlocked()) {
            block.setText("Blokdan chiqarish 🔓");
            block.setCallbackData("unblock." + user.getId());
        } else {
            block.setText("Bloklash 🚫");
            block.setCallbackData("block." + user.getId());
        }
        if (user.getRole().equalsIgnoreCase(Role.USER.getCode()))
            board.setKeyboard(List.of(getRow(changePrice), getRow(block), getRow(close)));
        else if (user.getRole().equalsIgnoreCase(Role.SELLER.getCode()))
            board.setKeyboard(List.of(getRow(block), getRow(close)));
        return board;
    }

    public InlineKeyboardMarkup transactionTime() {
        InlineKeyboardButton oneDay = new InlineKeyboardButton("1 kun");
        oneDay.setCallbackData("transactionTime." + 1);
        InlineKeyboardButton oneWeek = new InlineKeyboardButton("1 hafta");
        oneWeek.setCallbackData("transactionTime." + 7);
        InlineKeyboardButton twoWeek = new InlineKeyboardButton("2 hafta");
        twoWeek.setCallbackData("transactionTime." + 14);
        InlineKeyboardButton oneMonth = new InlineKeyboardButton("1 oy");
        oneMonth.setCallbackData("transactionTime." + 30);
        board.setKeyboard(List.of(getRow(oneDay, oneWeek), getRow(twoWeek, oneMonth), getRow(close())));
        return board;
    }

    public InlineKeyboardMarkup manageAdmin(AuthUser user) {
        InlineKeyboardButton removeAdmin = new InlineKeyboardButton("Adminni o'chirish ➖");
        removeAdmin.setCallbackData("removeA." + user.getId());
        InlineKeyboardButton close = new InlineKeyboardButton("✖️");
        close.setCallbackData("close");
        InlineKeyboardButton block = new InlineKeyboardButton();
        if (user.getBlocked()) {
            block.setText("Blokdan chiqarish 🔓");
            block.setCallbackData("unblock." + user.getId());
        } else {
            block.setText("Bloklash 🚫");
            block.setCallbackData("block." + user.getId());
        }
        board.setKeyboard(List.of(getRow(removeAdmin, block), getRow(close)));
        return board;
    }

    public InlineKeyboardMarkup userList(List<AuthUser> userList, Integer userPage) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (AuthUser user : userList) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            if (!user.getBlocked()) btn.setText(user.getName() + " ✅");
            else btn.setText(user.getName() + " 🚫");
            btn.setCallbackData("user." + user.getId());
            buttons.add(btn);
        }
        board.setKeyboard(prepareButtons(buttons, ".user", userPage));
        return board;
    }

    public InlineKeyboardMarkup sellerList(List<AuthUser> sellerList, Integer userPage) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (AuthUser user : sellerList) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            if (!user.getBlocked()) btn.setText(user.getName() + " ✅");
            else btn.setText(user.getName() + " 🚫");
            btn.setCallbackData("user." + user.getId());
            buttons.add(btn);
        }
        board.setKeyboard(prepareButtons(buttons, ".seller", userPage));
        return board;
    }

    public InlineKeyboardMarkup adminList(List<AuthUser> users) {
        List<InlineKeyboardButton> inlineButtons = new ArrayList<>();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (AuthUser user : users) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            if (!user.getBlocked()) btn.setText(user.getName() + " ✅");
            else btn.setText(user.getName() + " 🚫");
            btn.setCallbackData("admin." + user.getId());
            inlineButtons.add(btn);
        }
        for (int i = 0; i < inlineButtons.size(); i += 2) {
            if (i + 1 < inlineButtons.size()) {
                buttons.add(getRow(inlineButtons.get(i), inlineButtons.get(i + 1)));
            } else {
                buttons.add(getRow(inlineButtons.get(i)));
            }
        }
        buttons.add(List.of(close()));
        board.setKeyboard(buttons);
        return board;
    }

    private List<List<InlineKeyboardButton>> prepareButtons(List<InlineKeyboardButton> input, String mark, Integer userPage) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (int i = 0; i < input.size(); i += 2) {
            if (i + 1 < input.size()) {
                buttons.add(getRow(input.get(i), input.get(i + 1)));
            } else {
                buttons.add(getRow(input.get(i)));
            }
        }
        if (input.size() < 10 && userPage > 0) buttons.add(prevX(mark));
        else if (userPage < 1) buttons.add(nextX(mark));
        else buttons.add(prevXNext(mark));
        return buttons;
    }

    public List<InlineKeyboardButton> prevX(String mark) {
        InlineKeyboardButton previous = new InlineKeyboardButton("⬅️");
        previous.setCallbackData("previous" + mark);
        InlineKeyboardButton close = new InlineKeyboardButton("✖️");
        close.setCallbackData("close");
        return new ArrayList<>(getRow(previous, close));
    }

    public List<InlineKeyboardButton> nextX(String mark) {
        InlineKeyboardButton next = new InlineKeyboardButton("➡️");
        next.setCallbackData("next" + mark);
        InlineKeyboardButton close = new InlineKeyboardButton("✖️");
        close.setCallbackData("close");
        return new ArrayList<>(getRow(close, next));
    }

    public InlineKeyboardButton close() {
        InlineKeyboardButton close = new InlineKeyboardButton("✖️");
        close.setCallbackData("close");
        return close;
    }

    public List<InlineKeyboardButton> prevXNext(String mark) {
        InlineKeyboardButton previous = new InlineKeyboardButton("⬅️");
        previous.setCallbackData("previous" + mark);
        InlineKeyboardButton close = new InlineKeyboardButton("✖️");
        close.setCallbackData("close");
        InlineKeyboardButton next = new InlineKeyboardButton("➡️");
        next.setCallbackData("next" + mark);
        return new ArrayList<>(getRow(previous, close, next));
    }

    private List<InlineKeyboardButton> getRow(InlineKeyboardButton... buttons) {
        return Arrays.stream(buttons).collect(Collectors.toList());
    }
}
