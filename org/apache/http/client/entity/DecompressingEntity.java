package org.apache.http.client.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

abstract class DecompressingEntity
  extends HttpEntityWrapper
{
  private static final int BUFFER_SIZE = 2048;
  private InputStream content;
  
  public DecompressingEntity(HttpEntity wrapped)
  {
    super(wrapped);
  }
  
  abstract InputStream decorate(InputStream paramInputStream)
    throws IOException;
  
  private InputStream getDecompressingStream()
    throws IOException
  {
    InputStream in = this.wrappedEntity.getContent();
    try
    {
      return decorate(in);
    }
    catch (IOException ex)
    {
      in.close();
      throw ex;
    }
  }
  
  public InputStream getContent()
    throws IOException
  {
    if (this.wrappedEntity.isStreaming())
    {
      if (this.content == null) {
        this.content = getDecompressingStream();
      }
      return this.content;
    }
    return getDecompressingStream();
  }
  
  public void writeTo(OutputStream outstream)
    throws IOException
  {
    if (outstream == null) {
      throw new IllegalArgumentException("Output stream may not be null");
    }
    InputStream instream = getContent();
    try
    {
      byte[] buffer = new byte['ࠀ'];
      int l;
      while ((l = instream.read(buffer)) != -1) {
        outstream.write(buffer, 0, l);
      }
    }
    finally
    {
      instream.close();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\entity\DecompressingEntity.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */