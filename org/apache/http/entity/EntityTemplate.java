package org.apache.http.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EntityTemplate
  extends AbstractHttpEntity
{
  private final ContentProducer contentproducer;
  
  public EntityTemplate(ContentProducer contentproducer)
  {
    if (contentproducer == null) {
      throw new IllegalArgumentException("Content producer may not be null");
    }
    this.contentproducer = contentproducer;
  }
  
  public long getContentLength()
  {
    return -1L;
  }
  
  public InputStream getContent()
    throws IOException
  {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    writeTo(buf);
    return new ByteArrayInputStream(buf.toByteArray());
  }
  
  public boolean isRepeatable()
  {
    return true;
  }
  
  public void writeTo(OutputStream outstream)
    throws IOException
  {
    if (outstream == null) {
      throw new IllegalArgumentException("Output stream may not be null");
    }
    this.contentproducer.writeTo(outstream);
  }
  
  public boolean isStreaming()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\entity\EntityTemplate.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */