package com.sun.tools.xjc.grammar;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.util.ExpressionWalker;

public abstract class BGMWalker
  extends ExpressionWalker
  implements JavaItemVisitor
{
  public void onOther(OtherExp exp)
  {
    if ((exp instanceof JavaItem)) {
      ((JavaItem)exp).visitJI(this);
    } else {
      exp.exp.visit(this);
    }
  }
  
  public Object onClass(ClassItem item)
  {
    item.exp.visit(this);
    return null;
  }
  
  public Object onField(FieldItem item)
  {
    item.exp.visit(this);
    return null;
  }
  
  public Object onIgnore(IgnoreItem item)
  {
    item.exp.visit(this);
    return null;
  }
  
  public Object onInterface(InterfaceItem item)
  {
    item.exp.visit(this);
    return null;
  }
  
  public Object onPrimitive(PrimitiveItem item)
  {
    item.exp.visit(this);
    return null;
  }
  
  public Object onSuper(SuperClassItem item)
  {
    item.exp.visit(this);
    return null;
  }
  
  public Object onExternal(ExternalItem item)
  {
    item.exp.visit(this);
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\BGMWalker.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */