package com.sun.tools.xjc.grammar;

import com.sun.msv.grammar.Expression;
import org.xml.sax.Locator;

public class IgnoreItem
  extends JavaItem
{
  public IgnoreItem(Locator loc)
  {
    super("$ignore", loc);
  }
  
  public IgnoreItem(Expression exp, Locator loc)
  {
    this(loc);
    this.exp = exp;
  }
  
  public Object visitJI(JavaItemVisitor visitor)
  {
    return visitor.onIgnore(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\IgnoreItem.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */