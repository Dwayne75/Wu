package com.sun.codemodel;

public abstract interface JExpression
  extends JGenerable
{
  public abstract JExpression minus();
  
  public abstract JExpression not();
  
  public abstract JExpression complement();
  
  public abstract JExpression incr();
  
  public abstract JExpression decr();
  
  public abstract JExpression plus(JExpression paramJExpression);
  
  public abstract JExpression minus(JExpression paramJExpression);
  
  public abstract JExpression mul(JExpression paramJExpression);
  
  public abstract JExpression div(JExpression paramJExpression);
  
  public abstract JExpression mod(JExpression paramJExpression);
  
  public abstract JExpression shl(JExpression paramJExpression);
  
  public abstract JExpression shr(JExpression paramJExpression);
  
  public abstract JExpression shrz(JExpression paramJExpression);
  
  public abstract JExpression band(JExpression paramJExpression);
  
  public abstract JExpression bor(JExpression paramJExpression);
  
  public abstract JExpression cand(JExpression paramJExpression);
  
  public abstract JExpression cor(JExpression paramJExpression);
  
  public abstract JExpression xor(JExpression paramJExpression);
  
  public abstract JExpression lt(JExpression paramJExpression);
  
  public abstract JExpression lte(JExpression paramJExpression);
  
  public abstract JExpression gt(JExpression paramJExpression);
  
  public abstract JExpression gte(JExpression paramJExpression);
  
  public abstract JExpression eq(JExpression paramJExpression);
  
  public abstract JExpression ne(JExpression paramJExpression);
  
  public abstract JExpression _instanceof(JType paramJType);
  
  public abstract JInvocation invoke(JMethod paramJMethod);
  
  public abstract JInvocation invoke(String paramString);
  
  public abstract JFieldRef ref(JVar paramJVar);
  
  public abstract JFieldRef ref(String paramString);
  
  public abstract JArrayCompRef component(JExpression paramJExpression);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JExpression.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */