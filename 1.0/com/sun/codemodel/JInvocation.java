package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JInvocation
  extends JExpressionImpl
  implements JStatement
{
  private JGenerable object;
  private String name;
  private boolean isConstructor = false;
  private List args = new ArrayList();
  private JType type = null;
  
  JInvocation(JExpression object, String name)
  {
    this(object, name);
  }
  
  JInvocation(JClass type, String name)
  {
    this(type, name);
  }
  
  private JInvocation(JGenerable object, String name)
  {
    this.object = object;
    if (name.indexOf('.') >= 0) {
      throw new IllegalArgumentException("JClass name contains '.': " + name);
    }
    this.name = name;
  }
  
  JInvocation(JType c)
  {
    this.object = null;
    this.name = c.fullName();
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
  
  public void generate(JFormatter f)
  {
    if ((this.isConstructor) && (this.type.isArray())) {
      f.p("new").p(this.name).p('{');
    } else if (this.isConstructor) {
      f.p("new").p(this.name).p('(');
    } else if (this.object != null) {
      f.g(this.object).p('.').p(this.name).p('(');
    } else {
      f.p(this.name).p('(');
    }
    boolean first = true;
    for (Iterator i = this.args.iterator(); i.hasNext();)
    {
      if (!first) {
        f.p(',');
      }
      f.g((JExpression)i.next());
      first = false;
    }
    if ((this.isConstructor) && (this.type.isArray())) {
      f.p('}');
    } else {
      f.p(')');
    }
    if ((this.type instanceof JAnonymousClass)) {
      ((JAnonymousClass)this.type).declareBody(f);
    }
  }
  
  public void state(JFormatter f)
  {
    f.g(this).p(';').nl();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JInvocation.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */