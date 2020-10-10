package com.sun.xml.bind.util;

import com.sun.xml.bind.ValidationEventLocatorEx;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

public class ValidationEventLocatorExImpl
  extends ValidationEventLocatorImpl
  implements ValidationEventLocatorEx
{
  private final String fieldName;
  
  public ValidationEventLocatorExImpl(Object target, String fieldName)
  {
    super(target);
    this.fieldName = fieldName;
  }
  
  public String getFieldName()
  {
    return this.fieldName;
  }
  
  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    buf.append("[url=");
    buf.append(getURL());
    buf.append(",line=");
    buf.append(getLineNumber());
    buf.append(",column=");
    buf.append(getColumnNumber());
    buf.append(",node=");
    buf.append(getNode());
    buf.append(",object=");
    buf.append(getObject());
    buf.append(",field=");
    buf.append(getFieldName());
    buf.append("]");
    
    return buf.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\util\ValidationEventLocatorExImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */