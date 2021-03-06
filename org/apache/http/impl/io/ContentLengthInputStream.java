package org.apache.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.ConnectionClosedException;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.io.BufferInfo;
import org.apache.http.io.SessionInputBuffer;

@NotThreadSafe
public class ContentLengthInputStream
  extends InputStream
{
  private static final int BUFFER_SIZE = 2048;
  private long contentLength;
  private long pos = 0L;
  private boolean closed = false;
  private SessionInputBuffer in = null;
  
  public ContentLengthInputStream(SessionInputBuffer in, long contentLength)
  {
    if (in == null) {
      throw new IllegalArgumentException("Input stream may not be null");
    }
    if (contentLength < 0L) {
      throw new IllegalArgumentException("Content length may not be negative");
    }
    this.in = in;
    this.contentLength = contentLength;
  }
  
  public void close()
    throws IOException
  {
    if (!this.closed) {
      try
      {
        if (this.pos < this.contentLength)
        {
          byte[] buffer = new byte['ࠀ'];
          while (read(buffer) >= 0) {}
        }
      }
      finally
      {
        this.closed = true;
      }
    }
  }
  
  public int available()
    throws IOException
  {
    if ((this.in instanceof BufferInfo))
    {
      int len = ((BufferInfo)this.in).length();
      return Math.min(len, (int)(this.contentLength - this.pos));
    }
    return 0;
  }
  
  public int read()
    throws IOException
  {
    if (this.closed) {
      throw new IOException("Attempted read from closed stream.");
    }
    if (this.pos >= this.contentLength) {
      return -1;
    }
    int b = this.in.read();
    if (b == -1)
    {
      if (this.pos < this.contentLength) {
        throw new ConnectionClosedException("Premature end of Content-Length delimited message body (expected: " + this.contentLength + "; received: " + this.pos);
      }
    }
    else {
      this.pos += 1L;
    }
    return b;
  }
  
  public int read(byte[] b, int off, int len)
    throws IOException
  {
    if (this.closed) {
      throw new IOException("Attempted read from closed stream.");
    }
    if (this.pos >= this.contentLength) {
      return -1;
    }
    if (this.pos + len > this.contentLength) {
      len = (int)(this.contentLength - this.pos);
    }
    int count = this.in.read(b, off, len);
    if ((count == -1) && (this.pos < this.contentLength)) {
      throw new ConnectionClosedException("Premature end of Content-Length delimited message body (expected: " + this.contentLength + "; received: " + this.pos);
    }
    if (count > 0) {
      this.pos += count;
    }
    return count;
  }
  
  public int read(byte[] b)
    throws IOException
  {
    return read(b, 0, b.length);
  }
  
  public long skip(long n)
    throws IOException
  {
    if (n <= 0L) {
      return 0L;
    }
    byte[] buffer = new byte['ࠀ'];
    
    long remaining = Math.min(n, this.contentLength - this.pos);
    
    long count = 0L;
    while (remaining > 0L)
    {
      int l = read(buffer, 0, (int)Math.min(2048L, remaining));
      if (l == -1) {
        break;
      }
      count += l;
      remaining -= l;
    }
    return count;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\io\ContentLengthInputStream.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */