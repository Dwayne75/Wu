package com.sun.tools.xjc.reader.xmlschema.bindinfo.parser;

import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.NGCCRuntimeEx;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class conversion
  extends NGCCHandler
{
  private BIConversion r;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public conversion(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 3;
  }
  
  public conversion(NGCCRuntimeEx runtime)
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
    case 0: 
      revertToParentFromEnterElement(this.r, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 3: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 2;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 2: 
      int $ai;
      if ((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "parseMethod")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "printMethod")) >= 0))
      {
        NGCCHandler h = new conversionBody(this, this._source, this.$runtime, 421);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 1: 
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
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType"))
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
      revertToParentFromLeaveElement(this.r, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      int $ai;
      if (((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType")) || ((($ai = this.$runtime.getAttributeIndex("", "parseMethod")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType")) || ((($ai = this.$runtime.getAttributeIndex("", "printMethod")) >= 0) && ($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType")))
      {
        NGCCHandler h = new conversionBody(this, this._source, this.$runtime, 421);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
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
    case 0: 
      revertToParentFromEnterAttribute(this.r, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      if ((($__uri == "") && ($__local == "name")) || (($__uri == "") && ($__local == "parseMethod")) || (($__uri == "") && ($__local == "printMethod")))
      {
        NGCCHandler h = new conversionBody(this, this._source, this.$runtime, 421);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
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
    case 0: 
      revertToParentFromLeaveAttribute(this.r, this._cookie, $__uri, $__local, $__qname);
      
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
    case 0: 
      revertToParentFromText(this.r, this._cookie, $value);
      
      break;
    case 2: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "printMethod")) >= 0)
      {
        NGCCHandler h = new conversionBody(this, this._source, this.$runtime, 421);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "parseMethod")) >= 0)
      {
        NGCCHandler h = new conversionBody(this, this._source, this.$runtime, 421);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        NGCCHandler h = new conversionBody(this, this._source, this.$runtime, 421);
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
    case 421: 
      this.r = ((BIConversion)$__result__);
      this.$_ngcc_current_state = 1;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\parser\conversion.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */