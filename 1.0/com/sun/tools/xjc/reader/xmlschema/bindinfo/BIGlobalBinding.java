package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.generator.field.DefaultFieldRendererFactory;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public final class BIGlobalBinding
  extends AbstractDeclarationImpl
{
  private final NameConverter nameConverter;
  private final boolean enableJavaNamingConvention;
  private final boolean modelGroupBinding;
  private final BIProperty property;
  private final boolean generateEnumMemberName;
  private final boolean choiceContentPropertyWithModelGroupBinding;
  private final Set enumBaseTypes;
  private final BIXSerializable serializable;
  private final BIXSuperClass superClass;
  public final boolean smartWildcardDefaultBinding;
  private final boolean enableTypeSubstitutionSupport;
  private final Map globalConversions;
  
  private static Set createSet()
  {
    Set s = new HashSet();
    s.add(new QName("http://www.w3.org/2001/XMLSchema", "NCName"));
    return s;
  }
  
  public BIGlobalBinding(JCodeModel codeModel)
  {
    this(codeModel, new HashMap(), NameConverter.standard, false, false, true, false, false, false, createSet(), null, null, null, false, false, null);
  }
  
  public BIGlobalBinding(JCodeModel codeModel, Map _globalConvs, NameConverter nconv, boolean _modelGroupBinding, boolean _choiceContentPropertyWithModelGroupBinding, boolean _enableJavaNamingConvention, boolean _fixedAttrToConstantProperty, boolean _needIsSetMethod, boolean _generateEnumMemberName, Set _enumBaseTypes, FieldRendererFactory collectionFieldRenderer, BIXSerializable _serializable, BIXSuperClass _superClass, boolean _enableTypeSubstitutionSupport, boolean _smartWildcardDefaultBinding, Locator _loc)
  {
    super(_loc);
    
    this.globalConversions = _globalConvs;
    this.nameConverter = nconv;
    this.modelGroupBinding = _modelGroupBinding;
    this.choiceContentPropertyWithModelGroupBinding = _choiceContentPropertyWithModelGroupBinding;
    this.enableJavaNamingConvention = _enableJavaNamingConvention;
    this.generateEnumMemberName = _generateEnumMemberName;
    this.enumBaseTypes = _enumBaseTypes;
    this.serializable = _serializable;
    this.superClass = _superClass;
    this.enableTypeSubstitutionSupport = _enableTypeSubstitutionSupport;
    this.smartWildcardDefaultBinding = _smartWildcardDefaultBinding;
    
    this.property = new BIProperty(_loc, null, null, null, null, collectionFieldRenderer == null ? new DefaultFieldRendererFactory(codeModel) : new DefaultFieldRendererFactory(collectionFieldRenderer), _fixedAttrToConstantProperty ? Boolean.TRUE : Boolean.FALSE, _needIsSetMethod ? Boolean.TRUE : Boolean.FALSE);
  }
  
  public NameConverter getNameConverter()
  {
    return this.nameConverter;
  }
  
  boolean isJavaNamingConventionEnabled()
  {
    return this.enableJavaNamingConvention;
  }
  
  public boolean isModelGroupBinding()
  {
    return this.modelGroupBinding;
  }
  
  public boolean isChoiceContentPropertyModelGroupBinding()
  {
    return this.choiceContentPropertyWithModelGroupBinding;
  }
  
  public boolean isTypeSubstitutionSupportEnabled()
  {
    return this.enableTypeSubstitutionSupport;
  }
  
  public BIProperty getDefaultProperty()
  {
    return this.property;
  }
  
  public void setParent(BindInfo parent)
  {
    super.setParent(parent);
    this.property.setParent(parent);
  }
  
  public void dispatchGlobalConversions(XSSchemaSet schema)
  {
    for (Iterator itr = this.globalConversions.entrySet().iterator(); itr.hasNext();)
    {
      Map.Entry e = (Map.Entry)itr.next();
      
      QName name = (QName)e.getKey();
      BIConversion conv = (BIConversion)e.getValue();
      
      XSSimpleType st = schema.getSimpleType(name.getNamespaceURI(), name.getLocalPart());
      if (st == null) {
        getBuilder().errorReceiver.error(getLocation(), Messages.format("UndefinedSimpleType", name));
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
  
  public boolean needsToGenerateEnumMemberName()
  {
    return this.generateEnumMemberName;
  }
  
  public BIXSerializable getSerializableExtension()
  {
    return this.serializable;
  }
  
  public BIXSuperClass getSuperClassExtension()
  {
    return this.superClass;
  }
  
  public QName getName()
  {
    return NAME;
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "globalBinding");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIGlobalBinding.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */