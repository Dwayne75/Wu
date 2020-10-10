package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.impl.AnnotationImpl;
import com.sun.xml.xsom.impl.ForeignAttributesImpl;
import com.sun.xml.xsom.impl.NotationImpl;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.parser.AnnotationContext;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class notation
  extends NGCCHandler
{
  private String name;
  private String pub;
  private ForeignAttributesImpl fa;
  private String sys;
  private AnnotationImpl ann;
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
  
  public notation(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 15;
  }
  
  public notation(NGCCRuntimeEx runtime)
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
    int $ai;
    switch (this.$_ngcc_current_state)
    {
    case 10: 
      if (($ai = this.$runtime.getAttributeIndex("", "public")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 2: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 386, null, AnnotationContext.NOTATION);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 13: 
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 15: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("notation")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action0();
        this.$_ngcc_current_state = 14;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(makeResult(), this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 4: 
      if (($ai = this.$runtime.getAttributeIndex("", "system")) >= 0)
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
    case 14: 
      if ((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 399, null);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 1: 
    case 3: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 11: 
    case 12: 
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
    case 10: 
      if (($ai = this.$runtime.getAttributeIndex("", "public")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
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
    case 13: 
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
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
      revertToParentFromLeaveElement(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      if (($ai = this.$runtime.getAttributeIndex("", "system")) >= 0)
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
    case 14: 
      if ((($ai = this.$runtime.getAttributeIndex("", "name")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("notation")))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 399, null);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 1: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("notation")))
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
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 11: 
    case 12: 
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
      if (($__uri.equals("")) && ($__local.equals("public"))) {
        this.$_ngcc_current_state = 9;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 13: 
      if (($__uri.equals("")) && ($__local.equals("name"))) {
        this.$_ngcc_current_state = 12;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      if (($__uri.equals("")) && ($__local.equals("system")))
      {
        this.$_ngcc_current_state = 6;
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 14: 
      if (($__uri.equals("")) && ($__local.equals("name")))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 399, null);
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
    case 7: 
    case 8: 
    case 9: 
    case 11: 
    case 12: 
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
    case 8: 
      if (($__uri.equals("")) && ($__local.equals("public"))) {
        this.$_ngcc_current_state = 4;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 11: 
      if (($__uri.equals("")) && ($__local.equals("name"))) {
        this.$_ngcc_current_state = 10;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 5: 
      if (($__uri.equals("")) && ($__local.equals("system"))) {
        this.$_ngcc_current_state = 2;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveAttribute(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
    case 3: 
    case 6: 
    case 7: 
    case 9: 
    case 10: 
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
    case 9: 
      this.pub = $value;
      this.$_ngcc_current_state = 8;
      
      break;
    case 6: 
      this.sys = $value;
      this.$_ngcc_current_state = 5;
      
      break;
    case 10: 
      if (($ai = this.$runtime.getAttributeIndex("", "public")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 12: 
      this.name = $value;
      this.$_ngcc_current_state = 11;
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 13: 
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 0: 
      revertToParentFromText(makeResult(), this._cookie, $value);
      
      break;
    case 4: 
      if (($ai = this.$runtime.getAttributeIndex("", "system")) >= 0)
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
    case 14: 
      if (($ai = this.$runtime.getAttributeIndex("", "name")) >= 0)
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 399, null);
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
    case 386: 
      this.ann = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 1;
      
      break;
    case 399: 
      this.fa = ((ForeignAttributesImpl)$__result__);
      this.$_ngcc_current_state = 13;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
  
  private XSNotation makeResult()
  {
    return new NotationImpl(this.$runtime.document, this.ann, this.loc, this.fa, this.name, this.pub, this.sys);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\state\notation.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */