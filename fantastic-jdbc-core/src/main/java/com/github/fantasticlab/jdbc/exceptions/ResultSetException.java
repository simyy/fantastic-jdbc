package com.github.fantasticlab.jdbc.exceptions;


public class ResultSetException extends RuntimeException {

    public ResultSetException() {
      super();
    }

    public ResultSetException(String message) {
        super(message);
    }

    public ResultSetException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResultSetException(Throwable cause) {
        super(cause);
    }
}
