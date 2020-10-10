package com.sun.tools.xjc.reader.xmlschema.bindinfo.parser;

import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIClass;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnum;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnumMember;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXDom;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXIdSymbolSpace;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.NGCCRuntimeEx;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class declaration
  extends NGCCHandler
{
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private BIDeclaration result;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public declaration(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 1;
  }
  
  public declaration(NGCCRuntimeEx runtime)
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
    case 1: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "globalBindings"))
      {
        NGCCHandler h = new globalBindings(this, this._source, this.$runtime, 71);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "schemaBindings"))
      {
        NGCCHandler h = new schemaBindings(this, this._source, this.$runtime, 72);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "class"))
      {
        NGCCHandler h = new BIClassState(this, this._source, this.$runtime, 73);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType"))
      {
        NGCCHandler h = new conversion(this, this._source, this.$runtime, 74);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "property"))
      {
        NGCCHandler h = new property(this, this._source, this.$runtime, 75);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "typesafeEnumClass"))
      {
        NGCCHandler h = new enumDef(this, this._source, this.$runtime, 76);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "typesafeEnumMember"))
      {
        NGCCHandler h = new enumMember(this, this._source, this.$runtime, 77);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "idSymbolSpace"))
      {
        NGCCHandler h = new idSymbolSpace(this, this._source, this.$runtime, 78);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "dom"))
      {
        NGCCHandler h = new dom(this, this._source, this.$runtime, 27);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(this.result, this._cookie, $__uri, $__local, $__qname, $attrs);
      
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
    case 0: 
      revertToParentFromLeaveElement(this.result, this._cookie, $__uri, $__local, $__qname);
      
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
      revertToParentFromEnterAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
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
      revertToParentFromLeaveAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
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
      revertToParentFromText(this.result, this._cookie, $value);
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 71: 
      this.result = ((BIGlobalBinding)$__result__);
      this.$_ngcc_current_state = 0;
      
      break;
    case 72: 
      this.result = ((BISchemaBinding)$__result__);
      this.$_ngcc_current_state = 0;
      
      break;
    case 73: 
      this.result = ((BIClass)$__result__);
      this.$_ngcc_current_state = 0;
      
      break;
    case 74: 
      this.result = ((BIConversion)$__result__);
      this.$_ngcc_current_state = 0;
      
      break;
    case 75: 
      this.result = ((BIProperty)$__result__);
      this.$_ngcc_current_state = 0;
      
      break;
    case 76: 
      this.result = ((BIEnum)$__result__);
      this.$_ngcc_current_state = 0;
      
      break;
    case 77: 
      this.result = ((BIEnumMember)$__result__);
      this.$_ngcc_current_state = 0;
      
      break;
    case 78: 
      this.result = ((BIXIdSymbolSpace)$__result__);
      this.$_ngcc_current_state = 0;
      
      break;
    case 27: 
      this.result = ((BIXDom)$__result__);
      this.$_ngcc_current_state = 0;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\parser\declaration.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */