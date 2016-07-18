package com.ethanco.retrofit2_0test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    //聚合提供的免费开放接口，和自行申请
    String apiKey = "83f9ef77f0ccca3cca30710d8ce97c4c";
    String baseUrl = "http://apis.juhe.cn/";

    private TextView textView, textViewRxJava;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tv_details);
        textViewRxJava = (TextView) findViewById(R.id.tv_details_rxjava);
        getIdCardDetail();
        getByRxJava();

    }

    private void getByRxJava() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        APIService service = retrofit.create(APIService.class);
        Observable<IdCardEntity> observable = service.getIdCardDetailsWithRx("220182198708010915", "json", apiKey);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<IdCardEntity>() {
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
                                "Error:" + e.getMessage(),
                                Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onNext(IdCardEntity idCardEntity) {
                        IdCardEntity.ResultEntity result = idCardEntity.getResult();
                        String info = "handled by RxJava:\n\r";
                        if (result != null) {
                            info += "area:" + result.getArea() + "\n\r";
                            info += "sex:" + result.getSex() + "\n\r";
                            info += "birthday:" + result.getBirthday() + "\n\r";
                            textViewRxJava.setText(info);
                        } else {
                            Toast.makeText(MainActivity.this, "结果解析错误", Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    private void getIdCardDetail() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        APIService service = retrofit.create(APIService.class);
        Call<IdCardEntity> call = service.getIdCardDetails("220182198708010915", "json", apiKey);
        call.enqueue(new Callback<IdCardEntity>() {
            @Override
            public void onResponse(Response<IdCardEntity> response, Retrofit retrofit) {
                if (response.body() != null) {
                    IdCardEntity idCardEntity = response.body();
                    IdCardEntity.ResultEntity result = idCardEntity.getResult();
                    String info = "";
                    if (result != null) {
                        info += "area:" + result.getArea() + "\n\r";
                        info += "sex:" + result.getSex() + "\n\r";
                        info += "birthday:" + result.getBirthday() + "\n\r";
                        textView.setText(info);
                    } else {
                        Toast.makeText(MainActivity.this, "结果解析错误", Toast.LENGTH_LONG).show();

                    }
                } else {
                    Toast.makeText(MainActivity.this, "没有返回数据", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(MainActivity.this, "接口请求失败", Toast.LENGTH_LONG).show();
            }
        });
    }
}
