package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroup.Compositor;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermVisitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

final class DefaultParticleBinder
  extends ParticleBinder
{
  public void build(XSParticle p, Collection<XSParticle> forcedProps)
  {
    Checker checker = checkCollision(p, forcedProps);
    if (checker.hasNameCollision())
    {
      CReferencePropertyInfo prop = new CReferencePropertyInfo(getCurrentBean().getBaseClass() == null ? "Content" : "Rest", true, false, p, this.builder.getBindInfo(p).toCustomizationList(), p.getLocator());
      
      RawTypeSetBuilder.build(p, false).addTo(prop);
      prop.javadoc = Messages.format("DefaultParticleBinder.FallbackJavadoc", new Object[] { checker.getCollisionInfo().toString() });
      
      getCurrentBean().addProperty(prop);
    }
    else
    {
      new Builder(checker.markedParticles).particle(p);
    }
  }
  
  public boolean checkFallback(XSParticle p)
  {
    return checkCollision(p, Collections.emptyList()).hasNameCollision();
  }
  
  private Checker checkCollision(XSParticle p, Collection<XSParticle> forcedProps)
  {
    Checker checker = new Checker(forcedProps);
    
    CClassInfo superClass = getCurrentBean().getBaseClass();
    if (superClass != null) {
      checker.readSuperClass(superClass);
    }
    checker.particle(p);
    
    return checker;
  }
  
  private final class Checker
    implements XSTermVisitor
  {
    Checker()
    {
      this.forcedProps = forcedProps;
    }
    
    boolean hasNameCollision()
    {
      return this.collisionInfo != null;
    }
    
    CollisionInfo getCollisionInfo()
    {
      return this.collisionInfo;
    }
    
    private CollisionInfo collisionInfo = null;
    private final NameCollisionChecker cchecker = new NameCollisionChecker(null);
    private final Collection<XSParticle> forcedProps;
    private XSParticle outerParticle;
    
    public void particle(XSParticle p)
    {
      if ((DefaultParticleBinder.this.getLocalPropCustomization(p) != null) || (DefaultParticleBinder.this.builder.getLocalDomCustomization(p) != null))
      {
        check(p);
        mark(p);
        return;
      }
      XSTerm t = p.getTerm();
      if ((p.isRepeated()) && ((t.isModelGroup()) || (t.isModelGroupDecl())))
      {
        mark(p);
        return;
      }
      if (this.forcedProps.contains(p))
      {
        mark(p);
        return;
      }
      this.outerParticle = p;
      t.visit(this);
    }
    
    public void elementDecl(XSElementDecl decl)
    {
      check(this.outerParticle);
      mark(this.outerParticle);
    }
    
    public void modelGroup(XSModelGroup mg)
    {
      if ((mg.getCompositor() == XSModelGroup.Compositor.CHOICE) && (DefaultParticleBinder.this.builder.getGlobalBinding().isChoiceContentPropertyEnabled()))
      {
        mark(this.outerParticle);
        return;
      }
      for (XSParticle child : mg.getChildren()) {
        particle(child);
      }
    }
    
    public void modelGroupDecl(XSModelGroupDecl decl)
    {
      modelGroup(decl.getModelGroup());
    }
    
    public void wildcard(XSWildcard wc)
    {
      mark(this.outerParticle);
    }
    
    void readSuperClass(CClassInfo ci)
    {
      this.cchecker.readSuperClass(ci);
    }
    
    private void check(XSParticle p)
    {
      if (this.collisionInfo == null) {
        this.collisionInfo = this.cchecker.check(p);
      }
    }
    
    private void mark(XSParticle p)
    {
      this.markedParticles.put(p, computeLabel(p));
    }
    
    public final Map<XSParticle, String> markedParticles = new HashMap();
    
    private final class NameCollisionChecker
    {
      private NameCollisionChecker() {}
      
      CollisionInfo check(XSParticle p)
      {
        String label = DefaultParticleBinder.Checker.this.computeLabel(p);
        if (this.occupiedLabels.containsKey(label)) {
          return new CollisionInfo(label, p.getLocator(), ((CPropertyInfo)this.occupiedLabels.get(label)).locator);
        }
        for (XSParticle jp : this.particles) {
          if (!check(p, jp)) {
            return new CollisionInfo(label, p.getLocator(), jp.getLocator());
          }
        }
        this.particles.add(p);
        return null;
      }
      
      private final List<XSParticle> particles = new ArrayList();
      private final Map<String, CPropertyInfo> occupiedLabels = new HashMap();
      
      private boolean check(XSParticle p1, XSParticle p2)
      {
        return !DefaultParticleBinder.Checker.this.computeLabel(p1).equals(DefaultParticleBinder.Checker.this.computeLabel(p2));
      }
      
      void readSuperClass(CClassInfo base)
      {
        for (; base != null; base = base.getBaseClass()) {
          for (CPropertyInfo p : base.getProperties()) {
            this.occupiedLabels.put(p.getName(true), p);
          }
        }
      }
    }
    
    private final Map<XSParticle, String> labelCache = new Hashtable();
    
    private String computeLabel(XSParticle p)
    {
      String label = (String)this.labelCache.get(p);
      if (label == null) {
        this.labelCache.put(p, label = DefaultParticleBinder.this.computeLabel(p));
      }
      return label;
    }
  }
  
  private final class Builder
    implements XSTermVisitor
  {
    private final Map<XSParticle, String> markedParticles;
    private boolean insideOptionalParticle;
    
    Builder()
    {
      this.markedParticles = markedParticles;
    }
    
    private boolean marked(XSParticle p)
    {
      return this.markedParticles.containsKey(p);
    }
    
    private String getLabel(XSParticle p)
    {
      return (String)this.markedParticles.get(p);
    }
    
    public void particle(XSParticle p)
    {
      XSTerm t = p.getTerm();
      if (marked(p))
      {
        BIProperty cust = BIProperty.getCustomization(p);
        CPropertyInfo prop = cust.createElementOrReferenceProperty(getLabel(p), false, p, RawTypeSetBuilder.build(p, this.insideOptionalParticle));
        
        DefaultParticleBinder.this.getCurrentBean().addProperty(prop);
      }
      else
      {
        assert (!p.isRepeated());
        
        boolean oldIOP = this.insideOptionalParticle;
        this.insideOptionalParticle |= p.getMinOccurs() == 0;
        
        t.visit(this);
        this.insideOptionalParticle = oldIOP;
      }
    }
    
    public void elementDecl(XSElementDecl e)
    {
      if (!$assertionsDisabled) {
        throw new AssertionError();
      }
    }
    
    public void wildcard(XSWildcard wc)
    {
      if (!$assertionsDisabled) {
        throw new AssertionError();
      }
    }
    
    public void modelGroupDecl(XSModelGroupDecl decl)
    {
      modelGroup(decl.getModelGroup());
    }
    
    public void modelGroup(XSModelGroup mg)
    {
      boolean oldIOP = this.insideOptionalParticle;
      this.insideOptionalParticle |= mg.getCompositor() == XSModelGroup.CHOICE;
      for (XSParticle p : mg.getChildren()) {
        particle(p);
      }
      this.insideOptionalParticle = oldIOP;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\DefaultParticleBinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */