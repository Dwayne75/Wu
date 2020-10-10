package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.impl.AttGroupDeclImpl;
import com.sun.xml.xsom.impl.ComplexTypeImpl;
import com.sun.xml.xsom.impl.ModelGroupDeclImpl;
import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.impl.SimpleTypeImpl;
import com.sun.xml.xsom.impl.parser.Messages;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.parser.AnnotationContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class redefine
  extends NGCCHandler
{
  private String schemaLocation;
  private ModelGroupDeclImpl newGrp;
  private AttGroupDeclImpl newAg;
  private SimpleTypeImpl newSt;
  private ComplexTypeImpl newCt;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public redefine(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 15;
  }
  
  public redefine(NGCCRuntimeEx runtime)
  {
    this(null, runtime, runtime, -1);
  }
  
  private void action0()
    throws SAXException
  {
    XSAttGroupDecl oldAg = this.$runtime.currentSchema.getAttGroupDecl(this.newAg.getName());
    if (oldAg == null)
    {
      this.$runtime.reportError(Messages.format("UndefinedAttributeGroup", new Object[] { this.newAg.getName() }));
    }
    else
    {
      this.newAg.redefine((AttGroupDeclImpl)oldAg);
      this.$runtime.currentSchema.addAttGroupDecl(this.newAg, true);
    }
  }
  
  private void action1()
    throws SAXException
  {
    XSModelGroupDecl oldGrp = this.$runtime.currentSchema.getModelGroupDecl(this.newGrp.getName());
    if (oldGrp == null)
    {
      this.$runtime.reportError(Messages.format("UndefinedModelGroup", new Object[] { this.newGrp.getName() }));
    }
    else
    {
      this.newGrp.redefine((ModelGroupDeclImpl)oldGrp);
      this.$runtime.currentSchema.addModelGroupDecl(this.newGrp, true);
    }
  }
  
  private void action2()
    throws SAXException
  {
    XSComplexType oldCt = this.$runtime.currentSchema.getComplexType(this.newCt.getName());
    if (oldCt == null)
    {
      this.$runtime.reportError(Messages.format("UndefinedCompplexType", new Object[] { this.newCt.getName() }));
    }
    else
    {
      this.newCt.redefine((ComplexTypeImpl)oldCt);
      this.$runtime.currentSchema.addComplexType(this.newCt, true);
    }
  }
  
  private void action3()
    throws SAXException
  {
    XSSimpleType oldSt = this.$runtime.currentSchema.getSimpleType(this.newSt.getName());
    if (oldSt == null)
    {
      this.$runtime.reportError(Messages.format("UndefinedSimpleType", new Object[] { this.newSt.getName() }));
    }
    else
    {
      this.newSt.redefine((SimpleTypeImpl)oldSt);
      this.$runtime.currentSchema.addSimpleType(this.newSt, true);
    }
  }
  
  private void action4()
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
    case 1: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 369, null, AnnotationContext.SCHEMA);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType")))
      {
        NGCCHandler h = new simpleType(this, this._source, this.$runtime, 370);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType")))
      {
        NGCCHandler h = new complexType(this, this._source, this.$runtime, 371);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group")))
      {
        NGCCHandler h = new group(this, this._source, this.$runtime, 372);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup")))
      {
        NGCCHandler h = new attributeGroupDecl(this, this._source, this.$runtime, 373);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 2: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 374, null, AnnotationContext.SCHEMA);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType")))
      {
        NGCCHandler h = new simpleType(this, this._source, this.$runtime, 375);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("complexType")))
      {
        NGCCHandler h = new complexType(this, this._source, this.$runtime, 376);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group")))
      {
        NGCCHandler h = new group(this, this._source, this.$runtime, 377);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("attributeGroup")))
      {
        NGCCHandler h = new attributeGroupDecl(this, this._source, this.$runtime, 378);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 15: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("redefine")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 14;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 14: 
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
    case 1: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("redefine")))
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
    case 14: 
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
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 14: 
      if (($__uri.equals("")) && ($__local.equals("schemaLocation"))) {
        this.$_ngcc_current_state = 13;
      } else {
        unexpectedEnterAttribute($__qname);
      }
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
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 12: 
      if (($__uri.equals("")) && ($__local.equals("schemaLocation"))) {
        this.$_ngcc_current_state = 2;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
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
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 14: 
      int $ai;
      if (($ai = this.$runtime.getAttributeIndex("", "schemaLocation")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 13: 
      this.schemaLocation = $value;
      this.$_ngcc_current_state = 12;
      action4();
      
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
    case 374: 
      this.$_ngcc_current_state = 1;
      
      break;
    case 375: 
      this.newSt = ((SimpleTypeImpl)$__result__);
      action3();
      this.$_ngcc_current_state = 1;
      
      break;
    case 376: 
      this.newCt = ((ComplexTypeImpl)$__result__);
      action2();
      this.$_ngcc_current_state = 1;
      
      break;
    case 377: 
      this.newGrp = ((ModelGroupDeclImpl)$__result__);
      action1();
      this.$_ngcc_current_state = 1;
      
      break;
    case 378: 
      this.newAg = ((AttGroupDeclImpl)$__result__);
      action0();
      this.$_ngcc_current_state = 1;
      
      break;
    case 369: 
      this.$_ngcc_current_state = 1;
      
      break;
    case 370: 
      this.newSt = ((SimpleTypeImpl)$__result__);
      action3();
      this.$_ngcc_current_state = 1;
      
      break;
    case 371: 
      this.newCt = ((ComplexTypeImpl)$__result__);
      action2();
      this.$_ngcc_current_state = 1;
      
      break;
    case 372: 
      this.newGrp = ((ModelGroupDeclImpl)$__result__);
      action1();
      this.$_ngcc_current_state = 1;
      
      break;
    case 373: 
      this.newAg = ((AttGroupDeclImpl)$__result__);
      action0();
      this.$_ngcc_current_state = 1;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\state\redefine.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */