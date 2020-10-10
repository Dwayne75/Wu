package com.sun.codemodel;

public class JFieldRef
  extends JExpressionImpl
  implements JAssignmentTarget
{
  private JGenerable object;
  private String name;
  private JVar var;
  private boolean explicitThis;
  
  JFieldRef(JExpression object, String name)
  {
    this(object, name, false);
  }
  
  JFieldRef(JExpression object, JVar v)
  {
    this(object, v, false);
  }
  
  JFieldRef(JType type, String name)
  {
    this(type, name, false);
  }
  
  JFieldRef(JType type, JVar v)
  {
    this(type, v, false);
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
  
  JFieldRef(JGenerable object, JVar var, boolean explicitThis)
  {
    this.explicitThis = explicitThis;
    this.object = object;
    this.var = var;
  }
  
  public void generate(JFormatter f)
  {
    String name = this.name;
    if (name == null) {
      name = this.var.name();
    }
    if (this.object != null) {
      f.g(this.object).p('.').p(name);
    } else if (this.explicitThis) {
      f.p("this.").p(name);
    } else {
      f.id(name);
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JFieldRef.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */