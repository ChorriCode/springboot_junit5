package com.chorricode.junit5.exceptions;

public class AmountNotEnoughException extends RuntimeException{
    public AmountNotEnoughException(String message) {
        super(message);
    }
}
