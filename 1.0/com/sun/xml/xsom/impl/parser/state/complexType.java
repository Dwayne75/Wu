package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.impl.AnnotationImpl;
import com.sun.xml.xsom.impl.ComplexTypeImpl;
import com.sun.xml.xsom.impl.ContentTypeImpl;
import com.sun.xml.xsom.impl.Ref.ContentType;
import com.sun.xml.xsom.impl.Ref.SimpleType;
import com.sun.xml.xsom.impl.Ref.Type;
import com.sun.xml.xsom.impl.RestrictionSimpleTypeImpl;
import com.sun.xml.xsom.impl.SchemaSetImpl;
import com.sun.xml.xsom.impl.SimpleTypeImpl;
import com.sun.xml.xsom.impl.UName;
import com.sun.xml.xsom.impl.parser.DelayedRef.Type;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.impl.parser.ParserContext;
import com.sun.xml.xsom.parser.AnnotationContext;
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
    this.$_ngcc_current_state = 81;
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
    this.contentType = new complexType.BaseContentRef(this.baseType, null);
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
      this.baseContentType = new complexType.BaseContentSimpleTypeRef(this.baseType, null);
    }
    this.contentSimpleType = new RestrictionSimpleTypeImpl(this.$runtime.currentSchema, null, this.locator2, null, true, this.baseContentType);
    
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
    switch (this.$_ngcc_current_state)
    {
    case 7: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action7();
        this.$_ngcc_current_state = 22;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action4();
        this.$_ngcc_current_state = 14;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 47: 
      action13();
      this.$_ngcc_current_state = 45;
      this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 32: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action15();
        this.$_ngcc_current_state = 54;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action10();
        this.$_ngcc_current_state = 40;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 40: 
      int $ai;
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
    case 65: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "mixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 61;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(this.result, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 35: 
      action8();
      this.$_ngcc_current_state = 34;
      this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 77: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 73;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 54: 
      int $ai;
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
    case 48: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType")))
      {
        NGCCHandler h = new simpleType(this, this._source, this.$runtime, 197);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 47;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 36: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 181, this.annotation, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 35;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 18: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 159, this.annotation, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 17;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 56: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 208, this.annotation, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 32;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 44: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("totalDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("fractionDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("length"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("enumeration"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("whiteSpace"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("pattern"))))
      {
        NGCCHandler h = new facet(this, this._source, this.$runtime, 192);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        action11();
        this.$_ngcc_current_state = 43;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 59: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 215, null, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 81: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action16();
        this.$_ngcc_current_state = 77;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 9: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))))
      {
        NGCCHandler h = new complexType_complexContent_body(this, this._source, this.$runtime, 147, this.result);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 50: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 200, this.annotation, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 48;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 69: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 65;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 26: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "mixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 24;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 2: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleContent")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 56;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexContent")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 26;
      }
      else if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))))
      {
        action1();
        NGCCHandler h = new complexType_complexContent_body(this, this._source, this.$runtime, 144, this.result);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 45: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("totalDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("fractionDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("length"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("enumeration"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("whiteSpace"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("pattern"))))
      {
        NGCCHandler h = new facet(this, this._source, this.$runtime, 193);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 44;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 10: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 149, this.annotation, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 9;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 73: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 69;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 43: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))))
      {
        NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 189, this.result);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 24: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 167, this.annotation, AnnotationContext.COMPLEXTYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 7;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 14: 
      int $ai;
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
    case 22: 
      int $ai;
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
    case 61: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
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
    case 17: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))))
      {
        NGCCHandler h = new complexType_complexContent_body(this, this._source, this.$runtime, 157, this.result);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 34: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))))
      {
        NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 178, this.result);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
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
    case 12: 
    case 13: 
    case 15: 
    case 16: 
    case 19: 
    case 20: 
    case 21: 
    case 23: 
    case 25: 
    case 27: 
    case 28: 
    case 29: 
    case 30: 
    case 31: 
    case 33: 
    case 37: 
    case 38: 
    case 39: 
    case 41: 
    case 42: 
    case 46: 
    case 49: 
    case 51: 
    case 52: 
    case 53: 
    case 55: 
    case 57: 
    case 58: 
    case 60: 
    case 62: 
    case 63: 
    case 64: 
    case 66: 
    case 67: 
    case 68: 
    case 70: 
    case 71: 
    case 72: 
    case 74: 
    case 75: 
    case 76: 
    case 78: 
    case 79: 
    case 80: 
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
    case 16: 
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
    case 47: 
      action13();
      this.$_ngcc_current_state = 45;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 40: 
      int $ai;
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
    case 65: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "mixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 61;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveElement(this.result, this._cookie, $__uri, $__local, $__qname);
      
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
    case 35: 
      action8();
      this.$_ngcc_current_state = 34;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 77: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 73;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 54: 
      int $ai;
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
    case 48: 
      this.$_ngcc_current_state = 47;
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
    case 18: 
      this.$_ngcc_current_state = 17;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 36: 
      this.$_ngcc_current_state = 35;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 56: 
      this.$_ngcc_current_state = 32;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 44: 
      action11();
      this.$_ngcc_current_state = 43;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 59: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 9: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension")))
      {
        NGCCHandler h = new complexType_complexContent_body(this, this._source, this.$runtime, 147, this.result);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 50: 
      this.$_ngcc_current_state = 48;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 33: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 31;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 69: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 65;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 26: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "mixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 24;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 31: 
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
    case 2: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType")))
      {
        action1();
        NGCCHandler h = new complexType_complexContent_body(this, this._source, this.$runtime, 144, this.result);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 45: 
      this.$_ngcc_current_state = 44;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 42: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 31;
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
    case 73: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 69;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 43: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 189, this.result);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 24: 
      this.$_ngcc_current_state = 7;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 14: 
      int $ai;
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
    case 22: 
      int $ai;
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
    case 61: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
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
    case 17: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        NGCCHandler h = new complexType_complexContent_body(this, this._source, this.$runtime, 157, this.result);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 34: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("extension")))
      {
        NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 178, this.result);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
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
    case 12: 
    case 13: 
    case 15: 
    case 19: 
    case 20: 
    case 21: 
    case 23: 
    case 25: 
    case 27: 
    case 28: 
    case 29: 
    case 30: 
    case 32: 
    case 37: 
    case 38: 
    case 39: 
    case 41: 
    case 46: 
    case 49: 
    case 51: 
    case 52: 
    case 53: 
    case 55: 
    case 57: 
    case 58: 
    case 60: 
    case 62: 
    case 63: 
    case 64: 
    case 66: 
    case 67: 
    case 68: 
    case 70: 
    case 71: 
    case 72: 
    case 74: 
    case 75: 
    case 76: 
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
    case 44: 
      action11();
      this.$_ngcc_current_state = 43;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 47: 
      action13();
      this.$_ngcc_current_state = 45;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 59: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 40: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 39;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 65: 
      if (($__uri.equals("")) && ($__local.equals("mixed")))
      {
        this.$_ngcc_current_state = 67;
      }
      else
      {
        this.$_ngcc_current_state = 61;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 50: 
      this.$_ngcc_current_state = 48;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 69: 
      if (($__uri.equals("")) && ($__local.equals("final")))
      {
        this.$_ngcc_current_state = 71;
      }
      else
      {
        this.$_ngcc_current_state = 65;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 35: 
      action8();
      this.$_ngcc_current_state = 34;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 26: 
      if (($__uri.equals("")) && ($__local.equals("mixed")))
      {
        this.$_ngcc_current_state = 28;
      }
      else
      {
        this.$_ngcc_current_state = 24;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 77: 
      if (($__uri.equals("")) && ($__local.equals("abstract")))
      {
        this.$_ngcc_current_state = 79;
      }
      else
      {
        this.$_ngcc_current_state = 73;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 54: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 53;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 45: 
      this.$_ngcc_current_state = 44;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 10: 
      this.$_ngcc_current_state = 9;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 73: 
      if (($__uri.equals("")) && ($__local.equals("block")))
      {
        this.$_ngcc_current_state = 75;
      }
      else
      {
        this.$_ngcc_current_state = 69;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 48: 
      this.$_ngcc_current_state = 47;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 18: 
      this.$_ngcc_current_state = 17;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 36: 
      this.$_ngcc_current_state = 35;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 24: 
      this.$_ngcc_current_state = 7;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 14: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 13;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 22: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 21;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 61: 
      if (($__uri.equals("")) && ($__local.equals("name")))
      {
        this.$_ngcc_current_state = 63;
      }
      else
      {
        this.$_ngcc_current_state = 59;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 56: 
      this.$_ngcc_current_state = 32;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
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
    case 15: 
    case 16: 
    case 17: 
    case 19: 
    case 20: 
    case 21: 
    case 23: 
    case 25: 
    case 27: 
    case 28: 
    case 29: 
    case 30: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 37: 
    case 38: 
    case 39: 
    case 41: 
    case 42: 
    case 43: 
    case 46: 
    case 49: 
    case 51: 
    case 52: 
    case 53: 
    case 55: 
    case 57: 
    case 58: 
    case 60: 
    case 62: 
    case 63: 
    case 64: 
    case 66: 
    case 67: 
    case 68: 
    case 70: 
    case 71: 
    case 72: 
    case 74: 
    case 75: 
    case 76: 
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
    case 47: 
      action13();
      this.$_ngcc_current_state = 45;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 65: 
      this.$_ngcc_current_state = 61;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 35: 
      action8();
      this.$_ngcc_current_state = 34;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 70: 
      if (($__uri.equals("")) && ($__local.equals("final"))) {
        this.$_ngcc_current_state = 65;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 77: 
      this.$_ngcc_current_state = 73;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 62: 
      if (($__uri.equals("")) && ($__local.equals("name"))) {
        this.$_ngcc_current_state = 59;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 66: 
      if (($__uri.equals("")) && ($__local.equals("mixed"))) {
        this.$_ngcc_current_state = 61;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 48: 
      this.$_ngcc_current_state = 47;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 12: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 10;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 38: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 36;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 36: 
      this.$_ngcc_current_state = 35;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 18: 
      this.$_ngcc_current_state = 17;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 20: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 18;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 56: 
      this.$_ngcc_current_state = 32;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 44: 
      action11();
      this.$_ngcc_current_state = 43;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 59: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 27: 
      if (($__uri.equals("")) && ($__local.equals("mixed"))) {
        this.$_ngcc_current_state = 24;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 50: 
      this.$_ngcc_current_state = 48;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 69: 
      this.$_ngcc_current_state = 65;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 52: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 50;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 26: 
      this.$_ngcc_current_state = 24;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 74: 
      if (($__uri.equals("")) && ($__local.equals("block"))) {
        this.$_ngcc_current_state = 69;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 45: 
      this.$_ngcc_current_state = 44;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 10: 
      this.$_ngcc_current_state = 9;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 73: 
      this.$_ngcc_current_state = 69;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 24: 
      this.$_ngcc_current_state = 7;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 78: 
      if (($__uri.equals("")) && ($__local.equals("abstract"))) {
        this.$_ngcc_current_state = 73;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 61: 
      this.$_ngcc_current_state = 59;
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
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 19: 
    case 21: 
    case 22: 
    case 23: 
    case 25: 
    case 28: 
    case 29: 
    case 30: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 37: 
    case 39: 
    case 40: 
    case 41: 
    case 42: 
    case 43: 
    case 46: 
    case 49: 
    case 51: 
    case 53: 
    case 54: 
    case 55: 
    case 57: 
    case 58: 
    case 60: 
    case 63: 
    case 64: 
    case 67: 
    case 68: 
    case 71: 
    case 72: 
    case 75: 
    case 76: 
    default: 
      unexpectedLeaveAttribute($__qname);
    }
  }
  
  public void text(String $value)
    throws SAXException
  {
    switch (this.$_ngcc_current_state)
    {
    case 47: 
      action13();
      this.$_ngcc_current_state = 45;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 39: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 184);
      spawnChildFromText(h, $value);
      
      break;
    case 40: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 65: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "mixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 61;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 79: 
      this.abstractValue = $value;
      this.$_ngcc_current_state = 78;
      
      break;
    case 0: 
      revertToParentFromText(this.result, this._cookie, $value);
      
      break;
    case 35: 
      action8();
      this.$_ngcc_current_state = 34;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 53: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 203);
      spawnChildFromText(h, $value);
      
      break;
    case 77: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 73;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 54: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 71: 
      NGCCHandler h = new erSet(this, this._source, this.$runtime, 228);
      spawnChildFromText(h, $value);
      
      break;
    case 13: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 152);
      spawnChildFromText(h, $value);
      
      break;
    case 48: 
      this.$_ngcc_current_state = 47;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 36: 
      this.$_ngcc_current_state = 35;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 21: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 162);
      spawnChildFromText(h, $value);
      
      break;
    case 18: 
      this.$_ngcc_current_state = 17;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 75: 
      NGCCHandler h = new erSet(this, this._source, this.$runtime, 233);
      spawnChildFromText(h, $value);
      
      break;
    case 56: 
      this.$_ngcc_current_state = 32;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 63: 
      this.name = $value;
      this.$_ngcc_current_state = 62;
      
      break;
    case 44: 
      action11();
      this.$_ngcc_current_state = 43;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 59: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 50: 
      this.$_ngcc_current_state = 48;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 69: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 65;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 26: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "mixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 24;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 45: 
      this.$_ngcc_current_state = 44;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 10: 
      this.$_ngcc_current_state = 9;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 73: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 69;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 28: 
      this.mixedValue = $value;
      this.$_ngcc_current_state = 27;
      
      break;
    case 24: 
      this.$_ngcc_current_state = 7;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 14: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 22: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 67: 
      this.mixedValue = $value;
      this.$_ngcc_current_state = 66;
      
      break;
    case 61: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
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
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 203: 
      this.baseTypeName = ((UName)$__result__);
      action14();
      this.$_ngcc_current_state = 52;
      
      break;
    case 228: 
      this.finalValue = ((Integer)$__result__);
      this.$_ngcc_current_state = 70;
      
      break;
    case 197: 
      this.baseContentType = ((SimpleTypeImpl)$__result__);
      this.$_ngcc_current_state = 47;
      
      break;
    case 181: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 35;
      
      break;
    case 192: 
      this.facet = ((XSFacet)$__result__);
      action12();
      this.$_ngcc_current_state = 44;
      
      break;
    case 215: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 2;
      
      break;
    case 200: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 48;
      
      break;
    case 147: 
      this.explicitContent = ((ContentTypeImpl)$__result__);
      action2();
      this.$_ngcc_current_state = 8;
      
      break;
    case 189: 
      this.$_ngcc_current_state = 42;
      
      break;
    case 167: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 7;
      
      break;
    case 178: 
      this.$_ngcc_current_state = 33;
      
      break;
    case 184: 
      this.baseTypeName = ((UName)$__result__);
      action9();
      this.$_ngcc_current_state = 38;
      
      break;
    case 152: 
      this.baseTypeName = ((UName)$__result__);
      action3();
      this.$_ngcc_current_state = 12;
      
      break;
    case 162: 
      this.baseTypeName = ((UName)$__result__);
      action6();
      this.$_ngcc_current_state = 20;
      
      break;
    case 159: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 17;
      
      break;
    case 233: 
      this.blockValue = ((Integer)$__result__);
      this.$_ngcc_current_state = 74;
      
      break;
    case 208: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 32;
      
      break;
    case 144: 
      this.explicitContent = ((ContentTypeImpl)$__result__);
      action0();
      this.$_ngcc_current_state = 1;
      
      break;
    case 193: 
      this.facet = ((XSFacet)$__result__);
      action12();
      this.$_ngcc_current_state = 44;
      
      break;
    case 149: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 9;
      
      break;
    case 157: 
      this.explicitContent = ((ContentTypeImpl)$__result__);
      action5();
      this.$_ngcc_current_state = 16;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
  
  private void makeResult(int derivationMethod)
  {
    if (this.finalValue == null) {
      this.finalValue = new Integer(this.$runtime.finalDefault);
    }
    if (this.blockValue == null) {
      this.blockValue = new Integer(this.$runtime.blockDefault);
    }
    this.result = new ComplexTypeImpl(this.$runtime.currentSchema, this.annotation, this.locator, this.name, this.name == null, this.$runtime.parseBoolean(this.abstractValue), derivationMethod, this.baseType, this.finalValue.intValue(), this.blockValue.intValue(), this.$runtime.parseBoolean(this.mixedValue));
  }
  
  private Ref.ContentType buildComplexExtensionContentModel(XSContentType explicitContent)
  {
    if (explicitContent == this.$runtime.parser.schemaSet.empty) {
      return new complexType.BaseComplexTypeContentRef(this.baseType, null);
    }
    return new complexType.InheritBaseContentTypeRef(this.baseType, explicitContent, this.$runtime, null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\parser\state\complexType.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */