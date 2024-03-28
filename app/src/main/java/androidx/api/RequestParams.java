package androidx.api;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 * Api请求参数
 */
public class RequestParams {

    /**
     * 文件参数
     */
    private TreeMap<String, File> file;
    /**
     * 文字参数
     */
    private TreeMap<String, String> data;
    /**
     * Header参数
     */
    private TreeMap<String, String> header;
    /**
     * 接口标识
     */
    private String tag;
    /**
     * 字符串参数
     */
    private String body;
    private byte[] bytes;

    public RequestParams() {

    }

    /**
     * 构造请求参数
     *
     * @param domain 切换域名（跨域参数），非跨域在Api.initialize()之后参数设置域名
     */
    public RequestParams(String domain) {
        header().put(Api.DOMAIN, domain);
    }

    /**
     * 添加参数
     *
     * @param key   键
     * @param value 值
     */
    public void add(String key, int value) {
        if (data == null) {
            data = new TreeMap<>();
        }
        data.put(key, String.valueOf(value));
    }

    /**
     * 添加参数
     *
     * @param key   键
     * @param value 值
     */
    public void add(String key, double value) {
        if (data == null) {
            data = new TreeMap<>();
        }
        data.put(key, String.valueOf(value));
    }

    /**
     * 添加参数
     *
     * @param key   键
     * @param value 值
     */
    public void add(String key, long value) {
        if (data == null) {
            data = new TreeMap<>();
        }
        data.put(key, String.valueOf(value));
    }

    /**
     * 添加参数
     *
     * @param key   键
     * @param value 值
     */
    public void add(String key, float value) {
        if (data == null) {
            data = new TreeMap<>();
        }
        data.put(key, String.valueOf(value));
    }

    /**
     * 添加参数
     *
     * @param key   键
     * @param value 值
     */
    public void add(String key, String value) {
        if (data == null) {
            data = new TreeMap<>();
        }
        data.put(key, value == null ? "" : value);
    }

    /**
     * 添加参数
     *
     * @param key   键
     * @param value 值，true:1; false:0
     */
    public void add(String key, Boolean value) {
        if (data == null) {
            data = new TreeMap<>();
        }
        data.put(key, value ? "1" : "0");
    }

    /**
     * 添加文件参数
     *
     * @param key   键
     * @param value 值
     */
    public void add(String key, File value) {
        if (value == null) {
            return;
        }
        if (file == null) {
            file = new TreeMap<>();
        }
        if (!value.exists()) {
            new RuntimeException("addParams file is not exist!" + value.getAbsolutePath()).printStackTrace();
        }
        file.put(key, value);
    }

    /**
     * 添加头文件参数
     *
     * @param key
     * @param value
     */
    public void addHeader(String key, String value) {
        header = header();
        if (value == null) {
            return;
        }
        header.put(key, value);
    }

    /**
     * 获取文字参数
     *
     * @return
     */
    public TreeMap<String, String> data() {
        return data;
    }

    /**
     * 获取参数JSON
     *
     * @return
     */
    public String dataJson() {
        return from(data);
    }

    /**
     * 获取文件参数
     *
     * @return
     */
    public TreeMap<String, File> file() {
        return file;
    }

    /**
     * 获取头文件参数
     *
     * @return
     */
    public TreeMap<String, String> header() {
        if (header == null) {
            header = new TreeMap<>();
            header.put(Header.USER_AGENT, "Android");
            header.put(Header.CONTENT_TYPE, Configure.Config().contentType());
        }
        return header;
    }

    /**
     * 添加字符串实例
     *
     * @param body
     */
    public void body(String body) {
        this.body = body;
    }

    /**
     * 字节数据
     *
     * @param bytes
     */
    public void body(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * 返回字符串的Body实例
     *
     * @return
     */
    public String body() {
        return body;
    }

    /**
     * 获取字节数据
     *
     * @return
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * 接口标识
     *
     * @param tag
     */
    public void tag(String tag) {
        this.tag = tag;
    }

    /**
     * 接口标识
     *
     * @return
     */
    public String tag() {
        return tag;
    }

    /**
     * 所有数据转JSON
     *
     * @return
     */
    public String toJson() {
        JSONObject obj = new JSONObject();
        put(obj, "file", from(file));
        put(obj, "data", from(data));
        put(obj, "header", from(header));
        put(obj, "tag", tag);
        put(obj, "body", body);
        return obj.toString();
    }

    /**
     * JSONObject插入值
     *
     * @param obj   JSONObject
     * @param key   键
     * @param value 值
     */
    public void put(JSONObject obj, String key, Object value) {
        if (TextUtils.isEmpty(key) || value == null) {
            return;
        }
        try {
            obj.putOpt(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Map对象转JSON
     *
     * @param map
     * @return
     */
    public String from(TreeMap<String, ?> map) {
        if (map == null) {
            return "{}";
        }
        JSONObject obj = new JSONObject();
        Set set = map.keySet();
        if (set == null) {
            return obj.toString();
        }
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = map.get(key);
            if (value instanceof File) {
                File file = (File) value;
                put(obj, key, file.getAbsolutePath());
            } else {
                put(obj, key, value);
            }
        }
        return obj.toString();
    }

}
