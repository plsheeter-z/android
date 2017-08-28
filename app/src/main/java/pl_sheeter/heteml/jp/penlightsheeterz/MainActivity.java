package pl_sheeter.heteml.jp.penlightsheeterz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView psWebView = (WebView) findViewById(R.id.webview_penlight_sheeter);

        psWebView.setWebChromeClient(new WebChromeClient());

        WebSettings settings = psWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowUniversalAccessFromFileURLs(true);

        psWebView.loadUrl("http://souseiji.heteml.jp/kingbz/");
    }
}
