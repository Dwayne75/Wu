package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.impl.AnnotationImpl;
import com.sun.xml.xsom.impl.FacetImpl;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.parser.AnnotationContext;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class facet
  extends NGCCHandler
{
  private AnnotationImpl annotation;
  private String fixed;
  private String value;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private FacetImpl result;
  private Locator locator;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public facet(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 11;
  }
  
  public facet(NGCCRuntimeEx runtime)
  {
    this(null, runtime, runtime, -1);
  }
  
  private void action0()
    throws SAXException
  {
    this.result = new FacetImpl(this.$runtime.currentSchema, this.annotation, this.locator, this.$localName, this.value, this.$runtime.createValidationContext(), this.$runtime.parseBoolean(this.fixed));
  }
  
  private void action1()
    throws SAXException
  {
    this.locator = this.$runtime.copyLocator();
  }
  
  public void enterElement(String $__uri, String $__local, String $__qname, Attributes $attrs)
    throws SAXException
  {
    this.$uri = $__uri;
    this.$localName = $__local;
    this.$qname = $__qname;
    switch (this.$_ngcc_current_state)
    {
    case 11: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("totalDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("fractionDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("length"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("enumeration"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("whiteSpace"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("pattern"))))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action1();
        this.$_ngcc_current_state = 10;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 10: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "value")) >= 0)
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
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 671, null, AnnotationContext.SIMPLETYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(this.result, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 4: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
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
    case 1: 
    case 3: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
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
    case 10: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "value")) >= 0)
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
    case 0: 
      revertToParentFromLeaveElement(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
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
    case 1: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("totalDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("fractionDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("length"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("enumeration"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("whiteSpace"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("pattern"))))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
        action0();
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
      if (($__uri.equals("")) && ($__local.equals("value"))) {
        this.$_ngcc_current_state = 9;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromEnterAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      if (($__uri.equals("")) && ($__local.equals("fixed")))
      {
        this.$_ngcc_current_state = 6;
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
    case 5: 
      if (($__uri.equals("")) && ($__local.equals("fixed"))) {
        this.$_ngcc_current_state = 2;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 8: 
      if (($__uri.equals("")) && ($__local.equals("value"))) {
        this.$_ngcc_current_state = 4;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
    case 3: 
    case 6: 
    case 7: 
    default: 
      unexpectedLeaveAttribute($__qname);
    }
  }
  
  public void text(String $value)
    throws SAXException
  {
    switch (this.$_ngcc_current_state)
    {
    case 10: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "value")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 9: 
      this.value = $value;
      this.$_ngcc_current_state = 8;
      
      break;
    case 0: 
      revertToParentFromText(this.result, this._cookie, $value);
      
      break;
    case 6: 
      this.fixed = $value;
      this.$_ngcc_current_state = 5;
      
      break;
    case 4: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "fixed")) >= 0)
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
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 671: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 1;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\parser\state\facet.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */