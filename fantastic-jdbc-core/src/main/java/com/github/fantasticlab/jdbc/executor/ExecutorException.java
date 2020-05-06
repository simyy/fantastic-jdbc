package com.github.fantasticlab.jdbc.executor;

public class ExecutorException extends RuntimeException {

  public ExecutorException() {
    super();
  }

  public ExecutorException(String message) {
    super(message);
  }

  public ExecutorException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExecutorException(Throwable cause) {
    super(cause);
  }

}
