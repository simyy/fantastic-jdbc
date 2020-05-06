package com.github.fantasticlab.jdbc.xml.parsing;

import java.util.Properties;

/**
 * 属性解析器
 */
public class PropertyParser {

    private PropertyParser() {

    }

    public static String parse(String string, Properties variables) {
        VariableTokenHandler handler = new VariableTokenHandler(variables);
        GenericTokenParser parser = new GenericTokenParser("${", "}", handler);
        return parser.parse(string);
    }

    /**
     * Token替换 标记key替换变量value
     */
    private static class VariableTokenHandler implements TokenHandler {

        private Properties variables;

        public VariableTokenHandler(Properties variables) {
            this.variables = variables;
        }

        @Override
        public String handleToken(String content) {
            if (variables != null && variables.containsKey(content)) {
                return variables.getProperty(content);
            }
            return "${" + content + "}";
        }
    }

    public static void main(String[] args) {
        Properties variables = new Properties();
        variables.put("key", "value");
        String rs = PropertyParser.parse("select ${key}", variables);
        assert rs != null;
    }
}
