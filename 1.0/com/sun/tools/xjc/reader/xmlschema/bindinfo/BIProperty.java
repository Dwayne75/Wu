package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.JType;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.generator.field.IsSetFieldRenderer;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.util.XSFinder;
import com.sun.xml.xsom.visitor.XSFunction;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public final class BIProperty
  extends AbstractDeclarationImpl
{
  private final String propName;
  private final String javadoc;
  private final JType baseType;
  public final BIConversion conv;
  private final FieldRendererFactory realization;
  private final Boolean needIsSetMethod;
  private Boolean isConstantProperty;
  
  public BIProperty(Locator loc, String _propName, String _javadoc, JType _baseType, BIConversion _conv, FieldRendererFactory real, Boolean isConst, Boolean isSet)
  {
    super(loc);
    
    this.propName = _propName;
    this.javadoc = _javadoc;
    this.baseType = _baseType;
    this.realization = real;
    this.isConstantProperty = isConst;
    this.needIsSetMethod = isSet;
    this.conv = _conv;
  }
  
  public void setParent(BindInfo parent)
  {
    super.setParent(parent);
    if (this.conv != null) {
      this.conv.setParent(parent);
    }
  }
  
  public String getPropertyName(boolean forConstant)
  {
    if (this.propName != null)
    {
      BIGlobalBinding gb = getBuilder().getGlobalBinding();
      if ((gb.isJavaNamingConventionEnabled()) && (!forConstant)) {
        return gb.getNameConverter().toPropertyName(this.propName);
      }
      return this.propName;
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
    if (this.baseType != null) {
      return this.baseType;
    }
    BIProperty next = getDefault();
    if (next != null) {
      return next.getBaseType();
    }
    return null;
  }
  
  public FieldRendererFactory getRealization()
  {
    if (this.realization != null) {
      return this.realization;
    }
    BIProperty next = getDefault();
    if (next != null) {
      return next.getRealization();
    }
    throw new JAXBAssertionError();
  }
  
  public boolean needIsSetMethod()
  {
    if (this.needIsSetMethod != null) {
      return this.needIsSetMethod.booleanValue();
    }
    BIProperty next = getDefault();
    if (next != null) {
      return next.needIsSetMethod();
    }
    throw new JAXBAssertionError();
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
    throw new JAXBAssertionError();
  }
  
  public FieldItem createFieldItem(String defaultName, boolean forConstant, Expression body, XSComponent source)
  {
    markAsAcknowledged();
    constantPropertyErrorCheck();
    
    String name = getPropertyName(forConstant);
    if (name == null) {
      name = defaultName;
    }
    FieldItem fi = new FieldItem(name, body, getBaseType(), source.getLocator());
    fi.javadoc = concat(this.javadoc, getBuilder().getBindInfo(source).getDocumentation());
    
    fi.realization = getRealization();
    
    _assert(fi.realization != null);
    if (needIsSetMethod()) {
      fi.realization = IsSetFieldRenderer.createFactory(fi.realization);
    }
    return fi;
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
        getBuilder().errorReceiver.error(getLocation(), Messages.format("BIProperty.IllegalFixedAttributeAsConstantProperty"));
        
        this.isConstantProperty = null;
      }
    }
  }
  
  private final XSFinder hasFixedValue = new BIProperty.1(this);
  
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
        BIProperty prop = (BIProperty)builder.getBindInfo(c).get(NAME);
        if (prop != null) {
          return prop;
        }
      }
    }
    return builder.getGlobalBinding().getDefaultProperty();
  }
  
  public static BIProperty getCustomization(BGMBuilder builder, XSComponent c)
  {
    if (c != null)
    {
      BIProperty prop = (BIProperty)builder.getBindInfo(c).get(NAME);
      if (prop != null) {
        return prop;
      }
    }
    return getDefault(builder, c);
  }
  
  private static final XSFunction defaultCustomizationFinder = new BIProperty.2();
  
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
  private static final String ERR_ILLEGAL_FIXEDATTR = "BIProperty.IllegalFixedAttributeAsConstantProperty";
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIProperty.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */