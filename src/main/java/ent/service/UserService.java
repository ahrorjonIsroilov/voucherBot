package ent.service;

import ent.entity.auth.AuthUser;
import ent.entity.auth.Role;
import ent.entity.auth.Session;
import ent.entity.auth.SessionUser;
import ent.repo.auth.AuthRepo;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;

@Service
@EnableScheduling
public class UserService implements BaseService {
    private final AuthRepo repo;
    private final Session session;

    public UserService(AuthRepo repo, Session session) {
        this.repo = repo;
        this.session = session;
    }

    public void save(AuthUser user) {
        repo.save(user);
    }

    public AuthUser getById(String id) {
        return repo.findById(id);
    }

    public List<AuthUser> getAllByRole(String role, Pageable pageable) {
        return repo.findAllByRoleAndRegisteredTrue(role, pageable);
    }

    public List<AuthUser> getAllByRole(String role) {
        return repo.findAllByRoleAndRegisteredTrue(role);
    }

    public AuthUser getByChatId(Long chatId) {
        return repo.findByChatId(chatId);
    }

    public AuthUser getByUsername(String username) {
        username = username.toLowerCase(Locale.ROOT);
        return repo.findByUsername(username);
    }

    public void register(String role, String username, Long balanceLimit) {
        repo.registerUser(true, balanceLimit, balanceLimit, role, username);
    }

    public void register(User user) {
        AuthUser authUser = AuthUser.builder().username(user.getUserName().toLowerCase(Locale.ROOT)).name(user.getFirstName()).chatId(user.getId()).registered(true).blocked(false).page(0).state("default").build();
        repo.registerUser(authUser.getUsername().toLowerCase(Locale.ROOT), authUser.getRegistered(), authUser.getName(), authUser.getChatId(), authUser.getState(), authUser.getBlocked(), authUser.getPage());
        session.setSession(user.getId(), prepareSession(getByChatId(authUser.getChatId())));
    }

    public Boolean isRegistered(Long chatId) {
        AuthUser user = getByChatId(chatId);
        if (session.getByChatId(chatId).isPresent()) return true;
        else {
            if (Objects.nonNull(user) && user.getRegistered() && !user.getBlocked())
                session.setSession(chatId, Optional.of(SessionUser.builder().username(user.getUsername()).id(user.getId()).chatId(chatId).page(user.getPage()).role(user.getRole()).state(user.getState()).build()));
            return Objects.nonNull(user) && user.getRegistered() && !user.getBlocked();
        }
    }

    public void setUserBalanceLimit(Long limit, String userId) {
        repo.setUserBalanceLimit(limit, userId);
    }

    @Scheduled(cron = "0 */10 * ? * *")
    public void updatePage() {
        for (Map.Entry<Long, Optional<SessionUser>> entry : session.sessions.entrySet()) {
            if (entry.getValue().isPresent()) {
                SessionUser user = entry.getValue().get();
                repo.updatePage(user.getPage(), user.getChatId());
            }
        }
    }

    @Scheduled(cron = "0 * * ? * *")
    public void restoreVoucher() {
        repo.restoreVoucher();
    }

    public void withdrawMoney(Long amount, String id) {
        repo.withdrawMoney(amount, id);
    }

    public Boolean isAdmin(Long chatId) {
        Optional<SessionUser> user = session.getByChatId(chatId);
        return user.map(sessionUser -> sessionUser.getRole().equalsIgnoreCase(Role.OWNER.getCode()) || sessionUser.getRole().equalsIgnoreCase(Role.ADMIN.getCode())).orElseGet(() -> getByChatId(chatId).getRole().equalsIgnoreCase("admin"));
    }

    public Boolean isOwner(Long chatId) {
        Optional<SessionUser> user = session.getByChatId(chatId);
        return user.map(sessionUser -> sessionUser.getRole().equalsIgnoreCase(Role.OWNER.getCode())).orElseGet(() -> getByChatId(chatId).getRole().equalsIgnoreCase("admin"));
    }

    public Boolean isSeller(Long chatId) {
        Optional<SessionUser> user = session.getByChatId(chatId);
        return user.map(sessionUser -> sessionUser.getRole().equalsIgnoreCase(Role.SELLER.getCode())).orElseGet(() -> getByChatId(chatId).getRole().equalsIgnoreCase(Role.SELLER.getCode()));
    }

    public Boolean isUser(Long chatId) {
        Optional<SessionUser> user = session.getByChatId(chatId);
        return user.map(sessionUser -> sessionUser.getRole().equalsIgnoreCase(Role.USER.getCode())).orElseGet(() -> getByChatId(chatId).getRole().equalsIgnoreCase(Role.USER.getCode()));
    }

    public Optional<SessionUser> prepareSession(AuthUser authUser) {
        return Optional.of(SessionUser.builder().role(authUser.getRole()).state(authUser.getState()).chatId(authUser.getChatId()).page(authUser.getPage()).build());
    }

    public void assignAdmin(String id) {
        repo.assignAdmin("admin", true, id);
    }

    public void blockUser(String id) {
        repo.setUserBlockedStatus(id, true);
    }

    public void unblockUser(String id) {
        repo.setUserBlockedStatus(id, false);
    }

    public void removeAdminById(String id) {
        repo.removeAdminById(id);
    }
}
