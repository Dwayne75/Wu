package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.parser.AnnotationContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class identityConstraint
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
  
  public identityConstraint(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 12;
  }
  
  public identityConstraint(NGCCRuntimeEx runtime)
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
    case 12: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 10;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 9: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("selector")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 7;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 7: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 584, null, AnnotationContext.IDENTITY_CONSTRAINT);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 6;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 1: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("field")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 3;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 10: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 588, null, AnnotationContext.IDENTITY_CONSTRAINT);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 9;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 3: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 578, null, AnnotationContext.IDENTITY_CONSTRAINT);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(this, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 5: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("field")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 3;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 2: 
    case 4: 
    case 6: 
    case 8: 
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
    case 6: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("selector")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 5;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 7: 
      this.$_ngcc_current_state = 6;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("key"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("keyref"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("unique"))))
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
      this.$_ngcc_current_state = 9;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 3: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveElement(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("field")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 4: 
    case 5: 
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
    case 7: 
      this.$_ngcc_current_state = 6;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 10: 
      this.$_ngcc_current_state = 9;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 3: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
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
    case 7: 
      this.$_ngcc_current_state = 6;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 10: 
      this.$_ngcc_current_state = 9;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 3: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
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
    case 7: 
      this.$_ngcc_current_state = 6;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 10: 
      this.$_ngcc_current_state = 9;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 3: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 0: 
      revertToParentFromText(this, this._cookie, $value);
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 584: 
      this.$_ngcc_current_state = 6;
      
      break;
    case 578: 
      this.$_ngcc_current_state = 2;
      
      break;
    case 588: 
      this.$_ngcc_current_state = 9;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\parser\state\identityConstraint.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */