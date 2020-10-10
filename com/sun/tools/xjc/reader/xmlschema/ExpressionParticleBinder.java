package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.Multiplicity;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.tools.xjc.reader.gbind.ConnectedComponent;
import com.sun.tools.xjc.reader.gbind.Element;
import com.sun.tools.xjc.reader.gbind.Expression;
import com.sun.tools.xjc.reader.gbind.Graph;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.xsom.XSParticle;
import java.util.Collection;
import java.util.Set;

final class ExpressionParticleBinder
  extends ParticleBinder
{
  public void build(XSParticle p, Collection<XSParticle> forcedProps)
  {
    Expression tree = ExpressionBuilder.createTree(p);
    Graph g = new Graph(tree);
    for (ConnectedComponent cc : g) {
      buildProperty(cc);
    }
  }
  
  private void buildProperty(ConnectedComponent cc)
  {
    StringBuilder propName = new StringBuilder();
    int nameTokenCount = 0;
    
    RawTypeSetBuilder rtsb = new RawTypeSetBuilder();
    for (Element e : cc)
    {
      GElement ge = (GElement)e;
      if (nameTokenCount < 3)
      {
        if (nameTokenCount != 0) {
          propName.append("And");
        }
        propName.append(makeJavaName(cc.isCollection(), ge.getPropertyNameSeed()));
        nameTokenCount++;
      }
      if ((e instanceof GElementImpl))
      {
        GElementImpl ei = (GElementImpl)e;
        rtsb.elementDecl(ei.decl);
      }
      else if ((e instanceof GWildcardElement))
      {
        GWildcardElement w = (GWildcardElement)e;
        rtsb.getRefs().add(new RawTypeSetBuilder.WildcardRef(w.isStrict() ? WildcardMode.STRICT : WildcardMode.SKIP));
      }
      else if (!$assertionsDisabled)
      {
        throw new AssertionError(e);
      }
    }
    Multiplicity m = Multiplicity.ONE;
    if (cc.isCollection()) {
      m = m.makeRepeated();
    }
    if (!cc.isRequired()) {
      m = m.makeOptional();
    }
    RawTypeSet rts = new RawTypeSet(rtsb.getRefs(), m);
    
    XSParticle p = findSourceParticle(cc);
    
    BIProperty cust = BIProperty.getCustomization(p);
    CPropertyInfo prop = cust.createElementOrReferenceProperty(propName.toString(), false, p, rts);
    
    getCurrentBean().addProperty(prop);
  }
  
  private XSParticle findSourceParticle(ConnectedComponent cc)
  {
    XSParticle first = null;
    for (Element e : cc)
    {
      GElement ge = (GElement)e;
      for (XSParticle p : ge.particles)
      {
        if (first == null) {
          first = p;
        }
        if (getLocalPropCustomization(p) != null) {
          return p;
        }
      }
    }
    return first;
  }
  
  public boolean checkFallback(XSParticle p)
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\ExpressionParticleBinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */