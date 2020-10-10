package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JBlock
  implements JGenerable, JStatement
{
  private final List content = new ArrayList();
  private boolean bracesRequired = true;
  private boolean indentRequired = true;
  public static JBlock dummyInstance = new JBlock();
  
  JBlock()
  {
    this(true, true);
  }
  
  JBlock(boolean bracesRequired, boolean indentRequired)
  {
    this.bracesRequired = bracesRequired;
    this.indentRequired = indentRequired;
  }
  
  public JVar decl(JType type, String name)
  {
    return decl(0, type, name, null);
  }
  
  public JVar decl(JType type, String name, JExpression init)
  {
    return decl(0, type, name, init);
  }
  
  public JVar decl(int mods, JType type, String name, JExpression init)
  {
    JVar v = new JVar(JMods.forVar(mods), type, name, init);
    this.content.add(v);
    this.bracesRequired = true;
    this.indentRequired = true;
    return v;
  }
  
  public JBlock assign(JAssignmentTarget lhs, JExpression exp)
  {
    this.content.add(new JAssignment(lhs, exp));
    return this;
  }
  
  public JBlock assignPlus(JAssignmentTarget lhs, JExpression exp)
  {
    this.content.add(new JAssignment(lhs, exp, "+"));
    return this;
  }
  
  public JInvocation invoke(JExpression expr, String method)
  {
    JInvocation i = new JInvocation(expr, method);
    this.content.add(i);
    return i;
  }
  
  public JInvocation invoke(JExpression expr, JMethod method)
  {
    return invoke(expr, method.name());
  }
  
  public JInvocation staticInvoke(JClass type, String method)
  {
    JInvocation i = new JInvocation(type, method);
    this.content.add(i);
    return i;
  }
  
  public JInvocation invoke(String method)
  {
    JInvocation i = new JInvocation((JExpression)null, method);
    this.content.add(i);
    return i;
  }
  
  public JInvocation invoke(JMethod method)
  {
    return invoke(method.name());
  }
  
  public JBlock add(JStatement s)
  {
    this.content.add(s);
    return this;
  }
  
  public JConditional _if(JExpression expr)
  {
    JConditional c = new JConditional(expr);
    this.content.add(c);
    return c;
  }
  
  public JForLoop _for()
  {
    JForLoop f = new JForLoop();
    this.content.add(f);
    return f;
  }
  
  public JWhileLoop _while(JExpression test)
  {
    JWhileLoop w = new JWhileLoop(test);
    this.content.add(w);
    return w;
  }
  
  public JSwitch _switch(JExpression test)
  {
    JSwitch s = new JSwitch(test);
    this.content.add(s);
    return s;
  }
  
  public JDoLoop _do(JExpression test)
  {
    JDoLoop d = new JDoLoop(test);
    this.content.add(d);
    return d;
  }
  
  public JTryBlock _try()
  {
    JTryBlock t = new JTryBlock();
    this.content.add(t);
    return t;
  }
  
  public void _return()
  {
    this.content.add(new JReturn(null));
  }
  
  public void _return(JExpression exp)
  {
    this.content.add(new JReturn(exp));
  }
  
  public void _throw(JExpression exp)
  {
    this.content.add(new JThrow(exp));
  }
  
  public void _break()
  {
    _break(null);
  }
  
  public void _break(JLabel label)
  {
    this.content.add(new JBreak(label));
  }
  
  public JLabel label(String name)
  {
    JLabel l = new JLabel(name);
    this.content.add(l);
    return l;
  }
  
  public void _continue(JLabel label)
  {
    this.content.add(new JContinue(label));
  }
  
  public void _continue()
  {
    _continue(null);
  }
  
  public JBlock block()
  {
    JBlock b = new JBlock();
    b.bracesRequired = false;
    b.indentRequired = false;
    this.content.add(b);
    return b;
  }
  
  public JStatement directStatement(String source)
  {
    JStatement s = new JBlock.1(this, source);
    
    add(s);
    return s;
  }
  
  public void generate(JFormatter f)
  {
    if (this.bracesRequired) {
      f.p('{').nl();
    }
    if (this.indentRequired) {
      f.i();
    }
    for (Iterator i = this.content.iterator(); i.hasNext();)
    {
      Object o = i.next();
      if ((o instanceof JDeclaration)) {
        f.d((JDeclaration)o);
      } else {
        f.s((JStatement)o);
      }
    }
    if (this.indentRequired) {
      f.o();
    }
    if (this.bracesRequired) {
      f.p('}');
    }
  }
  
  public void state(JFormatter f)
  {
    f.g(this);
    if (this.bracesRequired) {
      f.nl();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JBlock.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */