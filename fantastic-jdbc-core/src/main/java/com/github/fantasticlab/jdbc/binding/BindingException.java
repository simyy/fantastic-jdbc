package com.github.fantasticlab.jdbc.binding;

public class BindingException extends RuntimeException {

    public BindingException() {
        super();
    }

    public BindingException(String message) {
        super(message);
    }

    public BindingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BindingException(Throwable cause) {
        super(cause);
    }

}
