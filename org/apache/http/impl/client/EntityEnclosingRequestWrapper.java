package org.apache.http.impl.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.ProtocolException;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.entity.HttpEntityWrapper;

@NotThreadSafe
public class EntityEnclosingRequestWrapper
  extends RequestWrapper
  implements HttpEntityEnclosingRequest
{
  private HttpEntity entity;
  private boolean consumed;
  
  public EntityEnclosingRequestWrapper(HttpEntityEnclosingRequest request)
    throws ProtocolException
  {
    super(request);
    setEntity(request.getEntity());
  }
  
  public HttpEntity getEntity()
  {
    return this.entity;
  }
  
  public void setEntity(HttpEntity entity)
  {
    this.entity = (entity != null ? new EntityWrapper(entity) : null);
    this.consumed = false;
  }
  
  public boolean expectContinue()
  {
    Header expect = getFirstHeader("Expect");
    return (expect != null) && ("100-continue".equalsIgnoreCase(expect.getValue()));
  }
  
  public boolean isRepeatable()
  {
    return (this.entity == null) || (this.entity.isRepeatable()) || (!this.consumed);
  }
  
  class EntityWrapper
    extends HttpEntityWrapper
  {
    EntityWrapper(HttpEntity entity)
    {
      super();
    }
    
    public void consumeContent()
      throws IOException
    {
      EntityEnclosingRequestWrapper.this.consumed = true;
      super.consumeContent();
    }
    
    public InputStream getContent()
      throws IOException
    {
      EntityEnclosingRequestWrapper.this.consumed = true;
      return super.getContent();
    }
    
    public void writeTo(OutputStream outstream)
      throws IOException
    {
      EntityEnclosingRequestWrapper.this.consumed = true;
      super.writeTo(outstream);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\client\EntityEnclosingRequestWrapper.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */