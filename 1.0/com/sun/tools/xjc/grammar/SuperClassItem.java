package com.sun.tools.xjc.grammar;

import com.sun.msv.grammar.Expression;
import org.xml.sax.Locator;

public class SuperClassItem
  extends JavaItem
{
  public SuperClassItem(Expression exp, Locator loc)
  {
    super("superClass-marker", loc);
    this.exp = exp;
  }
  
  public ClassItem definition = null;
  
  public Object visitJI(JavaItemVisitor visitor)
  {
    return visitor.onSuper(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\SuperClassItem.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */