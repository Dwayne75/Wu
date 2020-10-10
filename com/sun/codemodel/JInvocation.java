package com.sun.codemodel;

import java.util.ArrayList;
import java.util.List;

public final class JInvocation
  extends JExpressionImpl
  implements JStatement
{
  private JGenerable object;
  private String name;
  private JMethod method;
  private boolean isConstructor = false;
  private List<JExpression> args = new ArrayList();
  private JType type = null;
  
  JInvocation(JExpression object, String name)
  {
    this(object, name);
  }
  
  JInvocation(JExpression object, JMethod method)
  {
    this(object, method);
  }
  
  JInvocation(JClass type, String name)
  {
    this(type, name);
  }
  
  JInvocation(JClass type, JMethod method)
  {
    this(type, method);
  }
  
  private JInvocation(JGenerable object, String name)
  {
    this.object = object;
    if (name.indexOf('.') >= 0) {
      throw new IllegalArgumentException("method name contains '.': " + name);
    }
    this.name = name;
  }
  
  private JInvocation(JGenerable object, JMethod method)
  {
    this.object = object;
    this.method = method;
  }
  
  JInvocation(JType c)
  {
    this.isConstructor = true;
    this.type = c;
  }
  
  public JInvocation arg(JExpression arg)
  {
    if (arg == null) {
      throw new IllegalArgumentException();
    }
    this.args.add(arg);
    return this;
  }
  
  public JInvocation arg(String v)
  {
    return arg(JExpr.lit(v));
  }
  
  public void generate(JFormatter f)
  {
    if ((this.isConstructor) && (this.type.isArray()))
    {
      f.p("new").g(this.type).p('{');
    }
    else if (this.isConstructor)
    {
      f.p("new").g(this.type).p('(');
    }
    else
    {
      String name = this.name;
      if (name == null) {
        name = this.method.name();
      }
      if (this.object != null) {
        f.g(this.object).p('.').p(name).p('(');
      } else {
        f.id(name).p('(');
      }
    }
    f.g(this.args);
    if ((this.isConstructor) && (this.type.isArray())) {
      f.p('}');
    } else {
      f.p(')');
    }
    if (((this.type instanceof JDefinedClass)) && (((JDefinedClass)this.type).isAnonymous())) {
      ((JAnonymousClass)this.type).declareBody(f);
    }
  }
  
  public void state(JFormatter f)
  {
    f.g(this).p(';').nl();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JInvocation.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */