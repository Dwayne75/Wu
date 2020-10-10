package winterwell.jtwitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.MalformedInputException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import winterwell.json.JSONArray;
import winterwell.json.JSONObject;
import winterwell.jtwitter.guts.Base64Encoder;

public class URLConnectionHttpClient
  implements Twitter.IHttpClient, Serializable, Cloneable
{
  private static final int dfltTimeOutMilliSecs = 10000;
  private static final long serialVersionUID = 1L;
  private Map<String, List<String>> headers;
  int minRateLimit;
  protected String name;
  private String password;
  private Map<String, RateLimit> rateLimits = Collections.synchronizedMap(new HashMap());
  boolean retryOnError;
  
  public boolean isRetryOnError()
  {
    return this.retryOnError;
  }
  
  protected int timeout = 10000;
  private boolean htmlImpliesError = true;
  private boolean gzip = false;
  
  public void setGzip(boolean gzip)
  {
    this.gzip = gzip;
  }
  
  public void setHtmlImpliesError(boolean htmlImpliesError)
  {
    this.htmlImpliesError = htmlImpliesError;
  }
  
  public URLConnectionHttpClient()
  {
    this(null, null);
  }
  
  public URLConnectionHttpClient(String name, String password)
  {
    this.name = name;
    this.password = password;
    assert (((name != null) && (password != null)) || (
      (name == null) && (password == null)));
  }
  
  public boolean canAuthenticate()
  {
    return (this.name != null) && (this.password != null);
  }
  
  public HttpURLConnection connect(String url, Map<String, String> vars, boolean authenticate)
    throws IOException
  {
    String resource = checkRateLimit(url);
    if ((vars != null) && (vars.size() != 0))
    {
      StringBuilder uri = new StringBuilder(url);
      if (url.indexOf('?') == -1) {
        uri.append("?");
      } else if (!url.endsWith("&")) {
        uri.append("&");
      }
      for (Map.Entry e : vars.entrySet()) {
        if (e.getValue() != null)
        {
          String ek = InternalUtils.encode(e.getKey());
          assert (!url.contains(ek + "=")) : (url + " " + vars);
          uri.append(ek + "=" + InternalUtils.encode(e.getValue()) + "&");
        }
      }
      url = uri.toString();
    }
    HttpURLConnection connection = (HttpURLConnection)new URL(url)
      .openConnection();
    if (authenticate) {
      setAuthentication(connection, this.name, this.password);
    }
    connection.setRequestProperty("User-Agent", "JTwitter/2.8.7");
    connection.setRequestProperty("Host", "api.twitter.com");
    if (this.gzip) {
      connection.setRequestProperty("Accept-Encoding", "gzip");
    }
    connection.setDoInput(true);
    connection.setConnectTimeout(this.timeout);
    connection.setReadTimeout(this.timeout);
    connection.setConnectTimeout(this.timeout);
    
    processError(connection, resource);
    processHeaders(connection, resource);
    return connection;
  }
  
  public Twitter.IHttpClient copy()
  {
    return clone();
  }
  
  public URLConnectionHttpClient clone()
  {
    try
    {
      URLConnectionHttpClient c = (URLConnectionHttpClient)super.clone();
      c.name = this.name;
      c.password = this.password;
      c.gzip = this.gzip;
      c.htmlImpliesError = this.htmlImpliesError;
      c.setRetryOnError(this.retryOnError);
      c.setTimeout(this.timeout);
      c.setMinRateLimit(this.minRateLimit);
      c.rateLimits = this.rateLimits;
      
      return c;
    }
    catch (CloneNotSupportedException ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  protected final void disconnect(HttpURLConnection connection)
  {
    if (connection == null) {
      return;
    }
    try
    {
      connection.disconnect();
    }
    catch (Throwable localThrowable) {}
  }
  
  private String getErrorStream(HttpURLConnection connection)
  {
    try
    {
      return InternalUtils.read(connection.getErrorStream());
    }
    catch (NullPointerException e) {}
    return null;
  }
  
  public String getHeader(String headerName)
  {
    if (this.headers == null) {
      return null;
    }
    List<String> vals = (List)this.headers.get(headerName);
    return (vals == null) || (vals.isEmpty()) ? null : (String)vals.get(0);
  }
  
  String getName()
  {
    return this.name;
  }
  
  public Map<String, RateLimit> getRateLimits()
  {
    return this.rateLimits;
  }
  
  public Map<String, RateLimit> updateRateLimits()
  {
    Map<String, String> vars = null;
    String json = getPage("https://api.twitter.com/1.1/application/rate_limit_status.json", vars, true);
    
    JSONObject jo = new JSONObject(json).getJSONObject("resources");
    
    Collection<JSONObject> families = jo.getMap().values();
    Iterator localIterator2;
    for (Iterator localIterator1 = families.iterator(); localIterator1.hasNext(); localIterator2.hasNext())
    {
      JSONObject family = (JSONObject)localIterator1.next();
      localIterator2 = family.getMap().keySet().iterator(); continue;String res = (String)localIterator2.next();
      
      JSONObject jrl = (JSONObject)family.getMap().get(res);
      RateLimit rl = new RateLimit(jrl);
      this.rateLimits.put(res, rl);
    }
    return getRateLimits();
  }
  
  public final String getPage(String url, Map<String, String> vars, boolean authenticate)
    throws TwitterException
  {
    assert (url != null);
    InternalUtils.count(url);
    try
    {
      String json = getPage2(url, vars, authenticate);
      if ((this.htmlImpliesError) && (
        (json.startsWith("<!DOCTYPE html")) || (json.startsWith("<html")))) {
        if (!url.startsWith("https://twitter.com"))
        {
          String meat = InternalUtils.stripTags(json);
          throw new TwitterException.E50X(meat);
        }
      }
      return json;
    }
    catch (SocketTimeoutException e)
    {
      if (!this.retryOnError) {
        throw getPage2_ex(e, url);
      }
      try
      {
        Thread.sleep(500L);
        return getPage2(url, vars, authenticate);
      }
      catch (Exception e2)
      {
        throw getPage2_ex(e, url);
      }
    }
    catch (TwitterException.E50X e)
    {
      if (!this.retryOnError) {
        throw getPage2_ex(e, url);
      }
      try
      {
        Thread.sleep(500L);
        return getPage2(url, vars, authenticate);
      }
      catch (Exception e2)
      {
        throw getPage2_ex(e, url);
      }
    }
    catch (IOException e)
    {
      throw new TwitterException.IO(e);
    }
  }
  
  private TwitterException getPage2_ex(Exception ex, String url)
  {
    if ((ex instanceof TwitterException)) {
      return (TwitterException)ex;
    }
    if ((ex instanceof SocketTimeoutException)) {
      return new TwitterException.Timeout(url);
    }
    if ((ex instanceof IOException)) {
      return new TwitterException.IO((IOException)ex);
    }
    return new TwitterException(ex);
  }
  
  private String getPage2(String url, Map<String, String> vars, boolean authenticate)
    throws IOException
  {
    HttpURLConnection connection = null;
    try
    {
      connection = connect(url, vars, authenticate);
      InputStream inStream = connection.getInputStream();
      
      String contentEncoding = connection.getContentEncoding();
      if ("gzip".equals(contentEncoding)) {
        inStream = new GZIPInputStream(inStream);
      }
      String page = InternalUtils.read(inStream);
      
      return page;
    }
    catch (MalformedInputException ex)
    {
      throw new IOException(ex + " enc:" + connection.getContentEncoding());
    }
    finally
    {
      disconnect(connection);
    }
  }
  
  public RateLimit getRateLimit(Twitter.KRequestType reqType)
  {
    return (RateLimit)this.rateLimits.get(reqType.rateLimit);
  }
  
  public final String post(String uri, Map<String, String> vars, boolean authenticate)
    throws TwitterException
  {
    InternalUtils.count(uri);
    try
    {
      return post2(uri, vars, authenticate);
    }
    catch (TwitterException.E50X e)
    {
      if (!this.retryOnError) {
        throw getPage2_ex(e, uri);
      }
      try
      {
        Thread.sleep(500L);
        return post2(uri, vars, authenticate);
      }
      catch (Exception e2)
      {
        throw getPage2_ex(e, uri);
      }
    }
    catch (SocketTimeoutException e)
    {
      if (!this.retryOnError) {
        throw getPage2_ex(e, uri);
      }
      try
      {
        Thread.sleep(500L);
        return post2(uri, vars, authenticate);
      }
      catch (Exception e2)
      {
        throw getPage2_ex(e, uri);
      }
    }
    catch (Exception e)
    {
      throw getPage2_ex(e, uri);
    }
  }
  
  private String post2(String uri, Map<String, String> vars, boolean authenticate)
    throws Exception
  {
    HttpURLConnection connection = null;
    try
    {
      connection = post2_connect(uri, vars);
      
      String response = InternalUtils.read(connection
        .getInputStream());
      return response;
    }
    finally
    {
      disconnect(connection);
    }
  }
  
  public HttpURLConnection post2_connect(String uri, Map<String, String> vars)
    throws Exception
  {
    String resource = checkRateLimit(uri);
    InternalUtils.count(uri);
    HttpURLConnection connection = (HttpURLConnection)new URL(uri)
      .openConnection();
    connection.setRequestMethod("POST");
    connection.setDoOutput(true);
    
    setAuthentication(connection, this.name, this.password);
    
    connection.setRequestProperty("Content-Type", 
      "application/x-www-form-urlencoded");
    connection.setReadTimeout(this.timeout);
    connection.setConnectTimeout(this.timeout);
    
    String payload = post2_getPayload(vars);
    connection.setRequestProperty("Content-Length", payload.length());
    OutputStream os = connection.getOutputStream();
    os.write(payload.getBytes());
    InternalUtils.close(os);
    
    processError(connection, resource);
    processHeaders(connection, resource);
    return connection;
  }
  
  protected String checkRateLimit(String url)
  {
    String resource = RateLimit.getResource(url);
    RateLimit limit = (RateLimit)this.rateLimits.get(resource);
    if ((limit != null) && (limit.getRemaining() <= this.minRateLimit) && 
      (!limit.isOutOfDate())) {
      throw new TwitterException.RateLimit(
        "Pre-emptive rate-limit block for " + limit + " for " + url);
    }
    return resource;
  }
  
  protected String post2_getPayload(Map<String, String> vars)
  {
    if ((vars == null) || (vars.isEmpty())) {
      return "";
    }
    StringBuilder encodedData = new StringBuilder();
    String val;
    if (vars.size() == 1)
    {
      String key = (String)vars.keySet().iterator().next();
      if ("".equals(key))
      {
        val = InternalUtils.encode(vars.get(key));
        return val;
      }
    }
    for (String key : vars.keySet())
    {
      String val = InternalUtils.encode(vars.get(key));
      encodedData.append(InternalUtils.encode(key));
      encodedData.append('=');
      encodedData.append(val);
      encodedData.append('&');
    }
    encodedData.deleteCharAt(encodedData.length() - 1);
    return encodedData.toString();
  }
  
  final void processError(HttpURLConnection connection, String resource)
  {
    try
    {
      int code = connection.getResponseCode();
      if (code == 200) {
        return;
      }
      URL url = connection.getURL();
      
      String error = processError2_reason(connection);
      if (code == 401)
      {
        if (error.contains("Basic authentication is not supported")) {
          throw new TwitterException.UpdateToOAuth();
        }
        throw new TwitterException.E401(error + "\n" + url + " (" + (
          this.name == null ? "anonymous" : this.name) + ")");
      }
      if ((code == 400) && (error.startsWith("code 215"))) {
        throw new TwitterException.E401(error);
      }
      if (code == 403) {
        processError2_403(connection, resource, url, error);
      }
      if (code == 404)
      {
        if ((error != null) && (error.contains("deleted"))) {
          throw new TwitterException.SuspendedUser(error + "\n" + url);
        }
        throw new TwitterException.E404(error + "\n" + url);
      }
      if (code == 406) {
        throw new TwitterException.E406(error + "\n" + url);
      }
      if (code == 413) {
        throw new TwitterException.E413(error + "\n" + url);
      }
      if (code == 416) {
        throw new TwitterException.E416(error + "\n" + url);
      }
      if (code == 420) {
        throw new TwitterException.TooManyLogins(error + "\n" + url);
      }
      if ((code >= 500) && (code < 600)) {
        throw new TwitterException.E50X(error + "\n" + url);
      }
      processError2_rateLimit(connection, resource, code, error);
      if ((code > 299) && (code < 400))
      {
        String locn = connection.getHeaderField("Location");
        throw new TwitterException(code + " " + error + " " + url + " -> " + locn);
      }
      throw new TwitterException(code + " " + error + " " + url);
    }
    catch (SocketTimeoutException e)
    {
      URL url = connection.getURL();
      throw new TwitterException.Timeout(this.timeout + "milli-secs for " + 
        url);
    }
    catch (ConnectException e)
    {
      URL url = connection.getURL();
      throw new TwitterException.Timeout(url.toString());
    }
    catch (SocketException e)
    {
      throw new TwitterException.E50X(e.toString());
    }
    catch (IOException e)
    {
      throw new TwitterException(e);
    }
  }
  
  private String processError2_reason(HttpURLConnection connection)
    throws IOException
  {
    String errorPage = readErrorPage(connection);
    if (errorPage != null) {
      try
      {
        JSONObject je = new JSONObject(errorPage);
        Object error = je.get("errors");
        if ((error instanceof JSONArray))
        {
          JSONObject err = ((JSONArray)error).getJSONObject(0);
          return "code " + err.get("code") + ": " + err.getString("message");
        }
        if ((error instanceof String)) {
          return (String)error;
        }
      }
      catch (Exception localException) {}
    }
    String error = connection.getResponseMessage();
    Map<String, List<String>> connHeaders = connection.getHeaderFields();
    List<String> errorMessage = (List)connHeaders.get(null);
    if ((errorMessage != null) && (!errorMessage.isEmpty())) {
      error = error + "\n" + (String)errorMessage.get(0);
    }
    if ((errorPage != null) && (!errorPage.isEmpty())) {
      error = error + "\n" + errorPage;
    }
    return error;
  }
  
  private void processError2_403(HttpURLConnection connection, String resource, URL url, String errorPage)
  {
    String _name = this.name == null ? "anon" : this.name;
    if (errorPage == null) {
      throw new TwitterException.E403(url + " (" + _name + ")");
    }
    if ((errorPage.startsWith("code 185")) || (errorPage.contains("Wow, that's a lot of Twittering!")))
    {
      processHeaders(connection, resource);
      throw new TwitterException.RateLimit(errorPage);
    }
    if (errorPage.contains("too old")) {
      throw new TwitterException.BadParameter(errorPage + "\n" + url);
    }
    if (errorPage.contains("suspended")) {
      throw new TwitterException.SuspendedUser(errorPage + "\n" + url);
    }
    if (errorPage.contains("Could not find")) {
      throw new TwitterException.SuspendedUser(errorPage + "\n" + url);
    }
    if (errorPage.contains("too recent")) {
      throw new TwitterException.TooRecent(errorPage + "\n" + url);
    }
    if (errorPage.contains("already requested to follow")) {
      throw new TwitterException.Repetition(errorPage + "\n" + url);
    }
    if (errorPage.contains("duplicate")) {
      throw new TwitterException.Repetition(errorPage);
    }
    if (errorPage.contains("unable to follow more people")) {
      throw new TwitterException.FollowerLimit(this.name + " " + errorPage);
    }
    if (errorPage.contains("application is not allowed to access")) {
      throw new TwitterException.AccessLevel(this.name + " " + errorPage);
    }
    throw new TwitterException.E403(errorPage + "\n" + url + " (" + _name + ")");
  }
  
  private void processError2_rateLimit(HttpURLConnection connection, String resource, int code, String error)
  {
    boolean rateLimitExceeded = error.contains("Rate limit exceeded");
    if (rateLimitExceeded)
    {
      processHeaders(connection, resource);
      throw new TwitterException.RateLimit(getName() + ": " + error);
    }
    if (code == 400) {
      try
      {
        String json = getPage(
          "http://twitter.com/account/rate_limit_status.json", 
          null, this.password != null);
        JSONObject obj = new JSONObject(json);
        int hits = obj.getInt("remaining_hits");
        if (hits < 1) {
          throw new TwitterException.RateLimit(error);
        }
      }
      catch (Exception localException) {}
    }
  }
  
  protected final void processHeaders(HttpURLConnection connection, String resource)
  {
    this.headers = connection.getHeaderFields();
    updateRateLimits(resource);
  }
  
  static String readErrorPage(HttpURLConnection connection)
  {
    InputStream stream = connection.getErrorStream();
    if (stream == null) {
      return null;
    }
    try
    {
      if ("gzip".equals(connection.getHeaderField("Content-Encoding"))) {
        stream = new GZIPInputStream(stream);
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(
        stream));
      int bufSize = 8192;
      
      StringBuilder sb = new StringBuilder(8192);
      char[] cbuf = new char[' '];
      String str;
      try
      {
        for (;;)
        {
          int chars = reader.read(cbuf);
          if (chars == -1) {
            break;
          }
          sb.append(cbuf, 0, chars);
        }
        str = sb.toString();
      }
      catch (IOException e)
      {
        if (sb.length() == 0) {
          return null;
        }
        return sb.toString();
      }
      return str;
    }
    catch (IOException e)
    {
      return null;
    }
    finally
    {
      InternalUtils.close(stream);
    }
  }
  
  protected void setAuthentication(URLConnection connection, String name, String password)
  {
    if ((name == null) || (password == null)) {
      throw new TwitterException.E401("Authentication requested but no authorisation details are set!");
    }
    String token = name + ":" + password;
    String encoding = Base64Encoder.encode(token);
    
    encoding = encoding.replace("\r\n", "");
    connection.setRequestProperty("Authorization", "Basic " + encoding);
  }
  
  public void setMinRateLimit(int minRateLimit)
  {
    this.minRateLimit = minRateLimit;
  }
  
  public void setRetryOnError(boolean retryOnError)
  {
    this.retryOnError = retryOnError;
  }
  
  public void setTimeout(int millisecs)
  {
    this.timeout = millisecs;
  }
  
  public String toString()
  {
    return 
      getClass().getName() + "[name=" + this.name + ", password=" + (this.password == null ? "null" : "XXX") + "]";
  }
  
  void updateRateLimits(String resource)
  {
    if (resource == null) {
      return;
    }
    String limit = getHeader("X-Rate-Limit-Limit");
    if (limit == null) {
      return;
    }
    String remaining = getHeader("X-Rate-Limit-Remaining");
    String reset = getHeader("X-Rate-Limit-Reset");
    this.rateLimits.put(resource, new RateLimit(limit, remaining, reset));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\URLConnectionHttpClient.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */