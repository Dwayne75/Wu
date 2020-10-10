package com.sun.tools.xjc.reader.xmlschema.bindinfo.parser;

import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXSerializable;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXSuperClass;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.NGCCRuntimeEx;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class globalBindings
  extends NGCCHandler
{
  private String xmlType;
  private BIXSerializable xSerializable;
  private boolean xTypeSubstitution;
  private FieldRendererFactory ct;
  private String value;
  private String __text;
  private BIConversion conv;
  private BIXSuperClass xSuperClass;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private Locator loc;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public globalBindings(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 81;
  }
  
  public globalBindings(NGCCRuntimeEx runtime)
  {
    this(null, runtime, runtime, -1);
  }
  
  private void action0()
    throws SAXException
  {
    this.$runtime.options.generateValidatingUnmarshallingCode = false;
  }
  
  private void action1()
    throws SAXException
  {
    this.$runtime.options.generateValidationCode = false;
  }
  
  private void action2()
    throws SAXException
  {
    this.$runtime.options.generateUnmarshallingCode = false;
    this.$runtime.options.generateValidatingUnmarshallingCode = false;
  }
  
  private void action3()
    throws SAXException
  {
    this.$runtime.options.generateMarshallingCode = false;
  }
  
  private void action4()
    throws SAXException
  {
    this.xSmartWildcardDefaultBinding = true;
  }
  
  private void action5()
    throws SAXException
  {
    this.globalConvs.put(this.$runtime.parseQName(this.xmlType), this.conv);
  }
  
  private void action6()
    throws SAXException
  {
    if (this.$runtime.parseBoolean(this.value) == true) {
      this.$runtime.reportUnsupportedFeature("enableFailFastCheck");
    }
  }
  
  private void action7()
    throws SAXException
  {
    if (this.$runtime.parseBoolean(this.value) == true) {
      this.$runtime.reportUnsupportedFeature("enableValidation");
    }
  }
  
  private void action8()
    throws SAXException
  {
    this.choiceContentPropertyWithModelGroupBinding = this.$runtime.parseBoolean(this.value);
  }
  
  private void action9()
    throws SAXException
  {
    this.modelGroupBinding = true;
  }
  
  private void action10()
    throws SAXException
  {
    this.modelGroupBinding = false;
  }
  
  private void action11()
    throws SAXException
  {
    QName qn = this.$runtime.parseQName(this.value);
    this.enumBaseTypes.add(qn);
  }
  
  private void action12()
    throws SAXException
  {
    this.$runtime.processList(this.__text);
  }
  
  private void action13()
    throws SAXException
  {
    this.generateEnumMemberName = true;
  }
  
  private void action14()
    throws SAXException
  {
    this.nameConverter = NameConverter.jaxrpcCompatible;
  }
  
  private void action15()
    throws SAXException
  {
    this.loc = this.$runtime.copyLocator();
  }
  
  public void enterElement(String $__uri, String $__local, String $__qname, Attributes $attrs)
    throws SAXException
  {
    this.$uri = $__uri;
    this.$localName = $__local;
    this.$qname = $__qname;
    switch (this.$_ngcc_current_state)
    {
    case 63: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "generateIsSetMethod")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 59;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 75: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "underscoreBinding")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 71;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 38: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "choiceContentProperty")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 34;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 30: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "enableFailFastCheck")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 1: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 28;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "serializable"))
      {
        NGCCHandler h = new serializable(this, this._source, this.$runtime, 168);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "superClass"))
      {
        NGCCHandler h = new superClass(this, this._source, this.$runtime, 169);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "typeSubstitution"))
      {
        NGCCHandler h = new typeSubstitution(this, this._source, this.$runtime, 170);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "smartWildcardDefaultBinding"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action4();
        this.$_ngcc_current_state = 16;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "noMarshaller"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action3();
        this.$_ngcc_current_state = 13;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "noUnmarshaller"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action2();
        this.$_ngcc_current_state = 10;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "noValidator"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action1();
        this.$_ngcc_current_state = 7;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "noValidatingUnmarshaller"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action0();
        this.$_ngcc_current_state = 4;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 48: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "typesafeEnumBase")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 42;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 25: 
      int $ai;
      if ((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "parseMethod")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "printMethod")) >= 0))
      {
        NGCCHandler h = new conversionBody(this, this._source, this.$runtime, 126);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 53: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "typesafeEnumMemberName")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 48;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 59: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "collectionType")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 53;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 42: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "bindingStyle")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 38;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 2: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 28;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "serializable"))
      {
        NGCCHandler h = new serializable(this, this._source, this.$runtime, 177);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "superClass"))
      {
        NGCCHandler h = new superClass(this, this._source, this.$runtime, 178);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "typeSubstitution"))
      {
        NGCCHandler h = new typeSubstitution(this, this._source, this.$runtime, 179);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "smartWildcardDefaultBinding"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action4();
        this.$_ngcc_current_state = 16;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "noMarshaller"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action3();
        this.$_ngcc_current_state = 13;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "noUnmarshaller"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action2();
        this.$_ngcc_current_state = 10;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "noValidator"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action1();
        this.$_ngcc_current_state = 7;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "noValidatingUnmarshaller"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action0();
        this.$_ngcc_current_state = 4;
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 28: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "xmlType")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 67: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "fixedAttributeAsConstantProperty")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 63;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 34: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "enableValidation")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 30;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 71: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "enableJavaNamingConventions")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 67;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 81: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "globalBindings"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action15();
        this.$_ngcc_current_state = 75;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(makeResult(), this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    default: 
      unexpectedEnterElement($__qname);
    }
  }
  
  public void leaveElement(String $__uri, String $__local, String $__qname)
    throws SAXException
  {
    this.$uri = $__uri;
    this.$localName = $__local;
    this.$qname = $__qname;
    switch (this.$_ngcc_current_state)
    {
    case 63: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "generateIsSetMethod")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 59;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 75: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "underscoreBinding")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 71;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 13: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "noMarshaller"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 38: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "choiceContentProperty")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 34;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 7: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "noValidator"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 30: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "enableFailFastCheck")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 24: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 4: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "noValidatingUnmarshaller"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 1: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "globalBindings"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 48: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "typesafeEnumBase")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 42;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 25: 
      int $ai;
      if (((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType")) || ((($ai = this.$runtime.getAttributeIndex("", "parseMethod")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType")) || ((($ai = this.$runtime.getAttributeIndex("", "printMethod")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType")))
      {
        NGCCHandler h = new conversionBody(this, this._source, this.$runtime, 126);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 53: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "typesafeEnumMemberName")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 48;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 59: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "collectionType")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 53;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 10: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "noUnmarshaller"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 42: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "bindingStyle")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 38;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 67: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "fixedAttributeAsConstantProperty")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 63;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 28: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "xmlType")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 34: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "enableValidation")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 30;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 16: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "smartWildcardDefaultBinding"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 71: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "enableJavaNamingConventions")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 67;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveElement(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 3: 
    case 5: 
    case 6: 
    case 8: 
    case 9: 
    case 11: 
    case 12: 
    case 14: 
    case 15: 
    case 17: 
    case 18: 
    case 19: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 26: 
    case 27: 
    case 29: 
    case 31: 
    case 32: 
    case 33: 
    case 35: 
    case 36: 
    case 37: 
    case 39: 
    case 40: 
    case 41: 
    case 43: 
    case 44: 
    case 45: 
    case 46: 
    case 47: 
    case 49: 
    case 50: 
    case 51: 
    case 52: 
    case 54: 
    case 55: 
    case 56: 
    case 57: 
    case 58: 
    case 60: 
    case 61: 
    case 62: 
    case 64: 
    case 65: 
    case 66: 
    case 68: 
    case 69: 
    case 70: 
    case 72: 
    case 73: 
    case 74: 
    default: 
      unexpectedLeaveElement($__qname);
    }
  }
  
  public void enterAttribute(String $__uri, String $__local, String $__qname)
    throws SAXException
  {
    this.$uri = $__uri;
    this.$localName = $__local;
    this.$qname = $__qname;
    switch (this.$_ngcc_current_state)
    {
    case 63: 
      if (($__uri == "") && ($__local == "generateIsSetMethod"))
      {
        this.$_ngcc_current_state = 65;
      }
      else
      {
        this.$_ngcc_current_state = 59;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 75: 
      if (($__uri == "") && ($__local == "underscoreBinding"))
      {
        this.$_ngcc_current_state = 77;
      }
      else
      {
        this.$_ngcc_current_state = 71;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 38: 
      if (($__uri == "") && ($__local == "choiceContentProperty"))
      {
        this.$_ngcc_current_state = 40;
      }
      else
      {
        this.$_ngcc_current_state = 34;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 30: 
      if (($__uri == "") && ($__local == "enableFailFastCheck"))
      {
        this.$_ngcc_current_state = 32;
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 48: 
      if (($__uri == "") && ($__local == "typesafeEnumBase"))
      {
        this.$_ngcc_current_state = 51;
      }
      else
      {
        this.$_ngcc_current_state = 42;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 25: 
      if ((($__uri == "") && ($__local == "name")) || (($__uri == "") && ($__local == "parseMethod")) || (($__uri == "") && ($__local == "printMethod")))
      {
        NGCCHandler h = new conversionBody(this, this._source, this.$runtime, 126);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 59: 
      if (($__uri == "") && ($__local == "collectionType"))
      {
        this.$_ngcc_current_state = 61;
      }
      else
      {
        this.$_ngcc_current_state = 53;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 53: 
      if (($__uri == "") && ($__local == "typesafeEnumMemberName"))
      {
        this.$_ngcc_current_state = 55;
      }
      else
      {
        this.$_ngcc_current_state = 48;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 42: 
      if (($__uri == "") && ($__local == "bindingStyle"))
      {
        this.$_ngcc_current_state = 44;
      }
      else
      {
        this.$_ngcc_current_state = 38;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 28: 
      if (($__uri == "") && ($__local == "xmlType")) {
        this.$_ngcc_current_state = 27;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 67: 
      if (($__uri == "") && ($__local == "fixedAttributeAsConstantProperty"))
      {
        this.$_ngcc_current_state = 69;
      }
      else
      {
        this.$_ngcc_current_state = 63;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 34: 
      if (($__uri == "") && ($__local == "enableValidation"))
      {
        this.$_ngcc_current_state = 36;
      }
      else
      {
        this.$_ngcc_current_state = 30;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 71: 
      if (($__uri == "") && ($__local == "enableJavaNamingConventions"))
      {
        this.$_ngcc_current_state = 73;
      }
      else
      {
        this.$_ngcc_current_state = 67;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    default: 
      unexpectedEnterAttribute($__qname);
    }
  }
  
  public void leaveAttribute(String $__uri, String $__local, String $__qname)
    throws SAXException
  {
    this.$uri = $__uri;
    this.$localName = $__local;
    this.$qname = $__qname;
    switch (this.$_ngcc_current_state)
    {
    case 63: 
      this.$_ngcc_current_state = 59;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 64: 
      if (($__uri == "") && ($__local == "generateIsSetMethod")) {
        this.$_ngcc_current_state = 59;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 30: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 26: 
      if (($__uri == "") && ($__local == "xmlType")) {
        this.$_ngcc_current_state = 25;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 59: 
      this.$_ngcc_current_state = 53;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 76: 
      if (($__uri == "") && ($__local == "underscoreBinding")) {
        this.$_ngcc_current_state = 71;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 35: 
      if (($__uri == "") && ($__local == "enableValidation")) {
        this.$_ngcc_current_state = 30;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 54: 
      if (($__uri == "") && ($__local == "typesafeEnumMemberName")) {
        this.$_ngcc_current_state = 48;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 67: 
      this.$_ngcc_current_state = 63;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 39: 
      if (($__uri == "") && ($__local == "choiceContentProperty")) {
        this.$_ngcc_current_state = 34;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 71: 
      this.$_ngcc_current_state = 67;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 68: 
      if (($__uri == "") && ($__local == "fixedAttributeAsConstantProperty")) {
        this.$_ngcc_current_state = 63;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 75: 
      this.$_ngcc_current_state = 71;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 38: 
      this.$_ngcc_current_state = 34;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 60: 
      if (($__uri == "") && ($__local == "collectionType")) {
        this.$_ngcc_current_state = 53;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 43: 
      if (($__uri == "") && ($__local == "bindingStyle")) {
        this.$_ngcc_current_state = 38;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 31: 
      if (($__uri == "") && ($__local == "enableFailFastCheck")) {
        this.$_ngcc_current_state = 2;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 48: 
      this.$_ngcc_current_state = 42;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 53: 
      this.$_ngcc_current_state = 48;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 42: 
      this.$_ngcc_current_state = 38;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 34: 
      this.$_ngcc_current_state = 30;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 72: 
      if (($__uri == "") && ($__local == "enableJavaNamingConventions")) {
        this.$_ngcc_current_state = 67;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 49: 
      if (($__uri == "") && ($__local == "typesafeEnumBase")) {
        this.$_ngcc_current_state = 42;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 1: 
    case 3: 
    case 4: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 10: 
    case 11: 
    case 12: 
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 18: 
    case 19: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 25: 
    case 27: 
    case 28: 
    case 29: 
    case 32: 
    case 33: 
    case 36: 
    case 37: 
    case 40: 
    case 41: 
    case 44: 
    case 45: 
    case 46: 
    case 47: 
    case 50: 
    case 51: 
    case 52: 
    case 55: 
    case 56: 
    case 57: 
    case 58: 
    case 61: 
    case 62: 
    case 65: 
    case 66: 
    case 69: 
    case 70: 
    case 73: 
    case 74: 
    default: 
      unexpectedLeaveAttribute($__qname);
    }
  }
  
  public void text(String $value)
    throws SAXException
  {
    switch (this.$_ngcc_current_state)
    {
    case 63: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "generateIsSetMethod")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 59;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 32: 
      this.value = $value;
      this.$_ngcc_current_state = 31;
      action6();
      
      break;
    case 27: 
      this.xmlType = $value;
      this.$_ngcc_current_state = 26;
      
      break;
    case 30: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "enableFailFastCheck")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 44: 
      if ($value.equals("elementBinding"))
      {
        this.$_ngcc_current_state = 43;
        action10();
      }
      else if ($value.equals("modelGroupBinding"))
      {
        this.$_ngcc_current_state = 43;
        action9();
      }
      break;
    case 25: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "printMethod")) >= 0)
      {
        NGCCHandler h = new conversionBody(this, this._source, this.$runtime, 126);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "parseMethod")) >= 0)
      {
        NGCCHandler h = new conversionBody(this, this._source, this.$runtime, 126);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        NGCCHandler h = new conversionBody(this, this._source, this.$runtime, 126);
        spawnChildFromText(h, $value);
      }
      break;
    case 59: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "collectionType")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 53;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 69: 
      this.fixedAttrToConstantProperty = $value;
      this.$_ngcc_current_state = 68;
      
      break;
    case 67: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "fixedAttributeAsConstantProperty")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 63;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 71: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "enableJavaNamingConventions")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 67;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 75: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "underscoreBinding")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 71;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 38: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "choiceContentProperty")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 34;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 51: 
      this.__text = $value;
      this.$_ngcc_current_state = 50;
      action12();
      
      break;
    case 40: 
      this.value = $value;
      this.$_ngcc_current_state = 39;
      action8();
      
      break;
    case 61: 
      NGCCHandler h = new CollectionTypeState(this, this._source, this.$runtime, 225);
      spawnChildFromText(h, $value);
      
      break;
    case 48: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "typesafeEnumBase")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 42;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 53: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "typesafeEnumMemberName")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 48;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 65: 
      this.needIsSetMethod = $value;
      this.$_ngcc_current_state = 64;
      
      break;
    case 42: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "bindingStyle")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 38;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 28: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "xmlType")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 34: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "enableValidation")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 30;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 36: 
      this.value = $value;
      this.$_ngcc_current_state = 35;
      action7();
      
      break;
    case 50: 
      this.value = $value;
      this.$_ngcc_current_state = 49;
      action11();
      
      break;
    case 55: 
      if ($value.equals("generateError"))
      {
        this.$_ngcc_current_state = 54;
      }
      else if ($value.equals("generateName"))
      {
        this.$_ngcc_current_state = 54;
        action13();
      }
      break;
    case 73: 
      this.enableJavaNamingConvention = $value;
      this.$_ngcc_current_state = 72;
      
      break;
    case 77: 
      if ($value.equals("asWordSeparator"))
      {
        this.$_ngcc_current_state = 76;
      }
      else if ($value.equals("asCharInWord"))
      {
        this.$_ngcc_current_state = 76;
        action14();
      }
      break;
    case 0: 
      revertToParentFromText(makeResult(), this._cookie, $value);
      
      break;
    case 49: 
      this.value = $value;
      this.$_ngcc_current_state = 49;
      action11();
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 168: 
      this.xSerializable = ((BIXSerializable)$__result__);
      this.$_ngcc_current_state = 1;
      
      break;
    case 169: 
      this.xSuperClass = ((BIXSuperClass)$__result__);
      this.$_ngcc_current_state = 1;
      
      break;
    case 170: 
      this.xTypeSubstitution = ((Boolean)$__result__).booleanValue();
      this.$_ngcc_current_state = 1;
      
      break;
    case 177: 
      this.xSerializable = ((BIXSerializable)$__result__);
      this.$_ngcc_current_state = 1;
      
      break;
    case 178: 
      this.xSuperClass = ((BIXSuperClass)$__result__);
      this.$_ngcc_current_state = 1;
      
      break;
    case 179: 
      this.xTypeSubstitution = ((Boolean)$__result__).booleanValue();
      this.$_ngcc_current_state = 1;
      
      break;
    case 126: 
      this.conv = ((BIConversion)$__result__);
      action5();
      this.$_ngcc_current_state = 24;
      
      break;
    case 225: 
      this.ct = ((FieldRendererFactory)$__result__);
      this.$_ngcc_current_state = 60;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
  
  private Map globalConvs = new HashMap();
  private NameConverter nameConverter = NameConverter.standard;
  private String enableJavaNamingConvention = "true";
  private String fixedAttrToConstantProperty = "false";
  private String needIsSetMethod = "false";
  private Set enumBaseTypes = new HashSet();
  private boolean generateEnumMemberName = false;
  private boolean modelGroupBinding = false;
  private boolean choiceContentPropertyWithModelGroupBinding = false;
  private boolean xSmartWildcardDefaultBinding = false;
  
  public BIGlobalBinding makeResult()
  {
    if (this.enumBaseTypes.size() == 0) {
      this.enumBaseTypes.add(new QName("http://www.w3.org/2001/XMLSchema", "NCName"));
    }
    return new BIGlobalBinding(this.$runtime.codeModel, this.globalConvs, this.nameConverter, this.modelGroupBinding, this.choiceContentPropertyWithModelGroupBinding, this.$runtime.parseBoolean(this.enableJavaNamingConvention), this.$runtime.parseBoolean(this.fixedAttrToConstantProperty), this.$runtime.parseBoolean(this.needIsSetMethod), this.generateEnumMemberName, this.enumBaseTypes, this.ct, this.xSerializable, this.xSuperClass, this.xTypeSubstitution, this.xSmartWildcardDefaultBinding, this.loc);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\parser\globalBindings.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */