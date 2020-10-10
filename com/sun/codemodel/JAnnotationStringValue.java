package com.sun.codemodel;

final class JAnnotationStringValue
  extends JAnnotationValue
{
  private final JExpression value;
  
  JAnnotationStringValue(JExpression value)
  {
    this.value = value;
  }
  
  public void generate(JFormatter f)
  {
    f.g(this.value);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JAnnotationStringValue.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */