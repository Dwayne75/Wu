package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class occurs
  extends NGCCHandler
{
  private String v;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public occurs(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 5;
  }
  
  public occurs(NGCCRuntimeEx runtime)
  {
    this(null, runtime, runtime, -1);
  }
  
  private void action0()
    throws SAXException
  {
    this.min = Integer.parseInt(this.v);
  }
  
  private void action1()
    throws SAXException
  {
    this.max = -1;
  }
  
  private void action2()
    throws SAXException
  {
    this.max = Integer.parseInt(this.v);
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
    case 5: 
      if (($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0)
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
    case 1: 
      if (($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0)
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
    case 0: 
      revertToParentFromEnterElement(this, this._cookie, $__uri, $__local, $__qname, $attrs);
      
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
    int $ai;
    switch (this.$_ngcc_current_state)
    {
    case 5: 
      if (($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0)
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
    case 1: 
      if (($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0)
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
    case 0: 
      revertToParentFromLeaveElement(this, this._cookie, $__uri, $__local, $__qname);
      
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
      if (($__uri.equals("")) && ($__local.equals("maxOccurs")))
      {
        this.$_ngcc_current_state = 7;
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 1: 
      if (($__uri.equals("")) && ($__local.equals("minOccurs")))
      {
        this.$_ngcc_current_state = 3;
      }
      else
      {
        this.$_ngcc_current_state = 0;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
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
    case 5: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 6: 
      if (($__uri.equals("")) && ($__local.equals("maxOccurs"))) {
        this.$_ngcc_current_state = 1;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 2: 
      if (($__uri.equals("")) && ($__local.equals("minOccurs"))) {
        this.$_ngcc_current_state = 0;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 1: 
      this.$_ngcc_current_state = 0;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
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
    int $ai;
    switch (this.$_ngcc_current_state)
    {
    case 5: 
      if (($ai = this.$runtime.getAttributeIndex("", "maxOccurs")) >= 0)
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
    case 1: 
      if (($ai = this.$runtime.getAttributeIndex("", "minOccurs")) >= 0)
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
      if ($value.equals("unbounded"))
      {
        this.$_ngcc_current_state = 6;
        action1();
      }
      else
      {
        this.v = $value;
        this.$_ngcc_current_state = 6;
        action2();
      }
      break;
    case 3: 
      this.v = $value;
      this.$_ngcc_current_state = 2;
      action0();
      
      break;
    case 0: 
      revertToParentFromText(this, this._cookie, $value);
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {}
  
  public boolean accepted()
  {
    return (this.$_ngcc_current_state == 0) || (this.$_ngcc_current_state == 1) || (this.$_ngcc_current_state == 5);
  }
  
  int max = 1;
  int min = 1;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\state\occurs.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */