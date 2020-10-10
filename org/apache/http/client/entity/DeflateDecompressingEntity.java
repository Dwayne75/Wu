package org.apache.http.client.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;

public class DeflateDecompressingEntity
  extends DecompressingEntity
{
  public DeflateDecompressingEntity(HttpEntity entity)
  {
    super(entity);
  }
  
  InputStream decorate(InputStream wrapped)
    throws IOException
  {
    byte[] peeked = new byte[6];
    
    PushbackInputStream pushback = new PushbackInputStream(wrapped, peeked.length);
    
    int headerLength = pushback.read(peeked);
    if (headerLength == -1) {
      throw new IOException("Unable to read the response");
    }
    byte[] dummy = new byte[1];
    
    Inflater inf = new Inflater();
    try
    {
      int n;
      while ((n = inf.inflate(dummy)) == 0)
      {
        if (inf.finished()) {
          throw new IOException("Unable to read the response");
        }
        if (inf.needsDictionary()) {
          break;
        }
        if (inf.needsInput()) {
          inf.setInput(peeked);
        }
      }
      if (n == -1) {
        throw new IOException("Unable to read the response");
      }
      pushback.unread(peeked, 0, headerLength);
      return new DeflateStream(pushback, new Inflater());
    }
    catch (DataFormatException e)
    {
      DeflateStream localDeflateStream;
      pushback.unread(peeked, 0, headerLength);
      return new DeflateStream(pushback, new Inflater(true));
    }
    finally
    {
      inf.end();
    }
  }
  
  public Header getContentEncoding()
  {
    return null;
  }
  
  public long getContentLength()
  {
    return -1L;
  }
  
  static class DeflateStream
    extends InflaterInputStream
  {
    private boolean closed = false;
    
    public DeflateStream(InputStream in, Inflater inflater)
    {
      super(inflater);
    }
    
    public void close()
      throws IOException
    {
      if (this.closed) {
        return;
      }
      this.closed = true;
      this.inf.end();
      super.close();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\entity\DeflateDecompressingEntity.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */