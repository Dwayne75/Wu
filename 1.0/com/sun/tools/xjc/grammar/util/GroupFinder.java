package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.InterleaveExp;
import com.sun.msv.grammar.ListExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.msv.grammar.util.ExpressionFinder;

public final class GroupFinder
  extends ExpressionFinder
{
  private static final ExpressionFinder theInstance = new GroupFinder();
  
  public static boolean find(Expression e)
  {
    return e.visit(theInstance);
  }
  
  public boolean onAttribute(AttributeExp exp)
  {
    return false;
  }
  
  public boolean onElement(ElementExp exp)
  {
    return false;
  }
  
  public boolean onList(ListExp exp)
  {
    return false;
  }
  
  public boolean onInterleave(InterleaveExp exp)
  {
    return true;
  }
  
  public boolean onSequence(SequenceExp exp)
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\util\GroupFinder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */