package com.androidx.okapi;

/**
 * 日志打印类<br/>
 * 该类主要适应日志打印不全问题，主要是因为Log最大长度4*1024，考虑到汉字问题<br/>
 * 此处我们采用的是2*1024长度打印，主要是分行打印数据，同时对每行进行[index]标识<br/>
 * 目前支持Log.i() Log.w()  Log.e()  Log.d()<br/>
 * 自定义Log.format,采用格式化打印日志。
 */
public class ApiLog {

    /**
     * 换行
     */
    public final static String NEW_LINE = " \n";
    /**
     * 头部分割线
     */
    public final static String HEAD_LINE = "┌────────────────────────────────────────────────────────────────────────";
    /**
     * 左边分割线
     */
    public final static String LEFT_LINE = "│";
    /**
     * 中间分割线
     */
    public final static String MIDDLE_LINE = "├──────────────────────────────────────────────────────────────────────── ";
    /**
     * 底部分割线
     */
    public final static String BOTTOM_LINE = "└────────────────────────────────────────────────────────────────────────";
    /**
     * 最大一次打印长度
     */
    public final static int MAX_LENGTH = 2000;

    /**
     * 日志类型
     */
    public enum Type {
        I, E, D, W
    }

    /**
     * 打印信息日志
     *
     * @param tag 标志
     * @param msg 内容
     */
    public static void i(String tag, String msg) {
        maxPrint(Type.I, tag, msg);
    }

    /**
     * 打印错误日志
     *
     * @param tag 标志
     * @param msg 内容
     */
    public static void e(String tag, String msg) {
        maxPrint(Type.E, tag, msg);
    }

    /**
     * 打印调试日志
     *
     * @param tag 标志
     * @param msg 内容
     */
    public static void d(String tag, String msg) {
        maxPrint(Type.D, tag, msg);
    }

    /**
     * 打印警告日志
     *
     * @param tag 标志
     * @param msg 打印内容
     */
    public static void w(String tag, String msg) {
        maxPrint(Type.W, tag, msg);
    }

    /**
     * 打印格式化信息日志
     *
     * @param tag      标志
     * @param header   标题
     * @param contents 内容
     */
    public static void i(String tag, String header, String contents) {
        maxPrint(Type.I, tag, format(header, contents));
    }

    /**
     * 打印格式化错误日志
     *
     * @param tag      标志
     * @param header   标题
     * @param contents 内容
     */
    public static void e(String tag, String header, String contents) {
        maxPrint(Type.E, tag, format(header, contents));
    }

    /**
     * 打印格式化调试日志
     *
     * @param tag      标志
     * @param header   标题
     * @param contents 内容
     */
    public static void d(String tag, String header, String contents) {
        maxPrint(Type.D, tag, format(header, contents));
    }

    /**
     * 打印格式化警告日志
     *
     * @param tag      标志
     * @param header   标题
     * @param contents 内容
     */
    public static void w(String tag, String header, String contents) {
        maxPrint(Type.W, tag, format(header, contents));
    }


    /**
     * 打印格式化信息日志
     *
     * @param tag      标志
     * @param header   标题
     * @param contents 内容
     */
    public static void i(String tag, String header, String[] contents) {
        maxPrint(Type.I, tag, format(header, contents));
    }

    /**
     * 打印格式化错误日志
     *
     * @param tag      标志
     * @param header   标题
     * @param contents 内容
     */
    public static void e(String tag, String header, String[] contents) {
        maxPrint(Type.E, tag, format(header, contents));
    }

    /**
     * 打印格式化调试日志
     *
     * @param tag      标志
     * @param header   标题
     * @param contents 内容
     */
    public static void d(String tag, String header, String[] contents) {
        maxPrint(Type.D, tag, format(header, contents));
    }

    /**
     * 打印格式化警告日志
     *
     * @param tag     标志
     * @param header  标题
     * @param content 内容
     */
    public static void w(String tag, String header, String[] content) {
        maxPrint(Type.W, tag, format(header, content));
    }

    /**
     * 格式化打印日志
     *
     * @param header
     * @param content
     * @return
     */
    public static String format(String header, String content) {
        String[] items = split(content, ApiLog.HEAD_LINE.length());
        return format(header, items);
    }

    /**
     * 格式化数据
     *
     * @param header   标题
     * @param contents 消息数组
     * @return
     */
    public static String format(String header, String contents[]) {
        StringBuffer sb = new StringBuffer();
        sb.append(ApiLog.NEW_LINE).append(ApiLog.HEAD_LINE).append(ApiLog.NEW_LINE);
        sb.append(ApiLog.LEFT_LINE).append(header).append(ApiLog.NEW_LINE);
        sb.append(ApiLog.MIDDLE_LINE).append(ApiLog.NEW_LINE);
        for (int i = 0; i < contents.length; i++) {
            sb.append(ApiLog.LEFT_LINE).append(contents[i]).append(ApiLog.NEW_LINE);
        }
        sb.append(ApiLog.BOTTOM_LINE);
        return sb.toString();
    }

    /**
     * 分割字符
     *
     * @param message 消息
     * @param unit    item长度
     * @return
     */
    public static String[] split(String message, int unit) {
        String[] messages;
        if (message.length() <= unit) {
            messages = new String[1];
            messages[0] = message;
        } else {
            int length = message.length() / unit;
            int mod = message.length() % unit;
            int size = mod == 0 ? length : length + 1;
            messages = new String[size];
            for (int i = 0; i < length; i++) {
                messages[i] = message.substring(unit * i, unit * (i + 1));
            }
            if (mod != 0) {
                messages[size - 1] = message.substring(unit * length);
            }
        }
        return messages;
    }

    /**
     * 适应最大长度打印
     *
     * @param type 日志类型
     * @param tag  标志
     * @param msg  信息
     */
    private static void maxPrint(Type type, String tag, String msg) {
        if (msg.length() > MAX_LENGTH) {
            int length = MAX_LENGTH + 1;
            String remain = msg;
            int index = 0;
            while (length > MAX_LENGTH) {
                index++;
                typePrint(type, tag + "[" + index + "]", " \n" + remain.substring(0, MAX_LENGTH));
                remain = remain.substring(MAX_LENGTH);
                length = remain.length();
            }
            if (length <= MAX_LENGTH) {
                index++;
                typePrint(type, tag + "[" + index + "]", " \n" + remain);
            }
        } else {
            typePrint(type, tag, msg);
        }
    }

    /**
     * 打印各种类型
     *
     * @param type 日志类型
     * @param tag  标志
     * @param msg  信息
     */
    private static void typePrint(Type type, String tag, String msg) {
        switch (type) {
            case I:
                android.util.Log.i(tag, msg);
                break;
            case E:
                android.util.Log.e(tag, msg);
                break;
            case W:
                android.util.Log.w(tag, msg);
                break;
            case D:
                android.util.Log.d(tag, msg);
                break;
        }
    }

}
