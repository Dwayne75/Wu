package org.fourthline.cling.model.message;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.fourthline.cling.model.message.header.ContentTypeHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.seamless.util.MimeType;

public abstract class UpnpMessage<O extends UpnpOperation>
{
  public static enum BodyType
  {
    STRING,  BYTES;
    
    private BodyType() {}
  }
  
  private int udaMajorVersion = 1;
  private int udaMinorVersion = 0;
  private O operation;
  private UpnpHeaders headers = new UpnpHeaders();
  private Object body;
  private BodyType bodyType = BodyType.STRING;
  
  protected UpnpMessage(UpnpMessage<O> source)
  {
    this.operation = source.getOperation();
    this.headers = source.getHeaders();
    this.body = source.getBody();
    this.bodyType = source.getBodyType();
    this.udaMajorVersion = source.getUdaMajorVersion();
    this.udaMinorVersion = source.getUdaMinorVersion();
  }
  
  protected UpnpMessage(O operation)
  {
    this.operation = operation;
  }
  
  protected UpnpMessage(O operation, BodyType bodyType, Object body)
  {
    this.operation = operation;
    this.bodyType = bodyType;
    this.body = body;
  }
  
  public int getUdaMajorVersion()
  {
    return this.udaMajorVersion;
  }
  
  public void setUdaMajorVersion(int udaMajorVersion)
  {
    this.udaMajorVersion = udaMajorVersion;
  }
  
  public int getUdaMinorVersion()
  {
    return this.udaMinorVersion;
  }
  
  public void setUdaMinorVersion(int udaMinorVersion)
  {
    this.udaMinorVersion = udaMinorVersion;
  }
  
  public UpnpHeaders getHeaders()
  {
    return this.headers;
  }
  
  public void setHeaders(UpnpHeaders headers)
  {
    this.headers = headers;
  }
  
  public Object getBody()
  {
    return this.body;
  }
  
  public void setBody(String string)
  {
    this.bodyType = BodyType.STRING;
    this.body = string;
  }
  
  public void setBody(BodyType bodyType, Object body)
  {
    this.bodyType = bodyType;
    this.body = body;
  }
  
  public void setBodyCharacters(byte[] characterData)
    throws UnsupportedEncodingException
  {
    setBody(BodyType.STRING, new String(characterData, 
    
      getContentTypeCharset() != null ? 
      getContentTypeCharset() : "UTF-8"));
  }
  
  public boolean hasBody()
  {
    return getBody() != null;
  }
  
  public BodyType getBodyType()
  {
    return this.bodyType;
  }
  
  public void setBodyType(BodyType bodyType)
  {
    this.bodyType = bodyType;
  }
  
  public String getBodyString()
  {
    try
    {
      if (!hasBody()) {
        return null;
      }
      if (getBodyType().equals(BodyType.STRING))
      {
        String body = (String)getBody();
        if (body.charAt(0) == 65279) {}
        return body.substring(1);
      }
      return new String((byte[])getBody(), "UTF-8");
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public byte[] getBodyBytes()
  {
    try
    {
      if (!hasBody()) {
        return null;
      }
      if (getBodyType().equals(BodyType.STRING)) {
        return getBodyString().getBytes();
      }
      return (byte[])getBody();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public O getOperation()
  {
    return this.operation;
  }
  
  public boolean isContentTypeMissingOrText()
  {
    ContentTypeHeader contentTypeHeader = getContentTypeHeader();
    if (contentTypeHeader == null) {
      return true;
    }
    if (contentTypeHeader.isText()) {
      return true;
    }
    return false;
  }
  
  public ContentTypeHeader getContentTypeHeader()
  {
    return (ContentTypeHeader)getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE, ContentTypeHeader.class);
  }
  
  public boolean isContentTypeText()
  {
    ContentTypeHeader ct = getContentTypeHeader();
    return (ct != null) && (ct.isText());
  }
  
  public boolean isContentTypeTextUDA()
  {
    ContentTypeHeader ct = getContentTypeHeader();
    return (ct != null) && (ct.isUDACompliantXML());
  }
  
  public String getContentTypeCharset()
  {
    ContentTypeHeader ct = getContentTypeHeader();
    return ct != null ? (String)((MimeType)ct.getValue()).getParameters().get("charset") : null;
  }
  
  public boolean hasHostHeader()
  {
    return getHeaders().getFirstHeader(UpnpHeader.Type.HOST) != null;
  }
  
  public boolean isBodyNonEmptyString()
  {
    return (hasBody()) && (getBodyType().equals(BodyType.STRING)) && (getBodyString().length() > 0);
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") " + getOperation().toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\UpnpMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */