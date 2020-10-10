package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.impl.AnnotationImpl;
import com.sun.xml.xsom.impl.AttGroupDeclImpl;
import com.sun.xml.xsom.impl.AttributeDeclImpl;
import com.sun.xml.xsom.impl.ComplexTypeImpl;
import com.sun.xml.xsom.impl.ElementDecl;
import com.sun.xml.xsom.impl.ModelGroupDeclImpl;
import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.impl.SchemaSetImpl;
import com.sun.xml.xsom.impl.SimpleTypeImpl;
import com.sun.xml.xsom.impl.parser.Messages;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.impl.parser.ParserContext;
import com.sun.xml.xsom.parser.AnnotationContext;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Schema
  extends NGCCHandler
{
  private Integer finalDefault;
  private boolean efd;
  private boolean afd;
  private Integer blockDefault;
  private boolean includeMode;
  private AnnotationImpl anno;
  private ComplexTypeImpl ct;
  private ElementDecl e;
  private String defaultValue;
  private XSNotation notation;
  private AttGroupDeclImpl ag;
  private String fixedValue;
  private ModelGroupDeclImpl group;
  private AttributeDeclImpl ad;
  private SimpleTypeImpl st;
  private String expectedNamespace;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public Schema(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie, boolean _includeMode, String _expectedNamespace)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.includeMode = _includeMode;
    this.expectedNamespace = _expectedNamespace;
    this.$_ngcc_current_state = 56;
  }
  
  public Schema(NGCCRuntimeEx runtime, boolean _includeMode, String _expectedNamespace)
  {
    this(null, runtime, runtime, -1, _includeMode, _expectedNamespace);
  }
  
  private void action0()
    throws SAXException
  {
    this.$runtime.checkDoubleDefError(this.$runtime.currentSchema.getAttGroupDecl(this.ag.getName()));
    this.$runtime.currentSchema.addAttGroupDecl(this.ag);
  }
  
  private void action1()
    throws SAXException
  {
    this.$runtime.currentSchema.addNotation(this.notation);
  }
  
  private void action2()
    throws SAXException
  {
    this.$runtime.checkDoubleDefError(this.$runtime.currentSchema.getModelGroupDecl(this.group.getName()));
    this.$runtime.currentSchema.addModelGroupDecl(this.group);
  }
  
  private void action3()
    throws SAXException
  {
    this.$runtime.checkDoubleDefError(this.$runtime.currentSchema.getAttributeDecl(this.ad.getName()));
    this.$runtime.currentSchema.addAttributeDecl(this.ad);
  }
  
  private void action4()
    throws SAXException
  {
    this.locator = this.$runtime.copyLocator();
  }
  
  private void action5()
    throws SAXException
  {
    this.$runtime.checkDoubleDefError(this.$runtime.currentSchema.getType(this.ct.getName()));
    this.$runtime.currentSchema.addComplexType(this.ct);
  }
  
  private void action6()
    throws SAXException
  {
    this.$runtime.checkDoubleDefError(this.$runtime.currentSchema.getType(this.st.getName()));
    this.$runtime.currentSchema.addSimpleType(this.st);
  }
  
  private void action7()
    throws SAXException
  {
    this.$runtime.checkDoubleDefError(this.$runtime.currentSchema.getElementDecl(this.e.getName()));
    this.$runtime.currentSchema.addElementDecl(this.e);
  }
  
  private void action8()
    throws SAXException
  {
    this.locator = this.$runtime.copyLocator();
  }
  
  private void action9()
    throws SAXException
  {
    this.$runtime.currentSchema.setAnnotation(this.anno);
  }
  
  private void action10()
    throws SAXException
  {
    this.$runtime.finalDefault = this.finalDefault.intValue();
  }
  
  private void action11()
    throws SAXException
  {
    this.$runtime.blockDefault = this.blockDefault.intValue();
  }
  
  private void action12()
    throws SAXException
  {
    this.$runtime.elementFormDefault = this.efd;
  }
  
  private void action13()
    throws SAXException
  {
    this.$runtime.attributeFormDefault = this.afd;
  }
  
  private void action14()
    throws SAXException
  {
    Attributes test = this.$runtime.getCurrentAttributes();
    String tns = test.getValue("targetNamespace");
    if (!this.includeMode)
    {
      if (tns == null) {
        tns = "";
      }
      this.$runtime.currentSchema = this.$runtime.parser.schemaSet.createSchema(tns, this.$runtime.copyLocator());
      if ((this.expectedNamespace != null) && (!this.expectedNamespace.equals(tns))) {
        this.$runtime.reportError(Messages.format("UnexpectedTargetnamespace.Import", tns, this.expectedNamespace, tns), this.$runtime.getLocator());
      }
    }
    else
    {
      if ((tns != null) && (this.expectedNamespace != null) && (!this.expectedNamespace.equals(tns))) {
        this.$runtime.reportError(Messages.format("UnexpectedTargetnamespace.Include", tns, this.expectedNamespace, tns), this.$runtime.getLocator());
      }
      if (tns == null) {
        this.$runtime.chameleonMode = true;
      }
    }
    if (this.$runtime.hasAlreadyBeenRead(this.$runtime.currentSchema.getTargetNamespace()))
    {
      this.$runtime.redirectSubtree(new DefaultHandler(), "", "", "");
      return;
    }
    this.anno = ((AnnotationImpl)this.$runtime.currentSchema.getAnnotation());
    this.$runtime.blockDefault = 0;
    this.$runtime.finalDefault = 0;
  }
  
  public void enterElement(String $__uri, String $__local, String $__qname, Attributes $attrs)
    throws SAXException
  {
    this.$uri = $__uri;
    this.$localName = $__local;
    this.$qname = $__qname;
    switch (this.$_ngcc_current_state)
    {
    case 2: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 103, this.anno, AnnotationContext.SCHEMA);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("include")))
      {
        NGCCHandler h = new includeDecl(this, this._source, this.$runtime, 104);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("import")))
      {
        NGCCHandler h = new importDecl(this, this._source, this.$runtime, 105);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("redefine")))
      {
        NGCCHandler h = new redefine(this, this._source, this.$runtime, 106);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action8();
        this.$_ngcc_current_state = 27;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType")))
      {
        NGCCHandler h = new simpleType(this, this._source, this.$runtime, 108);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType")))
      {
        NGCCHandler h = new complexType(this, this._source, this.$runtime, 109);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action4();
        this.$_ngcc_current_state = 16;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group")))
      {
        NGCCHandler h = new group(this, this._source, this.$runtime, 111);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("notation")))
      {
        NGCCHandler h = new notation(this, this._source, this.$runtime, 112);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup")))
      {
        NGCCHandler h = new attributeGroupDecl(this, this._source, this.$runtime, 113);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(this, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 11: 
      int $ai;
      if ((($ai = this.$runtime.getAttributeIndex("", "form")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0))
      {
        NGCCHandler h = new attributeDeclBody(this, this._source, this.$runtime, 9, this.locator, false, this.defaultValue, this.fixedValue);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 40: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "blockDefault")) >= 0)
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
    case 1: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 92, this.anno, AnnotationContext.SCHEMA);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("include")))
      {
        NGCCHandler h = new includeDecl(this, this._source, this.$runtime, 93);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("import")))
      {
        NGCCHandler h = new importDecl(this, this._source, this.$runtime, 94);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("redefine")))
      {
        NGCCHandler h = new redefine(this, this._source, this.$runtime, 95);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action8();
        this.$_ngcc_current_state = 27;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType")))
      {
        NGCCHandler h = new simpleType(this, this._source, this.$runtime, 97);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType")))
      {
        NGCCHandler h = new complexType(this, this._source, this.$runtime, 98);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action4();
        this.$_ngcc_current_state = 16;
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group")))
      {
        NGCCHandler h = new group(this, this._source, this.$runtime, 100);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("notation")))
      {
        NGCCHandler h = new notation(this, this._source, this.$runtime, 101);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup")))
      {
        NGCCHandler h = new attributeGroupDecl(this, this._source, this.$runtime, 102);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 36: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "finalDefault")) >= 0)
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
    case 52: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "targetNamespace")) >= 0)
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
    case 12: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
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
    case 27: 
      int $ai;
      if ((($ai = this.$runtime.getAttributeIndex("", "final")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "form")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0))
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 27, this.locator, true);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 48: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "attributeFormDefault")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 44;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 44: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "elementFormDefault")) >= 0)
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
    case 16: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 12;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 56: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("schema")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action14();
        this.$_ngcc_current_state = 52;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
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
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveElement(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 11: 
      int $ai;
      if (((($ai = this.$runtime.getAttributeIndex("", "form")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))) || ((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))))
      {
        NGCCHandler h = new attributeDeclBody(this, this._source, this.$runtime, 9, this.locator, false, this.defaultValue, this.fixedValue);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 40: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "blockDefault")) >= 0)
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
    case 1: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("schema")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 36: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "finalDefault")) >= 0)
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
    case 52: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "targetNamespace")) >= 0)
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
    case 12: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
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
    case 27: 
      int $ai;
      if (((($ai = this.$runtime.getAttributeIndex("", "final")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "form")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "block")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "default")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))) || ((($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))))
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 27, this.locator, true);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 48: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "attributeFormDefault")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 44;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 10: 
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
    case 26: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 44: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "elementFormDefault")) >= 0)
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
    case 16: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 12;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 3: 
    case 4: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 13: 
    case 14: 
    case 15: 
    case 17: 
    case 18: 
    case 19: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 25: 
    case 28: 
    case 29: 
    case 30: 
    case 31: 
    case 32: 
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
    case 49: 
    case 50: 
    case 51: 
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
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 12: 
      if (($__uri.equals("")) && ($__local.equals("fixed")))
      {
        this.$_ngcc_current_state = 14;
      }
      else
      {
        this.$_ngcc_current_state = 11;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 52: 
      if (($__uri.equals("")) && ($__local.equals("targetNamespace")))
      {
        this.$_ngcc_current_state = 54;
      }
      else
      {
        this.$_ngcc_current_state = 48;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 27: 
      if ((($__uri.equals("")) && ($__local.equals("final"))) || (($__uri.equals("")) && ($__local.equals("form"))) || (($__uri.equals("")) && ($__local.equals("block"))) || (($__uri.equals("")) && ($__local.equals("name"))) || (($__uri.equals("")) && ($__local.equals("abstract"))) || (($__uri.equals("")) && ($__local.equals("default"))) || (($__uri.equals("")) && ($__local.equals("fixed"))))
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 27, this.locator, true);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 11: 
      if ((($__uri.equals("")) && ($__local.equals("form"))) || (($__uri.equals("")) && ($__local.equals("name"))))
      {
        NGCCHandler h = new attributeDeclBody(this, this._source, this.$runtime, 9, this.locator, false, this.defaultValue, this.fixedValue);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 40: 
      if (($__uri.equals("")) && ($__local.equals("blockDefault")))
      {
        this.$_ngcc_current_state = 42;
      }
      else
      {
        this.$_ngcc_current_state = 36;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 48: 
      if (($__uri.equals("")) && ($__local.equals("attributeFormDefault")))
      {
        this.$_ngcc_current_state = 50;
      }
      else
      {
        this.$_ngcc_current_state = 44;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 44: 
      if (($__uri.equals("")) && ($__local.equals("elementFormDefault")))
      {
        this.$_ngcc_current_state = 46;
      }
      else
      {
        this.$_ngcc_current_state = 40;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 16: 
      if (($__uri.equals("")) && ($__local.equals("default")))
      {
        this.$_ngcc_current_state = 18;
      }
      else
      {
        this.$_ngcc_current_state = 12;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 36: 
      if (($__uri.equals("")) && ($__local.equals("finalDefault")))
      {
        this.$_ngcc_current_state = 38;
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
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
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 53: 
      if (($__uri.equals("")) && ($__local.equals("targetNamespace"))) {
        this.$_ngcc_current_state = 48;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 40: 
      this.$_ngcc_current_state = 36;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 17: 
      if (($__uri.equals("")) && ($__local.equals("default"))) {
        this.$_ngcc_current_state = 12;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 49: 
      if (($__uri.equals("")) && ($__local.equals("attributeFormDefault"))) {
        this.$_ngcc_current_state = 44;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 37: 
      if (($__uri.equals("")) && ($__local.equals("finalDefault"))) {
        this.$_ngcc_current_state = 2;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 36: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 41: 
      if (($__uri.equals("")) && ($__local.equals("blockDefault"))) {
        this.$_ngcc_current_state = 36;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 52: 
      this.$_ngcc_current_state = 48;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 12: 
      this.$_ngcc_current_state = 11;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 45: 
      if (($__uri.equals("")) && ($__local.equals("elementFormDefault"))) {
        this.$_ngcc_current_state = 40;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 48: 
      this.$_ngcc_current_state = 44;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 44: 
      this.$_ngcc_current_state = 40;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 13: 
      if (($__uri.equals("")) && ($__local.equals("fixed"))) {
        this.$_ngcc_current_state = 11;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 16: 
      this.$_ngcc_current_state = 12;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
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
    case 14: 
    case 15: 
    case 18: 
    case 19: 
    case 20: 
    case 21: 
    case 22: 
    case 23: 
    case 24: 
    case 25: 
    case 26: 
    case 27: 
    case 28: 
    case 29: 
    case 30: 
    case 31: 
    case 32: 
    case 33: 
    case 34: 
    case 35: 
    case 38: 
    case 39: 
    case 42: 
    case 43: 
    case 46: 
    case 47: 
    case 50: 
    case 51: 
    default: 
      unexpectedLeaveAttribute($__qname);
    }
  }
  
  public void text(String $value)
    throws SAXException
  {
    switch (this.$_ngcc_current_state)
    {
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 0: 
      revertToParentFromText(this, this._cookie, $value);
      
      break;
    case 38: 
      NGCCHandler h = new erSet(this, this._source, this.$runtime, 116);
      spawnChildFromText(h, $value);
      
      break;
    case 11: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        NGCCHandler h = new attributeDeclBody(this, this._source, this.$runtime, 9, this.locator, false, this.defaultValue, this.fixedValue);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "form")) >= 0)
      {
        NGCCHandler h = new attributeDeclBody(this, this._source, this.$runtime, 9, this.locator, false, this.defaultValue, this.fixedValue);
        spawnChildFromText(h, $value);
      }
      break;
    case 54: 
      this.$_ngcc_current_state = 53;
      
      break;
    case 40: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "blockDefault")) >= 0)
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
    case 36: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "finalDefault")) >= 0)
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
    case 42: 
      NGCCHandler h = new ersSet(this, this._source, this.$runtime, 121);
      spawnChildFromText(h, $value);
      
      break;
    case 52: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "targetNamespace")) >= 0)
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
    case 50: 
      if ($value.equals("unqualified"))
      {
        NGCCHandler h = new qualification(this, this._source, this.$runtime, 131);
        spawnChildFromText(h, $value);
      }
      else if ($value.equals("qualified"))
      {
        NGCCHandler h = new qualification(this, this._source, this.$runtime, 131);
        spawnChildFromText(h, $value);
      }
      break;
    case 12: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
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
    case 18: 
      this.defaultValue = $value;
      this.$_ngcc_current_state = 17;
      
      break;
    case 46: 
      if ($value.equals("unqualified"))
      {
        NGCCHandler h = new qualification(this, this._source, this.$runtime, 126);
        spawnChildFromText(h, $value);
      }
      else if ($value.equals("qualified"))
      {
        NGCCHandler h = new qualification(this, this._source, this.$runtime, 126);
        spawnChildFromText(h, $value);
      }
      break;
    case 27: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 27, this.locator, true);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 27, this.locator, true);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "abstract")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 27, this.locator, true);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 27, this.locator, true);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "block")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 27, this.locator, true);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "form")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 27, this.locator, true);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "final")) >= 0)
      {
        NGCCHandler h = new elementDeclBody(this, this._source, this.$runtime, 27, this.locator, true);
        spawnChildFromText(h, $value);
      }
      break;
    case 48: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "attributeFormDefault")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 44;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 14: 
      this.fixedValue = $value;
      this.$_ngcc_current_state = 13;
      
      break;
    case 44: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "elementFormDefault")) >= 0)
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
    case 16: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "default")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 12;
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
    case 9: 
      this.ad = ((AttributeDeclImpl)$__result__);
      action3();
      this.$_ngcc_current_state = 10;
      
      break;
    case 131: 
      this.afd = ((Boolean)$__result__).booleanValue();
      action13();
      this.$_ngcc_current_state = 49;
      
      break;
    case 27: 
      this.e = ((ElementDecl)$__result__);
      action7();
      this.$_ngcc_current_state = 26;
      
      break;
    case 116: 
      this.finalDefault = ((Integer)$__result__);
      action10();
      this.$_ngcc_current_state = 37;
      
      break;
    case 92: 
      this.anno = ((AnnotationImpl)$__result__);
      action9();
      this.$_ngcc_current_state = 1;
      
      break;
    case 93: 
      this.$_ngcc_current_state = 1;
      
      break;
    case 94: 
      this.$_ngcc_current_state = 1;
      
      break;
    case 95: 
      this.$_ngcc_current_state = 1;
      
      break;
    case 97: 
      this.st = ((SimpleTypeImpl)$__result__);
      action6();
      this.$_ngcc_current_state = 1;
      
      break;
    case 98: 
      this.ct = ((ComplexTypeImpl)$__result__);
      action5();
      this.$_ngcc_current_state = 1;
      
      break;
    case 100: 
      this.group = ((ModelGroupDeclImpl)$__result__);
      action2();
      this.$_ngcc_current_state = 1;
      
      break;
    case 101: 
      this.notation = ((XSNotation)$__result__);
      action1();
      this.$_ngcc_current_state = 1;
      
      break;
    case 102: 
      this.ag = ((AttGroupDeclImpl)$__result__);
      action0();
      this.$_ngcc_current_state = 1;
      
      break;
    case 103: 
      this.anno = ((AnnotationImpl)$__result__);
      action9();
      this.$_ngcc_current_state = 1;
      
      break;
    case 104: 
      this.$_ngcc_current_state = 1;
      
      break;
    case 105: 
      this.$_ngcc_current_state = 1;
      
      break;
    case 106: 
      this.$_ngcc_current_state = 1;
      
      break;
    case 108: 
      this.st = ((SimpleTypeImpl)$__result__);
      action6();
      this.$_ngcc_current_state = 1;
      
      break;
    case 109: 
      this.ct = ((ComplexTypeImpl)$__result__);
      action5();
      this.$_ngcc_current_state = 1;
      
      break;
    case 111: 
      this.group = ((ModelGroupDeclImpl)$__result__);
      action2();
      this.$_ngcc_current_state = 1;
      
      break;
    case 112: 
      this.notation = ((XSNotation)$__result__);
      action1();
      this.$_ngcc_current_state = 1;
      
      break;
    case 113: 
      this.ag = ((AttGroupDeclImpl)$__result__);
      action0();
      this.$_ngcc_current_state = 1;
      
      break;
    case 121: 
      this.blockDefault = ((Integer)$__result__);
      action11();
      this.$_ngcc_current_state = 41;
      
      break;
    case 126: 
      this.efd = ((Boolean)$__result__).booleanValue();
      action12();
      this.$_ngcc_current_state = 45;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
  
  private String tns = null;
  private Locator locator;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\parser\state\Schema.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */