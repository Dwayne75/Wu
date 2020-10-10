package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.impl.AnnotationImpl;
import com.sun.xml.xsom.impl.AttributeDeclImpl;
import com.sun.xml.xsom.impl.AttributeUseImpl;
import com.sun.xml.xsom.impl.AttributesHolder;
import com.sun.xml.xsom.impl.ForeignAttributesImpl;
import com.sun.xml.xsom.impl.Ref.Attribute;
import com.sun.xml.xsom.impl.UName;
import com.sun.xml.xsom.impl.WildcardImpl;
import com.sun.xml.xsom.impl.parser.DelayedRef.AttGroup;
import com.sun.xml.xsom.impl.parser.DelayedRef.Attribute;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.parser.AnnotationContext;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class attributeUses
  extends NGCCHandler
{
  private String use;
  private AttributesHolder owner;
  private ForeignAttributesImpl fa;
  private WildcardImpl wildcard;
  private AnnotationImpl annotation;
  private UName attDeclName;
  private AttributeDeclImpl anonymousDecl;
  private String defaultValue;
  private String fixedValue;
  private UName groupName;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private Ref.Attribute decl;
  private Locator wloc;
  private Locator locator;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public attributeUses(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie, AttributesHolder _owner)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.owner = _owner;
    this.$_ngcc_current_state = 5;
  }
  
  public attributeUses(NGCCRuntimeEx runtime, AttributesHolder _owner)
  {
    this(null, runtime, runtime, -1, _owner);
  }
  
  private void action0()
    throws SAXException
  {
    this.owner.setWildcard(this.wildcard);
  }
  
  private void action1()
    throws SAXException
  {
    this.wloc = this.$runtime.copyLocator();
  }
  
  private void action2()
    throws SAXException
  {
    this.owner.addAttGroup(new DelayedRef.AttGroup(this.$runtime, this.locator, this.$runtime.currentSchema, this.groupName));
  }
  
  private void action3()
    throws SAXException
  {
    this.locator = this.$runtime.copyLocator();
  }
  
  private void action4()
    throws SAXException
  {
    if ("prohibited".equals(this.use)) {
      this.owner.addProhibitedAttribute(this.attDeclName);
    } else {
      this.owner.addAttributeUse(this.attDeclName, new AttributeUseImpl(this.$runtime.document, this.annotation, this.locator, this.fa, this.decl, this.$runtime.createXmlString(this.defaultValue), this.$runtime.createXmlString(this.fixedValue), "required".equals(this.use)));
    }
  }
  
  private void action5()
    throws SAXException
  {
    this.decl = new DelayedRef.Attribute(this.$runtime, this.locator, this.$runtime.currentSchema, this.attDeclName);
  }
  
  private void action6()
    throws SAXException
  {
    this.decl = this.anonymousDecl;
    this.attDeclName = new UName(this.anonymousDecl.getTargetNamespace(), this.anonymousDecl.getName());
    
    this.defaultValue = null;
    this.fixedValue = null;
  }
  
  private void action7()
    throws SAXException
  {
    this.locator = this.$runtime.copyLocator();
    this.use = null;
    this.defaultValue = null;
    this.fixedValue = null;
    this.decl = null;
    this.annotation = null;
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
    case 1: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action7();
        this.$_ngcc_current_state = 33;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action3();
        this.$_ngcc_current_state = 13;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action1();
        this.$_ngcc_current_state = 3;
      }
      else
      {
        this.$_ngcc_current_state = 0;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 13: 
      if (($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 25: 
      if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 17;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 8: 
      action2();
      this.$_ngcc_current_state = 7;
      this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 33: 
      if (($ai = this.$runtime.getAttributeIndex("", "use")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 29;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 9: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 409, null, AnnotationContext.ATTRIBUTE_USE);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 8;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 3: 
      if ((($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0))
      {
        NGCCHandler h = new wildcardBody(this, this._source, this.$runtime, 402, this.wloc);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 29: 
      if (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 25;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(this, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 5: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action7();
        this.$_ngcc_current_state = 33;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action3();
        this.$_ngcc_current_state = 13;
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 19: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 420, null, AnnotationContext.ATTRIBUTE_USE);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 18;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 17: 
      if ((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "form")) >= 0))
      {
        NGCCHandler h = new attributeDeclBody(this, this._source, this.$runtime, 427, this.locator, true, this.defaultValue, this.fixedValue);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 16: 
      action4();
      this.$_ngcc_current_state = 15;
      this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 2: 
    case 4: 
    case 6: 
    case 7: 
    case 10: 
    case 11: 
    case 12: 
    case 14: 
    case 15: 
    case 18: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 26: 
    case 27: 
    case 28: 
    case 30: 
    case 31: 
    case 32: 
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
    case 2: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 1: 
      this.$_ngcc_current_state = 0;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 13: 
      if (($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 25: 
      if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 17;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 8: 
      action2();
      this.$_ngcc_current_state = 7;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 33: 
      if (($ai = this.$runtime.getAttributeIndex("", "use")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 29;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 9: 
      this.$_ngcc_current_state = 8;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 29: 
      if (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 25;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 3: 
      if (((($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))) || ((($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))))
      {
        NGCCHandler h = new wildcardBody(this, this._source, this.$runtime, 402, this.wloc);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 7: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveElement(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 5: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 18: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute")))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 418, null);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 15: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 17: 
      if (((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || ((($ai = this.$runtime.getAttributeIndex("", "form")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))))
      {
        NGCCHandler h = new attributeDeclBody(this, this._source, this.$runtime, 427, this.locator, true, this.defaultValue, this.fixedValue);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
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
    case 16: 
      action4();
      this.$_ngcc_current_state = 15;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
    case 6: 
    case 10: 
    case 11: 
    case 12: 
    case 14: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 26: 
    case 27: 
    case 28: 
    case 30: 
    case 31: 
    case 32: 
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
    case 1: 
      this.$_ngcc_current_state = 0;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 13: 
      if (($__uri.equals("")) && ($__local.equals("ref"))) {
        this.$_ngcc_current_state = 12;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 25: 
      if (($__uri.equals("")) && ($__local.equals("fixed")))
      {
        this.$_ngcc_current_state = 27;
      }
      else
      {
        this.$_ngcc_current_state = 17;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 8: 
      action2();
      this.$_ngcc_current_state = 7;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 33: 
      if (($__uri.equals("")) && ($__local.equals("use")))
      {
        this.$_ngcc_current_state = 35;
      }
      else
      {
        this.$_ngcc_current_state = 29;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 9: 
      this.$_ngcc_current_state = 8;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 3: 
      if ((($__uri.equals("")) && ($__local.equals("processContents"))) || (($__uri.equals("")) && ($__local.equals("namespace"))))
      {
        NGCCHandler h = new wildcardBody(this, this._source, this.$runtime, 402, this.wloc);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 29: 
      if (($__uri.equals("")) && ($__local.equals("default")))
      {
        this.$_ngcc_current_state = 31;
      }
      else
      {
        this.$_ngcc_current_state = 25;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 5: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 19: 
      this.$_ngcc_current_state = 18;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 17: 
      if ((($__uri.equals("")) && ($__local.equals("name"))) || (($__uri.equals("")) && ($__local.equals("form"))))
      {
        NGCCHandler h = new attributeDeclBody(this, this._source, this.$runtime, 427, this.locator, true, this.defaultValue, this.fixedValue);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else if (($__uri.equals("")) && ($__local.equals("ref")))
      {
        this.$_ngcc_current_state = 22;
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 16: 
      action4();
      this.$_ngcc_current_state = 15;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
    case 4: 
    case 6: 
    case 7: 
    case 10: 
    case 11: 
    case 12: 
    case 14: 
    case 15: 
    case 18: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 26: 
    case 27: 
    case 28: 
    case 30: 
    case 31: 
    case 32: 
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
    case 1: 
      this.$_ngcc_current_state = 0;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 25: 
      this.$_ngcc_current_state = 17;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 8: 
      action2();
      this.$_ngcc_current_state = 7;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 33: 
      this.$_ngcc_current_state = 29;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 9: 
      this.$_ngcc_current_state = 8;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 29: 
      this.$_ngcc_current_state = 25;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 34: 
      if (($__uri.equals("")) && ($__local.equals("use"))) {
        this.$_ngcc_current_state = 29;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 30: 
      if (($__uri.equals("")) && ($__local.equals("default"))) {
        this.$_ngcc_current_state = 25;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 5: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 11: 
      if (($__uri.equals("")) && ($__local.equals("ref"))) {
        this.$_ngcc_current_state = 9;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 21: 
      if (($__uri.equals("")) && ($__local.equals("ref"))) {
        this.$_ngcc_current_state = 19;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 19: 
      this.$_ngcc_current_state = 18;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 26: 
      if (($__uri.equals("")) && ($__local.equals("fixed"))) {
        this.$_ngcc_current_state = 17;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 16: 
      action4();
      this.$_ngcc_current_state = 15;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
    case 3: 
    case 4: 
    case 6: 
    case 7: 
    case 10: 
    case 12: 
    case 13: 
    case 14: 
    case 15: 
    case 17: 
    case 18: 
    case 20: 
    case 22: 
    case 23: 
    case 24: 
    case 27: 
    case 28: 
    case 31: 
    case 32: 
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
    case 12: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 412);
      spawnChildFromText(h, $value);
      
      break;
    case 1: 
      this.$_ngcc_current_state = 0;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 13: 
      if (($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 27: 
      this.fixedValue = $value;
      this.$_ngcc_current_state = 26;
      
      break;
    case 25: 
      if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 17;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 8: 
      action2();
      this.$_ngcc_current_state = 7;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 33: 
      if (($ai = this.$runtime.getAttributeIndex("", "use")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 29;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 9: 
      this.$_ngcc_current_state = 8;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 3: 
      if (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0)
      {
        NGCCHandler h = new wildcardBody(this, this._source, this.$runtime, 402, this.wloc);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0)
      {
        NGCCHandler h = new wildcardBody(this, this._source, this.$runtime, 402, this.wloc);
        spawnChildFromText(h, $value);
      }
      break;
    case 29: 
      if (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 25;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 0: 
      revertToParentFromText(this, this._cookie, $value);
      
      break;
    case 5: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 22: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 423);
      spawnChildFromText(h, $value);
      
      break;
    case 17: 
      if (($ai = this.$runtime.getAttributeIndex("", "form")) >= 0)
      {
        NGCCHandler h = new attributeDeclBody(this, this._source, this.$runtime, 427, this.locator, true, this.defaultValue, this.fixedValue);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        NGCCHandler h = new attributeDeclBody(this, this._source, this.$runtime, 427, this.locator, true, this.defaultValue, this.fixedValue);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 19: 
      this.$_ngcc_current_state = 18;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 31: 
      this.defaultValue = $value;
      this.$_ngcc_current_state = 30;
      
      break;
    case 16: 
      action4();
      this.$_ngcc_current_state = 15;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 35: 
      this.use = $value;
      this.$_ngcc_current_state = 34;
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 412: 
      this.groupName = ((UName)$__result__);
      this.$_ngcc_current_state = 11;
      
      break;
    case 402: 
      this.wildcard = ((WildcardImpl)$__result__);
      action0();
      this.$_ngcc_current_state = 2;
      
      break;
    case 418: 
      this.fa = ((ForeignAttributesImpl)$__result__);
      this.$_ngcc_current_state = 16;
      
      break;
    case 420: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 18;
      
      break;
    case 409: 
      this.$_ngcc_current_state = 8;
      
      break;
    case 423: 
      this.attDeclName = ((UName)$__result__);
      action5();
      this.$_ngcc_current_state = 21;
      
      break;
    case 427: 
      this.anonymousDecl = ((AttributeDeclImpl)$__result__);
      action6();
      this.$_ngcc_current_state = 16;
    }
  }
  
  public boolean accepted()
  {
    return (this.$_ngcc_current_state == 5) || (this.$_ngcc_current_state == 0) || (this.$_ngcc_current_state == 1);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\state\attributeUses.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */