package winterwell.jtwitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringBufferInputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.basic.HttpURLConnectionRequestAdapter;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.signature.AuthorizationHeaderSigningStrategy;
import oauth.signpost.signature.SigningStrategy;
import winterwell.jtwitter.guts.ClientHttpRequest;

public class OAuthSignpostClient
  extends URLConnectionHttpClient
  implements Twitter.IHttpClient, Serializable
{
  public final String postMultipartForm(String url, Map<String, ?> vars)
    throws TwitterException
  {
    String resource = checkRateLimit(url);
    try
    {
      HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
      connection.setRequestMethod("POST");
      connection.setReadTimeout(this.timeout);
      connection.setConnectTimeout(this.timeout);
      
      Map<String, String> vars2 = new HashMap();
      
      final String payload = post2_getPayload(vars2);
      
      HttpURLConnectionRequestAdapter wrapped = new HttpURLConnectionRequestAdapter(
        connection)
        {
          public InputStream getMessagePayload()
            throws IOException
          {
            return new StringBufferInputStream(payload);
          }
        };
        SimpleOAuthConsumer _consumer = new SimpleOAuthConsumer(this.consumerKey, this.consumerSecret);
        _consumer.setTokenWithSecret(this.accessToken, this.accessTokenSecret);
        SigningStrategy ss = new AuthorizationHeaderSigningStrategy();
        _consumer.setSigningStrategy(ss);
        _consumer.sign(wrapped);
        
        ClientHttpRequest req = new ClientHttpRequest(connection);
        InputStream page = req.post(vars);
        
        processError(connection, resource);
        processHeaders(connection, resource);
        return InternalUtils.read(page);
      }
      catch (TwitterException e)
      {
        throw e;
      }
      catch (Exception e)
      {
        throw new TwitterException(e);
      }
    }
    
    private static final DefaultOAuthProvider FOURSQUARE_PROVIDER = new DefaultOAuthProvider(
      "http://foursquare.com/oauth/request_token", 
      "http://foursquare.com/oauth/access_token", 
      "http://foursquare.com/oauth/authorize");
    private static final DefaultOAuthProvider LINKEDIN_PROVIDER = new DefaultOAuthProvider(
      "https://api.linkedin.com/uas/oauth/requestToken", 
      "https://api.linkedin.com/uas/oauth/accessToken", 
      "https://www.linkedin.com/uas/oauth/authorize");
    public static final String JTWITTER_OAUTH_KEY = "Cz8ZLgitPR2jrQVaD6ncw";
    public static final String JTWITTER_OAUTH_SECRET = "9FFYaWJSvQ6Yi5tctN30eN6DnXWmdw0QgJMl7V6KGI";
    private static final long serialVersionUID = 1L;
    private String accessToken;
    private String accessTokenSecret;
    private String callbackUrl;
    private OAuthConsumer consumer;
    private String consumerKey;
    private String consumerSecret;
    private DefaultOAuthProvider provider;
    
    public static String askUser(String question)
    {
      try
      {
        Class<?> JOptionPaneClass = 
          Class.forName("javax.swing.JOptionPane");
        Method showInputDialog = JOptionPaneClass.getMethod(
          "showInputDialog", new Class[] { Object.class });
        return (String)showInputDialog.invoke(null, new Object[] { question });
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    }
    
    public OAuthSignpostClient(String consumerKey, String consumerSecret, String callbackUrl)
    {
      assert ((consumerKey != null) && (consumerSecret != null) && 
        (callbackUrl != null));
      this.consumerKey = consumerKey;
      this.consumerSecret = consumerSecret;
      this.callbackUrl = callbackUrl;
      init();
    }
    
    public OAuthSignpostClient(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret)
    {
      this.consumerKey = consumerKey.toString();
      this.consumerSecret = consumerSecret.toString();
      this.accessToken = accessToken.toString();
      this.accessTokenSecret = accessTokenSecret.toString();
      init();
    }
    
    @Deprecated
    public void authorizeDesktop()
    {
      URI uri = authorizeUrl();
      try
      {
        Class<?> desktopClass = Class.forName("java.awt.Desktop");
        Method getDesktop = desktopClass.getMethod("getDesktop", null);
        Object d = getDesktop.invoke(null, null);
        
        Method browse = desktopClass.getMethod("browse", new Class[] { URI.class });
        browse.invoke(d, new Object[] { uri });
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    }
    
    public URI authorizeUrl()
    {
      try
      {
        String url = this.provider.retrieveRequestToken(this.consumer, this.callbackUrl);
        return new URI(url);
      }
      catch (Exception e)
      {
        throw new TwitterException(e);
      }
    }
    
    public boolean canAuthenticate()
    {
      return this.consumer.getToken() != null;
    }
    
    public Twitter.IHttpClient copy()
    {
      return clone();
    }
    
    public URLConnectionHttpClient clone()
    {
      OAuthSignpostClient c = (OAuthSignpostClient)super.clone();
      c.consumerKey = this.consumerKey;
      c.consumerSecret = this.consumerSecret;
      c.accessToken = this.accessToken;
      c.accessTokenSecret = this.accessTokenSecret;
      c.callbackUrl = this.callbackUrl;
      c.init();
      return c;
    }
    
    public String[] getAccessToken()
    {
      if (this.accessToken == null) {
        return null;
      }
      return new String[] { this.accessToken, this.accessTokenSecret };
    }
    
    String getName()
    {
      return this.name == null ? "?user" : this.name;
    }
    
    private void init()
    {
      this.consumer = new SimpleOAuthConsumer(this.consumerKey, this.consumerSecret);
      if (this.accessToken != null) {
        this.consumer.setTokenWithSecret(this.accessToken, this.accessTokenSecret);
      }
      this.provider = new DefaultOAuthProvider(
        "https://api.twitter.com/oauth/request_token", 
        "https://api.twitter.com/oauth/access_token", 
        "https://api.twitter.com/oauth/authorize");
    }
    
    public HttpURLConnection post2_connect(String uri, Map<String, String> vars)
      throws IOException, OAuthException
    {
      String resource = checkRateLimit(uri);
      HttpURLConnection connection = (HttpURLConnection)new URL(uri)
        .openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setRequestProperty("Content-Type", 
        "application/x-www-form-urlencoded");
      connection.setReadTimeout(this.timeout);
      connection.setConnectTimeout(this.timeout);
      final String payload = post2_getPayload(vars);
      
      HttpURLConnectionRequestAdapter wrapped = new HttpURLConnectionRequestAdapter(
        connection)
        {
          public InputStream getMessagePayload()
            throws IOException
          {
            return new StringBufferInputStream(payload);
          }
        };
        this.consumer.sign(wrapped);
        
        OutputStream os = connection.getOutputStream();
        os.write(payload.getBytes());
        InternalUtils.close(os);
        
        processError(connection, resource);
        processHeaders(connection, resource);
        return connection;
      }
      
      protected void setAuthentication(URLConnection connection, String name, String password)
      {
        try
        {
          this.consumer.sign(connection);
        }
        catch (OAuthException e)
        {
          throw new TwitterException(e);
        }
      }
      
      public void setAuthorizationCode(String verifier)
        throws TwitterException
      {
        if (this.accessToken != null)
        {
          this.accessToken = null;
          init();
        }
        try
        {
          this.provider.retrieveAccessToken(this.consumer, verifier);
          this.accessToken = this.consumer.getToken();
          this.accessTokenSecret = this.consumer.getTokenSecret();
        }
        catch (Exception e)
        {
          if (e.getMessage().contains("401")) {
            throw new TwitterException.E401(e.getMessage());
          }
          throw new TwitterException(e);
        }
      }
      
      public void setFoursquareProvider()
      {
        setProvider(FOURSQUARE_PROVIDER);
      }
      
      public void setLinkedInProvider()
      {
        setProvider(LINKEDIN_PROVIDER);
      }
      
      public void setName(String name)
      {
        this.name = name;
      }
      
      public void setProvider(DefaultOAuthProvider provider)
      {
        this.provider = provider;
      }
    }


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\OAuthSignpostClient.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */