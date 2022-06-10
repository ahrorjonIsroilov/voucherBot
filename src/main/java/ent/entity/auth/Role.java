package ent.entity.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    ADMIN("admin"),
    OWNER("owner"),
    SELLER("seller"),
    USER("user");
    private final String code;
}
