package com.sun.tools.xjc.reader.relaxng;

import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.Multiplicity;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.tools.xjc.reader.RawTypeSet.Mode;
import com.sun.xml.bind.v2.model.core.ID;
import java.util.Iterator;
import java.util.Set;
import javax.xml.namespace.QName;
import org.kohsuke.rngom.digested.DAttributePattern;
import org.kohsuke.rngom.digested.DChoicePattern;
import org.kohsuke.rngom.digested.DMixedPattern;
import org.kohsuke.rngom.digested.DOneOrMorePattern;
import org.kohsuke.rngom.digested.DOptionalPattern;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DPatternWalker;
import org.kohsuke.rngom.digested.DZeroOrMorePattern;
import org.kohsuke.rngom.nc.NameClass;

final class ContentModelBinder
  extends DPatternWalker
{
  private final RELAXNGCompiler compiler;
  private final CClassInfo clazz;
  private boolean insideOptional = false;
  private int iota = 1;
  
  public ContentModelBinder(RELAXNGCompiler compiler, CClassInfo clazz)
  {
    this.compiler = compiler;
    this.clazz = clazz;
  }
  
  public Void onMixed(DMixedPattern p)
  {
    throw new UnsupportedOperationException();
  }
  
  public Void onChoice(DChoicePattern p)
  {
    boolean old = this.insideOptional;
    this.insideOptional = true;
    super.onChoice(p);
    this.insideOptional = old;
    return null;
  }
  
  public Void onOptional(DOptionalPattern p)
  {
    boolean old = this.insideOptional;
    this.insideOptional = true;
    super.onOptional(p);
    this.insideOptional = old;
    return null;
  }
  
  public Void onZeroOrMore(DZeroOrMorePattern p)
  {
    return onRepeated(p, true);
  }
  
  public Void onOneOrMore(DOneOrMorePattern p)
  {
    return onRepeated(p, this.insideOptional);
  }
  
  private Void onRepeated(DPattern p, boolean optional)
  {
    RawTypeSet rts = RawTypeSetBuilder.build(this.compiler, p, optional ? Multiplicity.STAR : Multiplicity.PLUS);
    if (rts.canBeTypeRefs == RawTypeSet.Mode.SHOULD_BE_TYPEREF)
    {
      CElementPropertyInfo prop = new CElementPropertyInfo(calcName(p), CElementPropertyInfo.CollectionMode.REPEATED_ELEMENT, ID.NONE, null, null, null, p.getLocation(), !optional);
      
      rts.addTo(prop);
      this.clazz.addProperty(prop);
    }
    else
    {
      CReferencePropertyInfo prop = new CReferencePropertyInfo(calcName(p), true, false, null, null, p.getLocation());
      
      rts.addTo(prop);
      this.clazz.addProperty(prop);
    }
    return null;
  }
  
  public Void onAttribute(DAttributePattern p)
  {
    QName name = (QName)p.getName().listNames().iterator().next();
    
    CAttributePropertyInfo ap = new CAttributePropertyInfo(calcName(p), null, null, p.getLocation(), name, (TypeUse)p.getChild().accept(this.compiler.typeUseBinder), null, !this.insideOptional);
    
    this.clazz.addProperty(ap);
    
    return null;
  }
  
  private String calcName(DPattern p)
  {
    return "field" + this.iota++;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\relaxng\ContentModelBinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */