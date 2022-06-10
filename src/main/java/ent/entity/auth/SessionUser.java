package ent.entity.auth;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionUser {
    private String id;
    private String username;
    private Long chatId;
    private String role;
    private String state;
    private Integer page;
    private String tempUsername;
    private Long tempChatId;
    private Long tempPrice;
    private Role tempRole;
}
