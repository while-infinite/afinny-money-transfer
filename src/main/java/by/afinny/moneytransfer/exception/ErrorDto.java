package by.afinny.moneytransfer.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class ErrorDto {

    private final String errorCode;
    private final String errorMessage;
}
