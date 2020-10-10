package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.impl.AnnotationImpl;
import com.sun.xml.xsom.impl.ComplexTypeImpl;
import com.sun.xml.xsom.impl.ContentTypeImpl;
import com.sun.xml.xsom.impl.ForeignAttributesImpl;
import com.sun.xml.xsom.impl.ModelGroupImpl;
import com.sun.xml.xsom.impl.ParticleImpl;
import com.sun.xml.xsom.impl.Ref.ContentType;
import com.sun.xml.xsom.impl.Ref.SimpleType;
import com.sun.xml.xsom.impl.Ref.Type;
import com.sun.xml.xsom.impl.RestrictionSimpleTypeImpl;
import com.sun.xml.xsom.impl.SchemaSetImpl;
import com.sun.xml.xsom.impl.SimpleTypeImpl;
import com.sun.xml.xsom.impl.UName;
import com.sun.xml.xsom.impl.parser.BaseContentRef;
import com.sun.xml.xsom.impl.parser.DelayedRef.Type;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.impl.parser.ParserContext;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.parser.AnnotationContext;
import java.util.Collections;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class complexType
  extends NGCCHandler
{
  private Integer finalValue;
  private String name;
  private String abstractValue;
  private Integer blockValue;
  private XSFacet facet;
  private ForeignAttributesImpl fa;
  private AnnotationImpl annotation;
  private ContentTypeImpl explicitContent;
  private UName baseTypeName;
  private String mixedValue;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private ComplexTypeImpl result;
  private Ref.Type baseType;
  private Ref.ContentType contentType;
  private Ref.SimpleType baseContentType;
  private RestrictionSimpleTypeImpl contentSimpleType;
  private Locator locator;
  private Locator locator2;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public complexType(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 88;
  }
  
  public complexType(NGCCRuntimeEx runtime)
  {
    this(null, runtime, runtime, -1);
  }
  
  private void action0()
    throws SAXException
  {
    this.result.setContentType(this.explicitContent);
  }
  
  private void action1()
    throws SAXException
  {
    this.baseType = this.$runtime.parser.schemaSet.anyType;
    makeResult(2);
  }
  
  private void action2()
    throws SAXException
  {
    this.result.setExplicitContent(this.explicitContent);
    this.result.setContentType(buildComplexExtensionContentModel(this.explicitContent));
  }
  
  private void action3()
    throws SAXException
  {
    this.baseType = new DelayedRef.Type(this.$runtime, this.locator2, this.$runtime.currentSchema, this.baseTypeName);
    
    makeResult(1);
  }
  
  private void action4()
    throws SAXException
  {
    this.locator2 = this.$runtime.copyLocator();
  }
  
  private void action5()
    throws SAXException
  {
    this.result.setContentType(this.explicitContent);
  }
  
  private void action6()
    throws SAXException
  {
    this.baseType = new DelayedRef.Type(this.$runtime, this.locator2, this.$runtime.currentSchema, this.baseTypeName);
    
    makeResult(2);
  }
  
  private void action7()
    throws SAXException
  {
    this.locator2 = this.$runtime.copyLocator();
  }
  
  private void action8()
    throws SAXException
  {
    this.contentType = new BaseContentRef(this.$runtime, this.baseType);
    makeResult(1);
    this.result.setContentType(this.contentType);
  }
  
  private void action9()
    throws SAXException
  {
    this.baseType = new DelayedRef.Type(this.$runtime, this.locator2, this.$runtime.currentSchema, this.baseTypeName);
  }
  
  private void action10()
    throws SAXException
  {
    this.locator2 = this.$runtime.copyLocator();
  }
  
  private void action11()
    throws SAXException
  {
    makeResult(2);
    this.result.setContentType(this.contentType);
  }
  
  private void action12()
    throws SAXException
  {
    this.contentSimpleType.addFacet(this.facet);
  }
  
  private void action13()
    throws SAXException
  {
    if (this.baseContentType == null) {
      this.baseContentType = new BaseContentSimpleTypeRef(this.baseType, null);
    }
    this.contentSimpleType = new RestrictionSimpleTypeImpl(this.$runtime.document, null, this.locator2, null, null, true, Collections.EMPTY_SET, this.baseContentType);
    
    this.contentType = this.contentSimpleType;
  }
  
  private void action14()
    throws SAXException
  {
    this.baseType = new DelayedRef.Type(this.$runtime, this.locator2, this.$runtime.currentSchema, this.baseTypeName);
  }
  
  private void action15()
    throws SAXException
  {
    this.locator2 = this.$runtime.copyLocator();
  }
  
  private void action16()
    throws SAXException
  {
    this.locator = this.$runtime.copyLocator();
  }
  
  public void enterElement(String $__uri, String $__local, String $__qname, Attributes $attrs)
    throws SAXException
  {
    this.$uri = $__uri;
    this.$localName = $__local;
    this.$qname = $__qname;
    int $ai;
    switch (this.$_ngcc_current_state)
    {
    case 7: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action7();
        this.$_ngcc_current_state = 24;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action4();
        this.$_ngcc_current_state = 15;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 38: 
      action8();
      this.$_ngcc_current_state = 37;
      this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 48: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("totalDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("fractionDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("length"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("enumeration"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("whiteSpace"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("pattern"))))
      {
        NGCCHandler h = new facet(this, this._source, this.$runtime, 257);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        action11();
        this.$_ngcc_current_state = 47;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 72: 
      if (($ai = this.$runtime.getAttributeIndex("", "mixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 68;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 44: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 49: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("totalDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("fractionDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("length"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("enumeration"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("whiteSpace"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("pattern"))))
      {
        NGCCHandler h = new facet(this, this._source, this.$runtime, 258);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 48;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 18: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))))
      {
        NGCCHandler h = new complexType_complexContent_body(this, this._source, this.$runtime, 219, this.result);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 67: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexContent"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleContent"))))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 284, this.fa);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 88: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action16();
        this.$_ngcc_current_state = 84;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 61: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 274, this.annotation, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 35;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 28: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension"))))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 232, this.fa);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 80: 
      if (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 76;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 12: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 212, this.fa);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 35: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action15();
        this.$_ngcc_current_state = 59;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action10();
        this.$_ngcc_current_state = 44;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 76: 
      if (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 72;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 51: 
      action13();
      this.$_ngcc_current_state = 49;
      this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 47: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))))
      {
        NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 254, this.result);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 68: 
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
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
    case 15: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 37: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))))
      {
        NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 242, this.result);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 26: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 230, this.annotation, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 7;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 56: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("totalDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("fractionDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("length"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("enumeration"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("whiteSpace"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("pattern"))))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 267, this.fa);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 24: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 10: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 210, this.annotation, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 9;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 41: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 247, this.fa);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 84: 
      if (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 80;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 2: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleContent")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 63;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexContent")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 29;
      }
      else if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))))
      {
        action1();
        NGCCHandler h = new complexType_complexContent_body(this, this._source, this.$runtime, 205, this.result);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 65: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 282, null, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 21: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 223, this.fa);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 54: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 265, this.annotation, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 52;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 52: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType")))
      {
        NGCCHandler h = new simpleType(this, this._source, this.$runtime, 262);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 51;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 39: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 245, this.annotation, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 38;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(this.result, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 29: 
      if (($ai = this.$runtime.getAttributeIndex("", "mixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 28;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 9: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))))
      {
        NGCCHandler h = new complexType_complexContent_body(this, this._source, this.$runtime, 208, this.result);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 19: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 221, this.annotation, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 18;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 63: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 276, this.fa);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 59: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 1: 
    case 3: 
    case 4: 
    case 5: 
    case 6: 
    case 8: 
    case 11: 
    case 13: 
    case 14: 
    case 16: 
    case 17: 
    case 20: 
    case 22: 
    case 23: 
    case 25: 
    case 27: 
    case 30: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 36: 
    case 40: 
    case 42: 
    case 43: 
    case 45: 
    case 46: 
    case 50: 
    case 53: 
    case 55: 
    case 57: 
    case 58: 
    case 60: 
    case 62: 
    case 64: 
    case 66: 
    case 69: 
    case 70: 
    case 71: 
    case 73: 
    case 74: 
    case 75: 
    case 77: 
    case 78: 
    case 79: 
    case 81: 
    case 82: 
    case 83: 
    case 85: 
    case 86: 
    case 87: 
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
    int $ai;
    switch (this.$_ngcc_current_state)
    {
    case 38: 
      action8();
      this.$_ngcc_current_state = 37;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 48: 
      action11();
      this.$_ngcc_current_state = 47;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 6: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexContent")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 36: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 34;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 44: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 72: 
      if (($ai = this.$runtime.getAttributeIndex("", "mixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 68;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 49: 
      this.$_ngcc_current_state = 48;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 18: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        NGCCHandler h = new complexType_complexContent_body(this, this._source, this.$runtime, 219, this.result);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 67: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType")))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 284, this.fa);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 61: 
      this.$_ngcc_current_state = 35;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 34: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleContent")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 80: 
      if (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 76;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 12: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension")))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 212, this.fa);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 76: 
      if (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 72;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 51: 
      action13();
      this.$_ngcc_current_state = 49;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 47: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 254, this.result);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 68: 
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
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
    case 15: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 37: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension")))
      {
        NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 242, this.result);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 46: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 34;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 26: 
      this.$_ngcc_current_state = 7;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 56: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 267, this.fa);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 24: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 10: 
      this.$_ngcc_current_state = 9;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 41: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension")))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 247, this.fa);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 84: 
      if (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 80;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 2: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType")))
      {
        action1();
        NGCCHandler h = new complexType_complexContent_body(this, this._source, this.$runtime, 205, this.result);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 65: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 17: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 6;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 21: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 223, this.fa);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 54: 
      this.$_ngcc_current_state = 52;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 52: 
      this.$_ngcc_current_state = 51;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 39: 
      this.$_ngcc_current_state = 38;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 8: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 6;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 1: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveElement(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 29: 
      if (($ai = this.$runtime.getAttributeIndex("", "mixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 28;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 9: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension")))
      {
        NGCCHandler h = new complexType_complexContent_body(this, this._source, this.$runtime, 208, this.result);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 19: 
      this.$_ngcc_current_state = 18;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 59: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 3: 
    case 4: 
    case 5: 
    case 7: 
    case 11: 
    case 13: 
    case 14: 
    case 16: 
    case 20: 
    case 22: 
    case 23: 
    case 25: 
    case 27: 
    case 28: 
    case 30: 
    case 31: 
    case 32: 
    case 33: 
    case 35: 
    case 40: 
    case 42: 
    case 43: 
    case 45: 
    case 50: 
    case 53: 
    case 55: 
    case 57: 
    case 58: 
    case 60: 
    case 62: 
    case 63: 
    case 64: 
    case 66: 
    case 69: 
    case 70: 
    case 71: 
    case 73: 
    case 74: 
    case 75: 
    case 77: 
    case 78: 
    case 79: 
    case 81: 
    case 82: 
    case 83: 
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
    case 68: 
      if (($__uri.equals("")) && ($__local.equals("name")))
      {
        this.$_ngcc_current_state = 70;
      }
      else
      {
        this.$_ngcc_current_state = 67;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 38: 
      action8();
      this.$_ngcc_current_state = 37;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 48: 
      action11();
      this.$_ngcc_current_state = 47;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 15: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 14;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 26: 
      this.$_ngcc_current_state = 7;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 24: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 23;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 10: 
      this.$_ngcc_current_state = 9;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 84: 
      if (($__uri.equals("")) && ($__local.equals("abstract")))
      {
        this.$_ngcc_current_state = 86;
      }
      else
      {
        this.$_ngcc_current_state = 80;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 44: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 43;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 72: 
      if (($__uri.equals("")) && ($__local.equals("mixed")))
      {
        this.$_ngcc_current_state = 74;
      }
      else
      {
        this.$_ngcc_current_state = 68;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 49: 
      this.$_ngcc_current_state = 48;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 65: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 54: 
      this.$_ngcc_current_state = 52;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 52: 
      this.$_ngcc_current_state = 51;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 39: 
      this.$_ngcc_current_state = 38;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 61: 
      this.$_ngcc_current_state = 35;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromEnterAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 80: 
      if (($__uri.equals("")) && ($__local.equals("block")))
      {
        this.$_ngcc_current_state = 82;
      }
      else
      {
        this.$_ngcc_current_state = 76;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 29: 
      if (($__uri.equals("")) && ($__local.equals("mixed")))
      {
        this.$_ngcc_current_state = 31;
      }
      else
      {
        this.$_ngcc_current_state = 28;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 76: 
      if (($__uri.equals("")) && ($__local.equals("final")))
      {
        this.$_ngcc_current_state = 78;
      }
      else
      {
        this.$_ngcc_current_state = 72;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 51: 
      action13();
      this.$_ngcc_current_state = 49;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 19: 
      this.$_ngcc_current_state = 18;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 59: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 58;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 1: 
    case 2: 
    case 3: 
    case 4: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 11: 
    case 12: 
    case 13: 
    case 14: 
    case 16: 
    case 17: 
    case 18: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 25: 
    case 27: 
    case 28: 
    case 30: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 35: 
    case 36: 
    case 37: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 45: 
    case 46: 
    case 47: 
    case 50: 
    case 53: 
    case 55: 
    case 56: 
    case 57: 
    case 58: 
    case 60: 
    case 62: 
    case 63: 
    case 64: 
    case 66: 
    case 67: 
    case 69: 
    case 70: 
    case 71: 
    case 73: 
    case 74: 
    case 75: 
    case 77: 
    case 78: 
    case 79: 
    case 81: 
    case 82: 
    case 83: 
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
    case 81: 
      if (($__uri.equals("")) && ($__local.equals("block"))) {
        this.$_ngcc_current_state = 76;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 22: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 21;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 38: 
      action8();
      this.$_ngcc_current_state = 37;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 48: 
      action11();
      this.$_ngcc_current_state = 47;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 73: 
      if (($__uri.equals("")) && ($__local.equals("mixed"))) {
        this.$_ngcc_current_state = 68;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 30: 
      if (($__uri.equals("")) && ($__local.equals("mixed"))) {
        this.$_ngcc_current_state = 28;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 72: 
      this.$_ngcc_current_state = 68;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 49: 
      this.$_ngcc_current_state = 48;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 61: 
      this.$_ngcc_current_state = 35;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 80: 
      this.$_ngcc_current_state = 76;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 69: 
      if (($__uri.equals("")) && ($__local.equals("name"))) {
        this.$_ngcc_current_state = 67;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 76: 
      this.$_ngcc_current_state = 72;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 51: 
      action13();
      this.$_ngcc_current_state = 49;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 77: 
      if (($__uri.equals("")) && ($__local.equals("final"))) {
        this.$_ngcc_current_state = 72;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 68: 
      this.$_ngcc_current_state = 67;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 85: 
      if (($__uri.equals("")) && ($__local.equals("abstract"))) {
        this.$_ngcc_current_state = 80;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 13: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 12;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 42: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 41;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 10: 
      this.$_ngcc_current_state = 9;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 26: 
      this.$_ngcc_current_state = 7;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 84: 
      this.$_ngcc_current_state = 80;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 65: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 54: 
      this.$_ngcc_current_state = 52;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 52: 
      this.$_ngcc_current_state = 51;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 39: 
      this.$_ngcc_current_state = 38;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 57: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 56;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 29: 
      this.$_ngcc_current_state = 28;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 19: 
      this.$_ngcc_current_state = 18;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
    case 2: 
    case 3: 
    case 4: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 11: 
    case 12: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 18: 
    case 20: 
    case 21: 
    case 23: 
    case 24: 
    case 25: 
    case 27: 
    case 28: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 35: 
    case 36: 
    case 37: 
    case 40: 
    case 41: 
    case 43: 
    case 44: 
    case 45: 
    case 46: 
    case 47: 
    case 50: 
    case 53: 
    case 55: 
    case 56: 
    case 58: 
    case 59: 
    case 60: 
    case 62: 
    case 63: 
    case 64: 
    case 66: 
    case 67: 
    case 70: 
    case 71: 
    case 74: 
    case 75: 
    case 78: 
    case 79: 
    case 82: 
    case 83: 
    default: 
      unexpectedLeaveAttribute($__qname);
    }
  }
  
  public void text(String $value)
    throws SAXException
  {
    int $ai;
    switch (this.$_ngcc_current_state)
    {
    case 38: 
      action8();
      this.$_ngcc_current_state = 37;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 48: 
      action11();
      this.$_ngcc_current_state = 47;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 70: 
      this.name = $value;
      this.$_ngcc_current_state = 69;
      
      break;
    case 82: 
      NGCCHandler h = new erSet(this, this._source, this.$runtime, 301);
      spawnChildFromText(h, $value);
      
      break;
    case 78: 
      NGCCHandler h = new erSet(this, this._source, this.$runtime, 296);
      spawnChildFromText(h, $value);
      
      break;
    case 72: 
      if (($ai = this.$runtime.getAttributeIndex("", "mixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 68;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 44: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 49: 
      this.$_ngcc_current_state = 48;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 31: 
      this.mixedValue = $value;
      this.$_ngcc_current_state = 30;
      
      break;
    case 61: 
      this.$_ngcc_current_state = 35;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 80: 
      if (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 76;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 14: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 214);
      spawnChildFromText(h, $value);
      
      break;
    case 74: 
      this.mixedValue = $value;
      this.$_ngcc_current_state = 73;
      
      break;
    case 76: 
      if (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 72;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 51: 
      action13();
      this.$_ngcc_current_state = 49;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 68: 
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
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
    case 15: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 58: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 269);
      spawnChildFromText(h, $value);
      
      break;
    case 26: 
      this.$_ngcc_current_state = 7;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 10: 
      this.$_ngcc_current_state = 9;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 24: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 84: 
      if (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 80;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 65: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 54: 
      this.$_ngcc_current_state = 52;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 52: 
      this.$_ngcc_current_state = 51;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 39: 
      this.$_ngcc_current_state = 38;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 0: 
      revertToParentFromText(this.result, this._cookie, $value);
      
      break;
    case 86: 
      this.abstractValue = $value;
      this.$_ngcc_current_state = 85;
      
      break;
    case 23: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 225);
      spawnChildFromText(h, $value);
      
      break;
    case 29: 
      if (($ai = this.$runtime.getAttributeIndex("", "mixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 28;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 43: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 249);
      spawnChildFromText(h, $value);
      
      break;
    case 19: 
      this.$_ngcc_current_state = 18;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 59: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 257: 
      this.facet = ((XSFacet)$__result__);
      action12();
      this.$_ngcc_current_state = 48;
      
      break;
    case 301: 
      this.blockValue = ((Integer)$__result__);
      this.$_ngcc_current_state = 81;
      
      break;
    case 258: 
      this.facet = ((XSFacet)$__result__);
      action12();
      this.$_ngcc_current_state = 48;
      
      break;
    case 219: 
      this.explicitContent = ((ContentTypeImpl)$__result__);
      action5();
      this.$_ngcc_current_state = 17;
      
      break;
    case 232: 
      this.fa = ((ForeignAttributesImpl)$__result__);
      this.$_ngcc_current_state = 26;
      
      break;
    case 212: 
      this.fa = ((ForeignAttributesImpl)$__result__);
      this.$_ngcc_current_state = 10;
      
      break;
    case 242: 
      this.$_ngcc_current_state = 36;
      
      break;
    case 267: 
      this.fa = ((ForeignAttributesImpl)$__result__);
      this.$_ngcc_current_state = 54;
      
      break;
    case 210: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 9;
      
      break;
    case 247: 
      this.fa = ((ForeignAttributesImpl)$__result__);
      this.$_ngcc_current_state = 39;
      
      break;
    case 205: 
      this.explicitContent = ((ContentTypeImpl)$__result__);
      action0();
      this.$_ngcc_current_state = 1;
      
      break;
    case 262: 
      this.baseContentType = ((SimpleTypeImpl)$__result__);
      this.$_ngcc_current_state = 51;
      
      break;
    case 225: 
      this.baseTypeName = ((UName)$__result__);
      action6();
      this.$_ngcc_current_state = 22;
      
      break;
    case 208: 
      this.explicitContent = ((ContentTypeImpl)$__result__);
      action2();
      this.$_ngcc_current_state = 8;
      
      break;
    case 249: 
      this.baseTypeName = ((UName)$__result__);
      action9();
      this.$_ngcc_current_state = 42;
      
      break;
    case 296: 
      this.finalValue = ((Integer)$__result__);
      this.$_ngcc_current_state = 77;
      
      break;
    case 284: 
      this.fa = ((ForeignAttributesImpl)$__result__);
      this.$_ngcc_current_state = 65;
      
      break;
    case 274: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 35;
      
      break;
    case 214: 
      this.baseTypeName = ((UName)$__result__);
      action3();
      this.$_ngcc_current_state = 13;
      
      break;
    case 254: 
      this.$_ngcc_current_state = 46;
      
      break;
    case 269: 
      this.baseTypeName = ((UName)$__result__);
      action14();
      this.$_ngcc_current_state = 57;
      
      break;
    case 230: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 7;
      
      break;
    case 282: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 2;
      
      break;
    case 265: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 52;
      
      break;
    case 223: 
      this.fa = ((ForeignAttributesImpl)$__result__);
      this.$_ngcc_current_state = 19;
      
      break;
    case 245: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 38;
      
      break;
    case 276: 
      this.fa = ((ForeignAttributesImpl)$__result__);
      this.$_ngcc_current_state = 61;
      
      break;
    case 221: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 18;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
  
  private static class BaseContentSimpleTypeRef
    implements Ref.SimpleType
  {
    private final Ref.Type baseType;
    
    private BaseContentSimpleTypeRef(Ref.Type _baseType)
    {
      this.baseType = _baseType;
    }
    
    public XSSimpleType getType()
    {
      return (XSSimpleType)((XSComplexType)this.baseType.getType()).getContentType();
    }
  }
  
  private void makeResult(int derivationMethod)
  {
    if (this.finalValue == null) {
      this.finalValue = Integer.valueOf(this.$runtime.finalDefault);
    }
    if (this.blockValue == null) {
      this.blockValue = Integer.valueOf(this.$runtime.blockDefault);
    }
    this.result = new ComplexTypeImpl(this.$runtime.document, this.annotation, this.locator, this.fa, this.name, this.name == null, this.$runtime.parseBoolean(this.abstractValue), derivationMethod, this.baseType, this.finalValue.intValue(), this.blockValue.intValue(), this.$runtime.parseBoolean(this.mixedValue));
  }
  
  private static class BaseComplexTypeContentRef
    implements Ref.ContentType
  {
    private final Ref.Type baseType;
    
    private BaseComplexTypeContentRef(Ref.Type _baseType)
    {
      this.baseType = _baseType;
    }
    
    public XSContentType getContentType()
    {
      return ((XSComplexType)this.baseType.getType()).getContentType();
    }
  }
  
  private static class InheritBaseContentTypeRef
    implements Ref.ContentType
  {
    private final Ref.Type baseType;
    private final XSContentType empty;
    private final XSContentType expContent;
    private final SchemaDocumentImpl currentDocument;
    
    private InheritBaseContentTypeRef(Ref.Type _baseType, XSContentType _explicitContent, NGCCRuntimeEx $runtime)
    {
      this.baseType = _baseType;
      this.currentDocument = $runtime.document;
      this.expContent = _explicitContent;
      this.empty = $runtime.parser.schemaSet.empty;
    }
    
    public XSContentType getContentType()
    {
      XSContentType baseContentType = ((XSComplexType)this.baseType.getType()).getContentType();
      if (baseContentType == this.empty) {
        return this.expContent;
      }
      return new ParticleImpl(this.currentDocument, null, new ModelGroupImpl(this.currentDocument, null, null, null, XSModelGroup.SEQUENCE, new ParticleImpl[] { (ParticleImpl)baseContentType, (ParticleImpl)this.expContent }), null);
    }
  }
  
  private Ref.ContentType buildComplexExtensionContentModel(XSContentType explicitContent)
  {
    if (explicitContent == this.$runtime.parser.schemaSet.empty) {
      return new BaseComplexTypeContentRef(this.baseType, null);
    }
    return new InheritBaseContentTypeRef(this.baseType, explicitContent, this.$runtime, null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\state\complexType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */