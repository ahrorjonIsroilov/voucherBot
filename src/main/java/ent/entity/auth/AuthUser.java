package ent.entity.auth;

import ent.config.StringPrefixedSequenceIdGenerator;
import ent.entity.BaseEntity;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "auth_user")
public class AuthUser implements BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @GenericGenerator(
            name = "user_id_seq",
            strategy = "ent.config.StringPrefixedSequenceIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = StringPrefixedSequenceIdGenerator.INCREMENT_PARAM, value = "1"),
                    @org.hibernate.annotations.Parameter(name = StringPrefixedSequenceIdGenerator.VALUE_PREFIX_PARAMETER, value = "U"),
                    @org.hibernate.annotations.Parameter(name = StringPrefixedSequenceIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%d")
            }
    )
    private String id;
    @Column(name = "chat_id", unique = true)
    private Long chatId;
    @Column(name = "username", unique = true)
    private String username;
    private String name;
    private Long balance;
    private Long balanceLimit;
    private String role;
    private Boolean registered;
    private Boolean blocked;
    private String state;
    private Integer page;
}
