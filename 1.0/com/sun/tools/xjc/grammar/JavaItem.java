package com.sun.tools.xjc.grammar;

import com.sun.msv.grammar.OtherExp;
import org.xml.sax.Locator;

public abstract class JavaItem
  extends OtherExp
{
  public String name;
  public final Locator locator;
  
  public JavaItem(String name, Locator loc)
  {
    this.name = name;
    this.locator = loc;
  }
  
  public abstract Object visitJI(JavaItemVisitor paramJavaItemVisitor);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\JavaItem.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */