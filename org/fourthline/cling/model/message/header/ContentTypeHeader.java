package org.fourthline.cling.model.message.header;

import org.seamless.util.MimeType;

public class ContentTypeHeader
  extends UpnpHeader<MimeType>
{
  public static final MimeType DEFAULT_CONTENT_TYPE = MimeType.valueOf("text/xml");
  public static final MimeType DEFAULT_CONTENT_TYPE_UTF8 = MimeType.valueOf("text/xml;charset=\"utf-8\"");
  
  public ContentTypeHeader()
  {
    setValue(DEFAULT_CONTENT_TYPE);
  }
  
  public ContentTypeHeader(MimeType contentType)
  {
    setValue(contentType);
  }
  
  public ContentTypeHeader(String s)
    throws InvalidHeaderException
  {
    setString(s);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    setValue(MimeType.valueOf(s));
  }
  
  public String getString()
  {
    return ((MimeType)getValue()).toString();
  }
  
  public boolean isUDACompliantXML()
  {
    return (isText()) && (((MimeType)getValue()).getSubtype().equals(DEFAULT_CONTENT_TYPE.getSubtype()));
  }
  
  public boolean isText()
  {
    return (getValue() != null) && (((MimeType)getValue()).getType().equals(DEFAULT_CONTENT_TYPE.getType()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\ContentTypeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */