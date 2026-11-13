package com.prepforge.exception;

// CUSTOM EXCEPTION:
// We create our own exception by writing "extends Exception".
// This is used if registration fails (e.g. password too short, or email already used).
public class RegistrationException extends Exception {

    public RegistrationException(String message) {
        // "super(message)" sends the error text to the parent Exception class
        super(message);
    }
}
