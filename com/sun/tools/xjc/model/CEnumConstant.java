package com.sun.tools.xjc.model;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.EnumConstant;
import org.xml.sax.Locator;

public final class CEnumConstant
  implements EnumConstant<NType, NClass>
{
  public final String name;
  public final String javadoc;
  private final String lexical;
  private CEnumLeafInfo parent;
  private final Locator locator;
  
  public CEnumConstant(String name, String javadoc, String lexical, Locator loc)
  {
    assert (name != null);
    this.name = name;
    this.javadoc = javadoc;
    this.lexical = lexical;
    this.locator = loc;
  }
  
  public CEnumLeafInfo getEnclosingClass()
  {
    return this.parent;
  }
  
  void setParent(CEnumLeafInfo parent)
  {
    this.parent = parent;
  }
  
  public String getLexicalValue()
  {
    return this.lexical;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public Locator getLocator()
  {
    return this.locator;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CEnumConstant.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */