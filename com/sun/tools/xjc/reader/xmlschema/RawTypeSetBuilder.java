package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.model.CClass;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.Multiplicity;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.tools.xjc.reader.RawTypeSet.Mode;
import com.sun.tools.xjc.reader.RawTypeSet.Ref;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDom;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXSubstitutable;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermVisitor;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import javax.activation.MimeType;
import javax.xml.namespace.QName;

public class RawTypeSetBuilder
  implements XSTermVisitor
{
  public static RawTypeSet build(XSParticle p, boolean optional)
  {
    RawTypeSetBuilder rtsb = new RawTypeSetBuilder();
    rtsb.particle(p);
    Multiplicity mul = MultiplicityCounter.theInstance.particle(p);
    if (optional) {
      mul = mul.makeOptional();
    }
    return new RawTypeSet(rtsb.refs, mul);
  }
  
  private final Set<QName> elementNames = new HashSet();
  private final Set<RawTypeSet.Ref> refs = new HashSet();
  protected final BGMBuilder builder = (BGMBuilder)Ring.get(BGMBuilder.class);
  
  public Set<RawTypeSet.Ref> getRefs()
  {
    return this.refs;
  }
  
  private void particle(XSParticle p)
  {
    BIDom dom = this.builder.getLocalDomCustomization(p);
    if (dom != null)
    {
      dom.markAsAcknowledged();
      this.refs.add(new WildcardRef(WildcardMode.SKIP));
    }
    else
    {
      p.getTerm().visit(this);
    }
  }
  
  public void wildcard(XSWildcard wc)
  {
    this.refs.add(new WildcardRef(wc));
  }
  
  public void modelGroupDecl(XSModelGroupDecl decl)
  {
    modelGroup(decl.getModelGroup());
  }
  
  public void modelGroup(XSModelGroup group)
  {
    for (XSParticle p : group.getChildren()) {
      particle(p);
    }
  }
  
  public void elementDecl(XSElementDecl decl)
  {
    QName n = BGMBuilder.getName(decl);
    if (this.elementNames.add(n))
    {
      CElement elementBean = ((ClassSelector)Ring.get(ClassSelector.class)).bindToType(decl, null);
      if (elementBean == null) {
        this.refs.add(new XmlTypeRef(decl));
      } else if ((elementBean instanceof CClass)) {
        this.refs.add(new CClassRef(decl, (CClass)elementBean));
      } else {
        this.refs.add(new CElementInfoRef(decl, (CElementInfo)elementBean));
      }
    }
  }
  
  public static final class WildcardRef
    extends RawTypeSet.Ref
  {
    private final WildcardMode mode;
    
    WildcardRef(XSWildcard wildcard)
    {
      this.mode = getMode(wildcard);
    }
    
    WildcardRef(WildcardMode mode)
    {
      this.mode = mode;
    }
    
    private static WildcardMode getMode(XSWildcard wildcard)
    {
      switch (wildcard.getMode())
      {
      case 1: 
        return WildcardMode.LAX;
      case 2: 
        return WildcardMode.STRICT;
      case 3: 
        return WildcardMode.SKIP;
      }
      throw new IllegalStateException();
    }
    
    protected CTypeRef toTypeRef(CElementPropertyInfo ep)
    {
      throw new IllegalStateException();
    }
    
    protected void toElementRef(CReferencePropertyInfo prop)
    {
      prop.setWildcard(this.mode);
    }
    
    protected RawTypeSet.Mode canBeType(RawTypeSet parent)
    {
      return RawTypeSet.Mode.MUST_BE_REFERENCE;
    }
    
    protected boolean isListOfValues()
    {
      return false;
    }
    
    protected ID id()
    {
      return ID.NONE;
    }
  }
  
  public static final class CClassRef
    extends RawTypeSet.Ref
  {
    public final CClass target;
    public final XSElementDecl decl;
    
    CClassRef(XSElementDecl decl, CClass target)
    {
      this.decl = decl;
      this.target = target;
    }
    
    protected CTypeRef toTypeRef(CElementPropertyInfo ep)
    {
      return new CTypeRef(this.target, this.decl);
    }
    
    protected void toElementRef(CReferencePropertyInfo prop)
    {
      prop.getElements().add(this.target);
    }
    
    protected RawTypeSet.Mode canBeType(RawTypeSet parent)
    {
      if (this.decl.getSubstitutables().size() > 1) {
        return RawTypeSet.Mode.MUST_BE_REFERENCE;
      }
      return RawTypeSet.Mode.SHOULD_BE_TYPEREF;
    }
    
    protected boolean isListOfValues()
    {
      return false;
    }
    
    protected ID id()
    {
      return ID.NONE;
    }
  }
  
  public final class CElementInfoRef
    extends RawTypeSet.Ref
  {
    public final CElementInfo target;
    public final XSElementDecl decl;
    
    CElementInfoRef(XSElementDecl decl, CElementInfo target)
    {
      this.decl = decl;
      this.target = target;
    }
    
    protected CTypeRef toTypeRef(CElementPropertyInfo ep)
    {
      assert (!this.target.isCollection());
      CAdapter a = this.target.getProperty().getAdapter();
      if ((a != null) && (ep != null)) {
        ep.setAdapter(a);
      }
      return new CTypeRef(this.target.getContentType(), this.decl);
    }
    
    protected void toElementRef(CReferencePropertyInfo prop)
    {
      prop.getElements().add(this.target);
    }
    
    protected RawTypeSet.Mode canBeType(RawTypeSet parent)
    {
      if (this.decl.getSubstitutables().size() > 1) {
        return RawTypeSet.Mode.MUST_BE_REFERENCE;
      }
      BIXSubstitutable subst = (BIXSubstitutable)RawTypeSetBuilder.this.builder.getBindInfo(this.decl).get(BIXSubstitutable.class);
      if (subst != null)
      {
        subst.markAsAcknowledged();
        return RawTypeSet.Mode.MUST_BE_REFERENCE;
      }
      CElementPropertyInfo p = this.target.getProperty();
      if (((parent.refs.size() > 1) || (!parent.mul.isAtMostOnce())) && (p.id() != ID.NONE)) {
        return RawTypeSet.Mode.MUST_BE_REFERENCE;
      }
      if ((parent.refs.size() > 1) && (p.getAdapter() != null)) {
        return RawTypeSet.Mode.MUST_BE_REFERENCE;
      }
      return RawTypeSet.Mode.SHOULD_BE_TYPEREF;
    }
    
    protected boolean isListOfValues()
    {
      return this.target.getProperty().isValueList();
    }
    
    protected ID id()
    {
      return this.target.getProperty().id();
    }
    
    protected MimeType getExpectedMimeType()
    {
      return this.target.getProperty().getExpectedMimeType();
    }
  }
  
  public static final class XmlTypeRef
    extends RawTypeSet.Ref
  {
    private final XSElementDecl decl;
    private final TypeUse target;
    
    public XmlTypeRef(XSElementDecl decl)
    {
      this.decl = decl;
      SimpleTypeBuilder stb = (SimpleTypeBuilder)Ring.get(SimpleTypeBuilder.class);
      stb.refererStack.push(decl);
      TypeUse r = ((ClassSelector)Ring.get(ClassSelector.class)).bindToType(decl.getType(), decl);
      stb.refererStack.pop();
      this.target = r;
    }
    
    protected CTypeRef toTypeRef(CElementPropertyInfo ep)
    {
      if ((ep != null) && (this.target.getAdapterUse() != null)) {
        ep.setAdapter(this.target.getAdapterUse());
      }
      return new CTypeRef(this.target.getInfo(), this.decl);
    }
    
    protected void toElementRef(CReferencePropertyInfo prop)
    {
      CClassInfo scope = ((ClassSelector)Ring.get(ClassSelector.class)).getCurrentBean();
      Model model = (Model)Ring.get(Model.class);
      
      CCustomizations custs = ((BGMBuilder)Ring.get(BGMBuilder.class)).getBindInfo(this.decl).toCustomizationList();
      if (((this.target instanceof CClassInfo)) && (((BIGlobalBinding)Ring.get(BIGlobalBinding.class)).isSimpleMode()))
      {
        CClassInfo bean = new CClassInfo(model, scope, model.getNameConverter().toClassName(this.decl.getName()), this.decl.getLocator(), null, BGMBuilder.getName(this.decl), this.decl, custs);
        
        bean.setBaseClass((CClassInfo)this.target);
        prop.getElements().add(bean);
      }
      else
      {
        CElementInfo e = new CElementInfo(model, BGMBuilder.getName(this.decl), scope, this.target, this.decl.getDefaultValue(), this.decl, custs, this.decl.getLocator());
        
        prop.getElements().add(e);
      }
    }
    
    protected RawTypeSet.Mode canBeType(RawTypeSet parent)
    {
      if (((parent.refs.size() > 1) || (!parent.mul.isAtMostOnce())) && (this.target.idUse() != ID.NONE)) {
        return RawTypeSet.Mode.MUST_BE_REFERENCE;
      }
      if ((parent.refs.size() > 1) && (this.target.getAdapterUse() != null)) {
        return RawTypeSet.Mode.MUST_BE_REFERENCE;
      }
      if ((this.decl.isNillable()) && (parent.mul.isOptional())) {
        return RawTypeSet.Mode.CAN_BE_TYPEREF;
      }
      return RawTypeSet.Mode.SHOULD_BE_TYPEREF;
    }
    
    protected boolean isListOfValues()
    {
      return this.target.isCollection();
    }
    
    protected ID id()
    {
      return this.target.idUse();
    }
    
    protected MimeType getExpectedMimeType()
    {
      return this.target.getExpectedMimeType();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\RawTypeSetBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */