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
import ent.service.ExcelExporterService;
import ent.service.TransactionService;
import ent.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class CallBackHandler extends AbstractHandler<UserService> implements IBaseHandler {
    private final BaseMethods a;
    private final MarkupBoards markup;
    private final InlineBoards inline;
    protected final MessageHandler messageHandler;
    protected final Session sessions;
    private final TransactionService tService;

    public CallBackHandler(UserService userService, BaseMethods a, MarkupBoards markup, InlineBoards inline, MessageHandler messageHandler, Session sessions, TransactionService tService) {
        super(userService);
        this.a = a;
        this.markup = markup;
        this.inline = inline;
        this.messageHandler = messageHandler;
        this.sessions = sessions;
        this.tService = tService;
    }

    @Override
    public void handle(Update update) {
        a.prepare(update);
        String data = update.getCallbackQuery().getData();
        if ("accept".equals(data)) {
            if (Objects.isNull(service.getByUsername(sessions.findByChatId(a.chatId).getTempUsername()))) {
                SendSticker sticker = new SendSticker();
                sticker.setChatId(a.chatId.toString());
                sticker.setReplyMarkup(markup.adminSettingPanel());
                sticker.setSticker(new InputFile(Stickers.REGISTERED.getFileId()));
                a.bot.sendSticker(sticker);
                service.save(AuthUser.builder().username(sessions.findByChatId(a.chatId).getTempUsername().toLowerCase(Locale.ROOT)).role(sessions.findByChatId(a.chatId).getTempRole().getCode()).balance(sessions.findByChatId(a.chatId).getTempPrice()).balanceLimit(sessions.findByChatId(a.chatId).getTempPrice()).registered(false).blocked(false).page(0).state(State.DEFAULT.getCode()).build());
                a.bot.executeMessage(new DeleteMessage(a.chatId.toString(), update.getCallbackQuery().getMessage().getMessageId()));
                sessions.setState(State.SETTINGS, a.chatId);
                SendMessage sendMessage = a.msgObject(a.chatId, "<b>Foydalanuvchi muvaffaqiyatli qo'shildi!</b>");
                sendMessage.setReplyMarkup(markup.adminSettingPanel());
                a.bot.executeMessage(sendMessage);
            } else {
                service.register(sessions.findByChatId(a.chatId).getTempRole().getCode(), sessions.findByChatId(a.chatId).getTempUsername().toLowerCase(Locale.ROOT), sessions.findByChatId(a.chatId).getTempPrice());
                Long chatId = service.getByUsername(sessions.findByChatId(a.chatId).getTempUsername()).getChatId();
                a.bot.executeMessage(new DeleteMessage(a.chatId.toString(), update.getCallbackQuery().getMessage().getMessageId()));
                sessions.setState(State.SETTINGS, a.chatId);
                SendMessage sendMessage = a.msgObject(a.chatId, "<b>Foydalanuvchi muvaffaqiyatli qo'shildi!</b>");
                sendMessage.setReplyMarkup(markup.adminSettingPanel());
                SendSticker sticker = new SendSticker();
                sticker.setChatId(chatId.toString());
                sticker.setSticker(new InputFile(Stickers.ACCEPTED.getFileId()));
                a.bot.sendSticker(sticker);
                SendMessage sendMessage1 = a.msgObject(chatId, "<b>Admin sizni ro'yxatga qo'shdi!\nBotdan foydalanishingiz mumkin</b>");
                a.bot.executeMessage(sendMessage);
                a.bot.executeMessage(sendMessage1);
            }
        } else if ("decline".equals(data)) {
            a.bot.executeMessage(new DeleteMessage(a.chatId.toString(), update.getCallbackQuery().getMessage().getMessageId()));
            SendMessage sendMessage = a.msgObject(a.chatId, "<b>O'zgarishlar bekor qilindi</b>");
            sendMessage.setReplyMarkup(markup.adminSettingPanel());
            a.bot.executeMessage(sendMessage);
            sessions.setState(State.SETTINGS, a.chatId);
        } else if (data.startsWith("previous")) {
            if (data.endsWith("user")) {
                sessions.previousPage(a.chatId);
                EditMessageText e = new EditMessageText();
                e.setChatId(a.chatId.toString());
                e.setText(update.getCallbackQuery().getMessage().getText());
                e.setEntities(update.getCallbackQuery().getMessage().getEntities());
                e.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                Pageable pageable = PageRequest.of(sessions.findByChatId(a.chatId).getPage(), 10, Sort.by("name"));
                e.setReplyMarkup(inline.userList(service.getAllByRole("user", pageable), sessions.findByChatId(a.chatId).getPage()));
                a.bot.executeMessage(e);
            } else if (data.endsWith("seller")) {
                sessions.previousPage(a.chatId);
                EditMessageText e = new EditMessageText();
                e.setChatId(a.chatId.toString());
                e.setText(update.getCallbackQuery().getMessage().getText());
                e.setEntities(update.getCallbackQuery().getMessage().getEntities());
                e.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                Pageable pageable = PageRequest.of(sessions.findByChatId(a.chatId).getPage(), 10, Sort.by("name"));
                e.setReplyMarkup(inline.sellerList(service.getAllByRole("seller", pageable), sessions.findByChatId(a.chatId).getPage()));
                a.bot.executeMessage(e);
            }
        } else if (data.startsWith("next")) {
            if (data.endsWith("user")) {
                sessions.nextPage(a.chatId);
                EditMessageText e = new EditMessageText();
                e.setText(update.getCallbackQuery().getMessage().getText());
                e.setEntities(update.getCallbackQuery().getMessage().getEntities());
                e.setChatId(a.chatId.toString());
                e.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                Pageable pageable = PageRequest.of(sessions.findByChatId(a.chatId).getPage(), 10, Sort.by("name"));
                e.setReplyMarkup(inline.userList(service.getAllByRole("user", pageable), sessions.findByChatId(a.chatId).getPage()));
                a.bot.executeMessage(e);
            } else if (data.endsWith("seller")) {
                sessions.nextPage(a.chatId);
                EditMessageText e = new EditMessageText();
                e.setText(update.getCallbackQuery().getMessage().getText());
                e.setEntities(update.getCallbackQuery().getMessage().getEntities());
                e.setChatId(a.chatId.toString());
                e.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                Pageable pageable = PageRequest.of(sessions.findByChatId(a.chatId).getPage(), 10, Sort.by("name"));
                e.setReplyMarkup(inline.sellerList(service.getAllByRole("seller", pageable), sessions.findByChatId(a.chatId).getPage()));
                a.bot.executeMessage(e);
            }
        } else if ("close".equals(data)) {
            a.bot.executeMessage(new DeleteMessage(a.chatId.toString(), update.getCallbackQuery().getMessage().getMessageId()));
        } else if (data.startsWith("user.")) {
            NumberFormat format = NumberFormat.getInstance(new Locale("en", "US"));
            String userId = data.split("\\.")[1];
            AuthUser user = service.getById(userId);
            SendMessage sendMessage = new SendMessage();
            if (user.getRole().equalsIgnoreCase(Role.USER.getCode()))
                sendMessage = a.msgObject(a.chatId, String.format("🏷<b> Ism: <code>%s</code>\n👮🏻 Tg username: @%s\n🛡 Maqom: <code>%s</code>\n————————————————\n🎁 Vaucher: <code>%s so'm(oylik)</code>\n🗃 Qoldiq:<code> %s so'm</code></b>", user.getName(), user.getUsername(), user.getRole(), format.format(user.getBalanceLimit()), format.format(user.getBalance())));
            else if (user.getRole().equalsIgnoreCase(Role.SELLER.getCode()))
                sendMessage = a.msgObject(a.chatId, String.format("🏷<b> Ism: <code>%s</code>\n👮🏻 Tg username: @%s\n🛡 Maqom: <code>%s</code></b>", user.getName(), user.getUsername(), user.getRole()));
            sendMessage.setReplyMarkup(inline.manageUserBalance(user));
            a.bot.executeMessage(sendMessage);
        } else if (data.startsWith("admin.")) {
            String userId = data.split("\\.")[1];
            AuthUser user = service.getById(userId);
            if (Objects.isNull(user)) {
                a.sendMessage(a.chatId, "<b>Bu foydalanuvchi mavjud emas</b>");
                return;
            }
            String role = user.getRole();
            if (Objects.isNull(role)) role = "Tayinlanmagan";
            a.sendMessage(a.chatId, String.format("🏷<b> Ism: <code>%s</code>\n👮🏻 Tg username: @%s\n🛡 Maqom: <code>%s</code></b>", user.getName(), user.getUsername(), role), inline.manageAdmin(user));
        } else if (data.startsWith("balance.")) {
            String userId = data.split("\\.")[1];
            sessions.setTempUsername(userId, a.chatId);
            SendMessage sendMessage = a.msgObject(a.chatId, "<b>Vaucher qiymatini kiriting\n<code>Masalan: 55000</code></b>");
            sendMessage.setReplyMarkup(markup.back());
            a.bot.executeMessage(sendMessage);
            sessions.setState(State.EDIT_MONTHLY_VOUCHER, a.chatId);
        } else if (data.startsWith("assignA.")) {
            String id = data.split("\\.")[1];
            service.assignAdmin(id);
            a.bot.executeMessage(new DeleteMessage(a.chatId.toString(), update.getCallbackQuery().getMessage().getMessageId()));
            SendMessage sendMessage = a.msgObject(a.chatId, "<b>Admin tayinlandi!</b>");
            sessions.setState(State.SETTINGS, a.chatId);
            a.bot.executeMessage(sendMessage);
            SendMessage message = a.msgObject(service.getById(id).getChatId(), "🔰 <b>Siz admin etib tayinlandingiz!\nIltimos botga qayta /start bering</b>");
            a.bot.executeMessage(message);
            SessionUser sessionUser = sessions.findByChatId(service.getById(id).getChatId());
            if (Objects.nonNull(sessionUser)) {
                sessionUser.setRole(Role.ADMIN.getCode());
                sessions.setSession(sessionUser.getChatId(), Optional.of(sessionUser));
            }
        } else if (data.startsWith("block.")) {
            String id = data.split("\\.")[1];
            service.blockUser(id);
            sessions.removeSession(service.getById(id).getChatId());
            EditMessageText e = editMessage(update, id);
            a.bot.executeMessage(e);
            a.sendMessage(service.getById(id).getChatId(), "<b>Bloklandingiz❗️</b>", new ReplyKeyboardRemove(true));
        } else if (data.startsWith("unblock.")) {
            String id = data.split("\\.")[1];
            service.unblockUser(id);
            EditMessageText e = editMessage(update, id);
            a.bot.executeMessage(e);
            a.sendMessage(service.getById(id).getChatId(), "<b>Blokdan chiqarildingiz ✅</b>\n<i>Botni qayta ishga tushiring /start</i>", new ReplyKeyboardRemove(true));
        } else if (data.startsWith("removeA.")) {
            String id = data.split("\\.")[1];
            AuthUser user = service.getById(id);
            service.removeAdminById(id);
            sessions.removeSession(user.getChatId());
            sendWarning(user.getChatId());
            a.sendMessage(user.getChatId(), "<b>Siz adminlar qatoridan o'chirib tashlandingiz. Botdan ortiq foydalana olmaysiz</b>", new ReplyKeyboardRemove(true));
            a.sendMessage(a.chatId, "<b>Admin olib tashlandi!</b>");
            a.bot.executeMessage(new DeleteMessage(a.chatId.toString(), update.getCallbackQuery().getMessage().getMessageId()));
        } else if (data.startsWith("transaction.")) {
            NumberFormat format = NumberFormat.getInstance(new Locale("en", "US"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            List<String> transactionData = List.of(data.split("\\."));
            Transaction t = Transaction.builder().transactionTime(Date.from(Instant.now())).EmployeeName(service.getById(transactionData.get(1)).getName()).EmployeeId(transactionData.get(1)).sellerName(service.getById(transactionData.get(2)).getName()).sellerId(transactionData.get(2)).amount(Long.parseLong(transactionData.get(3))).build();
            tService.save(t);
            Long amount = service.getById(t.getEmployeeId()).getBalance() - Long.parseLong(transactionData.get(3));
            service.withdrawMoney(amount, t.getEmployeeId());
            a.bot.executeMessage(new DeleteMessage(a.chatId.toString(), update.getCallbackQuery().getMessage().getMessageId()));
            a.sendMessage(a.chatId, "<b>Tranzaksiya muvaffaqiyatli amalga oshdi ✅</b>", markup.sellerPanel());
            a.sendMessage(service.getById(t.getEmployeeId()).getChatId(), String.format("<b>Hisobingizdan <code>%s so'm</code> mablag' %s soat %s da yechib olindi\n</b><i>Sotuvchi:</i>@%s", format.format(Long.parseLong(transactionData.get(3))), dateFormat.format(Date.from(Instant.now())), timeFormat.format(Date.from(Instant.now())), service.getById(transactionData.get(2)).getUsername()));
        } else if (data.equals("abort_transaction")) {
            sessions.setState(State.DEFAULT, a.chatId);
            a.sendMessage(a.chatId, "<b>Tranzaksiya bekor qilindi</b> 🤷‍♂️", markup.sellerPanel());
            a.bot.executeMessage(new DeleteMessage(a.chatId.toString(), update.getCallbackQuery().getMessage().getMessageId()));
        } else if (data.startsWith("transactionTime.")) {
            var days = Integer.parseInt(data.split("\\.")[1]);
            Date time = Date.from(Instant.now().minus(days, ChronoUnit.DAYS));
            List<Transaction> all = tService.getAll(time);
            exportToExcel(all);
            SendDocument document = new SendDocument();
            document.setChatId(a.chatId.toString());
            document.setParseMode("HTML");
            document.setCaption(adjustCaption(days));
            document.setDocument(new InputFile(new File("transactions.xlsx")));
            a.bot.executeMessage(new DeleteMessage(a.chatId.toString(), update.getCallbackQuery().getMessage().getMessageId()));
            a.bot.sendDocument(document);
            File d = new File("transaction.xlsx");
            d.deleteOnExit();
        }
    }

    private String adjustCaption(int days) {
        if (days <= 1) return "<b>Bugungi tranzaksiyalar</b>";
        else if (days <= 7) return "<b>O'tgan haftadagi tranzaksiyalar</b>";
        else if (days <= 14) return "<b>O'tgan 2 haftadagi tranzaksiyalar</b>";
        else return "<b>O'tgan oydagi tranzaksiyalar</b>";
    }

    private EditMessageText editMessage(Update update, String id) {
        EditMessageText e = new EditMessageText();
        e.setChatId(a.chatId.toString());
        e.setText(update.getCallbackQuery().getMessage().getText());
        e.setEntities(update.getCallbackQuery().getMessage().getEntities());
        e.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        if (!service.getById(id).getRole().equalsIgnoreCase(Role.ADMIN.getCode()))
            e.setReplyMarkup(inline.manageUserBalance(service.getById(id)));
        else e.setReplyMarkup(inline.manageAdmin(service.getById(id)));
        return e;
    }

    private void sendWarning(Long chatid) {
        SendSticker sticker = new SendSticker();
        sticker.setChatId(chatid.toString());
        sticker.setSticker(new InputFile(Stickers.WARNING.getFileId()));
        a.bot.sendSticker(sticker);
    }

    private void exportToExcel(List<Transaction> all) {
        ExcelExporterService service = new ExcelExporterService(all);
        service.export("transactions.xlsx");
    }
}
