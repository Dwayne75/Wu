package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.impl.AttributesHolder;
import com.sun.xml.xsom.impl.ContentTypeImpl;
import com.sun.xml.xsom.impl.ParticleImpl;
import com.sun.xml.xsom.impl.SchemaSetImpl;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.impl.parser.ParserContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class complexType_complexContent_body
  extends NGCCHandler
{
  private AttributesHolder owner;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private ContentTypeImpl particle;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public complexType_complexContent_body(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie, AttributesHolder _owner)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.owner = _owner;
    this.$_ngcc_current_state = 2;
  }
  
  public complexType_complexContent_body(NGCCRuntimeEx runtime, AttributesHolder _owner)
  {
    this(null, runtime, runtime, -1, _owner);
  }
  
  private void action0()
    throws SAXException
  {
    if (this.particle == null) {
      this.particle = this.$runtime.parser.schemaSet.empty;
    }
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
      revertToParentFromEnterElement(this.particle, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 1: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("anyAttribute"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attribute"))))
      {
        NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 515, this.owner);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 515, this.owner);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 2: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))))
      {
        NGCCHandler h = new particle(this, this._source, this.$runtime, 517);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
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
    switch (this.$_ngcc_current_state)
    {
    case 0: 
      revertToParentFromLeaveElement(this.particle, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 515, this.owner);
      spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
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
    case 0: 
      revertToParentFromEnterAttribute(this.particle, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 515, this.owner);
      spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      
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
    case 0: 
      revertToParentFromLeaveAttribute(this.particle, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 515, this.owner);
      spawnChildFromLeaveAttribute(h, $__uri, $__local, $__qname);
      
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
      revertToParentFromText(this.particle, this._cookie, $value);
      
      break;
    case 1: 
      NGCCHandler h = new attributeUses(this, this._source, this.$runtime, 515, this.owner);
      spawnChildFromText(h, $value);
      
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
    case 515: 
      action0();
      this.$_ngcc_current_state = 0;
      
      break;
    case 517: 
      this.particle = ((ParticleImpl)$__result__);
      this.$_ngcc_current_state = 1;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\state\complexType_complexContent_body.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */