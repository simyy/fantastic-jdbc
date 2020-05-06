package com.github.fantasticlab.jdbc.session;


public interface ResultContext {

  Object getResultObject();

  int getResultCount();

  boolean isStopped();

  void stop();

}
