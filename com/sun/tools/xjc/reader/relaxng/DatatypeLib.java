package com.sun.tools.xjc.reader.relaxng;

import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.reader.xmlschema.SimpleTypeBuilder;
import java.util.HashMap;
import java.util.Map;

final class DatatypeLib
{
  public final String nsUri;
  private final Map<String, TypeUse> types = new HashMap();
  
  public DatatypeLib(String nsUri)
  {
    this.nsUri = nsUri;
  }
  
  TypeUse get(String name)
  {
    return (TypeUse)this.types.get(name);
  }
  
  public static final DatatypeLib BUILTIN = new DatatypeLib("");
  public static final DatatypeLib XMLSCHEMA = new DatatypeLib("http://www.w3.org/2001/XMLSchema-datatypes");
  
  static
  {
    BUILTIN.types.put("token", CBuiltinLeafInfo.TOKEN);
    BUILTIN.types.put("string", CBuiltinLeafInfo.STRING);
    XMLSCHEMA.types.putAll(SimpleTypeBuilder.builtinConversions);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\relaxng\DatatypeLib.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */