# OkApi
Android接口联调工具
1.内含JSON、okhttp、okio
2.支持Get、Post、Put、Delete、Patch
# Maven
1.build.grade
```
allprojects {
    repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
2./app/build.grade
```
dependencies {
	implementation 'com.github.RelinRan:OkApi:2022.2.20.1'
}
```
# 权限
1.网络权限
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
2.非https请求，AndroidManifest.xml中application标签设置属性：
```
android:usesCleartextTraffic="true"
```
# 初始化
注意在Application里初始化
```
Configure configure = Api.initialize(this);
configure.debug(true);//调试模式，可以查看日志
configure.url(Api.BETA, "http://47.108.248.71:8109");//设置测试环境地址
configure.url(Api.ONLINE, "https://www.yiketianqi.com");//设置线上环境地址
configure.address(Api.BETA);//设置当前环境地址
configure.contentType(Api.JSON);//全局请求方式JSON
configure.addInterceptor(xxx);//添加拦截器
```
# 生命周期
在对应页面onDestroy()里调用api.cancel(context);会自动取消当前页面的请求。
```
Api api = new OkApi();
//to do get or post...

@Override
protected void onDestroy() {
    super.onDestroy();
    api.cancel(context);
}
```
# 表单
```
//方法一
Api.initialize(this).contentType(Api.FORM_DATA);
//方法二
Configure.Config().contentType(Api.FORM_DATA);
//方法三
RequestParams params = new RequestParams();
params.addHeader(Header.CONTENT_TYPE,Api.FORM_DATA);
```
# JSON
JSON工具，请点击[Gitee-JSON](https://gitee.com/relin/JSON) 或 [GitHub-JSON](https://github.com/RelinRan/JSON)
```
//方法一
Api.initialize(this).contentType(Api.JSON);
//方法二
Configure.Config().contentType(Api.JSON);
//方法三
RequestParams params = new RequestParams();
params.addHeader(Header.CONTENT_TYPE,Api.JSON);
```
# GET
```
Api api = new OkApi();
RequestParams params = new RequestParams();
params.add("key","value");
api.get(context, "/business/editShelf", params, new OnRequestListener() {
    @Override
    public void onRequestSucceed(Request request, Response response) {
           ApiLog.i("Api", response.body());
    }

    @Override
    public void onRequestFailed(Request request, Exception exception) {

    }
});
```
# POST
```
Api api = new OkApi();
RequestParams params = new RequestParams();
params.add("key","value");
api.post(context, "/business/editShelf", params, new OnRequestListener() {
    @Override
    public void onRequestSucceed(Request request, Response response) {
           ApiLog.i("Api", response.body());
    }

    @Override
    public void onRequestFailed(Request request, Exception exception) {

    }
});
```
# 实体转换
JSON工具，请点击[Gitee-JSON](https://gitee.com/relin/JSON) 或 [GitHub-JSON](https://github.com/RelinRan/JSON)
```
@Override
public void onRequestSucceed(Request request, Response response) {
     User user = response.convert(User.class);
}
```
# 实体上传
```
Api api = new OkApi();
RequestParams params = new RequestParams();
User user = new User();
user.setName("OkApi");
String json = new Gson().toJson(user);
params.add(json);
api.get(context, "/business/editShelf", params, new OnRequestListener() {
    @Override
    public void onRequestSucceed(Request request, Response response) {
           ApiLog.i("Api", response.body());
    }

    @Override
    public void onRequestFailed(Request request, Exception exception) {

    }
});
```
# 切换域名
```
Api api = new OkApi();
RequestParams params = new RequestParams();
params.addHeader(Api.DOMAIN,"http://47.206.248.71:8006");
api.get(context, "/business/editShelf", params, new OnRequestListener() {
    @Override
    public void onRequestSucceed(Request request, Response response) {
           ApiLog.i("Api", response.body());
    }

    @Override
    public void onRequestFailed(Request request, Exception exception) {

    }
});
```
# 文件上传
```
Api api = new OkApi();
RequestParams params = new RequestParams();
File file = new File("/storeage/xxxx.png")
params.add("file",file);
params.addHeader(Header.CONTENT_TYPE,Api.FORM_DATA);
api.post(context, "/business/editShelf", params, new OnRequestListener() {
    @Override
    public void onRequestSucceed(Request request, Response response) {
           ApiLog.i("Api", response.body());
    }

    @Override
    public void onRequestFailed(Request request, Exception exception) {

    }
});
```
# 上传进度
```
Api api = new OkApi();
RequestParams params = new RequestParams();
params.add("file",file);
params.addHeader(Header.CONTENT_TYPE,Api.FORM_DATA);
api.upload(context, "/upload/file", null, new OnBufferedSinkListener() {
    @Override
    public void onBufferedSinkWrite(long contentLength, long bytes) {
        // TODO:显示文件上传进度
    }
}, new OnRequestListener() {
    
    @Override
    public void onRequestSucceed(Request request, Response response) {
        // TODO:服务器请求成功
    }

    @Override
    public void onRequestFailed(Request request, Exception exception) {
        // TODO:服务器请求失败
    }
});
```
# 文件下载
1.在res/新建xml文件夹，在xml文件夹新建path.xml
```
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <root-path
        name="root"
        path="/storage/emulated/0" />
    <files-path
        name="files"
        path="/storage/emulated/0/Android/data/${applicationId}/files" />
    <cache-path
        name="cache"
        path="/storage/emulated/0/Android/data/${applicationId}/cache" />
    <external-path
        name="Download"
        path="/storage/emulated/0/Android/data/${applicationId}/cache/Download" />
</paths>
```
2.权限，注意6.0以上还需要动态申请权限
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
3.FileProvider配置
```
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/paths" />
</provider>
```
4.下载操作
```
Downloader.Builder builder = new Downloader.Builder(this);
builder.url("http://download.tianditu.com/download/mobile/Tiandituapi3.0.2%2820190103-02%29.zip");
builder.listener(new OnDownloadListener() {
    @Override
    public void onDownloading(long total, long progress) {

    }

    @Override
    public void onDownloadCompleted(File file) {

    }

    @Override
    public void onDownloadFailed(Exception e) {

    }
});
builder.breakpoint(true);
builder.build();
```
# RSA
## 初始化
以下设置会有默认值
```
//公钥字符串
RSA.PUBLIC_KEY = "xxx";
//私钥字符串
RSA.PRIVATE_KEY = "xxx";
//签名算法
RSA.SIGNATURE_ALGORITHM = "SHA1withRSA";
//加密算法
RSA.ENCRYPT_ALGORITHM = "RSA/ECB/PKCS1Padding";
//解密算法
RSA.DECRYPT_ALGORITHM = "RSA/ECB/PKCS1Padding";
```
## 加签
```
String sign(String param);
```
## 验签
```
boolean verifySign(String param, String sign);
```
## 加密
```
String encrypt(String param);
```
## 解密
```
String decrypt(String param);
```
# AES
## 初始化
```
AES.SECRET_KEY = "xxx";
```
## 加密
```
String encrypt(String content);
```
## 解密
```
String decrypt(String content);
```

