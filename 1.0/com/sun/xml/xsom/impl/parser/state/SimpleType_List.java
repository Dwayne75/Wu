package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.impl.AnnotationImpl;
import com.sun.xml.xsom.impl.ListSimpleTypeImpl;
import com.sun.xml.xsom.impl.Ref.SimpleType;
import com.sun.xml.xsom.impl.SimpleTypeImpl;
import com.sun.xml.xsom.impl.UName;
import com.sun.xml.xsom.impl.parser.DelayedRef.SimpleType;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.parser.AnnotationContext;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class SimpleType_List
  extends NGCCHandler
{
  private Locator locator;
  private AnnotationImpl annotation;
  private String name;
  private UName itemTypeName;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private ListSimpleTypeImpl result;
  private Ref.SimpleType itemType;
  private Locator lloc;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public SimpleType_List(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie, AnnotationImpl _annotation, Locator _locator, String _name)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.annotation = _annotation;
    this.locator = _locator;
    this.name = _name;
    this.$_ngcc_current_state = 9;
  }
  
  public SimpleType_List(NGCCRuntimeEx runtime, AnnotationImpl _annotation, Locator _locator, String _name)
  {
    this(null, runtime, runtime, -1, _annotation, _locator, _name);
  }
  
  private void action0()
    throws SAXException
  {
    this.result = new ListSimpleTypeImpl(this.$runtime.currentSchema, this.annotation, this.locator, this.name, this.name == null, this.itemType);
  }
  
  private void action1()
    throws SAXException
  {
    this.itemType = new DelayedRef.SimpleType(this.$runtime, this.lloc, this.$runtime.currentSchema, this.itemTypeName);
  }
  
  private void action2()
    throws SAXException
  {
    this.lloc = this.$runtime.copyLocator();
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
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 648, this.annotation, AnnotationContext.SIMPLETYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 9: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("list")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action2();
        this.$_ngcc_current_state = 7;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 2: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "itemType")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType")))
      {
        NGCCHandler h = new simpleType(this, this._source, this.$runtime, 642);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(this.result, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 1: 
    case 3: 
    case 4: 
    case 5: 
    case 6: 
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
    switch (this.$_ngcc_current_state)
    {
    case 7: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "itemType")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 1: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("list")))
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
    case 0: 
      revertToParentFromLeaveElement(this.result, this._cookie, $__uri, $__local, $__qname);
      
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
    case 7: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      if (($__uri.equals("")) && ($__local.equals("itemType"))) {
        this.$_ngcc_current_state = 5;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
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
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      if (($__uri.equals("")) && ($__local.equals("itemType"))) {
        this.$_ngcc_current_state = 1;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
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
      this.$_ngcc_current_state = 2;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 2: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "itemType")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 5: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 644);
      spawnChildFromText(h, $value);
      
      break;
    case 0: 
      revertToParentFromText(this.result, this._cookie, $value);
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 648: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 2;
      
      break;
    case 642: 
      this.itemType = ((SimpleTypeImpl)$__result__);
      this.$_ngcc_current_state = 1;
      
      break;
    case 644: 
      this.itemTypeName = ((UName)$__result__);
      action1();
      this.$_ngcc_current_state = 4;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\parser\state\SimpleType_List.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */