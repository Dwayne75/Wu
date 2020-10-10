package com.sun.codemodel;

public abstract interface JAssignmentTarget
  extends JGenerable, JExpression
{
  public abstract JExpression assign(JExpression paramJExpression);
  
  public abstract JExpression assignPlus(JExpression paramJExpression);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JAssignmentTarget.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */