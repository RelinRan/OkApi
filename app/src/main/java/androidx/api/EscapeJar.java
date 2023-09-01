package androidx.api;

/**
 * 内容转义<br/>
 * 主要处理Json传递转义。<br/>
 */
public interface EscapeJar {

    /**
     * 反转义
     *
     * @param content 内容
     * @return
     */
    String unescape(String content);

    /**
     * 转义
     *
     * @param content 内容
     * @return
     */
    String escape(String content);

}
