package com.github.fantasticlab.jdbc.executor.mapping;

import com.github.fantasticlab.jdbc.xml.parsing.ParsingException;

import java.util.HashMap;
import java.util.Map;

/**
 * Inline parameter expression parser. Supported grammar (simplified):
 *
 * <pre>
 * inline-parameter = (propertyName | expression) oldJdbcType attributes
 * propertyName = /expression language's property navigation path/
 * expression = '(' /expression language's expression/ ')'
 * oldJdbcType = ':' /any valid jdbc type/
 * attributes = (',' attribute)*
 * attribute = name '=' value
 * </pre>
 */

/**
 * 参数表达式, 解析参数存入HashMap
 */
public class ParameterExpression extends HashMap<String, String> {

    public ParameterExpression(String expression) {
        parse(expression);
    }

    public void parse(String expression) {
        // 去除空格
        int p = skipWS(expression, 0);
        //处理属性
        property(expression, p);
    }

    private void property(String expression, int left) {
        //#{property,javaType=INTEGER}
        if (left < expression.length()) {
            // 从非空格的起始位置到『,』结束
            int right = skipUntil(expression, left, ",");
            // trimmedStr 去掉property左右的空白
            String property = trimmedStr(expression, left, right);
            put("property", property);
            jdbcTypeOpt(expression, right + 1);
        }
    }

    private int skipWS(String expression, int p) {
        for (int i = p; i < expression.length(); i++) {
            if (expression.charAt(i) > 0x20) {
                return i;
            }
        }
        return expression.length();
    }

    private int skipUntil(String expression, int p, final String endChars) {
        for (int i = p; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (endChars.indexOf(c) > -1) {
                return i;
            }
        }
        return expression.length();
    }

    private void jdbcTypeOpt(String expression, int p) {
        p = skipWS(expression, p);
        if (p < expression.length()) {
            int left = skipWS(expression, p);
            if (left < expression.length()) {
                int right = skipUntil(expression, left, "=");
                String name = trimmedStr(expression, left, right);
                if (!"jdbcType".equals(name)) {
                    throw new ParsingException("Must be jdbcType, it's " + name);
                }
                left = right + 1;
                String value = trimmedStr(expression, left, expression.length());
                put(name, value);
            }
        }
    }

    private String trimmedStr(String str, int start, int end) {
        while (str.charAt(start) <= 0x20) {
            start++;
        }
        while (str.charAt(end - 1) <= 0x20) {
            end--;
        }
        return start >= end ? "" : str.substring(start, end);
    }

    public static void main(String[] args) {

        Map<String, String> rs = new ParameterExpression("id");
        assert 1 == rs.size();
        assert "id".equals(rs.get("property"));

        rs = new ParameterExpression(" with spaces ");
        assert 1 == rs.size();
        assert "with spaces".equals(rs.get("property"));

        rs = new ParameterExpression(" id , jdbcType =  VARCHAR");
        assert 2 == rs.size();
        assert "id".equals(rs.get("property"));
        assert "VARCHAR".equals(rs.get("jdbcType"));

        try {
            rs = new ParameterExpression("id , jdbcTType =  VARCHAR");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
