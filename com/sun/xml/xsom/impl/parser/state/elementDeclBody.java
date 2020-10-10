package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.impl.AnnotationImpl;
import com.sun.xml.xsom.impl.ComplexTypeImpl;
import com.sun.xml.xsom.impl.ElementDecl;
import com.sun.xml.xsom.impl.ForeignAttributesImpl;
import com.sun.xml.xsom.impl.IdentityConstraintImpl;
import com.sun.xml.xsom.impl.Ref.Type;
import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.impl.SchemaSetImpl;
import com.sun.xml.xsom.impl.SimpleTypeImpl;
import com.sun.xml.xsom.impl.UName;
import com.sun.xml.xsom.impl.parser.DelayedRef.Element;
import com.sun.xml.xsom.impl.parser.DelayedRef.Type;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.impl.parser.ParserContext;
import com.sun.xml.xsom.impl.parser.SubstGroupBaseTypeRef;
import com.sun.xml.xsom.parser.AnnotationContext;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class elementDeclBody
  extends NGCCHandler
{
  private Integer finalValue;
  private String name;
  private String nillable;
  private String abstractValue;
  private Integer blockValue;
  private ForeignAttributesImpl fa;
  private AnnotationImpl annotation;
  private Locator locator;
  private String defaultValue;
  private IdentityConstraintImpl idc;
  private boolean isGlobal;
  private String fixedValue;
  private UName typeName;
  private UName substRef;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private boolean form;
  private boolean formSpecified;
  private Ref.Type type;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public elementDeclBody(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie, Locator _locator, boolean _isGlobal)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.locator = _locator;
    this.isGlobal = _isGlobal;
    this.$_ngcc_current_state = 48;
  }
  
  public elementDeclBody(NGCCRuntimeEx runtime, Locator _locator, boolean _isGlobal)
  {
    this(null, runtime, runtime, -1, _locator, _isGlobal);
  }
  
  private void action0()
    throws SAXException
  {
    this.idcs.add(this.idc);
  }
  
  private void action1()
    throws SAXException
  {
    this.type = new DelayedRef.Type(this.$runtime, this.locator, this.$runtime.currentSchema, this.typeName);
  }
  
  private void action2()
    throws SAXException
  {
    this.substHeadRef = new DelayedRef.Element(this.$runtime, this.locator, this.$runtime.currentSchema, this.substRef);
  }
  
  private void action3()
    throws SAXException
  {
    this.formSpecified = true;
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
    case 24: 
      if (($ai = this.$runtime.getAttributeIndex("", "form")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 23;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 0: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))
      {
        NGCCHandler h = new identityConstraint(this, this._source, this.$runtime, 451);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        revertToParentFromEnterElement(makeResult(), this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 11: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 469, null, AnnotationContext.ELEMENT_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 3;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 17: 
      if (($ai = this.$runtime.getAttributeIndex("", "nillable")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 13;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 36: 
      if (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 32;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 1: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))
      {
        NGCCHandler h = new identityConstraint(this, this._source, this.$runtime, 452);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 0;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 48: 
      if (((($ai = this.$runtime.getAttributeIndex("", "block")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))))) || ((($ai = this.$runtime.getAttributeIndex("", "default")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))))) || ((($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))))) || ((($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))))) || ((($ai = this.$runtime.getAttributeIndex("", "final")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))))) || ((($ai = this.$runtime.getAttributeIndex("", "form")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))))) || ((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))))))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 514, this.fa);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 28: 
      if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
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
    case 32: 
      if (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0)
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
    case 13: 
      if (($ai = this.$runtime.getAttributeIndex("", "substitutionGroup")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 11;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 3: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType")))
      {
        NGCCHandler h = new simpleType(this, this._source, this.$runtime, 464);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType")))
      {
        NGCCHandler h = new complexType(this, this._source, this.$runtime, 465);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "type")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 40: 
      if (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 36;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 23: 
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 44: 
      if (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 40;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 2: 
    case 4: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 10: 
    case 12: 
    case 14: 
    case 15: 
    case 16: 
    case 18: 
    case 19: 
    case 20: 
    case 21: 
    case 22: 
    case 25: 
    case 26: 
    case 27: 
    case 29: 
    case 30: 
    case 31: 
    case 33: 
    case 34: 
    case 35: 
    case 37: 
    case 38: 
    case 39: 
    case 41: 
    case 42: 
    case 43: 
    case 45: 
    case 46: 
    case 47: 
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
    case 24: 
      if (($ai = this.$runtime.getAttributeIndex("", "form")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 23;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveElement(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 11: 
      this.$_ngcc_current_state = 3;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 17: 
      if (($ai = this.$runtime.getAttributeIndex("", "nillable")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 13;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 36: 
      if (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 32;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 1: 
      this.$_ngcc_current_state = 0;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 48: 
      if ((($ai = this.$runtime.getAttributeIndex("", "block")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "form")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 514, this.fa);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 28: 
      if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
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
    case 32: 
      if (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0)
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
    case 13: 
      if (($ai = this.$runtime.getAttributeIndex("", "substitutionGroup")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 11;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 3: 
      if (($ai = this.$runtime.getAttributeIndex("", "type")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 40: 
      if (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 36;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 23: 
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 44: 
      if (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 40;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 2: 
    case 4: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 10: 
    case 12: 
    case 14: 
    case 15: 
    case 16: 
    case 18: 
    case 19: 
    case 20: 
    case 21: 
    case 22: 
    case 25: 
    case 26: 
    case 27: 
    case 29: 
    case 30: 
    case 31: 
    case 33: 
    case 34: 
    case 35: 
    case 37: 
    case 38: 
    case 39: 
    case 41: 
    case 42: 
    case 43: 
    case 45: 
    case 46: 
    case 47: 
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
    case 24: 
      if (($__uri.equals("")) && ($__local.equals("form")))
      {
        this.$_ngcc_current_state = 26;
      }
      else
      {
        this.$_ngcc_current_state = 23;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 11: 
      this.$_ngcc_current_state = 3;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 17: 
      if (($__uri.equals("")) && ($__local.equals("nillable")))
      {
        this.$_ngcc_current_state = 19;
      }
      else
      {
        this.$_ngcc_current_state = 13;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 36: 
      if (($__uri.equals("")) && ($__local.equals("final")))
      {
        this.$_ngcc_current_state = 38;
      }
      else
      {
        this.$_ngcc_current_state = 32;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 1: 
      this.$_ngcc_current_state = 0;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 48: 
      if ((($__uri.equals("")) && ($__local.equals("block"))) || (($__uri.equals("")) && ($__local.equals("default"))) || (($__uri.equals("")) && ($__local.equals("fixed"))) || (($__uri.equals("")) && ($__local.equals("abstract"))) || (($__uri.equals("")) && ($__local.equals("final"))) || (($__uri.equals("")) && ($__local.equals("form"))) || (($__uri.equals("")) && ($__local.equals("name"))))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 514, this.fa);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 28: 
      if (($__uri.equals("")) && ($__local.equals("fixed")))
      {
        this.$_ngcc_current_state = 30;
      }
      else
      {
        this.$_ngcc_current_state = 24;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 32: 
      if (($__uri.equals("")) && ($__local.equals("default")))
      {
        this.$_ngcc_current_state = 34;
      }
      else
      {
        this.$_ngcc_current_state = 28;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 13: 
      if (($__uri.equals("")) && ($__local.equals("substitutionGroup")))
      {
        this.$_ngcc_current_state = 15;
      }
      else
      {
        this.$_ngcc_current_state = 11;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 3: 
      if (($__uri.equals("")) && ($__local.equals("type")))
      {
        this.$_ngcc_current_state = 6;
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 40: 
      if (($__uri.equals("")) && ($__local.equals("block")))
      {
        this.$_ngcc_current_state = 42;
      }
      else
      {
        this.$_ngcc_current_state = 36;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 23: 
      if (($__uri.equals("")) && ($__local.equals("name"))) {
        this.$_ngcc_current_state = 22;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 44: 
      if (($__uri.equals("")) && ($__local.equals("abstract")))
      {
        this.$_ngcc_current_state = 46;
      }
      else
      {
        this.$_ngcc_current_state = 40;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 2: 
    case 4: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 10: 
    case 12: 
    case 14: 
    case 15: 
    case 16: 
    case 18: 
    case 19: 
    case 20: 
    case 21: 
    case 22: 
    case 25: 
    case 26: 
    case 27: 
    case 29: 
    case 30: 
    case 31: 
    case 33: 
    case 34: 
    case 35: 
    case 37: 
    case 38: 
    case 39: 
    case 41: 
    case 42: 
    case 43: 
    case 45: 
    case 46: 
    case 47: 
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
    case 24: 
      this.$_ngcc_current_state = 23;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 45: 
      if (($__uri.equals("")) && ($__local.equals("abstract"))) {
        this.$_ngcc_current_state = 40;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 29: 
      if (($__uri.equals("")) && ($__local.equals("fixed"))) {
        this.$_ngcc_current_state = 24;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 5: 
      if (($__uri.equals("")) && ($__local.equals("type")))
      {
        this.$_ngcc_current_state = 1;
        action1();
      }
      else
      {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 21: 
      if (($__uri.equals("")) && ($__local.equals("name"))) {
        this.$_ngcc_current_state = 17;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 18: 
      if (($__uri.equals("")) && ($__local.equals("nillable"))) {
        this.$_ngcc_current_state = 13;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 25: 
      if (($__uri.equals("")) && ($__local.equals("form"))) {
        this.$_ngcc_current_state = 23;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 11: 
      this.$_ngcc_current_state = 3;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 17: 
      this.$_ngcc_current_state = 13;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      this.$_ngcc_current_state = 0;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 37: 
      if (($__uri.equals("")) && ($__local.equals("final"))) {
        this.$_ngcc_current_state = 32;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 36: 
      this.$_ngcc_current_state = 32;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 14: 
      if (($__uri.equals("")) && ($__local.equals("substitutionGroup"))) {
        this.$_ngcc_current_state = 11;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 28: 
      this.$_ngcc_current_state = 24;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 41: 
      if (($__uri.equals("")) && ($__local.equals("block"))) {
        this.$_ngcc_current_state = 36;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 33: 
      if (($__uri.equals("")) && ($__local.equals("default"))) {
        this.$_ngcc_current_state = 28;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 32: 
      this.$_ngcc_current_state = 28;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 13: 
      this.$_ngcc_current_state = 11;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 3: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 44: 
      this.$_ngcc_current_state = 40;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 40: 
      this.$_ngcc_current_state = 36;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
    case 4: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 10: 
    case 12: 
    case 15: 
    case 16: 
    case 19: 
    case 20: 
    case 22: 
    case 23: 
    case 26: 
    case 27: 
    case 30: 
    case 31: 
    case 34: 
    case 35: 
    case 38: 
    case 39: 
    case 42: 
    case 43: 
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
    case 24: 
      if (($ai = this.$runtime.getAttributeIndex("", "form")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 23;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 19: 
      this.nillable = $value;
      this.$_ngcc_current_state = 18;
      
      break;
    case 46: 
      this.abstractValue = $value;
      this.$_ngcc_current_state = 45;
      
      break;
    case 6: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 455);
      spawnChildFromText(h, $value);
      
      break;
    case 34: 
      this.defaultValue = $value;
      this.$_ngcc_current_state = 33;
      
      break;
    case 0: 
      revertToParentFromText(makeResult(), this._cookie, $value);
      
      break;
    case 11: 
      this.$_ngcc_current_state = 3;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 26: 
      if ($value.equals("unqualified"))
      {
        NGCCHandler h = new qualification(this, this._source, this.$runtime, 485);
        spawnChildFromText(h, $value);
      }
      else if ($value.equals("qualified"))
      {
        NGCCHandler h = new qualification(this, this._source, this.$runtime, 485);
        spawnChildFromText(h, $value);
      }
      break;
    case 17: 
      if (($ai = this.$runtime.getAttributeIndex("", "nillable")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 13;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 22: 
      this.name = $value;
      this.$_ngcc_current_state = 21;
      
      break;
    case 38: 
      NGCCHandler h = new erSet(this, this._source, this.$runtime, 500);
      spawnChildFromText(h, $value);
      
      break;
    case 1: 
      this.$_ngcc_current_state = 0;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 36: 
      if (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 32;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 48: 
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 514, this.fa);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "form")) >= 0)
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 514, this.fa);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0)
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 514, this.fa);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0)
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 514, this.fa);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 514, this.fa);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0)
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 514, this.fa);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0)
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 514, this.fa);
        spawnChildFromText(h, $value);
      }
      break;
    case 28: 
      if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
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
    case 32: 
      if (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0)
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
    case 13: 
      if (($ai = this.$runtime.getAttributeIndex("", "substitutionGroup")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 11;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 15: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 472);
      spawnChildFromText(h, $value);
      
      break;
    case 3: 
      if (($ai = this.$runtime.getAttributeIndex("", "type")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 44: 
      if (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 40;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 23: 
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 40: 
      if (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 36;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 42: 
      NGCCHandler h = new ersSet(this, this._source, this.$runtime, 505);
      spawnChildFromText(h, $value);
      
      break;
    case 30: 
      this.fixedValue = $value;
      this.$_ngcc_current_state = 29;
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 455: 
      this.typeName = ((UName)$__result__);
      this.$_ngcc_current_state = 5;
      
      break;
    case 464: 
      this.type = ((SimpleTypeImpl)$__result__);
      this.$_ngcc_current_state = 1;
      
      break;
    case 465: 
      this.type = ((ComplexTypeImpl)$__result__);
      this.$_ngcc_current_state = 1;
      
      break;
    case 451: 
      this.idc = ((IdentityConstraintImpl)$__result__);
      action0();
      this.$_ngcc_current_state = 0;
      
      break;
    case 469: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 3;
      
      break;
    case 500: 
      this.finalValue = ((Integer)$__result__);
      this.$_ngcc_current_state = 37;
      
      break;
    case 505: 
      this.blockValue = ((Integer)$__result__);
      this.$_ngcc_current_state = 41;
      
      break;
    case 485: 
      this.form = ((Boolean)$__result__).booleanValue();
      action3();
      this.$_ngcc_current_state = 25;
      
      break;
    case 452: 
      this.idc = ((IdentityConstraintImpl)$__result__);
      action0();
      this.$_ngcc_current_state = 0;
      
      break;
    case 514: 
      this.fa = ((ForeignAttributesImpl)$__result__);
      this.$_ngcc_current_state = 44;
      
      break;
    case 472: 
      this.substRef = ((UName)$__result__);
      action2();
      this.$_ngcc_current_state = 14;
    }
  }
  
  public boolean accepted()
  {
    return (this.$_ngcc_current_state == 1) || (this.$_ngcc_current_state == 17) || (this.$_ngcc_current_state == 11) || (this.$_ngcc_current_state == 0) || (this.$_ngcc_current_state == 3) || (this.$_ngcc_current_state == 13);
  }
  
  private List idcs = new ArrayList();
  private DelayedRef.Element substHeadRef;
  
  private ElementDecl makeResult()
  {
    if (this.finalValue == null) {
      this.finalValue = new Integer(this.$runtime.finalDefault);
    }
    if (this.blockValue == null) {
      this.blockValue = new Integer(this.$runtime.blockDefault);
    }
    if (!this.formSpecified) {
      this.form = this.$runtime.elementFormDefault;
    }
    if (this.isGlobal) {
      this.form = true;
    }
    String tns;
    String tns;
    if (this.form) {
      tns = this.$runtime.currentSchema.getTargetNamespace();
    } else {
      tns = "";
    }
    if (this.type == null) {
      if (this.substHeadRef != null) {
        this.type = new SubstGroupBaseTypeRef(this.substHeadRef);
      } else {
        this.type = this.$runtime.parser.schemaSet.anyType;
      }
    }
    ElementDecl ed = new ElementDecl(this.$runtime, this.$runtime.document, this.annotation, this.locator, this.fa, tns, this.name, !this.isGlobal, this.$runtime.createXmlString(this.defaultValue), this.$runtime.createXmlString(this.fixedValue), this.$runtime.parseBoolean(this.nillable), this.$runtime.parseBoolean(this.abstractValue), this.type, this.substHeadRef, this.blockValue.intValue(), this.finalValue.intValue(), this.idcs);
    if ((this.type instanceof ComplexTypeImpl)) {
      ((ComplexTypeImpl)this.type).setScope(ed);
    }
    return ed;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\state\elementDeclBody.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */