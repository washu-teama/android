package com.kaist.washu.android;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.prefs.Preferences;


public class MainActivity extends AppCompatActivity {
    private String TAG = "WashuMainActivity";

    //public static final String HOST = "http://15.164.7.33:8000";
    public static final String HOST = "http://143.248.55.31:8000";


    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //<< this
        setContentView(R.layout.activity_main);
        if (this.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs phone state information");
            builder.setMessage("Please grant phone state information access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                }
            });
            builder.show();
        }

        bluetoothSetting();
        webBiewSetting();
    }
    void webBiewSetting(){
        mWebView = findViewById(R.id.activity_main_webview);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }


        mWebView.setWebViewClient(new WebViewClient(){
            /**
             @Override
             public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
             handler.proceed();
             }**/

            String bef = "";
            @Override public void onPageFinished(WebView view, String url){
                /*
                if (bef.equals("http://54.180.12.69:8000/user/login/")){
                    SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                    String token =  pref.getString("token", "");

                    view.loadUrl("http://54.180.12.69:8000/user/fcm/register?token="+token);
                    Log.e("asdf",url);
                }
                bef = url;
                */
            }


            //@SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return true;
            }


            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                String message = "SSL Certificate error.";
                switch (error.getPrimaryError()) {
                    case SslError.SSL_UNTRUSTED:
                        message = "The certificate authority is not trusted.";
                        break;
                    case SslError.SSL_EXPIRED:
                        message = "The certificate has expired.";
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = "The certificate Hostname mismatch.";
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = "The certificate is not yet valid.";
                        break;
                }
                message += " Do you want to continue anyway?";

                builder.setTitle("SSL Certificate Error");
                builder.setMessage(message);
                builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }


            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
                switch(errorCode) {
                    case ERROR_AUTHENTICATION:break;               // 서버에서 사용자 인증 실패
                    case ERROR_BAD_URL:break;                           // 잘못된 URL
                    case ERROR_CONNECT: break;                          // 서버로 연결 실패
                    case ERROR_FAILED_SSL_HANDSHAKE:break;    // SSL handshake 수행 실패
                    case ERROR_FILE:break;                                  // 일반 파일 오류
                    case ERROR_FILE_NOT_FOUND:break;               // 파일을 찾을 수 없습니다
                    case ERROR_HOST_LOOKUP:break;           // 서버 또는 프록시 호스트 이름 조회 실패
                    case ERROR_IO:break;                              // 서버에서 읽거나 서버로 쓰기 실패
                    case ERROR_PROXY_AUTHENTICATION:break;   // 프록시에서 사용자 인증 실패
                    case ERROR_REDIRECT_LOOP:break;               // 너무 많은 리디렉션
                    case ERROR_TIMEOUT:break;                          // 연결 시간 초과
                    case ERROR_TOO_MANY_REQUESTS:break;     // 페이지 로드중 너무 많은 요청 발생
                    case ERROR_UNKNOWN:break;                        // 일반 오류
                    case ERROR_UNSUPPORTED_AUTH_SCHEME:break; // 지원되지 않는 인증 체계
                    case ERROR_UNSUPPORTED_SCHEME:break;          // URI가 지원되지 않는 방식
                }
                super.onReceivedError(view, errorCode, description, failingUrl);

                Log.e("onReceivedError", Integer.toString(errorCode));
                mWebView.setVisibility(View.GONE);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override

            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("알림")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message,
                                       final JsResult result){
                new AlertDialog.Builder(view.getContext())
                        .setTitle("알림")
                        .setMessage(message)
                        .setPositiveButton("yes",
                                new AlertDialog.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which){
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("No",
                                new AlertDialog.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which){
                                        result.cancel();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;

            }
        });

        mWebView.addJavascriptInterface(new AndroidBridge(), "android");
        mWebView.loadUrl(HOST +"/user/login");
        Log.i(TAG, "url: " + HOST +"/user/login");
    }
    void bluetoothSetting() {
        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();


        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
        startScanning();
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if(result.getDevice().getName() != null){
                ContentValues values = new ContentValues();

                Preferences prefs = Preferences.userNodeForPackage(MainActivity.class);

                values.put("id", prefs.get("id", ""));
                values.put("pwd", prefs.get("password", ""));
                values.put("uuid", getTelePhoneUUID());
                values.put("device_addr", result.getDevice().getAddress());
                values.put("device_name", result.getDevice().getName());
                NetworkTask task = new NetworkTask(HOST+"/user/IAmHere/", values);
                task.execute();
                Log.i(TAG, "Device Name: " + result.getDevice().getName() + " mac: " + result.getDevice().getAddress());
            }

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void startScanning() {
        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setDeviceAddress("B8:27:EB:7C:B7:7B");
        ScanFilter filter = builder.build();
        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(filter);
        Log.i(TAG, "start scanning");
        ScanSettings settings = new ScanSettings.Builder().build();
        btScanner.startScan(Collections.singletonList(new ScanFilter.Builder().setDeviceAddress("B8:27:EB:7C:B7:7B").build()),
                settings,leScanCallback);
    }

    String getTelePhoneUUID(){
        String deviceId;
        if (this.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs phone state information");
            builder.setMessage("Please grant phone state information access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                }
            });
            builder.show();
            return "";
        } else{
        }
        if (Build.VERSION.SDK_INT >= 26) {
            deviceId = getSystemService(TelephonyManager.class).getImei();
        }else{
            deviceId = getSystemService(TelephonyManager.class).getDeviceId();
        }
        return deviceId;
    }
    private class AndroidBridge {
        @JavascriptInterface
        public void setLoginInfo(final String id, final String password) {
            Preferences prefs = Preferences.userNodeForPackage(MainActivity.class);
            prefs.put("id", id);
            prefs.put("password", password);
            Toast.makeText(getApplicationContext(), String.format("id: %s password: %s",id, password), Toast.LENGTH_LONG).show();
        }
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result; // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
