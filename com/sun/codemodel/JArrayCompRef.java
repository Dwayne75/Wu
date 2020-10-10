package com.sun.codemodel;

final class JArrayCompRef
  extends JExpressionImpl
  implements JAssignmentTarget
{
  private final JExpression array;
  private final JExpression index;
  
  JArrayCompRef(JExpression array, JExpression index)
  {
    if ((array == null) || (index == null)) {
      throw new NullPointerException();
    }
    this.array = array;
    this.index = index;
  }
  
  public void generate(JFormatter f)
  {
    f.g(this.array).p('[').g(this.index).p(']');
  }
  
  public JExpression assign(JExpression rhs)
  {
    return JExpr.assign(this, rhs);
  }
  
  public JExpression assignPlus(JExpression rhs)
  {
    return JExpr.assignPlus(this, rhs);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JArrayCompRef.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */