package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.impl.AnnotationImpl;
import com.sun.xml.xsom.impl.ForeignAttributesImpl;
import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.impl.WildcardImpl;
import com.sun.xml.xsom.impl.WildcardImpl.Any;
import com.sun.xml.xsom.impl.WildcardImpl.Finite;
import com.sun.xml.xsom.impl.WildcardImpl.Other;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.parser.AnnotationContext;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class wildcardBody
  extends NGCCHandler
{
  private AnnotationImpl annotation;
  private Locator locator;
  private String modeValue;
  private String ns;
  private ForeignAttributesImpl fa;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public wildcardBody(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie, Locator _locator)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.locator = _locator;
    this.$_ngcc_current_state = 10;
  }
  
  public wildcardBody(NGCCRuntimeEx runtime, Locator _locator)
  {
    this(null, runtime, runtime, -1, _locator);
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
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 556, null, AnnotationContext.WILDCARD);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 9;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(makeResult(), this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 1: 
      if (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0)
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
    case 9: 
      if ((($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 554, null);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 554, null);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 5: 
      if (($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0)
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
    case 2: 
    case 3: 
    case 4: 
    case 6: 
    case 7: 
    case 8: 
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
      this.$_ngcc_current_state = 9;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveElement(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      if (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0)
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
    case 9: 
      if ((($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0) || (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 554, null);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 554, null);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      break;
    case 5: 
      if (($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0)
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
    case 2: 
    case 3: 
    case 4: 
    case 6: 
    case 7: 
    case 8: 
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
      this.$_ngcc_current_state = 9;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromEnterAttribute(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      if (($__uri.equals("")) && ($__local.equals("namespace")))
      {
        this.$_ngcc_current_state = 3;
      }
      else
      {
        this.$_ngcc_current_state = 0;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 9: 
      if ((($__uri.equals("")) && ($__local.equals("processContents"))) || (($__uri.equals("")) && ($__local.equals("namespace"))))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 554, null);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 554, null);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      break;
    case 5: 
      if (($__uri.equals("")) && ($__local.equals("processContents")))
      {
        this.$_ngcc_current_state = 7;
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 2: 
    case 3: 
    case 4: 
    case 6: 
    case 7: 
    case 8: 
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
    case 10: 
      this.$_ngcc_current_state = 9;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(makeResult(), this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      this.$_ngcc_current_state = 0;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 6: 
      if (($__uri.equals("")) && ($__local.equals("processContents"))) {
        this.$_ngcc_current_state = 1;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 9: 
      NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 554, null);
      spawnChildFromLeaveAttribute(h, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      if (($__uri.equals("")) && ($__local.equals("namespace"))) {
        this.$_ngcc_current_state = 0;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 5: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 3: 
    case 4: 
    case 7: 
    case 8: 
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
    case 10: 
      this.$_ngcc_current_state = 9;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 0: 
      revertToParentFromText(makeResult(), this._cookie, $value);
      
      break;
    case 1: 
      if (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0)
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
    case 3: 
      this.ns = $value;
      this.$_ngcc_current_state = 2;
      
      break;
    case 9: 
      if (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0)
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 554, null);
        spawnChildFromText(h, $value);
      }
      else if (($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0)
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 554, null);
        spawnChildFromText(h, $value);
      }
      else
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 554, null);
        spawnChildFromText(h, $value);
      }
      break;
    case 7: 
      this.modeValue = $value;
      this.$_ngcc_current_state = 6;
      
      break;
    case 5: 
      if (($ai = this.$runtime.getAttributeIndex("", "processContents")) >= 0)
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
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 556: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 9;
      
      break;
    case 554: 
      this.fa = ((ForeignAttributesImpl)$__result__);
      this.$_ngcc_current_state = 5;
    }
  }
  
  public boolean accepted()
  {
    return (this.$_ngcc_current_state == 5) || (this.$_ngcc_current_state == 1) || (this.$_ngcc_current_state == 0);
  }
  
  private WildcardImpl makeResult()
  {
    if (this.modeValue == null) {
      this.modeValue = "strict";
    }
    int mode = -1;
    if (this.modeValue.equals("strict")) {
      mode = 2;
    }
    if (this.modeValue.equals("lax")) {
      mode = 1;
    }
    if (this.modeValue.equals("skip")) {
      mode = 3;
    }
    if (mode == -1) {
      throw new InternalError();
    }
    if ((this.ns == null) || (this.ns.equals("##any"))) {
      return new WildcardImpl.Any(this.$runtime.document, this.annotation, this.locator, this.fa, mode);
    }
    if (this.ns.equals("##other")) {
      return new WildcardImpl.Other(this.$runtime.document, this.annotation, this.locator, this.fa, this.$runtime.currentSchema.getTargetNamespace(), mode);
    }
    StringTokenizer tokens = new StringTokenizer(this.ns);
    HashSet s = new HashSet();
    while (tokens.hasMoreTokens())
    {
      String ns = tokens.nextToken();
      if (ns.equals("##local")) {
        ns = "";
      }
      if (ns.equals("##targetNamespace")) {
        ns = this.$runtime.currentSchema.getTargetNamespace();
      }
      s.add(ns);
    }
    return new WildcardImpl.Finite(this.$runtime.document, this.annotation, this.locator, this.fa, s, mode);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\state\wildcardBody.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */