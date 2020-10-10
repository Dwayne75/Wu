package com.sun.tools.xjc.reader.xmlschema;

import com.sun.codemodel.JJavaName;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermVisitor;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;

public abstract class ParticleBinder
{
  protected final BGMBuilder builder = (BGMBuilder)Ring.get(BGMBuilder.class);
  
  protected ParticleBinder()
  {
    Ring.add(ParticleBinder.class, this);
  }
  
  public final void build(XSParticle p)
  {
    build(p, Collections.emptySet());
  }
  
  public abstract void build(XSParticle paramXSParticle, Collection<XSParticle> paramCollection);
  
  public abstract boolean checkFallback(XSParticle paramXSParticle);
  
  protected final CClassInfo getCurrentBean()
  {
    return getClassSelector().getCurrentBean();
  }
  
  protected final BIProperty getLocalPropCustomization(XSParticle p)
  {
    return (BIProperty)getLocalCustomization(p, BIProperty.class);
  }
  
  protected final <T extends BIDeclaration> T getLocalCustomization(XSParticle p, Class<T> type)
  {
    T cust = this.builder.getBindInfo(p).get(type);
    if (cust != null) {
      return cust;
    }
    cust = this.builder.getBindInfo(p.getTerm()).get(type);
    if (cust != null) {
      return cust;
    }
    return null;
  }
  
  protected final String computeLabel(XSParticle p)
  {
    BIProperty cust = getLocalPropCustomization(p);
    if ((cust != null) && (cust.getPropertyName(false) != null)) {
      return cust.getPropertyName(false);
    }
    XSTerm t = p.getTerm();
    if (t.isElementDecl()) {
      return makeJavaName(p, t.asElementDecl().getName());
    }
    if (t.isModelGroupDecl()) {
      return makeJavaName(p, t.asModelGroupDecl().getName());
    }
    if (t.isWildcard()) {
      return makeJavaName(p, "Any");
    }
    if (t.isModelGroup()) {
      try
      {
        return getSpecDefaultName(t.asModelGroup(), p.isRepeated());
      }
      catch (ParseException e)
      {
        getErrorReporter().error(t.getLocator(), "DefaultParticleBinder.UnableToGenerateNameFromModelGroup", new Object[0]);
        
        return "undefined";
      }
    }
    throw new AssertionError();
  }
  
  protected final String makeJavaName(boolean isRepeated, String xmlName)
  {
    String name = this.builder.getNameConverter().toPropertyName(xmlName);
    if ((this.builder.getGlobalBinding().isSimpleMode()) && (isRepeated)) {
      name = JJavaName.getPluralForm(name);
    }
    return name;
  }
  
  protected final String makeJavaName(XSParticle p, String xmlName)
  {
    return makeJavaName(p.isRepeated(), xmlName);
  }
  
  protected final String getSpecDefaultName(XSModelGroup mg, final boolean repeated)
    throws ParseException
  {
    final StringBuilder name = new StringBuilder();
    
    mg.visit(new XSTermVisitor()
    {
      private int count = 0;
      private boolean rep = repeated;
      
      public void wildcard(XSWildcard wc)
      {
        append("any");
      }
      
      public void modelGroupDecl(XSModelGroupDecl mgd)
      {
        modelGroup(mgd.getModelGroup());
      }
      
      public void modelGroup(XSModelGroup mg)
      {
        String operator;
        String operator;
        if (mg.getCompositor() == XSModelGroup.CHOICE) {
          operator = "Or";
        } else {
          operator = "And";
        }
        int size = mg.getSize();
        for (int i = 0; i < size; i++)
        {
          XSParticle p = mg.getChild(i);
          boolean oldRep = this.rep;
          this.rep |= p.isRepeated();
          p.getTerm().visit(this);
          this.rep = oldRep;
          if (this.count == 3) {
            return;
          }
          if (i != size - 1) {
            name.append(operator);
          }
        }
      }
      
      public void elementDecl(XSElementDecl ed)
      {
        append(ed.getName());
      }
      
      private void append(String token)
      {
        if (this.count < 3)
        {
          name.append(ParticleBinder.this.makeJavaName(this.rep, token));
          this.count += 1;
        }
      }
    });
    if (name.length() == 0) {
      throw new ParseException("no element", -1);
    }
    return name.toString();
  }
  
  protected final ErrorReporter getErrorReporter()
  {
    return (ErrorReporter)Ring.get(ErrorReporter.class);
  }
  
  protected final ClassSelector getClassSelector()
  {
    return (ClassSelector)Ring.get(ClassSelector.class);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\ParticleBinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */