package com.sun.tools.xjc.reader.xmlschema;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.reader.xmlschema.cs.ClassSelector;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import java.text.ParseException;

public abstract class ParticleBinder
{
  protected final BGMBuilder builder;
  protected final ExpressionPool pool;
  
  public abstract Expression build(XSParticle paramXSParticle, ClassItem paramClassItem);
  
  public abstract boolean checkFallback(XSParticle paramXSParticle, ClassItem paramClassItem);
  
  protected ParticleBinder(BGMBuilder builder)
  {
    this.builder = builder;
    this.pool = builder.pool;
  }
  
  protected final boolean needSkippableElement(XSElementDecl e)
  {
    return (e.isGlobal()) && (e.getType().isComplexType());
  }
  
  protected final boolean needSkip(XSTerm t)
  {
    return (isGlobalElementDecl(t)) && ((this.builder.selector.bindToType(t) instanceof ClassItem));
  }
  
  protected final boolean isGlobalElementDecl(XSTerm t)
  {
    XSElementDecl e = t.asElementDecl();
    return (e != null) && (e.isGlobal());
  }
  
  protected final BIProperty getLocalPropCustomization(XSParticle p)
  {
    BIProperty cust = (BIProperty)this.builder.getBindInfo(p).get(BIProperty.NAME);
    if (cust != null) {
      return cust;
    }
    cust = (BIProperty)this.builder.getBindInfo(p.getTerm()).get(BIProperty.NAME);
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
      return makeJavaName(t.asElementDecl().getName());
    }
    if (t.isModelGroupDecl()) {
      return makeJavaName(t.asModelGroupDecl().getName());
    }
    if (t.isWildcard()) {
      return "Any";
    }
    if (t.isModelGroup()) {
      try
      {
        return NameGenerator.getName(this.builder, t.asModelGroup());
      }
      catch (ParseException e)
      {
        this.builder.errorReporter.error(t.getLocator(), "DefaultParticleBinder.UnableToGenerateNameFromModelGroup");
        
        return "undefined";
      }
    }
    _assert(false);
    return null;
  }
  
  private String makeJavaName(String xmlName)
  {
    return this.builder.getNameConverter().toPropertyName(xmlName);
  }
  
  protected static void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ParticleBinder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */