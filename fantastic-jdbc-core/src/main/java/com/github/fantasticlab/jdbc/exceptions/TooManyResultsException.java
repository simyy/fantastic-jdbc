package com.github.fantasticlab.jdbc.exceptions;


public class TooManyResultsException extends RuntimeException {

    public TooManyResultsException() {
      super();
    }

    public TooManyResultsException(String message) {
        super(message);
    }

    public TooManyResultsException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooManyResultsException(Throwable cause) {
        super(cause);
    }
}
