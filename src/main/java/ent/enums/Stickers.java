package ent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Stickers {
    ACCEPTED("CAACAgEAAxkBAAEBHzRinOdpJW6gNLghHUipdyA4ZMq2VwACfgMAAi1c4ETza6tHNCNmISQE"),
    WARNING("CAACAgEAAxkBAAEBHylinObg7G_yhPTH0Pz4oLO-1CqWfgACfQEAAoVj6ETejlyxqu9SuSQE"),
    SOLD("CAACAgEAAxkBAAEBH0FinOhh__uFtCAGMnQRgPnrbN7H1wACmwIAAqII6UTTUwxzC8GH4yQE"),
    REGISTERED("CAACAgEAAxkBAAEBHzBinOc7Yacmns8WpaazD0k_tTRAIAACJgIAAu7E6ERc-vgiWlkVwSQE"),
    MONEY("CAACAgEAAxkBAAEBHzxinOeP1lR5HKefz6qZCWRLfzXeggACfQMAAs6k6ETX0hg4JysZ7iQE");
    private final String fileId;
}
