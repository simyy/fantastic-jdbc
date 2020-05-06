package com.github.fantasticlab.jdbc.scripting;



public class ScriptingException extends RuntimeException {

  public ScriptingException() {
    super();
  }

  public ScriptingException(String message) {
    super(message);
  }

  public ScriptingException(String message, Throwable cause) {
    super(message, cause);
  }

  public ScriptingException(Throwable cause) {
    super(cause);
  }

}
