package com.sun.codemodel;

public abstract interface JAssignmentTarget
  extends JGenerable, JExpression
{
  public abstract JExpression assign(JExpression paramJExpression);
  
  public abstract JExpression assignPlus(JExpression paramJExpression);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JAssignmentTarget.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */