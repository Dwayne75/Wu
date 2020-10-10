package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceNameClass;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.util.ExpressionWalker;
import java.util.HashSet;
import java.util.Set;

public abstract class NameFinder
  extends ExpressionWalker
{
  public static NameClass findElement(Expression e)
  {
    return find(e, new NameFinder.1());
  }
  
  public static NameClass findAttribute(Expression e)
  {
    return find(e, new NameFinder.2());
  }
  
  private static NameClass find(Expression e, NameFinder f)
  {
    e.visit(f);
    if (f.nc == null) {
      return NameClass.NONE;
    }
    return f.nc.simplify();
  }
  
  private NameClass nc = null;
  private final Set visited = new HashSet();
  
  protected void onName(NameClass child)
  {
    if (this.nc == null) {
      this.nc = child;
    } else {
      this.nc = new ChoiceNameClass(this.nc, child);
    }
  }
  
  public void onRef(ReferenceExp exp)
  {
    if (this.visited.add(exp)) {
      super.onRef(exp);
    }
  }
  
  public void onAttribute(AttributeExp exp) {}
  
  public void onElement(ElementExp exp) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\util\NameFinder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */