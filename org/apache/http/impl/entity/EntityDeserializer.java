package org.apache.http.impl.entity;

import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.annotation.Immutable;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.io.ChunkedInputStream;
import org.apache.http.impl.io.ContentLengthInputStream;
import org.apache.http.impl.io.IdentityInputStream;
import org.apache.http.io.SessionInputBuffer;

@Immutable
public class EntityDeserializer
{
  private final ContentLengthStrategy lenStrategy;
  
  public EntityDeserializer(ContentLengthStrategy lenStrategy)
  {
    if (lenStrategy == null) {
      throw new IllegalArgumentException("Content length strategy may not be null");
    }
    this.lenStrategy = lenStrategy;
  }
  
  protected BasicHttpEntity doDeserialize(SessionInputBuffer inbuffer, HttpMessage message)
    throws HttpException, IOException
  {
    BasicHttpEntity entity = new BasicHttpEntity();
    
    long len = this.lenStrategy.determineLength(message);
    if (len == -2L)
    {
      entity.setChunked(true);
      entity.setContentLength(-1L);
      entity.setContent(new ChunkedInputStream(inbuffer));
    }
    else if (len == -1L)
    {
      entity.setChunked(false);
      entity.setContentLength(-1L);
      entity.setContent(new IdentityInputStream(inbuffer));
    }
    else
    {
      entity.setChunked(false);
      entity.setContentLength(len);
      entity.setContent(new ContentLengthInputStream(inbuffer, len));
    }
    Header contentTypeHeader = message.getFirstHeader("Content-Type");
    if (contentTypeHeader != null) {
      entity.setContentType(contentTypeHeader);
    }
    Header contentEncodingHeader = message.getFirstHeader("Content-Encoding");
    if (contentEncodingHeader != null) {
      entity.setContentEncoding(contentEncodingHeader);
    }
    return entity;
  }
  
  public HttpEntity deserialize(SessionInputBuffer inbuffer, HttpMessage message)
    throws HttpException, IOException
  {
    if (inbuffer == null) {
      throw new IllegalArgumentException("Session input buffer may not be null");
    }
    if (message == null) {
      throw new IllegalArgumentException("HTTP message may not be null");
    }
    return doDeserialize(inbuffer, message);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\entity\EntityDeserializer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */