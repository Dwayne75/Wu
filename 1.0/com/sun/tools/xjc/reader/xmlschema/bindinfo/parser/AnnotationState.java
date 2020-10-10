package com.sun.tools.xjc.reader.xmlschema.bindinfo.parser;

import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.NGCCRuntimeEx;
import com.sun.xml.bind.marshaller.DataWriter;
import java.io.StringWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AnnotationState
  extends NGCCHandler
{
  private BIDeclaration result;
  private String msg;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  public BindInfo bi;
  private StringWriter w;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public AnnotationState(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.$_ngcc_current_state = 20;
  }
  
  public AnnotationState(NGCCRuntimeEx runtime)
  {
    this(null, runtime, runtime, -1);
  }
  
  private void action0()
    throws SAXException
  {
    this.bi.appendDocumentation("<pre>" + this.$runtime.escapeMarkup(this.$runtime.truncateDocComment(this.w.toString())) + "</pre>", false);
    
    this.w = null;
  }
  
  private void action1()
    throws SAXException
  {
    this.w = new StringWriter();
    DataWriter xw = new DataWriter(this.w, "UTF-8");
    xw.setXmlDecl(false);
    this.$runtime.redirectSubtree(xw, this.$uri, this.$localName, this.$qname);
  }
  
  private void action2()
    throws SAXException
  {
    this.bi.appendDocumentation(this.$runtime.truncateDocComment(this.msg), true);
  }
  
  private void action3()
    throws SAXException
  {
    this.$runtime.redirectSubtree(new DefaultHandler(), this.$uri, this.$localName, this.$qname);
  }
  
  private void action4()
    throws SAXException
  {
    this.bi.addDecl(this.result);
  }
  
  private void action5()
    throws SAXException
  {
    this.bi = new BindInfo(this.$runtime.copyLocator());
    this.$runtime.currentBindInfo = this.bi;
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
      if ((($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "typesafeEnumMember")) || (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "dom")) || (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "class")) || (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "typesafeEnumClass")) || (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "property")) || (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "idSymbolSpace")) || (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "globalBindings")) || (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "schemaBindings")) || (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType")))
      {
        NGCCHandler h = new declaration(this, this._source, this.$runtime, 343);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if ((!$__uri.equals("http://java.sun.com/xml/ns/jaxb/xjc")) && (!$__uri.equals("http://java.sun.com/xml/ns/jaxb")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action3();
        this.$_ngcc_current_state = 16;
      }
      else
      {
        this.$_ngcc_current_state = 11;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 2: 
      if (($__uri == "http://www.w3.org/2001/XMLSchema") && ($__local == "appinfo"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 12;
      }
      else if (($__uri == "http://www.w3.org/2001/XMLSchema") && ($__local == "documentation"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 5;
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 11: 
      if ((($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "typesafeEnumMember")) || (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "dom")) || (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "class")) || (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "typesafeEnumClass")) || (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "property")) || (($__uri == "http://java.sun.com/xml/ns/jaxb/xjc") && ($__local == "idSymbolSpace")) || (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "globalBindings")) || (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "schemaBindings")) || (($__uri == "http://java.sun.com/xml/ns/jaxb") && ($__local == "javaType")))
      {
        NGCCHandler h = new declaration(this, this._source, this.$runtime, 340);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else if ((!$__uri.equals("http://java.sun.com/xml/ns/jaxb/xjc")) && (!$__uri.equals("http://java.sun.com/xml/ns/jaxb")))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action3();
        this.$_ngcc_current_state = 16;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 4: 
      this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
      action1();
      this.$_ngcc_current_state = 7;
      
      break;
    case 5: 
      this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
      action1();
      this.$_ngcc_current_state = 7;
      
      break;
    case 20: 
      if (($__uri == "http://www.w3.org/2001/XMLSchema") && ($__local == "annotation"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        action5();
        this.$_ngcc_current_state = 2;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(this.bi, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 1: 
      if (($__uri == "http://www.w3.org/2001/XMLSchema") && ($__local == "appinfo"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 12;
      }
      else if (($__uri == "http://www.w3.org/2001/XMLSchema") && ($__local == "documentation"))
      {
        this.$runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
        this.$_ngcc_current_state = 5;
      }
      else
      {
        unexpectedEnterElement($__qname);
      }
      break;
    case 3: 
    case 6: 
    case 7: 
    case 8: 
    case 9: 
    case 10: 
    case 13: 
    case 14: 
    case 15: 
    case 16: 
    case 17: 
    case 18: 
    case 19: 
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
    case 12: 
      this.$_ngcc_current_state = 11;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 11: 
      if (($__uri == "http://www.w3.org/2001/XMLSchema") && ($__local == "appinfo"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 7: 
      this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
      this.$_ngcc_current_state = 4;
      action0();
      
      break;
    case 16: 
      if ((!$__uri.equals("http://java.sun.com/xml/ns/jaxb/xjc")) && (!$__uri.equals("http://java.sun.com/xml/ns/jaxb")))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 11;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 4: 
      if (($__uri == "http://www.w3.org/2001/XMLSchema") && ($__local == "documentation"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 1;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 5: 
      this.$_ngcc_current_state = 4;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveElement(this.bi, this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      if (($__uri == "http://www.w3.org/2001/XMLSchema") && ($__local == "annotation"))
      {
        this.$runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
        this.$_ngcc_current_state = 0;
      }
      else
      {
        unexpectedLeaveElement($__qname);
      }
      break;
    case 3: 
    case 6: 
    case 8: 
    case 9: 
    case 10: 
    case 13: 
    case 14: 
    case 15: 
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
    case 12: 
      this.$_ngcc_current_state = 11;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 5: 
      this.$_ngcc_current_state = 4;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromEnterAttribute(this.bi, this._cookie, $__uri, $__local, $__qname);
      
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
    case 12: 
      this.$_ngcc_current_state = 11;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 5: 
      this.$_ngcc_current_state = 4;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this.bi, this._cookie, $__uri, $__local, $__qname);
      
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
    case 12: 
      this.$_ngcc_current_state = 11;
      
      break;
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 11: 
      this.$_ngcc_current_state = 11;
      
      break;
    case 4: 
      this.msg = $value;
      this.$_ngcc_current_state = 4;
      action2();
      
      break;
    case 5: 
      this.msg = $value;
      this.$_ngcc_current_state = 4;
      action2();
      
      break;
    case 0: 
      revertToParentFromText(this.bi, this._cookie, $value);
    }
  }
  
  public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)
    throws SAXException
  {
    switch ($__cookie__)
    {
    case 340: 
      this.result = ((BIDeclaration)$__result__);
      action4();
      this.$_ngcc_current_state = 11;
      
      break;
    case 343: 
      this.result = ((BIDeclaration)$__result__);
      action4();
      this.$_ngcc_current_state = 11;
    }
  }
  
  public boolean accepted()
  {
    return this.$_ngcc_current_state == 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\parser\AnnotationState.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */