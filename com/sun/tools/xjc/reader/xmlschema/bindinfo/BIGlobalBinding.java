package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.ImplStructureStrategy;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.SimpleTypeBuilder;
import com.sun.tools.xjc.util.ReadOnlyAdapter;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

@XmlRootElement(name="globalBindings")
public final class BIGlobalBinding
  extends AbstractDeclarationImpl
{
  @XmlTransient
  public NameConverter nameConverter = NameConverter.standard;
  
  @XmlAttribute
  void setUnderscoreBinding(UnderscoreBinding ub)
  {
    this.nameConverter = ub.nc;
  }
  
  UnderscoreBinding getUnderscoreBinding()
  {
    throw new IllegalStateException();
  }
  
  public JDefinedClass getSuperClass()
  {
    if (this.superClass == null) {
      return null;
    }
    return this.superClass.getClazz(ClassType.CLASS);
  }
  
  public JDefinedClass getSuperInterface()
  {
    if (this.superInterface == null) {
      return null;
    }
    return this.superInterface.getClazz(ClassType.INTERFACE);
  }
  
  public BIProperty getDefaultProperty()
  {
    return this.defaultProperty;
  }
  
  public boolean isJavaNamingConventionEnabled()
  {
    return this.isJavaNamingConventionEnabled;
  }
  
  public BISerializable getSerializable()
  {
    return this.serializable;
  }
  
  public boolean isGenerateElementClass()
  {
    return this.generateElementClass;
  }
  
  public boolean isChoiceContentPropertyEnabled()
  {
    return this.choiceContentProperty;
  }
  
  public int getDefaultEnumMemberSizeCap()
  {
    return this.defaultEnumMemberSizeCap;
  }
  
  public boolean isSimpleMode()
  {
    return this.simpleMode != null;
  }
  
  public boolean isRestrictionFreshType()
  {
    return this.treatRestrictionLikeNewType != null;
  }
  
  public EnumMemberMode getEnumMemberMode()
  {
    return this.generateEnumMemberName;
  }
  
  public boolean isSimpleTypeSubstitution()
  {
    return this.simpleTypeSubstitution;
  }
  
  public ImplStructureStrategy getCodeGenerationStrategy()
  {
    return this.codeGenerationStrategy;
  }
  
  public LocalScoping getFlattenClasses()
  {
    return this.flattenClasses;
  }
  
  public void errorCheck()
  {
    ErrorReceiver er = (ErrorReceiver)Ring.get(ErrorReceiver.class);
    for (QName n : this.enumBaseTypes)
    {
      XSSchemaSet xs = (XSSchemaSet)Ring.get(XSSchemaSet.class);
      XSSimpleType st = xs.getSimpleType(n.getNamespaceURI(), n.getLocalPart());
      if (st == null) {
        er.error(this.loc, Messages.ERR_UNDEFINED_SIMPLE_TYPE.format(new Object[] { n }));
      } else if (!SimpleTypeBuilder.canBeMappedToTypeSafeEnum(st)) {
        er.error(this.loc, Messages.ERR_CANNOT_BE_BOUND_TO_SIMPLETYPE.format(new Object[] { n }));
      }
    }
  }
  
  private static enum UnderscoreBinding
  {
    WORD_SEPARATOR(NameConverter.standard),  CHAR_IN_WORD(NameConverter.jaxrpcCompatible);
    
    final NameConverter nc;
    
    private UnderscoreBinding(NameConverter nc)
    {
      this.nc = nc;
    }
  }
  
  @XmlAttribute(name="enableJavaNamingConventions")
  boolean isJavaNamingConventionEnabled = true;
  @XmlAttribute(name="mapSimpleTypeDef")
  boolean simpleTypeSubstitution = false;
  @XmlTransient
  private BIProperty defaultProperty;
  @XmlAttribute
  private boolean fixedAttributeAsConstantProperty = false;
  @XmlAttribute
  private CollectionTypeAttribute collectionType = new CollectionTypeAttribute();
  
  @XmlAttribute
  void setGenerateIsSetMethod(boolean b)
  {
    this.optionalProperty = (b ? OptionalPropertyMode.ISSET : OptionalPropertyMode.WRAPPER);
  }
  
  @XmlAttribute(name="typesafeEnumMemberName")
  EnumMemberMode generateEnumMemberName = EnumMemberMode.SKIP;
  @XmlAttribute(name="generateValueClass")
  ImplStructureStrategy codeGenerationStrategy = ImplStructureStrategy.BEAN_ONLY;
  @XmlAttribute(name="typesafeEnumBase")
  private Set<QName> enumBaseTypes;
  @XmlElement
  private BISerializable serializable = null;
  @XmlElement(namespace="http://java.sun.com/xml/ns/jaxb/xjc")
  ClassNameBean superClass = null;
  @XmlElement(namespace="http://java.sun.com/xml/ns/jaxb/xjc")
  ClassNameBean superInterface = null;
  @XmlElement(name="simple", namespace="http://java.sun.com/xml/ns/jaxb/xjc")
  String simpleMode = null;
  @XmlElement(name="treatRestrictionLikeNewType", namespace="http://java.sun.com/xml/ns/jaxb/xjc")
  String treatRestrictionLikeNewType = null;
  @XmlAttribute
  boolean generateElementClass = false;
  @XmlElement(namespace="http://java.sun.com/xml/ns/jaxb/xjc")
  Boolean generateElementProperty = null;
  
  @XmlAttribute(name="generateElementProperty")
  private void setGenerateElementPropertyStd(boolean value)
  {
    this.generateElementProperty = Boolean.valueOf(value);
  }
  
  @XmlAttribute
  boolean choiceContentProperty = false;
  @XmlAttribute
  OptionalPropertyMode optionalProperty = OptionalPropertyMode.WRAPPER;
  @XmlAttribute(name="typesafeEnumMaxMembers")
  int defaultEnumMemberSizeCap = 256;
  @XmlAttribute(name="localScoping")
  LocalScoping flattenClasses = LocalScoping.NESTED;
  @XmlTransient
  private final Map<QName, BIConversion> globalConversions = new HashMap();
  
  @XmlElement(name="javaType")
  private void setGlobalConversions(GlobalStandardConversion[] convs)
  {
    for (GlobalStandardConversion u : convs) {
      this.globalConversions.put(u.xmlType, u);
    }
  }
  
  @XmlElement(name="javaType", namespace="http://java.sun.com/xml/ns/jaxb/xjc")
  private void setGlobalConversions2(GlobalVendorConversion[] convs)
  {
    for (GlobalVendorConversion u : convs) {
      this.globalConversions.put(u.xmlType, u);
    }
  }
  
  @XmlElement(namespace="http://java.sun.com/xml/ns/jaxb/xjc")
  String noMarshaller = null;
  @XmlElement(namespace="http://java.sun.com/xml/ns/jaxb/xjc")
  String noUnmarshaller = null;
  @XmlElement(namespace="http://java.sun.com/xml/ns/jaxb/xjc")
  String noValidator = null;
  @XmlElement(namespace="http://java.sun.com/xml/ns/jaxb/xjc")
  String noValidatingUnmarshaller = null;
  @XmlElement(namespace="http://java.sun.com/xml/ns/jaxb/xjc")
  TypeSubstitutionElement typeSubstitution = null;
  
  @XmlElement(name="serializable", namespace="http://java.sun.com/xml/ns/jaxb/xjc")
  void setXjcSerializable(BISerializable s)
  {
    this.serializable = s;
  }
  
  public void onSetOwner()
  {
    super.onSetOwner();
    
    NameConverter nc = ((Model)Ring.get(Model.class)).options.getNameConverter();
    if (nc != null) {
      this.nameConverter = nc;
    }
  }
  
  public void setParent(BindInfo parent)
  {
    super.setParent(parent);
    if (this.enumBaseTypes == null) {
      this.enumBaseTypes = Collections.singleton(new QName("http://www.w3.org/2001/XMLSchema", "string"));
    }
    this.defaultProperty = new BIProperty(getLocation(), null, null, null, this.collectionType, Boolean.valueOf(this.fixedAttributeAsConstantProperty), this.optionalProperty, this.generateElementProperty);
    
    this.defaultProperty.setParent(parent);
  }
  
  public void dispatchGlobalConversions(XSSchemaSet schema)
  {
    for (Map.Entry<QName, BIConversion> e : this.globalConversions.entrySet())
    {
      QName name = (QName)e.getKey();
      BIConversion conv = (BIConversion)e.getValue();
      
      XSSimpleType st = schema.getSimpleType(name.getNamespaceURI(), name.getLocalPart());
      if (st == null) {
        ((ErrorReceiver)Ring.get(ErrorReceiver.class)).error(getLocation(), Messages.ERR_UNDEFINED_SIMPLE_TYPE.format(new Object[] { name }));
      } else {
        getBuilder().getOrCreateBindInfo(st).addDecl(conv);
      }
    }
  }
  
  public boolean canBeMappedToTypeSafeEnum(QName typeName)
  {
    return this.enumBaseTypes.contains(typeName);
  }
  
  public boolean canBeMappedToTypeSafeEnum(String nsUri, String localName)
  {
    return canBeMappedToTypeSafeEnum(new QName(nsUri, localName));
  }
  
  public boolean canBeMappedToTypeSafeEnum(XSDeclaration decl)
  {
    return canBeMappedToTypeSafeEnum(decl.getTargetNamespace(), decl.getName());
  }
  
  public QName getName()
  {
    return NAME;
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "globalBindings");
  
  private static final class TypeSubstitutionElement
  {
    @XmlAttribute
    String type;
  }
  
  static final class ClassNameBean
  {
    @XmlAttribute(required=true)
    String name;
    @XmlTransient
    JDefinedClass clazz;
    
    JDefinedClass getClazz(ClassType t)
    {
      if (this.clazz != null) {
        return this.clazz;
      }
      try
      {
        JCodeModel codeModel = (JCodeModel)Ring.get(JCodeModel.class);
        this.clazz = codeModel._class(this.name, t);
        this.clazz.hide();
        return this.clazz;
      }
      catch (JClassAlreadyExistsException e)
      {
        return e.getExistingClass();
      }
    }
  }
  
  static final class ClassNameAdapter
    extends ReadOnlyAdapter<BIGlobalBinding.ClassNameBean, String>
  {
    public String unmarshal(BIGlobalBinding.ClassNameBean bean)
      throws Exception
    {
      return bean.name;
    }
  }
  
  static final class GlobalStandardConversion
    extends BIConversion.User
  {
    @XmlAttribute
    QName xmlType;
  }
  
  static final class GlobalVendorConversion
    extends BIConversion.UserAdapter
  {
    @XmlAttribute
    QName xmlType;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIGlobalBinding.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */