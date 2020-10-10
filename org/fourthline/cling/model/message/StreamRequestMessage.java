package org.fourthline.cling.model.message;

import java.net.URI;
import java.net.URL;

public class StreamRequestMessage
  extends UpnpMessage<UpnpRequest>
{
  protected Connection connection;
  
  public StreamRequestMessage(StreamRequestMessage source)
  {
    super(source);
    this.connection = source.getConnection();
  }
  
  public StreamRequestMessage(UpnpRequest operation)
  {
    super(operation);
  }
  
  public StreamRequestMessage(UpnpRequest.Method method, URI uri)
  {
    super(new UpnpRequest(method, uri));
  }
  
  public StreamRequestMessage(UpnpRequest.Method method, URL url)
  {
    super(new UpnpRequest(method, url));
  }
  
  public StreamRequestMessage(UpnpRequest operation, String body)
  {
    super(operation, UpnpMessage.BodyType.STRING, body);
  }
  
  public StreamRequestMessage(UpnpRequest.Method method, URI uri, String body)
  {
    super(new UpnpRequest(method, uri), UpnpMessage.BodyType.STRING, body);
  }
  
  public StreamRequestMessage(UpnpRequest.Method method, URL url, String body)
  {
    super(new UpnpRequest(method, url), UpnpMessage.BodyType.STRING, body);
  }
  
  public StreamRequestMessage(UpnpRequest operation, byte[] body)
  {
    super(operation, UpnpMessage.BodyType.BYTES, body);
  }
  
  public StreamRequestMessage(UpnpRequest.Method method, URI uri, byte[] body)
  {
    super(new UpnpRequest(method, uri), UpnpMessage.BodyType.BYTES, body);
  }
  
  public StreamRequestMessage(UpnpRequest.Method method, URL url, byte[] body)
  {
    super(new UpnpRequest(method, url), UpnpMessage.BodyType.BYTES, body);
  }
  
  public URI getUri()
  {
    return ((UpnpRequest)getOperation()).getURI();
  }
  
  public void setUri(URI uri)
  {
    ((UpnpRequest)getOperation()).setUri(uri);
  }
  
  public void setConnection(Connection connection)
  {
    this.connection = connection;
  }
  
  public Connection getConnection()
  {
    return this.connection;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\StreamRequestMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */