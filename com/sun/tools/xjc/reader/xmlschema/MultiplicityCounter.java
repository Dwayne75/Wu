package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.model.Multiplicity;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermFunction;

public final class MultiplicityCounter
  implements XSTermFunction<Multiplicity>
{
  public static final MultiplicityCounter theInstance = new MultiplicityCounter();
  
  public Multiplicity particle(XSParticle p)
  {
    Multiplicity m = (Multiplicity)p.getTerm().apply(this);
    Integer max;
    Integer max;
    if ((m.max == null) || (p.getMaxOccurs() == -1)) {
      max = null;
    } else {
      max = Integer.valueOf(p.getMaxOccurs());
    }
    return Multiplicity.multiply(m, Multiplicity.create(p.getMinOccurs(), max));
  }
  
  public Multiplicity wildcard(XSWildcard wc)
  {
    return Multiplicity.ONE;
  }
  
  public Multiplicity modelGroupDecl(XSModelGroupDecl decl)
  {
    return modelGroup(decl.getModelGroup());
  }
  
  public Multiplicity modelGroup(XSModelGroup group)
  {
    boolean isChoice = group.getCompositor() == XSModelGroup.CHOICE;
    
    Multiplicity r = Multiplicity.ZERO;
    for (XSParticle p : group.getChildren())
    {
      Multiplicity m = particle(p);
      if (r == null) {
        r = m;
      } else if (isChoice) {
        r = Multiplicity.choice(r, m);
      } else {
        r = Multiplicity.group(r, m);
      }
    }
    return r;
  }
  
  public Multiplicity elementDecl(XSElementDecl decl)
  {
    return Multiplicity.ONE;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\MultiplicityCounter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */