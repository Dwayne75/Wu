package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum EnumMemberMode
{
  SKIP,  ERROR,  GENERATE;
  
  private EnumMemberMode() {}
  
  public EnumMemberMode getModeWithEnum()
  {
    if (this == SKIP) {
      return ERROR;
    }
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\EnumMemberMode.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */