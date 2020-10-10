package com.sun.tools.xjc.runtime;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ZeroOneBooleanAdapter
  extends XmlAdapter<String, Boolean>
{
  public Boolean unmarshal(String v)
  {
    if (v == null) {
      return null;
    }
    return Boolean.valueOf(DatatypeConverter.parseBoolean(v));
  }
  
  public String marshal(Boolean v)
  {
    if (v == null) {
      return null;
    }
    if (v.booleanValue()) {
      return "1";
    }
    return "0";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\runtime\ZeroOneBooleanAdapter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */