package is.jacek.markowski.dictionary.keepest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import is.jacek.markowski.dictionary.keepest.main_activity.fragment.TabGifsWordFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

public class GoogleSearchActivity extends AppCompatActivity {
    private WebView webView;
    public static int RESULT_IMAGE_FOUND = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_search);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(TabGifsWordFragment.EXTRA_MESSAGE);

        webView = findViewById(R.id.webview_google);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(false);
        try {
            webView.loadUrl("https://www.google.com/search?q=" + URLEncoder.encode(message, StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e) {
            webView.loadUrl("https://www.google.com/search?q=");
            e.printStackTrace();
        }
        registerForContextMenu(webView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        WebView.HitTestResult hitTestResult = webView.getHitTestResult();
        int resultType;
        if (hitTestResult != null) {
            resultType = hitTestResult.getType();
        } else {
            resultType = WebView.HitTestResult.UNKNOWN_TYPE;
        }
        if (WebView.HitTestResult.IMAGE_TYPE == resultType
                || WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE == resultType) {
            String urlOfImage = hitTestResult.getExtra();
            WordManager.WordEdit.saveTextItem(getApplicationContext(), WordDialogFragment.IMAGE_KEY, urlOfImage);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("url", urlOfImage);
            setResult(RESULT_IMAGE_FOUND, resultIntent);
            finish();
        }
    }

    static class Browser extends WebViewClient {

    }
}
