package com.sun.tools.jxc.gen.config;

import com.sun.tools.jxc.NGCCRuntimeEx;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Config
  extends NGCCHandler
{
  private String bd;
  private Schema _schema;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private File baseDir;
  private Classes classes;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public Config(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 8;
  }
  
  public Config(NGCCRuntimeEx runtime)
  {
    this(null, runtime, runtime, -1);
  }
  
  private void action0()
    throws SAXException
  {
    this.schema.add(this._schema);
  }
  
  private void action1()
    throws SAXException
  {
    this.baseDir = this.$runtime.getBaseDir(this.bd);
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
    case 1: 
      if (($__uri == "") && ($__local == "schema"))
      {
        NGCCHandler h = new Schema(this, this._source, this.$runtime, 19, this.baseDir);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 2: 
      if (($__uri == "") && ($__local == "schema"))
      {
        NGCCHandler h = new Schema(this, this._source, this.$runtime, 20, this.baseDir);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 8: 
      if (($__uri == "") && ($__local == "config"))
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
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "baseDir")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 4: 
      if (($__uri == "") && ($__local == "classes"))
      {
        NGCCHandler h = new Classes(this, this._source, this.$runtime, 22);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 3: 
    case 5: 
    case 6: 
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
    case 1: 
      if (($__uri == "") && ($__local == "config"))
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
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 7: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "baseDir")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 3: 
    case 4: 
    case 5: 
    case 6: 
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
    case 7: 
      if (($__uri == "") && ($__local == "baseDir")) {
        this.$_ngcc_current_state = 6;
      } else {
        unexpectedEnterAttribute($__qname);
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
    case 0: 
      revertToParentFromLeaveAttribute(this, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 5: 
      if (($__uri == "") && ($__local == "baseDir")) {
        this.$_ngcc_current_state = 4;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
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
      revertToParentFromText(this, this._cookie, $value);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 7: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "baseDir")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 6: 
      this.bd = $value;
      this.$_ngcc_current_state = 5;
      action1();
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 19: 
      this._schema = ((Schema)$__result__);
      action0();
      this.$_ngcc_current_state = 1;
      
      break;
    case 20: 
      this._schema = ((Schema)$__result__);
      action0();
      this.$_ngcc_current_state = 1;
      
      break;
    case 22: 
      this.classes = ((Classes)$__result__);
      this.$_ngcc_current_state = 2;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
  
  private List schema = new ArrayList();
  
  public Classes getClasses()
  {
    return this.classes;
  }
  
  public File getBaseDir()
  {
    return this.baseDir;
  }
  
  public List getSchema()
  {
    return this.schema;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\gen\config\Config.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */