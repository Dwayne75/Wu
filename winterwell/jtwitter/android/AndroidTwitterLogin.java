package winterwell.jtwitter.android;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.net.URI;
import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;

public abstract class AndroidTwitterLogin
{
  private String callbackUrl;
  private Activity context;
  OAuthSignpostClient client;
  
  public void setAuthoriseMessage(String authoriseMessage)
  {
    this.authoriseMessage = authoriseMessage;
  }
  
  public AndroidTwitterLogin(Activity myActivity, String oauthAppKey, String oauthAppSecret, String calbackUrl)
  {
    this.context = myActivity;
    this.consumerKey = oauthAppKey;
    this.consumerSecret = oauthAppSecret;
    this.callbackUrl = calbackUrl;
    this.client = new OAuthSignpostClient(this.consumerKey, this.consumerSecret, this.callbackUrl);
  }
  
  private String authoriseMessage = "Please authorize with Twitter";
  private String consumerSecret;
  private String consumerKey;
  
  public final void run()
  {
    Log.i("jtwitter", "TwitterAuth run!");
    final WebView webview = new WebView(this.context);
    webview.setBackgroundColor(-16777216);
    webview.setVisibility(0);
    final Dialog dialog = new Dialog(this.context, 16973834);
    dialog.setContentView(webview);
    dialog.show();
    
    webview.getSettings().setJavaScriptEnabled(true);
    webview.setWebViewClient(new WebViewClient()
    {
      public void onPageStarted(WebView view, String url, Bitmap favicon)
      {
        Log.d("jtwitter", "url: " + url);
        if (!url.contains(AndroidTwitterLogin.this.callbackUrl)) {
          return;
        }
        Uri uri = Uri.parse(url);
        String verifier = uri.getQueryParameter("oauth_verifier");
        if (verifier == null)
        {
          Log.i("jtwitter", "Auth-fail: " + url);
          dialog.dismiss();
          AndroidTwitterLogin.this.onFail(new Exception(url));
          return;
        }
        AndroidTwitterLogin.this.client.setAuthorizationCode(verifier);
        String[] tokens = AndroidTwitterLogin.this.client.getAccessToken();
        Twitter jtwitter = new Twitter(null, AndroidTwitterLogin.this.client);
        Log.i("jtwitter", "Authorised :)");
        dialog.dismiss();
        AndroidTwitterLogin.this.onSuccess(jtwitter, tokens);
      }
      
      public void onPageFinished(WebView view, String url)
      {
        Log.i("jtwitter", "url finished: " + url);
      }
    });
    webview.requestFocus(130);
    webview.setOnTouchListener(new View.OnTouchListener()
    {
      public boolean onTouch(View v, MotionEvent e)
      {
        if (((e.getAction() == 0) || 
          (e.getAction() == 1)) && 
          (!v.hasFocus())) {
          v.requestFocus();
        }
        return false;
      }
    });
    Toast.makeText(this.context, this.authoriseMessage, 0).show();
    Handler handler = new Handler();
    handler.postDelayed(new Runnable()
    {
      public void run()
      {
        try
        {
          URI authUrl = AndroidTwitterLogin.this.client.authorizeUrl();
          webview.loadUrl(authUrl.toString());
        }
        catch (Exception e)
        {
          AndroidTwitterLogin.this.onFail(e);
        }
      }
    }, 10L);
  }
  
  protected abstract void onSuccess(Twitter paramTwitter, String[] paramArrayOfString);
  
  protected void onFail(Exception e)
  {
    Toast.makeText(this.context, "Twitter authorisation failed?!", 1).show();
    Log.w("jtwitter", e.toString());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\android\AndroidTwitterLogin.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */