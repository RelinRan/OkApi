package com.androidx.okapi;

/**
 * Json参数反转义处理
 */
public class OkEscapeJar implements EscapeJar {

    public OkEscapeJar() {

    }

    @Override
    public String unescape(String content) {
        return content;
    }

    @Override
    public String escape(String content) {
        if (content.contains("\n")) {
            content = content.replace("\n", "\\n");
        }
        if (content.contains("\t")) {
            content = content.replace("\t", "\\t");
        }
        if (content.contains("\r")) {
            content = content.replace("\r", "\\r");
        }
        return content;
    }

}
