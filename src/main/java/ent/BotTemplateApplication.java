package ent;

import ent.config.BotRunner;
import ent.entity.auth.AuthUser;
import ent.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class BotTemplateApplication {

    private final UserService userService;
    private final BotRunner runner;

    public static void main(String[] args) {
        SpringApplication.run(BotTemplateApplication.class, args);
    }

//    @Bean
    CommandLineRunner runner() {
        return (args) -> {
            AuthUser authUser = AuthUser.builder()
                    .name("Ahrorjon")
                    .username("akhdeo")
                    .chatId(1992137199L)
                    .balanceLimit(99999999999L)
                    .balance(99999999999L)
                    .registered(true)
                    .blocked(false)
                    .role("admin")
                    .page(0)
                    .state("default")
                    .build();
            runner.main();
            userService.save(authUser);
        };
    }
}
