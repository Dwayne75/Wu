package org.apache.http.impl.entity;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.annotation.Immutable;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.io.ChunkedOutputStream;
import org.apache.http.impl.io.ContentLengthOutputStream;
import org.apache.http.impl.io.IdentityOutputStream;
import org.apache.http.io.SessionOutputBuffer;

@Immutable
public class EntitySerializer
{
  private final ContentLengthStrategy lenStrategy;
  
  public EntitySerializer(ContentLengthStrategy lenStrategy)
  {
    if (lenStrategy == null) {
      throw new IllegalArgumentException("Content length strategy may not be null");
    }
    this.lenStrategy = lenStrategy;
  }
  
  protected OutputStream doSerialize(SessionOutputBuffer outbuffer, HttpMessage message)
    throws HttpException, IOException
  {
    long len = this.lenStrategy.determineLength(message);
    if (len == -2L) {
      return new ChunkedOutputStream(outbuffer);
    }
    if (len == -1L) {
      return new IdentityOutputStream(outbuffer);
    }
    return new ContentLengthOutputStream(outbuffer, len);
  }
  
  public void serialize(SessionOutputBuffer outbuffer, HttpMessage message, HttpEntity entity)
    throws HttpException, IOException
  {
    if (outbuffer == null) {
      throw new IllegalArgumentException("Session output buffer may not be null");
    }
    if (message == null) {
      throw new IllegalArgumentException("HTTP message may not be null");
    }
    if (entity == null) {
      throw new IllegalArgumentException("HTTP entity may not be null");
    }
    OutputStream outstream = doSerialize(outbuffer, message);
    entity.writeTo(outstream);
    outstream.close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\entity\EntitySerializer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */