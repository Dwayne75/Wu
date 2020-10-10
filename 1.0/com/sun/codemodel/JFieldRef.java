package com.sun.codemodel;

public class JFieldRef
  extends JExpressionImpl
  implements JAssignmentTarget
{
  private JGenerable object;
  private String name;
  private boolean explicitThis;
  
  JFieldRef(JExpression object, String name)
  {
    this(object, name, false);
  }
  
  JFieldRef(JType type, String name)
  {
    this(type, name, false);
  }
  
  JFieldRef(JGenerable object, String name, boolean explicitThis)
  {
    this.explicitThis = explicitThis;
    this.object = object;
    if (name.indexOf('.') >= 0) {
      throw new IllegalArgumentException("Field name contains '.': " + name);
    }
    this.name = name;
  }
  
  public void generate(JFormatter f)
  {
    if (this.object != null) {
      f.g(this.object).p('.').p(this.name);
    } else if (this.explicitThis) {
      f.p("this.").p(this.name);
    } else {
      f.p(this.name);
    }
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JFieldRef.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */