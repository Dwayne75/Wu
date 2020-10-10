package com.sun.tools.jxc.gen.config;

import com.sun.tools.jxc.NGCCRuntimeEx;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Classes
  extends NGCCHandler
{
  private String __text;
  private String exclude_content;
  private String include_content;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public Classes(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 12;
  }
  
  public Classes(NGCCRuntimeEx runtime)
  {
    this(null, runtime, runtime, -1);
  }
  
  private void action0()
    throws SAXException
  {
    this.excludes.add(this.exclude_content);
  }
  
  private void action1()
    throws SAXException
  {
    this.$runtime.processList(this.__text);
  }
  
  private void action2()
    throws SAXException
  {
    this.includes.add(this.include_content);
  }
  
  private void action3()
    throws SAXException
  {
    this.$runtime.processList(this.__text);
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
      revertToParentFromEnterElement(this, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 2: 
      if (($__uri == "") && ($__local == "excludes"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 6;
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 11: 
      if (($__uri == "") && ($__local == "includes"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 10;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 4: 
      this.$_ngcc_current_state = 3;
      this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 12: 
      if (($__uri == "") && ($__local == "classes"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 11;
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
    case 10: 
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
      revertToParentFromLeaveElement(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 8: 
      if (($__uri == "") && ($__local == "includes"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 2;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 3: 
      if (($__uri == "") && ($__local == "excludes"))
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
      this.$_ngcc_current_state = 3;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      if (($__uri == "") && ($__local == "classes"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 5: 
    case 6: 
    case 7: 
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
      revertToParentFromEnterAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      this.$_ngcc_current_state = 3;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
    case 3: 
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
      revertToParentFromLeaveAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      this.$_ngcc_current_state = 3;
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
    case 0: 
      revertToParentFromText(this, this._cookie, $value);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 8: 
      this.include_content = $value;
      this.$_ngcc_current_state = 8;
      action2();
      
      break;
    case 6: 
      this.__text = $value;
      this.$_ngcc_current_state = 4;
      action1();
      
      break;
    case 3: 
      this.exclude_content = $value;
      this.$_ngcc_current_state = 3;
      action0();
      
      break;
    case 10: 
      this.__text = $value;
      this.$_ngcc_current_state = 9;
      action3();
      
      break;
    case 9: 
      this.include_content = $value;
      this.$_ngcc_current_state = 8;
      action2();
      
      break;
    case 4: 
      this.exclude_content = $value;
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
  
  private List includes = new ArrayList();
  
  public List getIncludes()
  {
    return this.$runtime.getIncludePatterns(this.includes);
  }
  
  private List excludes = new ArrayList();
  
  public List getExcludes()
  {
    return this.$runtime.getExcludePatterns(this.excludes);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\gen\config\Classes.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */