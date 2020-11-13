package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.swing.*;

@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {

    SUCCESS(200, "The remote is successful"),
    FAIL(500, "The remote call is fail");

    private final int code;
    private final String message;
}
