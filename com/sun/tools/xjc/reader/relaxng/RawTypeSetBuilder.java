package com.sun.tools.xjc.reader.relaxng;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CTypeInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.Multiplicity;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.tools.xjc.reader.RawTypeSet.Mode;
import com.sun.tools.xjc.reader.RawTypeSet.Ref;
import com.sun.xml.bind.v2.model.core.ID;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.kohsuke.rngom.digested.DAttributePattern;
import org.kohsuke.rngom.digested.DElementPattern;
import org.kohsuke.rngom.digested.DOneOrMorePattern;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DPatternWalker;
import org.kohsuke.rngom.digested.DZeroOrMorePattern;

public final class RawTypeSetBuilder
  extends DPatternWalker
{
  private Multiplicity mul;
  
  public static RawTypeSet build(RELAXNGCompiler compiler, DPattern contentModel, Multiplicity mul)
  {
    RawTypeSetBuilder builder = new RawTypeSetBuilder(compiler, mul);
    contentModel.accept(builder);
    return builder.create();
  }
  
  private final Set<RawTypeSet.Ref> refs = new HashSet();
  private final RELAXNGCompiler compiler;
  
  public RawTypeSetBuilder(RELAXNGCompiler compiler, Multiplicity mul)
  {
    this.mul = mul;
    this.compiler = compiler;
  }
  
  private RawTypeSet create()
  {
    return new RawTypeSet(this.refs, this.mul);
  }
  
  public Void onAttribute(DAttributePattern p)
  {
    return null;
  }
  
  public Void onElement(DElementPattern p)
  {
    CTypeInfo[] tis = (CTypeInfo[])this.compiler.classes.get(p);
    if (tis != null) {
      for (CTypeInfo ti : tis) {
        this.refs.add(new CClassInfoRef((CClassInfo)ti));
      }
    } else if (!$assertionsDisabled) {
      throw new AssertionError();
    }
    return null;
  }
  
  public Void onZeroOrMore(DZeroOrMorePattern p)
  {
    this.mul = this.mul.makeRepeated();
    return super.onZeroOrMore(p);
  }
  
  public Void onOneOrMore(DOneOrMorePattern p)
  {
    this.mul = this.mul.makeRepeated();
    return super.onOneOrMore(p);
  }
  
  private static final class CClassInfoRef
    extends RawTypeSet.Ref
  {
    private final CClassInfo ci;
    
    CClassInfoRef(CClassInfo ci)
    {
      this.ci = ci;
      assert (ci.isElement());
    }
    
    protected ID id()
    {
      return ID.NONE;
    }
    
    protected boolean isListOfValues()
    {
      return false;
    }
    
    protected RawTypeSet.Mode canBeType(RawTypeSet parent)
    {
      return RawTypeSet.Mode.SHOULD_BE_TYPEREF;
    }
    
    protected void toElementRef(CReferencePropertyInfo prop)
    {
      prop.getElements().add(this.ci);
    }
    
    protected CTypeRef toTypeRef(CElementPropertyInfo ep)
    {
      return new CTypeRef(this.ci, this.ci.getElementName(), this.ci.getTypeName(), false, null);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\relaxng\RawTypeSetBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */