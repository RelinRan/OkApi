package com.androidx.okapi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.internal.platform.Platform;

/**
 * 下载助手<br/>
 * 帮助你简单的调用函数进行下载，同时如果你需要自定义下载网络的方式，<br/>
 * 只需要继承该类然后重写download方法，在获取到文件流之后调用doHttpResponse（）<br/>
 * 处理对应逻辑就行了。<br/>
 */
public class Downloader {

    public final static String TAG = Downloader.class.getSimpleName();
    /**
     * byte
     */
    public static final int UNIT_BT = 1;
    /**
     * KiB
     */
    public static final int UNIT_KB = 2;
    /**
     * MiB
     */
    public static final int UNIT_MB = 3;
    /**
     * GiB
     */
    public static final int UNIT_GB = 4;
    /**
     * TiB
     */
    public static final int UNIT_TB = 5;
    /**
     * 正在下载
     */
    public static final int WHAT_DOWNLOADING = 0x001;
    /**
     * 完成下载
     */
    public static final int WHAT_DOWNLOAD_COMPLETED = 0x002;
    /**
     * 下载失败
     */
    public static final int WHAT_DOWNLOAD_FAILED = 0x003;
    /**
     * 上下文
     */
    private Context context;
    /**
     * 总的大小
     */
    private long totalSize = 0;
    /**
     * 是否取消
     */
    private boolean isCancel;
    /**
     * 是否暂停
     */
    private boolean isPause;
    /**
     * 是否在下载中
     */
    private boolean isDownloading;
    /**
     * 资源地址
     */
    public final String url;
    /**
     * 文件名称
     */
    public final String name;
    /**
     * 实发支持断点下载
     */
    public final boolean breakpoint;
    /**
     * 是否覆盖下载
     */
    public final boolean cover;
    /**
     * 下载Handler
     */
    protected DownloadHandler handler;
    /**
     * 下载监听
     */
    protected OnDownloadListener onDownloadListener;

    public Downloader(Builder builder) {
        this.context = builder.context;
        this.url = builder.url;
        this.name = builder.name;
        this.breakpoint = builder.breakpoint;
        this.cover = builder.cover;
        this.onDownloadListener = builder.onDownloadListener;
        handler = new DownloadHandler();
        start();
    }

    public static class Builder {

        private Context context;
        private String url;
        private String name;
        private boolean breakpoint;
        private boolean cover;
        private OnDownloadListener onDownloadListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public boolean isBreakpoint() {
            return breakpoint;
        }

        public boolean isCover() {
            return cover;
        }

        public Builder breakpoint(boolean breakpoint) {
            this.breakpoint = breakpoint;
            return this;
        }

        public Builder cover(boolean cover) {
            this.cover = cover;
            return this;
        }

        public Builder listener(OnDownloadListener onDownloadListener) {
            this.onDownloadListener = onDownloadListener;
            return this;
        }

        public Downloader build() {
            return new Downloader(this);
        }
    }

    /**
     * 是否断点下载
     *
     * @return
     */
    protected boolean isBreakpoint() {
        return breakpoint;
    }

    /**
     * 是否覆盖下载
     *
     * @return
     */
    public boolean isCover() {
        return cover;
    }

    /**
     * 是否暂停
     *
     * @return
     */
    protected boolean isPause() {
        return isPause;
    }

    /**
     * 是否取消
     *
     * @return
     */
    protected boolean isCancel() {
        return isCancel;
    }

    /**
     * 是否正在下载
     *
     * @return
     */
    protected boolean isDownloading() {
        return isDownloading;
    }

    /**
     * 设置下载状态
     *
     * @param downloading
     */
    public void setDownloading(boolean downloading) {
        this.isDownloading = downloading;
    }

    /**
     * 开始下载
     */
    public void start() {
        isPause = false;
        isCancel = false;
        if (!isDownloading) {
            download();
        }
    }

    /**
     * 暂停下载
     */
    public void pause() {
        isPause = true;
    }

    /**
     * 取消下载
     */
    public void cancel() {
        isCancel = true;
    }

    /**
     * 销毁下载
     */
    public void destroy() {
        cancel();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    /**
     * 下载文件夹
     *
     * @return
     */
    public static File dir(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
    }

    /**
     * 文件夹大小
     *
     * @param context 上下文对象
     * @param unit    单位 {@link #UNIT_BT}等
     * @return
     */
    public static long dirSize(Context context, int unit) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        long size = 0;
        for (File file : dir.listFiles()) {
            ApiLog.i(TAG, "file: " + file.getAbsolutePath() + " , length: " + file.length());
            size += file.length();
        }
        if (unit == UNIT_BT) {
            return size;
        }
        if (unit == UNIT_KB) {
            size /= 1024;
        }
        if (unit == UNIT_MB) {
            size /= Math.pow(1024, 2);
        }
        if (unit == UNIT_GB) {
            size /= Math.pow(1024, 3);
        }
        if (unit == UNIT_TB) {
            size /= Math.pow(1024, 4);
        }
        ApiLog.i(TAG, "dirSize: " + size + " , unit: " + unit);
        return size;
    }

    /**
     * 获取缓存对象
     *
     * @return
     */
    protected static SharedPreferences getSharedPreferences(Context context) {
        String PACKAGE_NAME = context.getApplicationContext().getPackageName().replace(".", "_").toUpperCase();
        return context.getSharedPreferences(PACKAGE_NAME + "_DOWNLOAD", Context.MODE_PRIVATE);
    }

    /**
     * 添加缓存长度
     *
     * @param context 上下文
     * @param url     地址
     * @param length  文件大小
     */
    protected static void addCacheLength(Context context, String url, long length) {
        getSharedPreferences(context).edit().putLong(url, length).apply();
    }

    /**
     * 获取文件大小
     *
     * @param context 上下文
     * @param url     地址
     * @return
     */
    protected static long getCacheLength(Context context, String url) {
        return getSharedPreferences(context).getLong(url, 0);
    }

    /**
     * 清空下载数据
     */
    public static void clear(Context context) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        for (File file : dir.listFiles()) {
            boolean status = file.delete();
            ApiLog.i(TAG, "clear file: " + file.getAbsolutePath() + " , status: " + status);
        }
        getSharedPreferences(context).edit().clear().apply();
    }

    /**
     * 删除文件
     *
     * @param context 上下文
     * @param url     资源URL
     */
    public static void delete(Context context, String url) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        ApiLog.i(TAG, "external files dir: " + dir.getAbsolutePath());
        String fileName = createFileName(url);
        ApiLog.i(TAG, "fileName: " + fileName);
        File file = new File(dir.getAbsolutePath() + File.separator + fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 创建文件
     *
     * @param url 资源地址
     * @return
     */
    protected File createFile(String url) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        ApiLog.i(TAG, "external files dir: " + dir.getAbsolutePath());
        String fileName = name == null ? createFileName(url) : name;
        ApiLog.i(TAG, "fileName: " + fileName);
        File file = new File(dir.getAbsolutePath() + File.separator + fileName);
        if (cover && file.exists()) {
            file.delete();
        }
        return file;
    }

    /**
     * 创建Url文件名称
     *
     * @param url 资源地址
     * @return
     */
    public static String createFileName(String url) {
        if (url.contains("/") && url.contains(".")) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        return format.format(format) + ".zip";
    }


    /**
     * 是否存在
     *
     * @param url
     * @return
     */
    public boolean isExist(String url) {
        File file = createFile(url);
        return file.exists();
    }

    /**
     * 下载文件
     */
    protected void download() {
        ApiLog.i(TAG, "url: " + url);
        if (TextUtils.isEmpty(url)) {
            sendFailedMsg(new IOException("File download network address is empty."));
            return;
        }
        if (!url.toUpperCase().startsWith("HTTP")) {
            sendFailedMsg(new IOException("File download address error, unable to download normal."));
            return;
        }
        long cacheLength = getCacheLength(context, url);
        if (isExist(url) && cacheLength == createFile(url).length()) {
            ApiLog.i(TAG, "file exist.");
            sendDownloadingMsg(cacheLength, cacheLength);
            sendCompletedMsg(createFile(url));
        } else {
            setDownloading(true);
            download(url);
        }
    }

    /**
     * 下载
     *
     * @param url
     */
    protected void download(final String url) {
        final long downloadedLength = calculateDownloadedLength(url);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .addHeader("User-Agent", "Android")
                .header("Content-Type", "text/html; charset=utf-8;")
                .addHeader("RANGE", "bytes=" + downloadedLength + "-")
                .url(url)
                .build();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        Configure configure = Configure.Config();
        SSLSocketFactory sslSocketFactory = configure.socketFactory();
        builder.sslSocketFactory(sslSocketFactory, Platform.get().trustManager(sslSocketFactory));
        builder.hostnameVerifier(new HttpsHostnameVerifier());
        OkHttpClient okHttpClient = builder.build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ApiLog.i(TAG, e.getMessage());
                sendFailedMsg(e);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) {
                doHttpResponse(response.body().byteStream(), response.body().contentLength(), downloadedLength, createFile(url));
            }
        });
    }

    /**
     * 计算已经下载过的文件大小
     *
     * @param url
     * @return
     */
    protected long calculateDownloadedLength(String url) {
        File file = createFile(url);
        if (file.exists()) {
            if (isBreakpoint()) {
                return file.length();
            } else {
                file.delete();
            }
        }
        return 0;
    }

    /**
     * 处理服务器返回数据
     */
    protected void doHttpResponse(InputStream is, long contentLength, long downloadedLength, File file) {
        long downloading = 0;
        byte[] buf = new byte[2048];
        int len;
        RandomAccessFile randomAccessFile = null;
        try {
            if (downloadedLength == 0) {
                totalSize = contentLength;
            } else {
                totalSize = downloadedLength + contentLength;
            }
            if (totalSize == downloadedLength) {
                //已下载字节和文件总字节相等，说明下载已经完成了
                sendCompletedMsg(file);
                return;
            }
            if (totalSize == 0) {
                if (downloadedLength == 0) {
                    sendFailedMsg(new IOException("The file length value is 0 and cannot be downloaded properly"));
                } else {
                    if (isBreakpoint()) {
                        sendCompletedMsg(file);
                    } else {
                        file.delete();
                    }
                }
                return;
            }
            randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(downloadedLength);
            while ((len = is.read(buf)) != -1) {
                if (isPause() || isCancel()) {
                    break;
                }
                randomAccessFile.write(buf, 0, len);
                downloading += len;
                long downSum = downloading + downloadedLength;
                //传递更新信息
                sendDownloadingMsg(totalSize, downSum);
            }
            randomAccessFile.close();
            sendCompletedMsg(file);
        } catch (Exception e) {
            ApiLog.i(TAG, e.getMessage());
            sendFailedMsg(e);
        } finally {
            setDownloading(false);
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                sendFailedMsg(e);
            }
            try {
                if (randomAccessFile != null)
                    randomAccessFile.close();
            } catch (IOException e) {
                sendFailedMsg(e);
            }
        }
    }

    /**
     * 发送成功的信息
     *
     * @param file
     */
    protected void sendCompletedMsg(File file) {
        Message msg = handler.obtainMessage();
        msg.what = WHAT_DOWNLOAD_COMPLETED;
        msg.obj = file;
        handler.sendMessage(msg);
    }


    /**
     * 发送下载失败信息
     *
     * @param e 文件异常
     */
    protected void sendFailedMsg(Exception e) {
        Message msg = handler.obtainMessage();
        msg.what = WHAT_DOWNLOAD_FAILED;
        msg.obj = e;
        handler.sendMessage(msg);
    }

    /**
     * 发送下载信息
     *
     * @param total    文件总大小
     * @param progress 文件进度
     */
    protected void sendDownloadingMsg(long total, long progress) {
        Message message = handler.obtainMessage();
        message.what = WHAT_DOWNLOADING;
        Bundle bundle = new Bundle();
        bundle.putLong("total", total);
        bundle.putLong("progress", progress);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private class DownloadHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (onDownloadListener == null) {
                return;
            }
            Bundle data = msg.getData();
            Object obj = msg.obj;
            switch (msg.what) {
                case WHAT_DOWNLOADING:
                    long total = data.getLong("total");
                    long progress = data.getLong("progress");
                    addCacheLength(context, url, total);
                    onDownloadListener.onDownloading(total, progress);
                    break;
                case WHAT_DOWNLOAD_COMPLETED:
                    ApiLog.i(TAG, "download completed.");
                    onDownloadListener.onDownloadCompleted((File) obj);
                    break;
                case WHAT_DOWNLOAD_FAILED:
                    onDownloadListener.onDownloadFailed((Exception) obj);
                    break;
            }
        }
    }

}
