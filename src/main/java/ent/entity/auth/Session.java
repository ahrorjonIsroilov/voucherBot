package ent.entity.auth;

import ent.enums.State;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class Session {

    public Map<Long, Optional<SessionUser>> sessions = new HashMap<>();

    public Optional<SessionUser> getByChatId(Long chatId) {
        for (Map.Entry<Long, Optional<SessionUser>> entry : sessions.entrySet()) {
            if (entry.getKey().equals(chatId)) return entry.getValue();
        }
        return Optional.empty();
    }

    public SessionUser findByChatId(Long chatId) {
        for (Map.Entry<Long, Optional<SessionUser>> entry : sessions.entrySet()) {
            if (entry.getKey().equals(chatId)) {
                Optional<SessionUser> optional = entry.getValue();
                if (optional.isPresent())
                    return optional.get();
            }
        }
        return null;
    }

    public void previousPage(Long chatId) {
        for (Map.Entry<Long, Optional<SessionUser>> entry : sessions.entrySet()) {
            if (entry.getKey().equals(chatId)) {
                Optional<SessionUser> optional = entry.getValue();
                if (optional.isPresent()) {
                    SessionUser user = optional.get();
                    user.setPage(user.getPage() - 1);
                    setSession(chatId, Optional.of(user));
                }
            }
        }
    }

    public void nextPage(Long chatId) {
        for (Map.Entry<Long, Optional<SessionUser>> entry : sessions.entrySet()) {
            if (entry.getKey().equals(chatId)) {
                Optional<SessionUser> optional = entry.getValue();
                if (optional.isPresent()) {
                    SessionUser user = optional.get();
                    user.setPage(user.getPage() + 1);
                    setSession(chatId, Optional.of(user));
                }
            }
        }
    }

    public void setPageZero(Long chatId) {
        Optional<SessionUser> user = sessions.get(chatId);
        if (user.isPresent()) {
            SessionUser sessionUser = user.get();
            sessionUser.setPage(0);
            setSession(chatId, Optional.of(sessionUser));
        }
    }


    public void setSession(Long chatId, Optional<SessionUser> user) {
        sessions.put(chatId, user);
    }

    public void removeSession(Long chatId) {
        sessions.remove(chatId);
    }

    public void setState(State state, Long chatId) {
        Optional<SessionUser> session = getByChatId(chatId);
        if (session.isPresent()) {
            SessionUser user = session.get();
            user.setState(state.getCode());
            setSession(chatId, Optional.of(user));
        }
    }

    public void setTempRole(Role role, Long chatId) {
        Optional<SessionUser> user = getByChatId(chatId);
        if (user.isPresent()) {
            SessionUser sessionUser = user.get();
            sessionUser.setTempRole(role);
            setSession(chatId, Optional.of(sessionUser));
        }
    }

    public void setTempUsername(String username, Long chatId) {
        Optional<SessionUser> user = getByChatId(chatId);
        if (user.isPresent()) {
            SessionUser sessionUser = user.get();
            sessionUser.setTempUsername(username);
            setSession(chatId, Optional.of(sessionUser));
        }
    }

    public void setTempChatId(Long tempChatId, Long chatId) {
        Optional<SessionUser> user = getByChatId(chatId);
        if (user.isPresent()) {
            SessionUser sessionUser = user.get();
            sessionUser.setTempChatId(tempChatId);
            setSession(chatId, Optional.of(sessionUser));
        }
    }

    public void setTempPrice(Long tempPrice, Long chatId) {
        Optional<SessionUser> user = getByChatId(chatId);
        if (user.isPresent()) {
            SessionUser sessionUser = user.get();
            sessionUser.setTempPrice(tempPrice);
            setSession(chatId, Optional.of(sessionUser));
        }
    }

    public Boolean checkState(State state, Long chatId) {
        Optional<SessionUser> session = getByChatId(chatId);
        return session.map(sessionUser -> sessionUser.getState().equals(state.getCode())).orElse(false);
    }

}
