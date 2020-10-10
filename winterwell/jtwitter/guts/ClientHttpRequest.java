package winterwell.jtwitter.guts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Random;
import java.util.Set;

public class ClientHttpRequest
  extends Observable
{
  URLConnection connection;
  OutputStream os = null;
  Map<String, String> cookies = new HashMap();
  String rawCookies = "";
  
  protected void connect()
    throws IOException
  {
    if (this.os == null) {
      this.os = this.connection.getOutputStream();
    }
  }
  
  protected void write(char c)
    throws IOException
  {
    connect();
    this.os.write(c);
  }
  
  protected void write(String s)
    throws IOException
  {
    connect();
    this.os.write(s.getBytes());
  }
  
  protected long newlineNumBytes()
  {
    return 2L;
  }
  
  protected void newline()
    throws IOException
  {
    connect();
    write("\r\n");
  }
  
  protected void writeln(String s)
    throws IOException
  {
    connect();
    write(s);
    newline();
  }
  
  private static Random random = new Random();
  
  protected static String randomString()
  {
    return Long.toString(random.nextLong(), 36);
  }
  
  private long boundaryNumBytes()
  {
    return this.boundary.length() + 2;
  }
  
  String boundary = "---------------------------" + 
    randomString() + randomString() + randomString();
  
  private void boundary()
    throws IOException
  {
    write("--");
    write(this.boundary);
  }
  
  public ClientHttpRequest(URLConnection connection)
    throws IOException
  {
    this.connection = connection;
    connection.setDoOutput(true);
    connection.setDoInput(true);
    connection.setRequestProperty("Content-Type", 
      "multipart/form-data; boundary=" + this.boundary);
  }
  
  public ClientHttpRequest(URL url)
    throws IOException
  {
    this(url.openConnection());
  }
  
  public ClientHttpRequest(String urlString)
    throws IOException
  {
    this(new URL(urlString));
  }
  
  private void postCookies()
  {
    StringBuffer cookieList = new StringBuffer(this.rawCookies);
    for (Map.Entry<String, String> cookie : this.cookies.entrySet())
    {
      if (cookieList.length() > 0) {
        cookieList.append("; ");
      }
      cookieList.append((String)cookie.getKey() + "=" + (String)cookie.getValue());
    }
    if (cookieList.length() > 0) {
      this.connection.setRequestProperty("Cookie", cookieList.toString());
    }
  }
  
  public void setCookies(String rawCookies)
    throws IOException
  {
    this.rawCookies = (rawCookies == null ? "" : rawCookies);
    this.cookies.clear();
  }
  
  public void setCookie(String name, String value)
    throws IOException
  {
    this.cookies.put(name, value);
  }
  
  public void setCookies(Map cookies)
    throws IOException
  {
    if (cookies != null) {
      this.cookies.putAll(cookies);
    }
  }
  
  public void setCookies(String[] cookies)
    throws IOException
  {
    if (cookies != null) {
      for (int i = 0; i < cookies.length - 1; i += 2) {
        setCookie(cookies[i], cookies[(i + 1)]);
      }
    }
  }
  
  private long writeNameNumBytes(String name)
  {
    return 
      newlineNumBytes() + 
      "Content-Disposition: form-data; name=\"".length() + 
      name.getBytes().length + 
      1L;
  }
  
  private void writeName(String name)
    throws IOException
  {
    newline();
    write("Content-Disposition: form-data; name=\"");
    write(name);
    write('"');
  }
  
  private boolean isCanceled = false;
  private int bytesSent = 0;
  
  public int getBytesSent()
  {
    return this.bytesSent;
  }
  
  public void cancel()
  {
    this.isCanceled = true;
  }
  
  private synchronized void pipe(InputStream in, OutputStream out)
    throws IOException
  {
    byte[] buf = new byte['Ѐ'];
    
    this.bytesSent = 0;
    this.isCanceled = false;
    synchronized (in)
    {
      int nread;
      while ((nread = in.read(buf, 0, buf.length)) >= 0)
      {
        int nread;
        out.write(buf, 0, nread);
        this.bytesSent += nread;
        if (this.isCanceled) {
          throw new IOException("Canceled");
        }
        out.flush();
        setChanged();
        notifyObservers(Integer.valueOf(this.bytesSent));
        clearChanged();
      }
    }
    int nread;
    out.flush();
    buf = null;
  }
  
  public void setParameter(String name, String value)
    throws IOException
  {
    boundary();
    writeName(name);
    newline();newline();
    writeln(value);
  }
  
  public void setParameter(String name, String filename, InputStream is)
    throws IOException
  {
    boundary();
    writeName(name);
    write("; filename=\"");
    write(filename);
    write('"');
    newline();
    write("Content-Type: ");
    String type = URLConnection.guessContentTypeFromName(filename);
    if (type == null) {
      type = "application/octet-stream";
    }
    writeln(type);
    newline();
    pipe(is, this.os);
    newline();
  }
  
  public long getFilePostSize(String name, File file)
  {
    String filename = file.getPath();
    String type = URLConnection.guessContentTypeFromName(filename);
    if (type == null) {
      type = "application/octet-stream";
    }
    return 
      boundaryNumBytes() + 
      writeNameNumBytes(name) + 
      "; filename=\"".length() + 
      filename.getBytes().length + 
      1L + 
      newlineNumBytes() + 
      "Content-Type: ".length() + 
      type.length() + 
      newlineNumBytes() + 
      newlineNumBytes() + 
      file.length() + 
      newlineNumBytes();
  }
  
  /* Error */
  public void setParameter(String name, File file)
    throws IOException
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore_3
    //   2: new 330	java/io/FileInputStream
    //   5: dup
    //   6: aload_2
    //   7: invokespecial 332	java/io/FileInputStream:<init>	(Ljava/io/File;)V
    //   10: astore_3
    //   11: aload_0
    //   12: aload_1
    //   13: aload_2
    //   14: invokevirtual 316	java/io/File:getPath	()Ljava/lang/String;
    //   17: aload_3
    //   18: invokevirtual 335	winterwell/jtwitter/guts/ClientHttpRequest:setParameter	(Ljava/lang/String;Ljava/lang/String;Ljava/io/InputStream;)V
    //   21: goto +16 -> 37
    //   24: astore 4
    //   26: aload_3
    //   27: ifnull +7 -> 34
    //   30: aload_3
    //   31: invokevirtual 337	java/io/FileInputStream:close	()V
    //   34: aload 4
    //   36: athrow
    //   37: aload_3
    //   38: ifnull +7 -> 45
    //   41: aload_3
    //   42: invokevirtual 337	java/io/FileInputStream:close	()V
    //   45: return
    // Line number table:
    //   Java source line #295	-> byte code offset #0
    //   Java source line #297	-> byte code offset #2
    //   Java source line #298	-> byte code offset #11
    //   Java source line #299	-> byte code offset #21
    //   Java source line #300	-> byte code offset #26
    //   Java source line #301	-> byte code offset #30
    //   Java source line #303	-> byte code offset #34
    //   Java source line #300	-> byte code offset #37
    //   Java source line #301	-> byte code offset #41
    //   Java source line #304	-> byte code offset #45
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	46	0	this	ClientHttpRequest
    //   0	46	1	name	String
    //   0	46	2	file	File
    //   1	41	3	fis	java.io.FileInputStream
    //   24	11	4	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   2	24	24	finally
  }
  
  public void setParameter(Object name, Object object)
    throws IOException
  {
    if ((object instanceof File)) {
      setParameter(name.toString(), (File)object);
    } else {
      setParameter(name.toString(), object.toString());
    }
  }
  
  public void setParameters(Map parameters)
    throws IOException
  {
    if (parameters != null) {
      for (Iterator i = parameters.entrySet().iterator(); i.hasNext();)
      {
        Map.Entry entry = (Map.Entry)i.next();
        setParameter(entry.getKey().toString(), entry.getValue());
      }
    }
  }
  
  public void setParameters(Object... parameters)
    throws IOException
  {
    for (int i = 0; i < parameters.length - 1; i += 2) {
      setParameter(parameters[i].toString(), parameters[(i + 1)]);
    }
  }
  
  public long getPostFooterSize()
  {
    return boundaryNumBytes() + 2L + 
      newlineNumBytes() + newlineNumBytes();
  }
  
  private InputStream doPost()
    throws IOException
  {
    boundary();
    writeln("--");
    this.os.close();
    
    return this.connection.getInputStream();
  }
  
  public InputStream post()
    throws IOException
  {
    postCookies();
    return doPost();
  }
  
  public InputStream post(Map parameters)
    throws IOException
  {
    postCookies();
    setParameters(parameters);
    return doPost();
  }
  
  public InputStream post(Object... parameters)
    throws IOException
  {
    postCookies();
    setParameters(parameters);
    return doPost();
  }
  
  public InputStream post(Map cookies, Map parameters)
    throws IOException
  {
    setCookies(cookies);
    postCookies();
    setParameters(parameters);
    return doPost();
  }
  
  public InputStream post(String raw_cookies, Map parameters)
    throws IOException
  {
    setCookies(raw_cookies);
    postCookies();
    setParameters(parameters);
    return doPost();
  }
  
  public InputStream post(String[] cookies, Object[] parameters)
    throws IOException
  {
    setCookies(cookies);
    postCookies();
    setParameters(parameters);
    return doPost();
  }
  
  public static InputStream post(URL url, Map parameters)
    throws IOException
  {
    return new ClientHttpRequest(url).post(parameters);
  }
  
  public static InputStream post(URL url, Object[] parameters)
    throws IOException
  {
    return new ClientHttpRequest(url).post(parameters);
  }
  
  public static InputStream post(URL url, Map cookies, Map parameters)
    throws IOException
  {
    return new ClientHttpRequest(url).post(cookies, parameters);
  }
  
  public static InputStream post(URL url, String[] cookies, Object[] parameters)
    throws IOException
  {
    return new ClientHttpRequest(url).post(cookies, parameters);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\guts\ClientHttpRequest.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */