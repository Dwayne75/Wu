package org.seamless.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.seamless.util.io.IO;

public class HttpFetch
{
  public static Representation<byte[]> fetchBinary(URL url)
    throws IOException
  {
    return fetchBinary(url, 500, 500);
  }
  
  public static Representation<byte[]> fetchBinary(URL url, int connectTimeoutMillis, int readTimeoutMillis)
    throws IOException
  {
    fetch(url, connectTimeoutMillis, readTimeoutMillis, new RepresentationFactory()
    {
      public Representation<byte[]> createRepresentation(URLConnection urlConnection, InputStream is)
        throws IOException
      {
        return new Representation(urlConnection, IO.readBytes(is));
      }
    });
  }
  
  public static Representation<String> fetchString(URL url, int connectTimeoutMillis, int readTimeoutMillis)
    throws IOException
  {
    fetch(url, connectTimeoutMillis, readTimeoutMillis, new RepresentationFactory()
    {
      public Representation<String> createRepresentation(URLConnection urlConnection, InputStream is)
        throws IOException
      {
        return new Representation(urlConnection, IO.readLines(is));
      }
    });
  }
  
  public static <E> Representation<E> fetch(URL url, int connectTimeoutMillis, int readTimeoutMillis, RepresentationFactory<E> factory)
    throws IOException
  {
    return fetch(url, "GET", connectTimeoutMillis, readTimeoutMillis, factory);
  }
  
  public static <E> Representation<E> fetch(URL url, String method, int connectTimeoutMillis, int readTimeoutMillis, RepresentationFactory<E> factory)
    throws IOException
  {
    HttpURLConnection urlConnection = null;
    InputStream is = null;
    try
    {
      urlConnection = (HttpURLConnection)url.openConnection();
      
      urlConnection.setRequestMethod(method);
      
      urlConnection.setConnectTimeout(connectTimeoutMillis);
      urlConnection.setReadTimeout(readTimeoutMillis);
      
      is = urlConnection.getInputStream();
      
      return factory.createRepresentation(urlConnection, is);
    }
    catch (IOException ex)
    {
      if (urlConnection != null)
      {
        int responseCode = urlConnection.getResponseCode();
        throw new IOException("Fetching resource failed, returned status code: " + responseCode);
      }
      throw ex;
    }
    finally
    {
      if (is != null) {
        is.close();
      }
    }
  }
  
  public static void validate(URL url)
    throws IOException
  {
    fetch(url, "HEAD", 500, 500, new RepresentationFactory()
    {
      public Representation createRepresentation(URLConnection urlConnection, InputStream is)
        throws IOException
      {
        return new Representation(urlConnection, null);
      }
    });
  }
  
  public static abstract interface RepresentationFactory<E>
  {
    public abstract Representation<E> createRepresentation(URLConnection paramURLConnection, InputStream paramInputStream)
      throws IOException;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\http\HttpFetch.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */