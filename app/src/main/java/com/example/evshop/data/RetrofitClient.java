// data/RetrofitClient.java
package com.example.evshop.data;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    private static ApiService api;

    // SỬA LẠI ĐỂ NHẮM THẲNG VÀO CỔNG HTTPS CỦA SERVER LOCAL
    private static final String BASE_URL = "https://10.0.2.2:7269";

    public static ApiService getApi(Context ctx) {
        if (api == null) {
            TokenManager tm = new TokenManager(ctx.getApplicationContext());
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            // TẠO MỘT CLIENT "KHÔNG AN TOÀN" ĐỂ CHẤP NHẬN MỌI CHỨNG CHỈ
            OkHttpClient client = getUnsafeOkHttpClient(tm, log);

            Gson gson = new GsonBuilder().setLenient().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // SỬ DỤNG CLIENT "KHÔNG AN TOÀN" NÀY
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            api = retrofit.create(ApiService.class);
        }
        return api;
    }

    // PHƯƠNG THỨC NÀY TẠO RA MỘT CLIENT BỎ QUA VIỆC XÁC THỰC SSL
    private static OkHttpClient getUnsafeOkHttpClient(TokenManager tm, HttpLoggingInterceptor log) {
        try {
            // Tạo một TrustManager chấp nhận mọi thứ
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Cài đặt TrustManager "dễ dãi"
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Tạo một SSLSocketFactory với TrustManager đó
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true); // Chấp nhận mọi hostname

            // Thêm các interceptor của bạn
            builder.addInterceptor(new AuthInterceptor(tm));
            builder.addInterceptor(log);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
