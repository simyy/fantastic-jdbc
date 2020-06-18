package com.github.fantasticlab.jdbc.xml.parsing;

/**
 * TokenHandler is used to replace token for {@code Statement}.
 */
public interface TokenHandler {

    String handleToken(String content);

}

