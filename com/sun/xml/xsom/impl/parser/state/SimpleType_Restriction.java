package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.impl.AnnotationImpl;
import com.sun.xml.xsom.impl.ForeignAttributesImpl;
import com.sun.xml.xsom.impl.Ref.SimpleType;
import com.sun.xml.xsom.impl.RestrictionSimpleTypeImpl;
import com.sun.xml.xsom.impl.SimpleTypeImpl;
import com.sun.xml.xsom.impl.UName;
import com.sun.xml.xsom.impl.parser.DelayedRef.SimpleType;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.parser.AnnotationContext;
import java.util.Set;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class SimpleType_Restriction
  extends NGCCHandler
{
  private Locator locator;
  private AnnotationImpl annotation;
  private String name;
  private UName baseTypeName;
  private Set finalSet;
  private ForeignAttributesImpl fa;
  private XSFacet facet;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private RestrictionSimpleTypeImpl result;
  private Ref.SimpleType baseType;
  private Locator rloc;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public SimpleType_Restriction(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie, AnnotationImpl _annotation, Locator _locator, ForeignAttributesImpl _fa, String _name, Set _finalSet)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.annotation = _annotation;
    this.locator = _locator;
    this.fa = _fa;
    this.name = _name;
    this.finalSet = _finalSet;
    this.$_ngcc_current_state = 13;
  }
  
  public SimpleType_Restriction(NGCCRuntimeEx runtime, AnnotationImpl _annotation, Locator _locator, ForeignAttributesImpl _fa, String _name, Set _finalSet)
  {
    this(null, runtime, runtime, -1, _annotation, _locator, _fa, _name, _finalSet);
  }
  
  private void action0()
    throws SAXException
  {
    this.result.addFacet(this.facet);
  }
  
  private void action1()
    throws SAXException
  {
    this.result = new RestrictionSimpleTypeImpl(this.$runtime.document, this.annotation, this.locator, this.fa, this.name, this.name == null, this.finalSet, this.baseType);
  }
  
  private void action2()
    throws SAXException
  {
    this.baseType = new DelayedRef.SimpleType(this.$runtime, this.rloc, this.$runtime.currentSchema, this.baseTypeName);
  }
  
  private void action3()
    throws SAXException
  {
    this.rloc = this.$runtime.copyLocator();
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
    case 1: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("totalDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("fractionDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("length"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("enumeration"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("whiteSpace"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("pattern"))))
      {
        NGCCHandler h = new facet(this, this._source, this.$runtime, 529);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 13: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action3();
        this.$_ngcc_current_state = 12;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 10: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 540, this.annotation, AnnotationContext.SIMPLETYPE_DECL);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 5;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 5: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      else if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType")))
      {
        NGCCHandler h = new simpleType(this, this._source, this.$runtime, 534);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 12: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || ((($ai = this.$runtime.getAttributeIndex("", "base")) >= 0) && ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("totalDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("fractionDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("length"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("enumeration"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("whiteSpace"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("pattern"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("simpleType"))))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 542, this.fa);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        int $ai;
        unexpectedEnterElement($__qname);
      }
      break;
    case 4: 
      action1();
      this.$_ngcc_current_state = 2;
      this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 2: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxExclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxInclusive"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("totalDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("fractionDigits"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("length"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("maxLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("minLength"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("enumeration"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("whiteSpace"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("pattern"))))
      {
        NGCCHandler h = new facet(this, this._source, this.$runtime, 530);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(this.result, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 3: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
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
    int $ai;
    switch (this.$_ngcc_current_state)
    {
    case 1: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
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
      this.$_ngcc_current_state = 5;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 5: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 12: 
      if ((($ai = this.$runtime.getAttributeIndex("", "base")) >= 0) && ($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("restriction")))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 542, this.fa);
        spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 4: 
      action1();
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveElement(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 3: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 11: 
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
      this.$_ngcc_current_state = 5;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 5: 
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 8;
      } else {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 12: 
      if (($__uri.equals("")) && ($__local.equals("base")))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 542, this.fa);
        spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      }
      else
      {
        unexpectedEnterAttribute($__qname);
      }
      break;
    case 4: 
      action1();
      this.$_ngcc_current_state = 2;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromEnterAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
    case 3: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 11: 
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
      if (($__uri.equals("")) && ($__local.equals("base"))) {
        this.$_ngcc_current_state = 4;
      } else {
        unexpectedLeaveAttribute($__qname);
      }
      break;
    case 10: 
      this.$_ngcc_current_state = 5;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      action1();
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
    case 3: 
    case 5: 
    case 6: 
    case 8: 
    case 9: 
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
    case 8: 
      NGCCHandler h = new qname(this, this._source, this.$runtime, 536);
      spawnChildFromText(h, $value);
      
      break;
    case 10: 
      this.$_ngcc_current_state = 5;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 5: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        this.$runtime.consumeAttribute($ai);
        this.$runtime.sendText(this._cookie, $value);
      }
      break;
    case 12: 
      if (($ai = this.$runtime.getAttributeIndex("", "base")) >= 0)
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 542, this.fa);
        spawnChildFromText(h, $value);
      }
      break;
    case 4: 
      action1();
      this.$_ngcc_current_state = 2;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
      
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
    case 529: 
      this.facet = ((XSFacet)$__result__);
      action0();
      this.$_ngcc_current_state = 1;
      
      break;
    case 530: 
      this.facet = ((XSFacet)$__result__);
      action0();
      this.$_ngcc_current_state = 1;
      
      break;
    case 536: 
      this.baseTypeName = ((UName)$__result__);
      action2();
      this.$_ngcc_current_state = 7;
      
      break;
    case 540: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 5;
      
      break;
    case 534: 
      this.baseType = ((SimpleTypeImpl)$__result__);
      this.$_ngcc_current_state = 4;
      
      break;
    case 542: 
      this.fa = ((ForeignAttributesImpl)$__result__);
      this.$_ngcc_current_state = 10;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\state\SimpleType_Restriction.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */