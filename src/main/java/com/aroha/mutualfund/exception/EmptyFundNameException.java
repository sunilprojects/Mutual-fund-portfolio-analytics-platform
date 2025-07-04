package com.aroha.mutualfund.exception;

public class EmptyFundNameException  extends RuntimeException {
    public EmptyFundNameException(String message) {
        super(message);
    }
}