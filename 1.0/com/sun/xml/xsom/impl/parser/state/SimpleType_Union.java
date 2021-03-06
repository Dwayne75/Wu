package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.impl.AnnotationImpl;
import com.sun.xml.xsom.impl.Ref.SimpleType;
import com.sun.xml.xsom.impl.SimpleTypeImpl;
import com.sun.xml.xsom.impl.UName;
import com.sun.xml.xsom.impl.UnionSimpleTypeImpl;
import com.sun.xml.xsom.impl.parser.DelayedRef.SimpleType;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.parser.AnnotationContext;
import java.util.Vector;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class SimpleType_Union
  extends NGCCHandler
{
  private Locator locator;
  private AnnotationImpl annotation;
  private String __text;
  private UName memberTypeName;
  private String name;
  private SimpleTypeImpl anonymousMemberType;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private UnionSimpleTypeImpl result;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public SimpleType_Union(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie, AnnotationImpl _annotation, Locator _locator, String _name)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.annotation = _annotation;
    this.locator = _locator;
    this.name = _name;
    this.$_ngcc_current_state = 11;
  }
  
  public SimpleType_Union(NGCCRuntimeEx runtime, AnnotationImpl _annotation, Locator _locator, String _name)
  {
    this(null, runtime, runtime, -1, _annotation, _locator, _name);
  }
  
  private void action0()
    throws SAXException
  {
    this.result = new UnionSimpleTypeImpl(this.$runtime.currentSchema, this.annotation, this.locator, this.name, this.name == null, (Ref.SimpleType[])this.members.toArray(new Ref.SimpleType[this.members.size()]));
  }
  
  private void action1()
    throws SAXException
  {
    this.members.add(this.anonymousMemberType);
  }
  
  private void action2()
    throws SAXException
  {
    this.members.add(new DelayedRef.SimpleType(this.$runtime, this.uloc, this.$runtime.currentSchema, this.memberTypeName));
  }
  
  private void action3()
    throws SAXException
  {
    this.$runtime.processList(this.__text);
  }
  
  private void action4()
    throws SAXException
  {
    this.uloc = this.$runtime.copyLocator();
  }
  
  public void enterElement(String $__uri, String $__local, String $__qname, Attributes $attrs)
    throws SAXException
  {
    this.$uri = $__uri;
    this.$localName = $__local;
    this.$qname = $__qname;
    switch (this.$_ngcc_current_state)
    {
    case 4: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 658, this.annotation, AnnotationContext.SIMPLETYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 2: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType")))
      {
        NGCCHandler h = new simpleType(this, this._source, this.$runtime, 655);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 1: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType")))
      {
        NGCCHandler h = new simpleType(this, this._source, this.$runtime, 654);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 11: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("union")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action4();
        this.$_ngcc_current_state = 6;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 6: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "memberTypes")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 4;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(this.result, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 3: 
    case 5: 
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
    case 4: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("union")))
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
    case 6: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "memberTypes")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        this.$_ngcc_current_state = 4;
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromLeaveElement(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 3: 
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
    case 4: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 6: 
      if (($__uri.equals("")) && ($__local.equals("memberTypes")))
      {
        this.$_ngcc_current_state = 9;
      }
      else
      {
        this.$_ngcc_current_state = 4;
        this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
    case 3: 
    case 5: 
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
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 6: 
      this.$_ngcc_current_state = 4;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 7: 
      if (($__uri.equals("")) && ($__local.equals("memberTypes"))) {
        this.$_ngcc_current_state = 4;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 1: 
    case 3: 
    case 5: 
    default: 
      unexpectedLeaveAttribute($__qname);
    }
  }
  
  public void text(String $value)
    throws SAXException
  {
    switch (this.$_ngcc_current_state)
    {
    case 4: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 9: 
      this.__text = $value;
      this.$_ngcc_current_state = 8;
      action3();
      
      break;
    case 8: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 661);
      spawnChildFromText(h, $value);
      
      break;
    case 6: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "memberTypes")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      else
      {
        this.$_ngcc_current_state = 4;
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 0: 
      revertToParentFromText(this.result, this._cookie, $value);
      
      break;
    case 7: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 662);
      spawnChildFromText(h, $value);
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 658: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 2;
      
      break;
    case 655: 
      this.anonymousMemberType = ((SimpleTypeImpl)$__result__);
      action1();
      this.$_ngcc_current_state = 1;
      
      break;
    case 661: 
      this.memberTypeName = ((UName)$__result__);
      action2();
      this.$_ngcc_current_state = 7;
      
      break;
    case 654: 
      this.anonymousMemberType = ((SimpleTypeImpl)$__result__);
      action1();
      this.$_ngcc_current_state = 1;
      
      break;
    case 662: 
      this.memberTypeName = ((UName)$__result__);
      action2();
      this.$_ngcc_current_state = 7;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
  
  private final Vector members = new Vector();
  private Locator uloc;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\parser\state\SimpleType_Union.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */