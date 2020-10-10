package com.sun.tools.xjc.reader.xmlschema.bindinfo.parser;

import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding.NamingRule;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.NGCCRuntimeEx;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class nameXmlTransformRule
  extends NGCCHandler
{
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public nameXmlTransformRule(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 5;
  }
  
  public nameXmlTransformRule(NGCCRuntimeEx runtime)
  {
    this(null, runtime, runtime, -1);
  }
  
  public void enterElement(String $__uri, String $__local, String $__qname, Attributes $attrs)
    throws SAXException
  {
    this.$uri = $__uri;
    this.$localName = $__local;
    this.$qname = $__qname;
    switch (this.$_ngcc_current_state)
    {
    case 5: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0)
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
    case 0: 
      revertToParentFromEnterElement(new BISchemaBinding.NamingRule(this.prefix, this.suffix), this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 1: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 0;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
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
    case 5: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0)
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
    case 0: 
      revertToParentFromLeaveElement(new BISchemaBinding.NamingRule(this.prefix, this.suffix), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 0;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
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
    case 5: 
      if (($__uri == "") && ($__local == "prefix"))
      {
        this.$_ngcc_current_state = 7;
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(new BISchemaBinding.NamingRule(this.prefix, this.suffix), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      if (($__uri == "") && ($__local == "suffix"))
      {
        this.$_ngcc_current_state = 3;
      }
      else
      {
        this.$_ngcc_current_state = 0;
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
      if (($__uri == "") && ($__local == "suffix")) {
        this.$_ngcc_current_state = 0;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 5: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(new BISchemaBinding.NamingRule(this.prefix, this.suffix), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 6: 
      if (($__uri == "") && ($__local == "prefix")) {
        this.$_ngcc_current_state = 1;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 1: 
      this.$_ngcc_current_state = 0;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 3: 
    case 4: 
    default: 
      unexpectedLeaveAttribute($__qname);
    }
  }
  
  public void text(String $value)
    throws SAXException
  {
    switch (this.$_ngcc_current_state)
    {
    case 5: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "prefix")) >= 0)
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
    case 0: 
      revertToParentFromText(new BISchemaBinding.NamingRule(this.prefix, this.suffix), this._cookie, $value);
      
      break;
    case 3: 
      this.suffix = $value;
      this.$_ngcc_current_state = 2;
      
      break;
    case 1: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "suffix")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 0;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 7: 
      this.prefix = $value;
      this.$_ngcc_current_state = 6;
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {}
  
  public boolean accepted()
  {
    return (this.$_ngcc_current_state == 1) || (this.$_ngcc_current_state == 0) || (this.$_ngcc_current_state == 5);
  }
  
  private String prefix = "";
  private String suffix = "";
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\parser\nameXmlTransformRule.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */