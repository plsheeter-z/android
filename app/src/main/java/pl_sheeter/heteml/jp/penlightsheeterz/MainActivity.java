package pl_sheeter.heteml.jp.penlightsheeterz;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.os.Handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private String dataUri = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WebView psWebView = (WebView) findViewById(R.id.webview_penlight_sheeter);

        WebSettings settings = psWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowUniversalAccessFromFileURLs(true);

        psWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (Build.VERSION.SDK_INT < 24) {
                    view.loadUrl("javascript:window.MainActivity.jsInterFace(document.getElementById('dllink').removeAttribute('download'));");
                }
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("data:image/png;base64,")) {
                    Toast.makeText(MainActivity.this, "開始します", Toast.LENGTH_SHORT).show();

                    dataUri = url;
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                    },
                                    1);
                        }
                    } else {
                        String absolutePath = savePNG(url);
                        if (null != absolutePath) {
                            addGallery(absolutePath);
                            Toast.makeText(MainActivity.this, "保存しました", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "保存できませんでした", Toast.LENGTH_LONG).show();
                        }
                    }

                } else {
                    view.loadUrl(url);
                }

                return true;
            }
        });
        psWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (url.startsWith("data:image/png;base64,")) {
                    System.out.println("#### Debug");
                    Toast.makeText(MainActivity.this, "開始します", Toast.LENGTH_SHORT).show();
                    dataUri = url;
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                    },
                                    1);
                        }
                    }
                }
            }
        });

        psWebView.addJavascriptInterface(this, "MainActivity");
        psWebView.loadUrl("http://souseiji.heteml.jp/kingbz/");
    }

    @JavascriptInterface
    public void jsInterFace(final String src) {
        handler.post(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permisions, int[] grantResults) {
        if (1 == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String absolutePath = savePNG(dataUri);
                if (null != absolutePath) {
                    addGallery(absolutePath);
                    Toast.makeText(MainActivity.this, "保存しました", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "保存できませんでした", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "拒否されました", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private String savePNG(final String base64Character) {
        String dirPath = Environment.getExternalStorageDirectory().getPath() + "/penlightsheeterz/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        File pngPath = new File(dir.getAbsolutePath() + "/" + "plsz_" + sdf.format(date) + ".png");

        String data = base64Character.replaceFirst("data:image/png;base64,", "");
        byte[] bytes = Base64.decode(data, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        bmp.setDensity(240); // 解像度

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pngPath);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (IOException ioe) {

        } finally {
            try {
                fos.close();
            } catch (IOException e) {

            }
        }

        return pngPath.getAbsolutePath();
    }

    private void addGallery(String absolutePath) {
        File file = new File(absolutePath);

        ContentValues values = new ContentValues();
        ContentResolver resolver = getContentResolver();

        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.SIZE, file.length());
        values.put(MediaStore.Images.Media.TITLE, file.getName());
        values.put(MediaStore.Images.Media.DATA, file.getPath());

        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}