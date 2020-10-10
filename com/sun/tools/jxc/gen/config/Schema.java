package com.sun.tools.jxc.gen.config;

import com.sun.tools.jxc.NGCCRuntimeEx;
import java.io.File;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Schema
  extends NGCCHandler
{
  private File baseDir;
  private String loc;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private File location;
  private String namespace;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public Schema(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie, File _baseDir)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.baseDir = _baseDir;
    this.$_ngcc_current_state = 10;
  }
  
  public Schema(NGCCRuntimeEx runtime, File _baseDir)
  {
    this(null, runtime, runtime, -1, _baseDir);
  }
  
  private void action0()
    throws SAXException
  {
    this.location = new File(this.baseDir, this.loc);
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
      if (($__uri == "") && ($__local == "schema"))
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
      if (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0)
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
    case 0: 
      revertToParentFromEnterElement(this, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 2: 
      if (($ai = this.$runtime.getAttributeIndex("", "location")) >= 0)
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
    case 6: 
      if (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0)
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
    case 0: 
      revertToParentFromLeaveElement(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      if (($__uri == "") && ($__local == "schema"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 2: 
      if (($ai = this.$runtime.getAttributeIndex("", "location")) >= 0)
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
      if (($__uri == "") && ($__local == "namespace"))
      {
        this.$_ngcc_current_state = 8;
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      if (($__uri == "") && ($__local == "location"))
      {
        this.$_ngcc_current_state = 4;
      }
      else
      {
        this.$_ngcc_current_state = 1;
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
    case 6: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 7: 
      if (($__uri == "") && ($__local == "namespace")) {
        this.$_ngcc_current_state = 2;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 3: 
      if (($__uri == "") && ($__local == "location")) {
        this.$_ngcc_current_state = 1;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 1: 
    case 4: 
    case 5: 
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
    case 6: 
      if (($ai = this.$runtime.getAttributeIndex("", "namespace")) >= 0)
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
    case 0: 
      revertToParentFromText(this, this._cookie, $value);
      
      break;
    case 8: 
      this.namespace = $value;
      this.$_ngcc_current_state = 7;
      
      break;
    case 2: 
      if (($ai = this.$runtime.getAttributeIndex("", "location")) >= 0)
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
    case 4: 
      this.loc = $value;
      this.$_ngcc_current_state = 3;
      action0();
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {}
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
  
  public String getNamespace()
  {
    return this.namespace;
  }
  
  public File getLocation()
  {
    return this.location;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\gen\config\Schema.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */