package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.xjc.generator.bean.field.FieldRendererFactory;
import com.sun.tools.xjc.generator.bean.field.IsSetFieldRenderer;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CValuePropertyInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.tools.xjc.reader.RawTypeSet.Mode;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.TypeUtil;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.util.XSFinder;
import com.sun.xml.xsom.visitor.XSFunction;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

@XmlRootElement(name="property")
public final class BIProperty
  extends AbstractDeclarationImpl
{
  @XmlAttribute
  private String name = null;
  @XmlElement
  private String javadoc = null;
  @XmlElement
  private BaseTypeBean baseType = null;
  @XmlAttribute
  private boolean generateFailFastSetterMethod = false;
  
  public BIProperty(Locator loc, String _propName, String _javadoc, BaseTypeBean _baseType, CollectionTypeAttribute collectionType, Boolean isConst, OptionalPropertyMode optionalProperty, Boolean genElemProp)
  {
    super(loc);
    
    this.name = _propName;
    this.javadoc = _javadoc;
    this.baseType = _baseType;
    this.collectionType = collectionType;
    this.isConstantProperty = isConst;
    this.optionalProperty = optionalProperty;
    this.generateElementProperty = genElemProp;
  }
  
  protected BIProperty() {}
  
  public Collection<BIDeclaration> getChildren()
  {
    BIConversion conv = getConv();
    if (conv == null) {
      return super.getChildren();
    }
    return Collections.singleton(conv);
  }
  
  public void setParent(BindInfo parent)
  {
    super.setParent(parent);
    if ((this.baseType != null) && (this.baseType.conv != null)) {
      this.baseType.conv.setParent(parent);
    }
  }
  
  public String getPropertyName(boolean forConstant)
  {
    if (this.name != null)
    {
      BIGlobalBinding gb = getBuilder().getGlobalBinding();
      NameConverter nc = getBuilder().model.getNameConverter();
      if ((gb.isJavaNamingConventionEnabled()) && (!forConstant)) {
        return nc.toPropertyName(this.name);
      }
      return this.name;
    }
    BIProperty next = getDefault();
    if (next != null) {
      return next.getPropertyName(forConstant);
    }
    return null;
  }
  
  public String getJavadoc()
  {
    return this.javadoc;
  }
  
  public JType getBaseType()
  {
    if ((this.baseType != null) && (this.baseType.name != null)) {
      return TypeUtil.getType(getCodeModel(), this.baseType.name, (ErrorReceiver)Ring.get(ErrorReceiver.class), getLocation());
    }
    BIProperty next = getDefault();
    if (next != null) {
      return next.getBaseType();
    }
    return null;
  }
  
  @XmlAttribute
  private CollectionTypeAttribute collectionType = null;
  
  CollectionTypeAttribute getCollectionType()
  {
    if (this.collectionType != null) {
      return this.collectionType;
    }
    return getDefault().getCollectionType();
  }
  
  @XmlAttribute
  private OptionalPropertyMode optionalProperty = null;
  
  @XmlAttribute
  void setGenerateIsSetMethod(boolean b)
  {
    this.optionalProperty = (b ? OptionalPropertyMode.ISSET : OptionalPropertyMode.WRAPPER);
  }
  
  public OptionalPropertyMode getOptionalPropertyMode()
  {
    if (this.optionalProperty != null) {
      return this.optionalProperty;
    }
    return getDefault().getOptionalPropertyMode();
  }
  
  @XmlAttribute
  private Boolean generateElementProperty = null;
  @XmlAttribute(name="fixedAttributeAsConstantProperty")
  private Boolean isConstantProperty;
  
  private Boolean generateElementProperty()
  {
    if (this.generateElementProperty != null) {
      return this.generateElementProperty;
    }
    BIProperty next = getDefault();
    if (next != null) {
      return next.generateElementProperty();
    }
    return null;
  }
  
  public boolean isConstantProperty()
  {
    if (this.isConstantProperty != null) {
      return this.isConstantProperty.booleanValue();
    }
    BIProperty next = getDefault();
    if (next != null) {
      return next.isConstantProperty();
    }
    throw new AssertionError();
  }
  
  public CValuePropertyInfo createValueProperty(String defaultName, boolean forConstant, XSComponent source, TypeUse tu, QName typeName)
  {
    markAsAcknowledged();
    constantPropertyErrorCheck();
    
    String name = getPropertyName(forConstant);
    if (name == null)
    {
      name = defaultName;
      if ((tu.isCollection()) && (getBuilder().getGlobalBinding().isSimpleMode())) {
        name = JJavaName.getPluralForm(name);
      }
    }
    CValuePropertyInfo prop = (CValuePropertyInfo)wrapUp(new CValuePropertyInfo(name, source, getCustomizations(source), source.getLocator(), tu, typeName), source);
    BIInlineBinaryData.handle(source, prop);
    return prop;
  }
  
  public CAttributePropertyInfo createAttributeProperty(XSAttributeUse use, TypeUse tu)
  {
    boolean forConstant = (getCustomization(use).isConstantProperty()) && (use.getFixedValue() != null);
    
    String name = getPropertyName(forConstant);
    if (name == null)
    {
      NameConverter conv = getBuilder().getNameConverter();
      if (forConstant) {
        name = conv.toConstantName(use.getDecl().getName());
      } else {
        name = conv.toPropertyName(use.getDecl().getName());
      }
      if ((tu.isCollection()) && (getBuilder().getGlobalBinding().isSimpleMode())) {
        name = JJavaName.getPluralForm(name);
      }
    }
    markAsAcknowledged();
    constantPropertyErrorCheck();
    
    return (CAttributePropertyInfo)wrapUp(new CAttributePropertyInfo(name, use, getCustomizations(use), use.getLocator(), BGMBuilder.getName(use.getDecl()), tu, BGMBuilder.getName(use.getDecl().getType()), use.isRequired()), use);
  }
  
  public CElementPropertyInfo createElementProperty(String defaultName, boolean forConstant, XSParticle source, RawTypeSet types)
  {
    if (!types.refs.isEmpty()) {
      markAsAcknowledged();
    }
    constantPropertyErrorCheck();
    
    String name = getPropertyName(forConstant);
    if (name == null) {
      name = defaultName;
    }
    CElementPropertyInfo prop = (CElementPropertyInfo)wrapUp(new CElementPropertyInfo(name, types.getCollectionMode(), types.id(), types.getExpectedMimeType(), source, getCustomizations(source), source.getLocator(), types.isRequired()), source);
    
    types.addTo(prop);
    
    BIInlineBinaryData.handle(source.getTerm(), prop);
    return prop;
  }
  
  public CReferencePropertyInfo createReferenceProperty(String defaultName, boolean forConstant, XSComponent source, RawTypeSet types, boolean isMixed)
  {
    if (!types.refs.isEmpty()) {
      markAsAcknowledged();
    }
    constantPropertyErrorCheck();
    
    String name = getPropertyName(forConstant);
    if (name == null) {
      name = defaultName;
    }
    CReferencePropertyInfo prop = (CReferencePropertyInfo)wrapUp(new CReferencePropertyInfo(name, (types.getCollectionMode().isRepeated()) || (isMixed), isMixed, source, getCustomizations(source), source.getLocator()), source);
    
    types.addTo(prop);
    
    BIInlineBinaryData.handle(source, prop);
    return prop;
  }
  
  public CPropertyInfo createElementOrReferenceProperty(String defaultName, boolean forConstant, XSParticle source, RawTypeSet types)
  {
    boolean generateRef;
    switch (types.canBeTypeRefs)
    {
    case CAN_BE_TYPEREF: 
    case SHOULD_BE_TYPEREF: 
      Boolean b = generateElementProperty();
      boolean generateRef;
      if (b == null) {
        generateRef = types.canBeTypeRefs == RawTypeSet.Mode.CAN_BE_TYPEREF;
      } else {
        generateRef = b.booleanValue();
      }
      break;
    case MUST_BE_REFERENCE: 
      generateRef = true;
      break;
    default: 
      throw new AssertionError();
    }
    if (generateRef) {
      return createReferenceProperty(defaultName, forConstant, source, types, false);
    }
    return createElementProperty(defaultName, forConstant, source, types);
  }
  
  private <T extends CPropertyInfo> T wrapUp(T prop, XSComponent source)
  {
    prop.javadoc = concat(this.javadoc, getBuilder().getBindInfo(source).getDocumentation());
    if (prop.javadoc == null) {
      prop.javadoc = "";
    }
    OptionalPropertyMode opm = getOptionalPropertyMode();
    FieldRenderer r;
    FieldRenderer r;
    if (prop.isCollection())
    {
      CollectionTypeAttribute ct = getCollectionType();
      r = ct.get(getBuilder().model);
    }
    else
    {
      FieldRendererFactory frf = getBuilder().fieldRendererFactory;
      if (prop.isOptionalPrimitive()) {
        switch (opm)
        {
        case PRIMITIVE: 
          r = frf.getRequiredUnboxed();
          break;
        case WRAPPER: 
          r = frf.getSingle();
          break;
        case ISSET: 
          r = frf.getSinglePrimitiveAccess();
          break;
        default: 
          throw new Error();
        }
      } else {
        r = frf.getDefault();
      }
    }
    if (opm == OptionalPropertyMode.ISSET) {
      r = new IsSetFieldRenderer(r, (prop.isOptionalPrimitive()) || (prop.isCollection()), true);
    }
    prop.realization = r;
    
    JType bt = getBaseType();
    if (bt != null) {
      prop.baseType = bt;
    }
    return prop;
  }
  
  private CCustomizations getCustomizations(XSComponent src)
  {
    return getBuilder().getBindInfo(src).toCustomizationList();
  }
  
  private CCustomizations getCustomizations(XSComponent... src)
  {
    CCustomizations c = null;
    for (XSComponent s : src)
    {
      CCustomizations r = getCustomizations(s);
      if (c == null) {
        c = r;
      } else {
        c = CCustomizations.merge(c, r);
      }
    }
    return c;
  }
  
  private CCustomizations getCustomizations(XSAttributeUse src)
  {
    if (src.getDecl().isLocal()) {
      return getCustomizations(new XSComponent[] { src, src.getDecl() });
    }
    return getCustomizations(src);
  }
  
  private CCustomizations getCustomizations(XSParticle src)
  {
    if (src.getTerm().isElementDecl())
    {
      XSElementDecl xed = src.getTerm().asElementDecl();
      if (xed.isGlobal()) {
        return getCustomizations(src);
      }
    }
    return getCustomizations(new XSComponent[] { src, src.getTerm() });
  }
  
  public void markAsAcknowledged()
  {
    if (isAcknowledged()) {
      return;
    }
    super.markAsAcknowledged();
    
    BIProperty def = getDefault();
    if (def != null) {
      def.markAsAcknowledged();
    }
  }
  
  private void constantPropertyErrorCheck()
  {
    if ((this.isConstantProperty != null) && (getOwner() != null)) {
      if (!this.hasFixedValue.find(getOwner()))
      {
        ((ErrorReceiver)Ring.get(ErrorReceiver.class)).error(getLocation(), Messages.ERR_ILLEGAL_FIXEDATTR.format(new Object[0]));
        
        this.isConstantProperty = null;
      }
    }
  }
  
  private final XSFinder hasFixedValue = new XSFinder()
  {
    public Boolean attributeDecl(XSAttributeDecl decl)
    {
      return Boolean.valueOf(decl.getFixedValue() != null);
    }
    
    public Boolean attributeUse(XSAttributeUse use)
    {
      return Boolean.valueOf(use.getFixedValue() != null);
    }
    
    public Boolean schema(XSSchema s)
    {
      return Boolean.valueOf(true);
    }
  };
  
  protected BIProperty getDefault()
  {
    if (getOwner() == null) {
      return null;
    }
    BIProperty next = getDefault(getBuilder(), getOwner());
    if (next == this) {
      return null;
    }
    return next;
  }
  
  private static BIProperty getDefault(BGMBuilder builder, XSComponent c)
  {
    while (c != null)
    {
      c = (XSComponent)c.apply(defaultCustomizationFinder);
      if (c != null)
      {
        BIProperty prop = (BIProperty)builder.getBindInfo(c).get(BIProperty.class);
        if (prop != null) {
          return prop;
        }
      }
    }
    return builder.getGlobalBinding().getDefaultProperty();
  }
  
  public static BIProperty getCustomization(XSComponent c)
  {
    BGMBuilder builder = (BGMBuilder)Ring.get(BGMBuilder.class);
    if (c != null)
    {
      BIProperty prop = (BIProperty)builder.getBindInfo(c).get(BIProperty.class);
      if (prop != null) {
        return prop;
      }
    }
    return getDefault(builder, c);
  }
  
  private static final XSFunction<XSComponent> defaultCustomizationFinder = new XSFunction()
  {
    public XSComponent attributeUse(XSAttributeUse use)
    {
      return use.getDecl();
    }
    
    public XSComponent particle(XSParticle particle)
    {
      return particle.getTerm();
    }
    
    public XSComponent schema(XSSchema schema)
    {
      return null;
    }
    
    public XSComponent attributeDecl(XSAttributeDecl decl)
    {
      return decl.getOwnerSchema();
    }
    
    public XSComponent wildcard(XSWildcard wc)
    {
      return wc.getOwnerSchema();
    }
    
    public XSComponent modelGroupDecl(XSModelGroupDecl decl)
    {
      return decl.getOwnerSchema();
    }
    
    public XSComponent modelGroup(XSModelGroup group)
    {
      return group.getOwnerSchema();
    }
    
    public XSComponent elementDecl(XSElementDecl decl)
    {
      return decl.getOwnerSchema();
    }
    
    public XSComponent complexType(XSComplexType type)
    {
      return type.getOwnerSchema();
    }
    
    public XSComponent simpleType(XSSimpleType st)
    {
      return st.getOwnerSchema();
    }
    
    public XSComponent attGroupDecl(XSAttGroupDecl decl)
    {
      throw new IllegalStateException();
    }
    
    public XSComponent empty(XSContentType empty)
    {
      throw new IllegalStateException();
    }
    
    public XSComponent annotation(XSAnnotation xsAnnotation)
    {
      throw new IllegalStateException();
    }
    
    public XSComponent facet(XSFacet xsFacet)
    {
      throw new IllegalStateException();
    }
    
    public XSComponent notation(XSNotation xsNotation)
    {
      throw new IllegalStateException();
    }
    
    public XSComponent identityConstraint(XSIdentityConstraint x)
    {
      throw new IllegalStateException();
    }
    
    public XSComponent xpath(XSXPath xsxPath)
    {
      throw new IllegalStateException();
    }
  };
  
  private static String concat(String s1, String s2)
  {
    if (s1 == null) {
      return s2;
    }
    if (s2 == null) {
      return s1;
    }
    return s1 + "\n\n" + s2;
  }
  
  public QName getName()
  {
    return NAME;
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "property");
  
  public BIConversion getConv()
  {
    if (this.baseType != null) {
      return this.baseType.conv;
    }
    return null;
  }
  
  private static final class BaseTypeBean
  {
    @XmlElementRef
    BIConversion conv;
    @XmlAttribute
    String name;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIProperty.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */