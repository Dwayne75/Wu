package com.google.common.jimfs;

import com.google.common.base.Ascii;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableListMultimap.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

final class PathURLConnection
  extends URLConnection
{
  private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
  private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
  private InputStream stream;
  private ImmutableListMultimap<String, String> headers = ImmutableListMultimap.of();
  
  PathURLConnection(URL url)
  {
    super((URL)Preconditions.checkNotNull(url));
  }
  
  public void connect()
    throws IOException
  {
    if (this.stream != null) {
      return;
    }
    Path path = Paths.get(toUri(this.url));
    long length;
    long length;
    if (Files.isDirectory(path, new LinkOption[0]))
    {
      StringBuilder builder = new StringBuilder();
      DirectoryStream<Path> files = Files.newDirectoryStream(path);Throwable localThrowable2 = null;
      try
      {
        for (Path file : files) {
          builder.append(file.getFileName()).append('\n');
        }
      }
      catch (Throwable localThrowable1)
      {
        localThrowable2 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (files != null) {
          if (localThrowable2 != null) {
            try
            {
              files.close();
            }
            catch (Throwable x2)
            {
              localThrowable2.addSuppressed(x2);
            }
          } else {
            files.close();
          }
        }
      }
      byte[] bytes = builder.toString().getBytes(StandardCharsets.UTF_8);
      this.stream = new ByteArrayInputStream(bytes);
      length = bytes.length;
    }
    else
    {
      this.stream = Files.newInputStream(path, new OpenOption[0]);
      length = Files.size(path);
    }
    FileTime lastModified = Files.getLastModifiedTime(path, new LinkOption[0]);
    String contentType = (String)MoreObjects.firstNonNull(Files.probeContentType(path), "application/octet-stream");
    
    Object builder = ImmutableListMultimap.builder();
    ((ImmutableListMultimap.Builder)builder).put("content-length", "" + length);
    ((ImmutableListMultimap.Builder)builder).put("content-type", contentType);
    if (lastModified != null)
    {
      DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
      format.setTimeZone(TimeZone.getTimeZone("GMT"));
      ((ImmutableListMultimap.Builder)builder).put("last-modified", format.format(new Date(lastModified.toMillis())));
    }
    this.headers = ((ImmutableListMultimap.Builder)builder).build();
  }
  
  private static URI toUri(URL url)
    throws IOException
  {
    try
    {
      return url.toURI();
    }
    catch (URISyntaxException e)
    {
      throw new IOException("URL " + url + " cannot be converted to a URI", e);
    }
  }
  
  public InputStream getInputStream()
    throws IOException
  {
    connect();
    return this.stream;
  }
  
  public Map<String, List<String>> getHeaderFields()
  {
    try
    {
      connect();
    }
    catch (IOException e)
    {
      return ImmutableMap.of();
    }
    return this.headers.asMap();
  }
  
  public String getHeaderField(String name)
  {
    try
    {
      connect();
    }
    catch (IOException e)
    {
      return null;
    }
    return (String)Iterables.getFirst(this.headers.get(Ascii.toLowerCase(name)), null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\PathURLConnection.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */