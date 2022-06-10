package ent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum State {
    ADD_EMPLOYEE("add_employee"),
    ADD_ADMIN("add_admin"),
    ADD_SELLER("add_seller"),
    ADD_PRICE("add_price"),
    EDIT_MONTHLY_VOUCHER("edit_monthly_voucher"),
    ACCEPT_EDIT_MONTHLY_VOUCHER("accept_edit_monthly_voucher"),
    SETTINGS("settings"),
    SELL("sell"),
    INSERT_EMPLOYEE_ID("insert_employee_id"),
    INSERT_AMOUNT("insert_amount"),
    DEFAULT("default");
    private final String code;
}

