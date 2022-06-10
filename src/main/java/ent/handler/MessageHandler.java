package ent.handler;


import ent.button.InlineBoards;
import ent.button.MarkupBoards;
import ent.entity.Transaction;
import ent.entity.auth.AuthUser;
import ent.entity.auth.Role;
import ent.entity.auth.Session;
import ent.entity.auth.SessionUser;
import ent.enums.State;
import ent.enums.Stickers;
import ent.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Component
@EnableScheduling
public class MessageHandler extends AbstractHandler<UserService> implements IBaseHandler {

    private final BaseMethods a;
    private final MarkupBoards markup;
    private final InlineBoards inline;
    protected final Session sessions;

    public MessageHandler(BaseMethods a, UserService userService, MarkupBoards markup, InlineBoards inline, Session sessions) {
        super(userService);
        this.a = a;
        this.markup = markup;
        this.inline = inline;
        this.sessions = sessions;
    }

    @Override
    public void handle(Update update) {
        a.prepare(update);
        if (service.isRegistered(a.chatId)) {
            //.......................ADMIN.......................\\
            if (service.isAdmin(a.chatId)) {
                if (hasTextEquals(update, "/start")) {
                    a.sendMessage(a.chatId, "<b>Xush kelibsiz:)</b>", markup.adminPanel());
                    sessions.setState(State.DEFAULT, a.chatId);
                    return;
                }
                if (hasTextEquals(update, "/myid")) sendID(service.getByChatId(a.chatId));
                if (hasTextEquals(update, "/balance")) sendBalance(service.getByChatId(a.chatId));
                if (hasTextEquals(update, "Orqaga 🔙") && !sessions.checkState(State.DEFAULT, a.chatId)) {
                    sessions.setState(State.DEFAULT, a.chatId);
                    a.sendMessage(a.chatId, "<b>Asosiy menyu </b>🏡", markup.adminPanel());
                    return;
                }
                if (hasTextEquals(update, "Xodimlar 👨🏻‍🔧")) {
                    sessions.setPageZero(a.chatId);
                    Pageable pageable = PageRequest.of(sessions.findByChatId(a.chatId).getPage(), 10, Sort.by("name"));
                    a.sendMessage(a.chatId, "<b>Barcha xodimlar ro'yxati</b>", inline.userList(service.getAllByRole("user", pageable), sessions.findByChatId(a.chatId).getPage()));
                    return;
                }
                if (hasTextEquals(update, "Sotuvchilar 🕵🏻‍♂️")) {
                    sessions.setPageZero(a.chatId);
                    Pageable pageable = PageRequest.of(sessions.findByChatId(a.chatId).getPage(), 10, Sort.by("name"));
                    a.sendMessage(a.chatId, "<b>Barcha sotuvchilar ro'yxati</b>", inline.sellerList(service.getAllByRole("seller", pageable), sessions.findByChatId(a.chatId).getPage()));
                    return;
                }
                if (hasTextEquals(update, "Sozlamalar ⚙️")) {
                    sessions.setState(State.SETTINGS, a.chatId);
                    a.sendMessage(a.chatId, "<b>Sozlamalar </b>⚙️", markup.adminSettingPanel());
                    return;
                }
                if (hasTextEquals(update, "Admin tayinlash ➕")) {
                    sessions.setState(State.ADD_ADMIN, a.chatId);
                    a.sendMessage(a.chatId, "<b>Foydalanuvchi nomini kiriting</b>");
                } else if (sessions.checkState(State.ADD_ADMIN, a.chatId)) {
                    if (update.getMessage().hasText()) {
                        String username = update.getMessage().getText();
                        if (username.startsWith("@")) username = username.substring(1).toLowerCase(Locale.ROOT);
                        if (Objects.nonNull(service.getByUsername(username))) {
                            AuthUser authUser = service.getByUsername(username);
                            String role = authUser.getRole();
                            if (Objects.isNull(role)) role = "Tayinlanmagan";
                            a.sendMessage(a.chatId, String.format("🏷<b> Ism: <code>%s</code>\n👮🏻 Tg username: @%s\n🛡 Maqom: <code>%s</code></b>", authUser.getName(), authUser.getUsername(), role), inline.assignAdmin(authUser.getId()));
                            return;
                        } else {
                            a.sendMessage(a.chatId, "<b>Bunday foydalanuvchi nomiga ega shaxs topilmadi</b>");
                            sessions.setState(State.SETTINGS, a.chatId);
                        }
                    } else {
                        a.sendMessage(a.chatId, "<b>Iltimos to'g'ri ma'lumot kiriting</b>");
                    }
                }
                if (hasTextEquals(update, "Xodim qo'shing ➕") && sessions.checkState(State.SETTINGS, a.chatId)) {
                    sessions.setTempRole(Role.USER, a.chatId);
                    sessions.setState(State.ADD_EMPLOYEE, a.chatId);
                    a.sendMessage(a.chatId, "<b>Foydalanuvchi nomini kiriting:</b>", markup.back());
                } else if (sessions.checkState(State.ADD_EMPLOYEE, a.chatId)) {
                    if (update.getMessage().hasText()) {
                        String username = update.getMessage().getText();
                        if (username.startsWith("@")) username = username.substring(1).toLowerCase(Locale.ROOT);
                        if (Objects.nonNull(service.getByUsername(username)) && service.getByUsername(username).getRegistered() && !service.getByUsername(username).getBlocked()) {
                            a.sendMessage(a.chatId, "<b>Ushbu username bilan avval ro'yxatdan o'tilgan!</b>", markup.adminSettingPanel());
                            sessions.setState(State.SETTINGS, a.chatId);
                            return;
                        }
                        sessions.setTempUsername(username, a.chatId);
                        sessions.setState(State.ADD_PRICE, a.chatId);
                        a.sendMessage(a.chatId, "<b>Ushbu oy uchun vaucher qiymatini kiriting\nMasalan: <code>50000</code></b>");
                    } else {
                        a.sendMessage(a.chatId, "<b>Iltimos to'g'ri ma'lumot kiriting</b>");
                    }
                    return;
                } else if (sessions.checkState(State.ADD_PRICE, a.chatId)) {
                    if (update.getMessage().hasText()) {
                        String price = update.getMessage().getText();
                        if (validNumber(price)) {
                            Long priceLong = Long.parseLong(price);
                            sessions.setTempPrice(priceLong, a.chatId);
                            a.sendMessage(a.chatId, String.format("<b>Ushbu foydalanuvchini tasdiqlang @%s</b>", sessions.findByChatId(a.chatId).getTempUsername()), inline.yesNo());
                        } else {
                            a.sendMessage(a.chatId, "<b>Iltimos to'g'ri ma'lumot kiriting</b>");
                        }

                    } else {
                        a.sendMessage(a.chatId, "<b>Iltimos to'g'ri ma'lumot kiriting</b>");
                    }
                }
                if (hasTextEquals(update, "Sotuvchi qo'shing ➕") && sessions.checkState(State.SETTINGS, a.chatId)) {
                    sessions.setTempRole(Role.SELLER, a.chatId);
                    sessions.setState(State.ADD_SELLER, a.chatId);
                    a.sendMessage(a.chatId, "<b>Foydalanuvchi nomini kiriting:</b>", markup.back());
                } else if (sessions.checkState(State.ADD_SELLER, a.chatId)) {
                    String username = update.getMessage().getText();
                    if (username.startsWith("@")) username = username.substring(1).toLowerCase(Locale.ROOT);
                    if (Objects.nonNull(service.getByUsername(username)) && service.getByUsername(username).getRegistered() && !service.getByUsername(username).getBlocked()) {
                        a.sendMessage(a.chatId, "<b>Ushbu username bilan avval ro'yxatdan o'tilgan!</b>", markup.adminSettingPanel());
                        sessions.setState(State.SETTINGS, a.chatId);
                        return;
                    }
                    sessions.setTempUsername(username, a.chatId);
                    a.sendMessage(a.chatId, String.format("<b>Ushbu foydalanuvchini tasdiqlang @%s</b>", username), inline.yesNo());
                    return;
                }
                if (sessions.checkState(State.EDIT_MONTHLY_VOUCHER, a.chatId)) {
                    if (update.getMessage().hasText()) {
                        String voucher_amount = update.getMessage().getText();
                        if (validNumber(voucher_amount)) {
                            Long value = Long.parseLong(voucher_amount);
                            service.setUserBalanceLimit(value, sessions.findByChatId(a.chatId).getTempUsername());
                            a.sendMessage(a.chatId, "<b>O'zgarishlar keyingi oy uchun sozlandi</b>", markup.adminPanel());
                            sessions.setState(State.DEFAULT, a.chatId);
                        } else {
                            a.sendMessage(a.chatId, "<b>Iltimos to'g'ri ma'lumot kiriting</b>");
                        }
                    } else {
                        a.sendMessage(a.chatId, "<b>Iltimos to'g'ri ma'lumot kiriting</b>");
                    }
                }
                if (hasTextEquals(update, "/admins") && service.isOwner(a.chatId)) {
                    sessions.setPageZero(a.chatId);
                    Pageable pageable = PageRequest.of(sessions.findByChatId(a.chatId).getPage(), 10, Sort.by("name"));
                    a.sendMessage(a.chatId, "<b>Adminlar</b>", inline.adminList(service.getAllByRole(Role.ADMIN.getCode(), pageable)));
                    return;
                }
                if (hasTextEquals(update, "/transactions") && service.isOwner(a.chatId)) {
                    a.sendMessage(a.chatId, "<b>Oraliqni tanlang</b>", inline.transactionTime());

                }
            }
            //.......................SELLER.......................\\
            else if (service.isSeller(a.chatId)) {
                if (hasTextEquals(update, "/start")) {
                    a.sendMessage(a.chatId, "<b>Xush kelibsiz:)</b>", markup.sellerPanel());
                    sessions.setState(State.DEFAULT, a.chatId);
                    return;
                }
                if (hasTextEquals(update, "/myid")) {
                    sendID(service.getByChatId(a.chatId));
                    return;
                }
                if (hasTextEquals(update, "/balance")) {
                    sendBalance(service.getByChatId(a.chatId));
                    return;
                }
                if (hasTextEquals(update, "Orqaga 🔙")) {
                    sessions.setState(State.DEFAULT, a.chatId);
                    a.sendMessage(a.chatId, "<b>Asosiy menyu </b>🏡", markup.sellerPanel());
                    return;
                }
                if (hasTextEquals(update, "Sotish 🔰")) {
                    sessions.setState(State.INSERT_EMPLOYEE_ID, a.chatId);
                    a.sendMessage(a.chatId, "<b>Foydalanuvchi IDsini kiriting:</b>", new ReplyKeyboardRemove(true));
                } else if (sessions.checkState(State.INSERT_EMPLOYEE_ID, a.chatId)) {
                    if (update.getMessage().hasText()) {
                        String id = update.getMessage().getText().toUpperCase(Locale.ROOT);
                        sessions.setTempUsername(id, a.chatId);
                        if (Objects.nonNull(service.getById(id)) && service.getById(id).getRole().equalsIgnoreCase(Role.USER.getCode())) {
                            a.sendMessage(a.chatId, "<b>Yechib olinadigan pul miqdorini kiriting</b>");
                            sessions.setState(State.INSERT_AMOUNT, a.chatId);
                        } else {
                            a.sendMessage(a.chatId, "<b>Ushbu IDga ega foydalanuvchi mavjud emas!</b>", markup.sellerPanel());
                        }
                    } else {
                        a.sendMessage(a.chatId, "<b>Iltimos to'g'ri ma'lumot kiriting</b>");
                    }
                } else if (sessions.checkState(State.INSERT_AMOUNT, a.chatId)) {
                    if (update.getMessage().hasText()) {
                        String amountString = update.getMessage().getText();
                        if (validNumber(amountString)) {
                            NumberFormat format = NumberFormat.getInstance(new Locale("en", "US"));
                            AuthUser byId = service.getById(sessions.findByChatId(a.chatId).getTempUsername());
                            Long amount = Long.parseLong(amountString);
                            sessions.setTempPrice(amount, a.chatId);
                            Transaction t = Transaction.builder().amount(amount).EmployeeId(byId.getId()).EmployeeName(byId.getName()).sellerName(service.getByChatId(a.chatId).getName()).sellerId(service.getByChatId(a.chatId).getId()).transactionTime(Date.from(Instant.now())).build();
                            a.sendMessage(a.chatId, String.format("🆔 <b>ID: </b><code>%s</code>\n🏷 <b>Ism: </b><code>%s</code>\n🚹 <b>Tg: </b>@%s\n\n💳 <b>Yechib olish miqdori: </b><code>%s so'm</code>\n\n❗️ <b>Diqqat:</b> <i>Ushbu ma'lumotlar to'g'riligiga ishonch hosil qiling. Tranzaksiyani ortga qaytarib bo'lmaydi.</i>", sessions.findByChatId(a.chatId).getTempUsername(), byId.getName(), byId.getUsername(), format.format(sessions.findByChatId(a.chatId).getTempPrice())), inline.doTransaction(t));
                        }
                    }
                } else {
                    a.sendMessage(a.chatId, "<b>Iltimos to'g'ri ma'lumot kiriting</b>");
                }
            }
            //.......................EMPLOYEE.......................\\
            else if (service.isUser(a.chatId)) {
                if (hasTextEquals(update, "/start")) {
                    a.sendMessage(a.chatId, "<b>Xush kelibsiz:)</b>", new ReplyKeyboardRemove(true));
                    sessions.setState(State.DEFAULT, a.chatId);
                } else if (hasTextEquals(update, "/myid")) {
                    sendID(service.getByChatId(a.chatId));
                } else if (hasTextEquals(update, "/balance")) sendBalance(service.getByChatId(a.chatId));
            }
        } else if (Objects.isNull(update.getMessage().getFrom().getUserName())) {
            SendSticker sticker = new SendSticker();
            sticker.setChatId(a.chatId.toString());
            sticker.setSticker(new InputFile(Stickers.WARNING.getFileId()));
            a.bot.sendSticker(sticker);
            a.sendMessage(a.chatId, "<b>Kechirasiz! Botni faqat telegram foydalanuvchi nomiga ega insonlar ishlatishlari mumkin 😕</b>");
        } else if (Objects.nonNull(service.getByUsername(update.getMessage().getFrom().getUserName()))) {
            if (!service.getByUsername(update.getMessage().getFrom().getUserName()).getBlocked()) {
                service.register(update.getMessage().getFrom());
                SendSticker sticker = new SendSticker();
                sticker.setChatId(a.chatId.toString());
                sticker.setSticker(new InputFile(Stickers.REGISTERED.getFileId()));
                a.bot.sendSticker(sticker);
                a.sendMessage(a.chatId, String.format("<b>Botdan foydalanishingiz mumkin</b>\n\n<i>Botdagi maqomingiz: %s</i>", service.getByUsername(update.getMessage().getFrom().getUserName()).getRole()));
            } else {
                a.sendMessage(a.chatId, "<b>Kechirasiz botdan vaqtincha foydalana olmaysiz.\nBloklangansiz</b>");
            }
        } else {
            AuthUser authUser = AuthUser.builder().username(update.getMessage().getFrom().getUserName().toLowerCase(Locale.ROOT)).name(update.getMessage().getFrom().getFirstName()).chatId(a.chatId).balance(0L).balanceLimit(0L).blocked(false).registered(false).state("default").page(0).build();
            service.save(authUser);
            a.sendMessage(a.chatId, "<b>❗️Admin sizni ro'yxatdan o'tkazmagan. Ro'yxatdan o'tishingiz bilan botdan foydalana olasiz</b>");
        }
    }

    private void sendID(AuthUser user) {
        String id = sessions.findByChatId(a.chatId).getId();
        a.sendMessage(user.getChatId(), String.format("<b>Sizning shaxsiy ID ma'lumotingiz: </b><code>%s</code>", id));
    }

    private void sendBalance(AuthUser user) {
        if (user.getRole().equalsIgnoreCase(Role.USER.getCode())) {
            NumberFormat format = NumberFormat.getInstance(new Locale("en", "US"));
            a.sendMessage(user.getChatId(), String.format("<b>Hisobingzdagi qolgan mablag' <code>%s</code> so'm</b>", format.format(user.getBalance())));
        } else {
            a.sendMessage(user.getChatId(), "<b>Ushbu buyruqni faqat xodimlar ishlatishlari mumkin</b>");
        }
    }

    private Optional<SessionUser> prepareSession(AuthUser authUser) {
        return Optional.of(SessionUser.builder().role(authUser.getRole()).state(authUser.getState()).chatId(authUser.getChatId()).page(authUser.getPage()).build());
    }

    private boolean hasTextEquals(Update update, String text) {
        return update.getMessage().hasText() && update.getMessage().getText().equals(text);
    }

    private Boolean validNumber(String input) {
        try {
            long output = Long.parseLong(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
