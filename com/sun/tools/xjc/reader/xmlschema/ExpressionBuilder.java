package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.reader.gbind.Choice;
import com.sun.tools.xjc.reader.gbind.Element;
import com.sun.tools.xjc.reader.gbind.Expression;
import com.sun.tools.xjc.reader.gbind.OneOrMore;
import com.sun.tools.xjc.reader.gbind.Sequence;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroup.Compositor;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermFunction;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

public final class ExpressionBuilder
  implements XSTermFunction<Expression>
{
  public static Expression createTree(XSParticle p)
  {
    return new ExpressionBuilder().particle(p);
  }
  
  private GWildcardElement wildcard = null;
  private final Map<QName, GElementImpl> decls = new HashMap();
  private XSParticle current;
  
  public Expression wildcard(XSWildcard wc)
  {
    if (this.wildcard == null) {
      this.wildcard = new GWildcardElement();
    }
    this.wildcard.merge(wc);
    this.wildcard.particles.add(this.current);
    return this.wildcard;
  }
  
  public Expression modelGroupDecl(XSModelGroupDecl decl)
  {
    return modelGroup(decl.getModelGroup());
  }
  
  public Expression modelGroup(XSModelGroup group)
  {
    XSModelGroup.Compositor comp = group.getCompositor();
    if (comp == XSModelGroup.CHOICE)
    {
      Expression e = Expression.EPSILON;
      for (XSParticle p : group.getChildren()) {
        if (e == null) {
          e = particle(p);
        } else {
          e = new Choice(e, particle(p));
        }
      }
      return e;
    }
    Expression e = Expression.EPSILON;
    for (XSParticle p : group.getChildren()) {
      if (e == null) {
        e = particle(p);
      } else {
        e = new Sequence(e, particle(p));
      }
    }
    return e;
  }
  
  public Element elementDecl(XSElementDecl decl)
  {
    QName n = BGMBuilder.getName(decl);
    
    GElementImpl e = (GElementImpl)this.decls.get(n);
    if (e == null) {
      this.decls.put(n, e = new GElementImpl(n, decl));
    }
    e.particles.add(this.current);
    assert (this.current.getTerm() == decl);
    
    return e;
  }
  
  public Expression particle(XSParticle p)
  {
    this.current = p;
    Expression e = (Expression)p.getTerm().apply(this);
    if (p.isRepeated()) {
      e = new OneOrMore(e);
    }
    if (p.getMinOccurs() == 0) {
      e = new Choice(e, Expression.EPSILON);
    }
    return e;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\ExpressionBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */