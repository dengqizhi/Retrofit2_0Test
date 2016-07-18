# Retrofit入门
Retrofit是由Square公司出品的针对于Android和Java的类型安全的Http客户端，网络服务基于[OkHttp](https://github.com/square/okhttp) 。
- [Retrofit项目GitHub](https://github.com/square/retrofit)
- [Retrofit项目官方文档](http://square.github.io/retrofit/)

### 参考博文
- [Retrofit 2.0:有史以来最大的改进](https://inthecheesefactory.com/blog/retrofit-2.0/en)
- [Retrofit从1.9升级至2.0](https://futurestud.io/blog/retrofit-2-upgrade-guide-from-1-9)          **整套教程文档**
- [Retrofit 2.0使用详解，配合OkHttp、Gson，Android最强网络请求框架](http://blog.csdn.net/u012301841/article/details/49685677)
## 使用举例
Java api http接口
``` java
public interface APIService {
  @GET("users/{user}/repos")
  Call<List<Repo>> listRepos(@Path("user") String user);
}
```
接口调用
``` java
// 创建Retrofit
Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(请求地址)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
 // 创建接口
        APIService service = retrofit.create(APIService.class);
       Call<返回实体> repos = service.请求的接口名称(请求参数);
//同步调用 ：返回实体 entity = call.execute();
//异步调用
        repos.enqueue(new Callback<返回实体>() {
            @Override
            public void onResponse(retrofit.Response<返回实体> response, Retrofit retrofit) {
                返回实体 body = response.body();
                LogUtil.i(TAG, body.toString());
            }
            @Override
            public void onFailure(Throwable throwable) {
                // TODO Auto-generated method stub
            }
        });
```
## API接口
Retrofit通过接口函数的注解和参数表明如何去处理请求。
每一个函数都必须有提供请求方式和相对URL的Http注解，Retrofit提供了5种内置的注解：GET、POST、PUT、DELETE和HEAD，在注解中指定的资源的相对URL
``` java
GET("users/list")
```
可以在URL中指定查询参数
``` java
GET("users/list?sort=desc")
```
请求的URL可以在函数中使用替换块和参数进行动态更新，替换块是**{**和**}**包围的字母数字组成的字符串，相应的参数必须使用相同的字符串被@Path进行注释
``` java
@GET("group/{id}/users")
List<User> groupList(@Path("id") int groupId);
```
可以添加查询参数
``` java
@GET("group/{id}/users")
List<User> groupList(@Path("id") int groupId, @Query("sort") String sort);
```
复杂的参数可以使用**Map**进行组合
``` java
@GET("group/{id}/users")
List<User> groupList(@Path("id") int groupId, @QueryMap Map<String, String> options);
```
Post请求可以通过**@Body**注解指定一个对象作为Http请求的请求体
``` java
@POST("users/new")
Call<User> createUser(@Body User user);
```
函数也可以声明为发送**form-encoded**和**multipart**数据。
当函数有**@FormUrlEncoded**注解的时候，将会发送form-encoded表单数据，每个键-值对都要被含有名字的**@Field**注解和提供值的对象所标注
``` java
@FormUrlEncoded
@POST("user/edit")
Call<User> updateUser(@Field("first_name") String first, @Field("last_name") String last);
```
使用**@Multipart**可以进行文件上传，使用**@Part**指定文件路径及类型
``` java
@Multipart
@POST("/user/edit")
Call<User> upload(@Part("image\"; filename=\"文件名.jpg") RequestBody file);
```
使用**@MapPart**可以方便批量上传
``` java
@Multipart
@POST("/user/edit")
Call<User> upload(@PartMap Map<String, RequestBody> params);

//入参
RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), imgFile);
map.put("image\"; filename=\""+imgFile.getName()+"", fileBody);
```
## 接口请求
接口定义
``` java
APIService service = retrofit.create(APIService.class);
```
异步请求
```
String username = "sarahjean";
Call<User> call = service.getUser(username);
call.enqueue(new Callback<User>() {
    @Override
    public void onResponse(Response<User> response) {
        int statusCode = response.code();
        User user = response.body();
    }
    @Override
    public void onFailure(Throwable t) {
        // Log error here since request failed
    }
});
```
同步请求
```
String username = "sarahjean";
Call<User> call = service.getUser(username);
User user = call.execute();
```
***在同步方法使用时可以直接调用execute方法，但是这个方法只能调用一次。解决办法：需要用clone方法生成一个新的之后在调用execute方法***
```
Call<List<Contributor>> call = service.repoContributors("square", "retrofit");
response = call.execute();
// This will throw IllegalStateException:
response = call.execute();
Call<List<Contributor>> call2 = call.clone();
// This will not throw:
response = call2.execute();
```
取消接口请求
```
call.cancel();
```
## 定义HTTP请求headers
Retrofit支持static和dynamic两种HTTP headers的定义方式。static headers对于所有的接口请求都是不可变的，dynamic headers需接口单独设置。
**static header**
第一种添加方式，通过给接口添加注解的方式
```
public interface UserService {
    @Headers("Cache-Control: max-age=640000")
    @Headers({
        "Accept: application/vnd.yourapi.v1.full+json",
        "User-Agent: Your-App-Name"
    })
    @GET("/tasks")
    Call<List<Task>> getTasks();
}
```
第二种方式，给Retrofit添加OKHttp Interceptor
```
OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
httpClient.addInterceptor(new Interceptor() {
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request original = chain.request();

        Request request = original.newBuilder()
            .header("User-Agent", "Your-App-Name")
            .header("Accept", "application/vnd.yourapi.v1.full+json")
            .method(original.method(), original.body())
            .build();

        return chain.proceed(request);
    }
}

OkHttpClient client = httpClient.build();
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl(API_BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .client(client)
    .build();
```
**Dynamic Header**
```
public interface UserService {
    @GET("/tasks")
    Call<List<Task>> getTasks(@Header("Content-Range") String contentRange);
}
```
Retrofit 2以上提供header重写功能，在Interceptor中Request.Builder提供了**.header(key, value)**和**.addHeader(key, value)**方法，前者会重写key值相同的属性，后者key值相同均会写入，不会被重写。
## CONVERTERS
Retrofit支持配置不同的converters,用于支持将结果解析为DAO。
-**Gson**: com.squareup.retrofit2:converter-gson
-**Jackson**: com.squareup.retrofit2:converter-jackson
-**Moshi**: com.squareup.retrofit2:converter-moshi
-**Protobuf**: com.squareup.retrofit2:converter-protobuf
-**Wire**: com.squareup.retrofit2:converter-wire
-**Simple** XML: com.squareup.retrofit2:converter-simplexml
-**Scalars** (primitives, boxed, and String): com.squareup.retrofit2:converter-scalars
使用方式
```
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("https://api.github.com")
    .addConverterFactory(GsonConverterFactory.create())
    .build();

GitHubService service = retrofit.create(GitHubService.class);
```
## URL定义方式
Retrofit 2.0使用了新的URL定义方式。Base URL与@Url 不是简单的组合在一起而是和\< a href="...">的处理方式一致。用下面的几个例子阐明。

![](http://www.jcodecraeer.com/uploads/20150915/1442298671884948.png)
![](http://www.jcodecraeer.com/uploads/20150915/1442298673477609.png)
![](http://www.jcodecraeer.com/uploads/20150915/1442298675317445.png)

推荐使用第二种方式：
- Base URL: 总是以 /结尾
- @Url: 不要以 / 开头
## 支持RxJava
在Retrofit Builder链表中如下调用addCallAdapterFactory：
```
Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://api.nuuneoi.com/base/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build();
```
Service接口现在可以作为Observable返回了
```
Observable<DessertItemCollectionDao> observable = service.loadDessertListRx();

observable.observeOn(AndroidSchedulers.mainThread())
    .subscribe(new Subscriber<DessertItemCollectionDao>() {
        @Override
        public void onCompleted() {
            Toast.makeText(getApplicationContext(),
                    "Completed",
                    Toast.LENGTH_SHORT)
                .show();
        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getApplicationContext(),
                    e.getMessage(),
                    Toast.LENGTH_SHORT)
                .show();
        }

        @Override
        public void onNext(DessertItemCollectionDao dessertItemCollectionDao) {
            Toast.makeText(getApplicationContext(),
                    dessertItemCollectionDao.getData().get(0).getName(),
                    Toast.LENGTH_SHORT)
                .show();
        }
    });
```