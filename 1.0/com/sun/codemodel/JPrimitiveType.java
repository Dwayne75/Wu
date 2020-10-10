package com.sun.codemodel;

public final class JPrimitiveType
  extends JType
{
  private final String typeName;
  private final JCodeModel owner;
  private final JClass wrapperClass;
  
  JPrimitiveType(JCodeModel owner, String typeName, Class wrapper)
  {
    this.owner = owner;
    this.typeName = typeName;
    this.wrapperClass = owner.ref(wrapper);
  }
  
  public JCodeModel owner()
  {
    return this.owner;
  }
  
  public String fullName()
  {
    return this.typeName;
  }
  
  public String name()
  {
    return fullName();
  }
  
  public boolean isPrimitive()
  {
    return true;
  }
  
  public JClass array()
  {
    return new JArrayClass(this.owner, this);
  }
  
  public JClass getWrapperClass()
  {
    return this.wrapperClass;
  }
  
  public JExpression wrap(JExpression exp)
  {
    return JExpr._new(getWrapperClass()).arg(exp);
  }
  
  public JExpression unwrap(JExpression exp)
  {
    return exp.invoke(this.typeName + "Value");
  }
  
  public void generate(JFormatter f)
  {
    f.p(this.typeName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JPrimitiveType.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */