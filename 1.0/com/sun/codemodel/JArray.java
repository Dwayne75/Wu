package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JArray
  extends JExpressionImpl
{
  private final JType type;
  private final JExpression size;
  private List exprs = null;
  
  public JArray add(JExpression e)
  {
    if (this.exprs == null) {
      this.exprs = new ArrayList();
    }
    this.exprs.add(e);
    return this;
  }
  
  JArray(JType type, JExpression size)
  {
    this.type = type;
    this.size = size;
  }
  
  public void generate(JFormatter f)
  {
    int arrayCount = 0;
    JType t = this.type;
    while (t.isArray())
    {
      t = t.elementType();
      arrayCount++;
    }
    f.p("new").g(t).p('[');
    if (this.size != null) {
      f.g(this.size);
    }
    f.p(']');
    for (int i = 0; i < arrayCount; i++) {
      f.p("[]");
    }
    if ((this.size == null) || (this.exprs != null)) {
      f.p('{');
    }
    boolean first;
    Iterator i;
    if (this.exprs != null)
    {
      first = true;
      if (this.exprs.size() > 0) {
        for (i = this.exprs.iterator(); i.hasNext();)
        {
          if (!first) {
            f.p(',');
          }
          f.g((JExpression)i.next());
          first = false;
        }
      }
    }
    else
    {
      f.p(' ');
    }
    if ((this.size == null) || (this.exprs != null)) {
      f.p('}');
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JArray.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */