package com.sun.codemodel;

public class JAssignment
  extends JExpressionImpl
  implements JStatement
{
  JAssignmentTarget lhs;
  JExpression rhs;
  String op = "";
  
  JAssignment(JAssignmentTarget lhs, JExpression rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  JAssignment(JAssignmentTarget lhs, JExpression rhs, String op)
  {
    this.lhs = lhs;
    this.rhs = rhs;
    this.op = op;
  }
  
  public void generate(JFormatter f)
  {
    f.g(this.lhs).p(this.op + '=').g(this.rhs);
  }
  
  public void state(JFormatter f)
  {
    f.g(this).p(';').nl();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JAssignment.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */