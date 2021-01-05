package gilbomi.com;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity{
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mWebView = (WebView)findViewById(R.id.webView);

        WebSettings mWebViewSetting = mWebView.getSettings();

        mWebView.setWebViewClient(new WebViewClientClass());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setNetworkAvailable(true);
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        mWebViewSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        mWebViewSetting.setJavaScriptEnabled(true);
        mWebViewSetting.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mWebViewSetting.setMediaPlaybackRequiresUserGesture(false);
        }
        mWebView.loadUrl("http://h2play.com:8100/intro");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { // 뒤로가기 버튼 이벤트
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebViewClientClass extends WebViewClient { // 페이지 이동
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("check URL",url);
            view.loadUrl(url);
            return true;
        }
    }
}
