package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.impl.AnnotationImpl;
import com.sun.xml.xsom.impl.ElementDecl;
import com.sun.xml.xsom.impl.ModelGroupImpl;
import com.sun.xml.xsom.impl.ParticleImpl;
import com.sun.xml.xsom.impl.UName;
import com.sun.xml.xsom.impl.WildcardImpl;
import com.sun.xml.xsom.impl.parser.DelayedRef.Element;
import com.sun.xml.xsom.impl.parser.DelayedRef.ModelGroup;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.parser.AnnotationContext;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class particle
  extends NGCCHandler
{
  private AnnotationImpl annotation;
  private ElementDecl anonymousElementDecl;
  private WildcardImpl wcBody;
  private ModelGroupImpl term;
  private UName elementTypeName;
  private occurs occurs;
  private UName groupName;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private Locator wloc;
  private Locator loc;
  private ParticleImpl result;
  private String compositorName;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public particle(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 1;
  }
  
  public particle(NGCCRuntimeEx runtime)
  {
    this(null, runtime, runtime, -1);
  }
  
  private void action0()
    throws SAXException
  {
    this.result = new ParticleImpl(this.$runtime.currentSchema, null, this.wcBody, this.wloc, this.occurs.max, this.occurs.min);
  }
  
  private void action1()
    throws SAXException
  {
    this.wloc = this.$runtime.copyLocator();
  }
  
  private void action2()
    throws SAXException
  {
    this.result = new ParticleImpl(this.$runtime.currentSchema, null, this.anonymousElementDecl, this.loc, this.occurs.max, this.occurs.min);
  }
  
  private void action3()
    throws SAXException
  {
    this.result = new ParticleImpl(this.$runtime.currentSchema, this.annotation, new DelayedRef.Element(this.$runtime, this.loc, this.$runtime.currentSchema, this.elementTypeName), this.loc, this.occurs.max, this.occurs.min);
  }
  
  private void action4()
    throws SAXException
  {
    this.loc = this.$runtime.copyLocator();
  }
  
  private void action5()
    throws SAXException
  {
    this.result = new ParticleImpl(this.$runtime.currentSchema, this.annotation, new DelayedRef.ModelGroup(this.$runtime, this.loc, this.$runtime.currentSchema, this.groupName), this.loc, this.occurs.max, this.occurs.min);
  }
  
  private void action6()
    throws SAXException
  {
    this.loc = this.$runtime.copyLocator();
  }
  
  private void action7()
    throws SAXException
  {
    this.result = new ParticleImpl(this.$runtime.currentSchema, null, this.term, this.loc, this.occurs.max, this.occurs.min);
  }
  
  private void action8()
    throws SAXException
  {
    this.compositorName = this.$localName;
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
    case 3: 
      int $ai;
      if ((($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))))
      {
        NGCCHandler h = new wildcardBody(this, this._source, this.$runtime, 416, this.wloc);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 11: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 425, null, AnnotationContext.PARTICLE);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 10;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 26: 
      int $ai;
      if (((($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || ((($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || ((($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))))
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 442);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 10: 
      action3();
      this.$_ngcc_current_state = 7;
      this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 4: 
      int $ai;
      if (((($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || ((($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || ((($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || ((($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))))
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 417);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 20: 
      action5();
      this.$_ngcc_current_state = 19;
      this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 8: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else if ((($ai = this.$runtime.getAttributeIndex("", "final")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "form")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0))
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 422, this.loc, false);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 30: 
      int $ai;
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || ((($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || ((($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))))))
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 447);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 1: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action8();
        this.$_ngcc_current_state = 30;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action6();
        this.$_ngcc_current_state = 26;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action4();
        this.$_ngcc_current_state = 16;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action1();
        this.$_ngcc_current_state = 4;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(this.result, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 16: 
      int $ai;
      if (((($ai = this.$runtime.getAttributeIndex("", "final")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))) || ((($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))) || ((($ai = this.$runtime.getAttributeIndex("", "form")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))) || ((($ai = this.$runtime.getAttributeIndex("", "block")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))) || ((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))) || ((($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))) || ((($ai = this.$runtime.getAttributeIndex("", "default")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))) || ((($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))) || ((($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))) || ((($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))))
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 431);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 25: 
      int $ai;
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
    case 29: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))))
      {
        NGCCHandler h = new modelGroupBody(this, this._source, this.$runtime, 446, this.loc, this.compositorName);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 21: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 437, null, AnnotationContext.PARTICLE);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 20;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 2: 
    case 5: 
    case 6: 
    case 7: 
    case 9: 
    case 12: 
    case 13: 
    case 14: 
    case 15: 
    case 17: 
    case 18: 
    case 19: 
    case 22: 
    case 23: 
    case 24: 
    case 27: 
    case 28: 
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
    case 7: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 3: 
      int $ai;
      if (((($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || ((($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))))
      {
        NGCCHandler h = new wildcardBody(this, this._source, this.$runtime, 416, this.wloc);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 11: 
      this.$_ngcc_current_state = 10;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 28: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 26: 
      int $ai;
      if (((($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || ((($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || ((($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))))
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 442);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 10: 
      action3();
      this.$_ngcc_current_state = 7;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      int $ai;
      if (((($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || ((($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || ((($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || ((($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))))
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 417);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 2: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 19: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 20: 
      action5();
      this.$_ngcc_current_state = 19;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 8: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else if (((($ai = this.$runtime.getAttributeIndex("", "final")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "form")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "block")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "default")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))))
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 422, this.loc, false);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 30: 
      int $ai;
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || ((($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))))) || ((($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))))))
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 447);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveElement(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 25: 
      int $ai;
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
    case 16: 
      int $ai;
      if (((($ai = this.$runtime.getAttributeIndex("", "final")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "form")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "block")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "default")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))))
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 431);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 29: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))))
      {
        NGCCHandler h = new modelGroupBody(this, this._source, this.$runtime, 446, this.loc, this.compositorName);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 21: 
      this.$_ngcc_current_state = 20;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
    case 5: 
    case 6: 
    case 9: 
    case 12: 
    case 13: 
    case 14: 
    case 15: 
    case 17: 
    case 18: 
    case 22: 
    case 23: 
    case 24: 
    case 27: 
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
    case 20: 
      action5();
      this.$_ngcc_current_state = 19;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 8: 
      if (($__uri.equals("")) && ($__local.equals("ref")))
      {
        this.$_ngcc_current_state = 14;
      }
      else if ((($__uri.equals("")) && ($__local.equals("final"))) || (($__uri.equals("")) && ($__local.equals("form"))) || (($__uri.equals("")) && ($__local.equals("block"))) || (($__uri.equals("")) && ($__local.equals("name"))) || (($__uri.equals("")) && ($__local.equals("abstract"))) || (($__uri.equals("")) && ($__local.equals("default"))) || (($__uri.equals("")) && ($__local.equals("fixed"))))
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 422, this.loc, false);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 30: 
      if ((($__uri.equals("")) && ($__local.equals("minOccurs"))) || (($__uri.equals("")) && ($__local.equals("maxOccurs"))))
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 447);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 25: 
      if (($__uri.equals("")) && ($__local.equals("ref"))) {
        this.$_ngcc_current_state = 24;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 16: 
      if ((($__uri.equals("")) && ($__local.equals("final"))) || (($__uri.equals("")) && ($__local.equals("ref"))) || (($__uri.equals("")) && ($__local.equals("form"))) || (($__uri.equals("")) && ($__local.equals("block"))) || (($__uri.equals("")) && ($__local.equals("name"))) || (($__uri.equals("")) && ($__local.equals("abstract"))) || (($__uri.equals("")) && ($__local.equals("default"))) || (($__uri.equals("")) && ($__local.equals("fixed"))) || (($__uri.equals("")) && ($__local.equals("minOccurs"))) || (($__uri.equals("")) && ($__local.equals("maxOccurs"))))
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 431);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 3: 
      if ((($__uri.equals("")) && ($__local.equals("processContents"))) || (($__uri.equals("")) && ($__local.equals("namespace"))))
      {
        NGCCHandler h = new wildcardBody(this, this._source, this.$runtime, 416, this.wloc);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 10: 
      action3();
      this.$_ngcc_current_state = 7;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 26: 
      if ((($__uri.equals("")) && ($__local.equals("ref"))) || (($__uri.equals("")) && ($__local.equals("minOccurs"))) || (($__uri.equals("")) && ($__local.equals("maxOccurs"))))
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 442);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 11: 
      this.$_ngcc_current_state = 10;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      if ((($__uri.equals("")) && ($__local.equals("processContents"))) || (($__uri.equals("")) && ($__local.equals("namespace"))) || (($__uri.equals("")) && ($__local.equals("minOccurs"))) || (($__uri.equals("")) && ($__local.equals("maxOccurs"))))
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 417);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 21: 
      this.$_ngcc_current_state = 20;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
    case 2: 
    case 5: 
    case 6: 
    case 7: 
    case 9: 
    case 12: 
    case 13: 
    case 14: 
    case 15: 
    case 17: 
    case 18: 
    case 19: 
    case 22: 
    case 23: 
    case 24: 
    case 27: 
    case 28: 
    case 29: 
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
    case 23: 
      if (($__uri.equals("")) && ($__local.equals("ref"))) {
        this.$_ngcc_current_state = 21;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 20: 
      action5();
      this.$_ngcc_current_state = 19;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 13: 
      if (($__uri.equals("")) && ($__local.equals("ref"))) {
        this.$_ngcc_current_state = 11;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 10: 
      action3();
      this.$_ngcc_current_state = 7;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 11: 
      this.$_ngcc_current_state = 10;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 21: 
      this.$_ngcc_current_state = 20;
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
    case 12: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 18: 
    case 19: 
    case 22: 
    default: 
      unexpectedLeaveAttribute($__qname);
    }
  }
  
  public void text(String $value)
    throws SAXException
  {
    switch (this.$_ngcc_current_state)
    {
    case 24: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 440);
      spawnChildFromText(h, $value);
      
      break;
    case 14: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 428);
      spawnChildFromText(h, $value);
      
      break;
    case 3: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0)
      {
        NGCCHandler h = new wildcardBody(this, this._source, this.$runtime, 416, this.wloc);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0)
      {
        NGCCHandler h = new wildcardBody(this, this._source, this.$runtime, 416, this.wloc);
        spawnChildFromText(h, $value);
      }
      break;
    case 11: 
      this.$_ngcc_current_state = 10;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 26: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 442);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 442);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 442);
        spawnChildFromText(h, $value);
      }
      break;
    case 10: 
      action3();
      this.$_ngcc_current_state = 7;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 4: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 417);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 417);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 417);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 417);
        spawnChildFromText(h, $value);
      }
      break;
    case 20: 
      action5();
      this.$_ngcc_current_state = 19;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 8: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 422, this.loc, false);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 422, this.loc, false);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 422, this.loc, false);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 422, this.loc, false);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 422, this.loc, false);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "form")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 422, this.loc, false);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 422, this.loc, false);
        spawnChildFromText(h, $value);
      }
      break;
    case 30: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 447);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 447);
        spawnChildFromText(h, $value);
      }
      break;
    case 0: 
      revertToParentFromText(this.result, this._cookie, $value);
      
      break;
    case 16: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 431);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 431);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 431);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 431);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 431);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 431);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 431);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "form")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 431);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 431);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0)
      {
        NGCCHandler h = new occurs(this, this._source, this.$runtime, 431);
        spawnChildFromText(h, $value);
      }
      break;
    case 25: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "ref")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 21: 
      this.$_ngcc_current_state = 20;
      this.$runtime.sendText(this._cookie, $value);
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 440: 
      this.groupName = ((UName)$__result__);
      this.$_ngcc_current_state = 23;
      
      break;
    case 416: 
      this.wcBody = ((WildcardImpl)$__result__);
      action0();
      this.$_ngcc_current_state = 2;
      
      break;
    case 425: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 10;
      
      break;
    case 447: 
      this.occurs = ((occurs)$__result__);
      this.$_ngcc_current_state = 29;
      
      break;
    case 431: 
      this.occurs = ((occurs)$__result__);
      this.$_ngcc_current_state = 8;
      
      break;
    case 446: 
      this.term = ((ModelGroupImpl)$__result__);
      action7();
      this.$_ngcc_current_state = 28;
      
      break;
    case 428: 
      this.elementTypeName = ((UName)$__result__);
      this.$_ngcc_current_state = 13;
      
      break;
    case 442: 
      this.occurs = ((occurs)$__result__);
      this.$_ngcc_current_state = 25;
      
      break;
    case 417: 
      this.occurs = ((occurs)$__result__);
      this.$_ngcc_current_state = 3;
      
      break;
    case 422: 
      this.anonymousElementDecl = ((ElementDecl)$__result__);
      action2();
      this.$_ngcc_current_state = 7;
      
      break;
    case 437: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 20;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\parser\state\particle.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */