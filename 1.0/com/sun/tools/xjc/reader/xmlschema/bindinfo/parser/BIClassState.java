package com.sun.tools.xjc.reader.xmlschema.bindinfo.parser;

import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIClass;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.NGCCRuntimeEx;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class BIClassState
  extends NGCCHandler
{
  private String implClass;
  private String javadoc;
  private String name;
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
  
  public BIClassState(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 12;
  }
  
  public BIClassState(NGCCRuntimeEx runtime)
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
    case 12: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "class"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action0();
        this.$_ngcc_current_state = 10;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 10: 
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javadoc"))
      {
        NGCCHandler h = new javadoc(this, this._source, this.$runtime, 414);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 6;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 6: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
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
    case 2: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "implClass")) >= 0)
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
      revertToParentFromEnterElement(makeResult(), this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 1: 
    case 3: 
    case 4: 
    case 5: 
    case 7: 
    case 8: 
    case 9: 
    case 11: 
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
      if (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "class"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 10: 
      this.$_ngcc_current_state = 6;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 6: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
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
    case 2: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "implClass")) >= 0)
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
      revertToParentFromLeaveElement(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 3: 
    case 4: 
    case 5: 
    case 7: 
    case 8: 
    case 9: 
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
    case 10: 
      this.$_ngcc_current_state = 6;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 6: 
      if (($__uri == "") && ($__local == "name"))
      {
        this.$_ngcc_current_state = 8;
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 2: 
      if (($__uri == "") && ($__local == "implClass"))
      {
        this.$_ngcc_current_state = 4;
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
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
    case 3: 
      if (($__uri == "") && ($__local == "implClass")) {
        this.$_ngcc_current_state = 1;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 10: 
      this.$_ngcc_current_state = 6;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 6: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 7: 
      if (($__uri == "") && ($__local == "name")) {
        this.$_ngcc_current_state = 2;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveAttribute(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
    case 4: 
    case 5: 
    case 8: 
    case 9: 
    default: 
      unexpectedLeaveAttribute($__qname);
    }
  }
  
  public void text(String $value)
    throws SAXException
  {
    switch (this.$_ngcc_current_state)
    {
    case 4: 
      this.implClass = $value;
      this.$_ngcc_current_state = 3;
      
      break;
    case 10: 
      this.$_ngcc_current_state = 6;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 6: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
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
    case 2: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "implClass")) >= 0)
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
      revertToParentFromText(makeResult(), this._cookie, $value);
      
      break;
    case 8: 
      this.name = $value;
      this.$_ngcc_current_state = 7;
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 414: 
      this.javadoc = ((String)$__result__);
      this.$_ngcc_current_state = 6;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
  
  public BIClass makeResult()
  {
    return new BIClass(this.loc, this.name, this.implClass, this.javadoc);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\parser\BIClassState.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */