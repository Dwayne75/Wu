package com.sun.tools.xjc.reader.xmlschema.bindinfo.parser;

import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding.NamingRule;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.NGCCRuntimeEx;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class schemaBindings
  extends NGCCHandler
{
  private BISchemaBinding.NamingRule at;
  private String packageName;
  private BISchemaBinding.NamingRule mt;
  private String javadoc;
  private BISchemaBinding.NamingRule nt;
  private BISchemaBinding.NamingRule tt;
  private BISchemaBinding.NamingRule et;
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
  
  public schemaBindings(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 34;
  }
  
  public schemaBindings(NGCCRuntimeEx runtime)
  {
    this(null, runtime, runtime, -1);
  }
  
  private void action0()
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
    case 7: 
      int $ai;
      if ((($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 261);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 22: 
      int $ai;
      if ((($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 277);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 27: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javadoc"))
      {
        NGCCHandler h = new javadoc(this, this._source, this.$runtime, 305);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 26;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 19: 
      int $ai;
      if ((($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 273);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 34: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "schemaBindings"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action0();
        this.$_ngcc_current_state = 25;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 2: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "nameXmlTransform"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 4;
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 15: 
      int $ai;
      if ((($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 269);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 3: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "typeName"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 22;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "elementName"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 19;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "attributeName"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 15;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "modelGroupName"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 11;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "anonymousTypeName"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 7;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 25: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "package"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 29;
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(makeResult(), this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 29: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 27;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 4: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "typeName"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 22;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "elementName"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 19;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "attributeName"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 15;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "modelGroupName"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 11;
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "anonymousTypeName"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 7;
      }
      else
      {
        this.$_ngcc_current_state = 3;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 11: 
      int $ai;
      if ((($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 265);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 1: 
    case 5: 
    case 6: 
    case 8: 
    case 9: 
    case 10: 
    case 12: 
    case 13: 
    case 14: 
    case 16: 
    case 17: 
    case 18: 
    case 20: 
    case 21: 
    case 23: 
    case 24: 
    case 26: 
    case 28: 
    case 30: 
    case 31: 
    case 32: 
    case 33: 
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
      int $ai;
      if ((($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "anonymousTypeName")) || ((($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "anonymousTypeName")) || ((($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "anonymousTypeName")))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 261);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 22: 
      int $ai;
      if ((($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "typeName")) || ((($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "typeName")) || ((($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "typeName")))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 277);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 27: 
      this.$_ngcc_current_state = 26;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 19: 
      int $ai;
      if ((($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "elementName")) || ((($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "elementName")) || ((($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "elementName")))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 273);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "schemaBindings"))
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
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "package"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 2;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 15: 
      int $ai;
      if ((($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "attributeName")) || ((($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "attributeName")) || ((($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "attributeName")))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 269);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 6: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "anonymousTypeName"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 3;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 3: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "nameXmlTransform"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 10: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "modelGroupName"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 3;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 25: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveElement(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 29: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 27;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 18: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "elementName"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 3;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 21: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "typeName"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 3;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 14: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "attributeName"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 3;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 4: 
      this.$_ngcc_current_state = 3;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 11: 
      int $ai;
      if (((($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "modelGroupName")) || (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "modelGroupName")) || ((($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "modelGroupName")))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 265);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 5: 
    case 8: 
    case 9: 
    case 12: 
    case 13: 
    case 16: 
    case 17: 
    case 20: 
    case 23: 
    case 24: 
    case 28: 
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
    case 15: 
      if ((($__uri == "") && ($__local == "suffix")) || (($__uri == "") && ($__local == "prefix")))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 269);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 7: 
      if ((($__uri == "") && ($__local == "suffix")) || (($__uri == "") && ($__local == "prefix")))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 261);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 22: 
      if ((($__uri == "") && ($__local == "suffix")) || (($__uri == "") && ($__local == "prefix")))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 277);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 25: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromEnterAttribute(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 19: 
      if ((($__uri == "") && ($__local == "suffix")) || (($__uri == "") && ($__local == "prefix")))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 273);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 29: 
      if (($__uri == "") && ($__local == "name"))
      {
        this.$_ngcc_current_state = 31;
      }
      else
      {
        this.$_ngcc_current_state = 27;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 27: 
      this.$_ngcc_current_state = 26;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      this.$_ngcc_current_state = 3;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 11: 
      if ((($__uri == "") && ($__local == "suffix")) || (($__uri == "") && ($__local == "prefix")))
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 265);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 1: 
    case 3: 
    case 5: 
    case 6: 
    case 8: 
    case 9: 
    case 10: 
    case 12: 
    case 13: 
    case 14: 
    case 16: 
    case 17: 
    case 18: 
    case 20: 
    case 21: 
    case 23: 
    case 24: 
    case 26: 
    case 28: 
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
    case 25: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 30: 
      if (($__uri == "") && ($__local == "name")) {
        this.$_ngcc_current_state = 27;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 29: 
      this.$_ngcc_current_state = 27;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 27: 
      this.$_ngcc_current_state = 26;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      this.$_ngcc_current_state = 3;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    default: 
      unexpectedLeaveAttribute($__qname);
    }
  }
  
  public void text(String $value)
    throws SAXException
  {
    switch (this.$_ngcc_current_state)
    {
    case 15: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0)
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 269);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0)
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 269);
        spawnChildFromText(h, $value);
      }
      break;
    case 7: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0)
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 261);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0)
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 261);
        spawnChildFromText(h, $value);
      }
      break;
    case 31: 
      this.packageName = $value;
      this.$_ngcc_current_state = 30;
      
      break;
    case 22: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0)
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 277);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0)
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 277);
        spawnChildFromText(h, $value);
      }
      break;
    case 25: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 0: 
      revertToParentFromText(makeResult(), this._cookie, $value);
      
      break;
    case 19: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0)
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 273);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0)
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 273);
        spawnChildFromText(h, $value);
      }
      break;
    case 29: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 27;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 27: 
      this.$_ngcc_current_state = 26;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 4: 
      this.$_ngcc_current_state = 3;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 11: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0)
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 265);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0)
      {
        NGCCHandler h = new nameXmlTransformRule(this, this._source, this.$runtime, 265);
        spawnChildFromText(h, $value);
      }
      break;
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 305: 
      this.javadoc = ((String)$__result__);
      this.$_ngcc_current_state = 26;
      
      break;
    case 261: 
      this.nt = ((BISchemaBinding.NamingRule)$__result__);
      this.$_ngcc_current_state = 6;
      
      break;
    case 277: 
      this.tt = ((BISchemaBinding.NamingRule)$__result__);
      this.$_ngcc_current_state = 21;
      
      break;
    case 273: 
      this.et = ((BISchemaBinding.NamingRule)$__result__);
      this.$_ngcc_current_state = 18;
      
      break;
    case 269: 
      this.at = ((BISchemaBinding.NamingRule)$__result__);
      this.$_ngcc_current_state = 14;
      
      break;
    case 265: 
      this.mt = ((BISchemaBinding.NamingRule)$__result__);
      this.$_ngcc_current_state = 10;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
  
  public BISchemaBinding makeResult()
  {
    return new BISchemaBinding(this.packageName, this.javadoc, this.tt, this.et, this.at, this.mt, this.nt, this.loc);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\parser\schemaBindings.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */