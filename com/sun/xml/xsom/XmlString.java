package com.sun.xml.xsom;

import org.relaxng.datatype.ValidationContext;

public final class XmlString
{
  public final String value;
  public final ValidationContext context;
  
  public XmlString(String value, ValidationContext context)
  {
    this.value = value;
    this.context = context;
    if (context == null) {
      throw new IllegalArgumentException();
    }
  }
  
  public XmlString(String value)
  {
    this(value, NULL_CONTEXT);
  }
  
  public final String resolvePrefix(String prefix)
  {
    return this.context.resolveNamespacePrefix(prefix);
  }
  
  public String toString()
  {
    return this.value;
  }
  
  private static final ValidationContext NULL_CONTEXT = new ValidationContext()
  {
    public String resolveNamespacePrefix(String s)
    {
      if (s.length() == 0) {
        return "";
      }
      if (s.equals("xml")) {
        return "http://www.w3.org/XML/1998/namespace";
      }
      return null;
    }
    
    public String getBaseUri()
    {
      return null;
    }
    
    public boolean isUnparsedEntity(String s)
    {
      return false;
    }
    
    public boolean isNotation(String s)
    {
      return false;
    }
  };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XmlString.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */