package com.example.helloweatherproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.helloweatherproject.WeatherClass.WeatherDailyForecast;
import com.example.helloweatherproject.WeatherClass.WeatherLifestyle;
import com.example.helloweatherproject.WeatherClass.WeatherResponseModel;
import com.example.helloweatherproject.util.HttpUtil;
import com.example.helloweatherproject.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {


    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout foreCastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comforttext;

    private TextView carWashText;

    private TextView sportText;


    private ImageView bingImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

//        if (Build.VERSION.SDK_INT >= 21){
//
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }

        //初始化各个控件


        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comforttext = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        foreCastLayout = (LinearLayout) findViewById(R.id.forecast_layout);





        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);
        if (weatherString != null) {
            //有缓存
            WeatherResponseModel model = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(model, "now");
        } else {
            //请求数据
            //接口允许城市名获取数据，并且city id对应不上
            String cityName = getIntent().getStringExtra("city_name");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(cityName, "now");
            requestWeather(cityName, "forecast");
            requestWeather(cityName, "lifestyle");
            requestWeather(cityName, "air");
        }

        bingImage = (ImageView) findViewById(R.id.bing_piv_img);
        String bingPic = preferences.getString("bing_pic", null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingImage);
        }else {


        }

    }


    public void requestWeather(final String CityName, final String requestType) {

        String weatherUrl = "";
        if (requestType == "now") {

            weatherUrl = "https://api.heweather.net/s6/weather/now?location=" + CityName + "&key=5cd68170941c4d13ab5c664263c26af7";
            loadbingPic();

        } else if (requestType == "forecast") {

            weatherUrl = "https://api.heweather.net/s6/weather/forecast?location=" + CityName + "&key=5cd68170941c4d13ab5c664263c26af7";

        } else if (requestType == "lifestyle") {

            weatherUrl = "https://api.heweather.net/s6/weather/lifestyle?location=" + CityName + "&key=5cd68170941c4d13ab5c664263c26af7";

        } else if (requestType == "air") {

            weatherUrl = "https://api.heweather.net/s6/air/now?location=" + CityName + "&key=5cd68170941c4d13ab5c664263c26af7";

        }
        HttpUtil.sendOkHtttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responsetext = response.body().string();
                Log.d("aaaaaaaa", "onResponse: " + responsetext);
                final WeatherResponseModel model = Utility.handleWeatherResponse(responsetext);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (model != null && "ok".equals(model.status)) {
                            //不做缓存
//                            SharedPreferences.Editor editor =  PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
//                            editor.putString("weather",responsetext);
//                            editor.apply();
                            showWeatherInfo(model, requestType);

                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败,无返回数据", Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        });
    }


    private void showWeatherInfo(WeatherResponseModel weatherResponseModel, String responsetype) {

        if ("now".equals(responsetype)) {

            String cityName = weatherResponseModel.basic.location;
            String updateTime = weatherResponseModel.update.utc.split(" ")[1];
            String degree = weatherResponseModel.now.tmp + "℃";
            String weatherInfo = weatherResponseModel.now.cond_txt;
            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);

        } else if ("forecast".equals(responsetype)) {


            foreCastLayout.removeAllViews();


            for (WeatherDailyForecast forecast :
                    weatherResponseModel.daily_forecast) {
                View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, foreCastLayout, false);
                TextView datetext = (TextView) view.findViewById(R.id.date_text);
                TextView infoText = (TextView) view.findViewById(R.id.info_text);
                TextView maxText = (TextView) view.findViewById(R.id.max_text);
                TextView minText = (TextView) view.findViewById(R.id.min_text);

                datetext.setText(forecast.date);
                infoText.setText(forecast.cond_txt_d);
                maxText.setText(forecast.tmp_max);
                minText.setText(forecast.tmp_min);
                foreCastLayout.addView(view);

            }

        } else if ("lifestyle".equals(responsetype)) {

            for (WeatherLifestyle lifeStyle : weatherResponseModel.lifestyle) {

                if ("comf".equals(lifeStyle.type)) {
                    String text = "舒适度：" + lifeStyle.brf + "\n" + lifeStyle.txt;
                    comforttext.setText(text);
                } else if ("cw".equals(lifeStyle.type)) {
                    String text = "洗车指数：" + lifeStyle.brf + "\n" + lifeStyle.txt;
                    carWashText.setText(text);
                } else if ("sport".equals(lifeStyle.type)) {
                    String text = "运动指数：" + lifeStyle.brf + "\n" + lifeStyle.txt;
                    sportText.setText(text);
                }

            }
        } else if ("air".equals(responsetype)) {
            if (weatherResponseModel.air_now_city != null) {
                aqiText.setText(weatherResponseModel.air_now_city.aqi);
                pm25Text.setText(weatherResponseModel.air_now_city.pm25);
            }
        }
        weatherLayout.setVisibility(View.VISIBLE);
    }


    private   void  loadbingPic(){
        String requestUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHtttpRequest(requestUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String bing_pic = response.body().string();

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bing_pic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bing_pic).into(bingImage);
                    }
                });

            }
        });
    }
}
