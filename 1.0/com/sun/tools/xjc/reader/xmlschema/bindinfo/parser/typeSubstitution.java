package com.sun.tools.xjc.reader.xmlschema.bindinfo.parser;

import com.sun.tools.xjc.reader.xmlschema.bindinfo.NGCCRuntimeEx;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class typeSubstitution
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
  
  public typeSubstitution(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 5;
  }
  
  public typeSubstitution(NGCCRuntimeEx runtime)
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
    case 4: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "type")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(new Boolean(true), this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 5: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "typeSubstitution"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 4;
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
    case 1: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "typeSubstitution"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 4: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "type")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveElement(new Boolean(true), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
    case 3: 
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
    case 4: 
      if (($__uri == "") && ($__local == "type")) {
        this.$_ngcc_current_state = 3;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(new Boolean(true), this._cookie, $__uri, $__local, $__qname);
      
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
    case 0: 
      revertToParentFromLeaveAttribute(new Boolean(true), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      if (($__uri == "") && ($__local == "type")) {
        this.$_ngcc_current_state = 1;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
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
    case 3: 
      if ($value.equals("complex")) {
        this.$_ngcc_current_state = 2;
      }
      break;
    case 4: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "type")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 0: 
      revertToParentFromText(new Boolean(true), this._cookie, $value);
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {}
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\parser\typeSubstitution.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */