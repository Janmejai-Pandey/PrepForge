package com.prepforge.exception;

// CUSTOM EXCEPTION:
// We create our own exception by writing "extends Exception".
// This is used when a user enters the wrong email or password.
public class InvalidLoginException extends Exception {

    public InvalidLoginException(String message) {
        // "super(message)" sends the error text to the parent Exception class
        super(message);
    }
}
