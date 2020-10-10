package com.sun.javaws.net;

import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.deploy.util.URLUtil;
import com.sun.javaws.Globals;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class BasicNetworkLayer
  implements HttpRequest
{
  private static final String USER_AGENT_JAVA_VERSION = "UA-Java-Version";
  private static final String USER_AGENT = "User-Agent";
  
  static class BasicHttpResponse
    implements HttpResponse
  {
    private URL _request;
    private int _status;
    private int _length;
    private long _lastModified;
    private String _mimeType;
    private Map _headers;
    private BufferedInputStream _bis;
    private HttpURLConnection _httpURLConnection;
    private String _contentEncoding;
    
    BasicHttpResponse(URL paramURL, int paramInt1, int paramInt2, long paramLong, String paramString1, Map paramMap, BufferedInputStream paramBufferedInputStream, HttpURLConnection paramHttpURLConnection, String paramString2)
    {
      this._request = paramURL;
      this._status = paramInt1;
      this._length = paramInt2;
      this._lastModified = paramLong;
      this._mimeType = paramString1;
      this._headers = paramMap;
      this._bis = paramBufferedInputStream;
      this._httpURLConnection = paramHttpURLConnection;
      this._contentEncoding = paramString2;
    }
    
    public void disconnect()
    {
      if (this._httpURLConnection != null)
      {
        this._httpURLConnection.disconnect();
        
        Trace.println("Disconnect connection to " + this._request, TraceLevel.NETWORK);
      }
    }
    
    public URL getRequest()
    {
      return this._request;
    }
    
    public int getStatusCode()
    {
      return this._status;
    }
    
    public int getContentLength()
    {
      return this._length;
    }
    
    public long getLastModified()
    {
      return this._lastModified;
    }
    
    public String getContentType()
    {
      return this._mimeType;
    }
    
    public String getContentEncoding()
    {
      return this._contentEncoding;
    }
    
    public String getResponseHeader(String paramString)
    {
      return (String)this._headers.get(paramString.toLowerCase());
    }
    
    public BufferedInputStream getInputStream()
    {
      return this._bis;
    }
  }
  
  public HttpResponse doGetRequest(URL paramURL)
    throws IOException
  {
    return doRequest(paramURL, false, null, null, true);
  }
  
  public HttpResponse doGetRequest(URL paramURL, boolean paramBoolean)
    throws IOException
  {
    return doRequest(paramURL, false, null, null, paramBoolean);
  }
  
  public HttpResponse doHeadRequest(URL paramURL)
    throws IOException
  {
    return doRequest(paramURL, true, null, null, true);
  }
  
  public HttpResponse doHeadRequest(URL paramURL, boolean paramBoolean)
    throws IOException
  {
    return doRequest(paramURL, true, null, null, paramBoolean);
  }
  
  public HttpResponse doGetRequest(URL paramURL, String[] paramArrayOfString1, String[] paramArrayOfString2)
    throws IOException
  {
    return doRequest(paramURL, false, paramArrayOfString1, paramArrayOfString2, true);
  }
  
  public HttpResponse doGetRequest(URL paramURL, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean)
    throws IOException
  {
    return doRequest(paramURL, false, paramArrayOfString1, paramArrayOfString2, paramBoolean);
  }
  
  public HttpResponse doHeadRequest(URL paramURL, String[] paramArrayOfString1, String[] paramArrayOfString2)
    throws IOException
  {
    return doRequest(paramURL, true, paramArrayOfString1, paramArrayOfString2, true);
  }
  
  public HttpResponse doHeadRequest(URL paramURL, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean)
    throws IOException
  {
    return doRequest(paramURL, true, paramArrayOfString1, paramArrayOfString2, paramBoolean);
  }
  
  private HttpResponse doRequest(URL paramURL, boolean paramBoolean1, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean2)
    throws IOException
  {
    long l1 = 0L;
    String str1 = null;
    if (("file".equals(paramURL.getProtocol())) && (paramURL.getFile() != null)) {
      try
      {
        String str2 = URLUtil.getPathFromURL(paramURL);
        localObject = new File(str2);
        l1 = ((File)localObject).lastModified();
        
        Trace.println("File URL discovered. Real timestamp: " + new Date(l1), TraceLevel.NETWORK);
        if (str2.endsWith(".jnlp")) {
          str1 = "application/x-java-jnlp-file";
        } else if (str2.endsWith(".jardiff")) {
          str1 = "application/x-java-archive-diff";
        }
      }
      catch (Exception localException) {}
    }
    URLConnection localURLConnection = null;
    if (paramURL.getProtocol().equals("file")) {
      localURLConnection = createUrlConnection(new URL(paramURL.getProtocol(), paramURL.getHost(), paramURL.getPath()), paramBoolean1, paramArrayOfString1, paramArrayOfString2, paramBoolean2);
    } else {
      localURLConnection = createUrlConnection(paramURL, paramBoolean1, paramArrayOfString1, paramArrayOfString2, paramBoolean2);
    }
    Object localObject = null;
    if ((localURLConnection instanceof HttpURLConnection)) {
      localObject = (HttpURLConnection)localURLConnection;
    }
    URLUtil.setHostHeader(localURLConnection);
    
    localURLConnection.connect();
    
    int i = 200;
    if (localObject != null) {
      i = ((HttpURLConnection)localObject).getResponseCode();
    }
    int j = localURLConnection.getContentLength();
    long l2 = l1 != 0L ? l1 : localURLConnection.getLastModified();
    String str3 = str1 != null ? str1 : localURLConnection.getContentType();
    if ((str3 != null) && (str3.indexOf(';') != -1)) {
      str3 = str3.substring(0, str3.indexOf(';')).trim();
    }
    HashMap localHashMap = new HashMap();
    int k = 1;
    String str4 = localURLConnection.getHeaderFieldKey(k);
    while (str4 != null)
    {
      localHashMap.put(str4.toLowerCase(), localURLConnection.getHeaderField(k));
      k++;
      str4 = localURLConnection.getHeaderFieldKey(k);
    }
    String str5 = (String)localHashMap.get("content-encoding");
    if (str5 != null) {
      str5 = str5.toLowerCase();
    }
    Trace.println("encoding = " + str5 + " for " + paramURL.toString(), TraceLevel.NETWORK);
    
    BufferedInputStream localBufferedInputStream1 = null;
    if (paramBoolean1)
    {
      localBufferedInputStream1 = null;
    }
    else
    {
      BufferedInputStream localBufferedInputStream2 = null;
      localBufferedInputStream2 = new BufferedInputStream(localURLConnection.getInputStream());
      if ((str5 != null) && ((str5.compareTo("pack200-gzip") == 0) || (str5.compareTo("gzip") == 0))) {
        localBufferedInputStream1 = new BufferedInputStream(new GZIPInputStream(localBufferedInputStream2));
      } else {
        localBufferedInputStream1 = new BufferedInputStream(localBufferedInputStream2);
      }
    }
    return new BasicHttpResponse(paramURL, i, j, l2, str3, localHashMap, localBufferedInputStream1, (HttpURLConnection)localObject, str5);
  }
  
  private URLConnection createUrlConnection(URL paramURL, boolean paramBoolean1, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean2)
    throws MalformedURLException, IOException
  {
    URLConnection localURLConnection = paramURL.openConnection();
    
    addToRequestProperty(localURLConnection, "pragma", "no-cache");
    if ((paramBoolean2) && (paramURL.getPath().endsWith(".jar")))
    {
      String str = Globals.havePack200() ? "pack200-gzip,gzip" : "gzip";
      
      addToRequestProperty(localURLConnection, "accept-encoding", str);
      addToRequestProperty(localURLConnection, "content-type", "application/x-java-archive");
      Trace.println("Requesting file " + paramURL.getFile() + " with Encoding = " + str, TraceLevel.NETWORK);
    }
    if (System.getProperty("http.agent") == null)
    {
      localURLConnection.setRequestProperty("User-Agent", Globals.getUserAgent());
      localURLConnection.setRequestProperty("UA-Java-Version", Globals.getJavaVersion());
    }
    if ((paramArrayOfString1 != null) && (paramArrayOfString2 != null)) {
      for (int i = 0; i < paramArrayOfString1.length; i++) {
        localURLConnection.setRequestProperty(paramArrayOfString1[i], paramArrayOfString2[i]);
      }
    }
    if ((localURLConnection instanceof HttpURLConnection)) {
      ((HttpURLConnection)localURLConnection).setRequestMethod(paramBoolean1 ? "HEAD" : "GET");
    }
    return localURLConnection;
  }
  
  private void addToRequestProperty(URLConnection paramURLConnection, String paramString1, String paramString2)
  {
    String str = paramURLConnection.getRequestProperty(paramString1);
    if ((str == null) || (str.trim().length() == 0)) {
      str = paramString2;
    } else {
      str = str + "," + paramString2;
    }
    paramURLConnection.setRequestProperty(paramString1, str);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\net\BasicNetworkLayer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */