package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.parser.AnnotationContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class includeDecl
  extends NGCCHandler
{
  private String schemaLocation;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public includeDecl(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 7;
  }
  
  public includeDecl(NGCCRuntimeEx runtime)
  {
    this(null, runtime, runtime, -1);
  }
  
  private void action0()
    throws SAXException
  {
    this.$runtime.includeSchema(this.schemaLocation);
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
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("include")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 6;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 6: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "schemaLocation")) >= 0)
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
      revertToParentFromEnterElement(this, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 2: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 569, null, AnnotationContext.SCHEMA);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 1: 
    case 3: 
    case 4: 
    case 5: 
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
    case 6: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "schemaLocation")) >= 0)
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
      revertToParentFromLeaveElement(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("include")))
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
    case 4: 
    case 5: 
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
    case 6: 
      if (($__uri.equals("")) && ($__local.equals("schemaLocation"))) {
        this.$_ngcc_current_state = 5;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
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
    case 4: 
      if (($__uri.equals("")) && ($__local.equals("schemaLocation"))) {
        this.$_ngcc_current_state = 2;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
    case 3: 
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
      this.schemaLocation = $value;
      this.$_ngcc_current_state = 4;
      
      break;
    case 6: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "schemaLocation")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 0: 
      revertToParentFromText(this, this._cookie, $value);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 569: 
      this.$_ngcc_current_state = 1;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\parser\state\includeDecl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */