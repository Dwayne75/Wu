package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ListExp;
import com.sun.msv.grammar.MixedExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.msv.grammar.util.ExpressionFinder;

public final class TextFinder
  extends ExpressionFinder
{
  public static boolean find(Expression e)
  {
    return e.visit(theInstance);
  }
  
  private static final ExpressionFinder theInstance = new TextFinder();
  
  public boolean onAttribute(AttributeExp exp)
  {
    return false;
  }
  
  public boolean onElement(ElementExp exp)
  {
    return false;
  }
  
  public boolean onAnyString()
  {
    return true;
  }
  
  public boolean onData(DataExp exp)
  {
    return true;
  }
  
  public boolean onList(ListExp exp)
  {
    return true;
  }
  
  public boolean onMixed(MixedExp exp)
  {
    return true;
  }
  
  public boolean onValue(ValueExp exp)
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\util\TextFinder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */