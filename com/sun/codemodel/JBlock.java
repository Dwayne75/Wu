package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JBlock
  implements JGenerable, JStatement
{
  private final List<Object> content = new ArrayList();
  private boolean bracesRequired = true;
  private boolean indentRequired = true;
  private int pos;
  
  public JBlock()
  {
    this(true, true);
  }
  
  public JBlock(boolean bracesRequired, boolean indentRequired)
  {
    this.bracesRequired = bracesRequired;
    this.indentRequired = indentRequired;
  }
  
  public List<Object> getContents()
  {
    return Collections.unmodifiableList(this.content);
  }
  
  private <T> T insert(T statementOrDeclaration)
  {
    this.content.add(this.pos, statementOrDeclaration);
    this.pos += 1;
    return statementOrDeclaration;
  }
  
  public int pos()
  {
    return this.pos;
  }
  
  public int pos(int newPos)
  {
    int r = this.pos;
    if ((newPos > this.content.size()) || (newPos < 0)) {
      throw new IllegalArgumentException();
    }
    this.pos = newPos;
    
    return r;
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
    insert(v);
    this.bracesRequired = true;
    this.indentRequired = true;
    return v;
  }
  
  public JBlock assign(JAssignmentTarget lhs, JExpression exp)
  {
    insert(new JAssignment(lhs, exp));
    return this;
  }
  
  public JBlock assignPlus(JAssignmentTarget lhs, JExpression exp)
  {
    insert(new JAssignment(lhs, exp, "+"));
    return this;
  }
  
  public JInvocation invoke(JExpression expr, String method)
  {
    JInvocation i = new JInvocation(expr, method);
    insert(i);
    return i;
  }
  
  public JInvocation invoke(JExpression expr, JMethod method)
  {
    return (JInvocation)insert(new JInvocation(expr, method));
  }
  
  public JInvocation staticInvoke(JClass type, String method)
  {
    return (JInvocation)insert(new JInvocation(type, method));
  }
  
  public JInvocation invoke(String method)
  {
    return (JInvocation)insert(new JInvocation((JExpression)null, method));
  }
  
  public JInvocation invoke(JMethod method)
  {
    return (JInvocation)insert(new JInvocation((JExpression)null, method));
  }
  
  public JBlock add(JStatement s)
  {
    insert(s);
    return this;
  }
  
  public JConditional _if(JExpression expr)
  {
    return (JConditional)insert(new JConditional(expr));
  }
  
  public JForLoop _for()
  {
    return (JForLoop)insert(new JForLoop());
  }
  
  public JWhileLoop _while(JExpression test)
  {
    return (JWhileLoop)insert(new JWhileLoop(test));
  }
  
  public JSwitch _switch(JExpression test)
  {
    return (JSwitch)insert(new JSwitch(test));
  }
  
  public JDoLoop _do(JExpression test)
  {
    return (JDoLoop)insert(new JDoLoop(test));
  }
  
  public JTryBlock _try()
  {
    return (JTryBlock)insert(new JTryBlock());
  }
  
  public void _return()
  {
    insert(new JReturn(null));
  }
  
  public void _return(JExpression exp)
  {
    insert(new JReturn(exp));
  }
  
  public void _throw(JExpression exp)
  {
    insert(new JThrow(exp));
  }
  
  public void _break()
  {
    _break(null);
  }
  
  public void _break(JLabel label)
  {
    insert(new JBreak(label));
  }
  
  public JLabel label(String name)
  {
    JLabel l = new JLabel(name);
    insert(l);
    return l;
  }
  
  public void _continue(JLabel label)
  {
    insert(new JContinue(label));
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
    return (JBlock)insert(b);
  }
  
  public JStatement directStatement(final String source)
  {
    JStatement s = new JStatement()
    {
      public void state(JFormatter f)
      {
        f.p(source).nl();
      }
    };
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
    generateBody(f);
    if (this.indentRequired) {
      f.o();
    }
    if (this.bracesRequired) {
      f.p('}');
    }
  }
  
  void generateBody(JFormatter f)
  {
    for (Object o : this.content) {
      if ((o instanceof JDeclaration)) {
        f.d((JDeclaration)o);
      } else {
        f.s((JStatement)o);
      }
    }
  }
  
  public JForEach forEach(JType varType, String name, JExpression collection)
  {
    return (JForEach)insert(new JForEach(varType, name, collection));
  }
  
  public void state(JFormatter f)
  {
    f.g(this);
    if (this.bracesRequired) {
      f.nl();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JBlock.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */