package com.sun.xml.xsom.impl.parser.state;

import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroup.Compositor;
import com.sun.xml.xsom.impl.AnnotationImpl;
import com.sun.xml.xsom.impl.ForeignAttributesImpl;
import com.sun.xml.xsom.impl.ModelGroupImpl;
import com.sun.xml.xsom.impl.ParticleImpl;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.parser.AnnotationContext;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class modelGroupBody
  extends NGCCHandler
{
  private AnnotationImpl annotation;
  private String compositorName;
  private Locator locator;
  private ParticleImpl childParticle;
  private ForeignAttributesImpl fa;
  protected final NGCCRuntimeEx $runtime;
  private int $_ngcc_current_state;
  protected String $uri;
  protected String $localName;
  protected String $qname;
  private ModelGroupImpl result;
  
  public final NGCCRuntime getRuntime()
  {
    return this.$runtime;
  }
  
  public modelGroupBody(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie, Locator _locator, String _compositorName)
  {
    super(source, parent, cookie);
    this.$runtime = runtime;
    this.locator = _locator;
    this.compositorName = _compositorName;
    this.$_ngcc_current_state = 6;
  }
  
  public modelGroupBody(NGCCRuntimeEx runtime, Locator _locator, String _compositorName)
  {
    this(null, runtime, runtime, -1, _locator, _compositorName);
  }
  
  private void action0()
    throws SAXException
  {
    XSModelGroup.Compositor compositor = null;
    if (this.compositorName.equals("all")) {
      compositor = XSModelGroup.ALL;
    }
    if (this.compositorName.equals("sequence")) {
      compositor = XSModelGroup.SEQUENCE;
    }
    if (this.compositorName.equals("choice")) {
      compositor = XSModelGroup.CHOICE;
    }
    if (compositor == null) {
      throw new InternalError("unable to process " + this.compositorName);
    }
    this.result = new ModelGroupImpl(this.$runtime.document, this.annotation, this.locator, this.fa, compositor, (ParticleImpl[])this.particles.toArray(new ParticleImpl[0]));
  }
  
  private void action1()
    throws SAXException
  {
    this.particles.add(this.childParticle);
  }
  
  public void enterElement(String $__uri, String $__local, String $__qname, Attributes $attrs)
    throws SAXException
  {
    this.$uri = $__uri;
    this.$localName = $__local;
    this.$qname = $__qname;
    switch (this.$_ngcc_current_state)
    {
    case 2: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))))
      {
        NGCCHandler h = new particle(this, this._source, this.$runtime, 179);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 1;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 6: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))))
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 184, null);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 184, null);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 1: 
      if ((($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("all"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("choice"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("sequence"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("any"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("group"))) || (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("element"))))
      {
        NGCCHandler h = new particle(this, this._source, this.$runtime, 178);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        action0();
        this.$_ngcc_current_state = 0;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 4: 
      if (($__uri.equals("http://www.w3.org/2001/XMLSchema")) && ($__local.equals("annotation")))
      {
        NGCCHandler h = new annotation(this, this._source, this.$runtime, 182, null, AnnotationContext.MODELGROUP);
        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
      }
      else
      {
        this.$_ngcc_current_state = 2;
        this.$runtime.sendEnterElement(this._cookie, $__uri, $__local, $__qname, $attrs);
      }
      break;
    case 0: 
      revertToParentFromEnterElement(this.result, this._cookie, $__uri, $__local, $__qname, $attrs);
      
      break;
    case 3: 
    case 5: 
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
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 6: 
      NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 184, null);
      spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      action0();
      this.$_ngcc_current_state = 0;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveElement(this._cookie, $__uri, $__local, $__qname);
      
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
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 6: 
      NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 184, null);
      spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      action0();
      this.$_ngcc_current_state = 0;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendEnterAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromEnterAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
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
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 6: 
      NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 184, null);
      spawnChildFromLeaveAttribute(h, $__uri, $__local, $__qname);
      
      break;
    case 1: 
      action0();
      this.$_ngcc_current_state = 0;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 4: 
      this.$_ngcc_current_state = 2;
      this.$runtime.sendLeaveAttribute(this._cookie, $__uri, $__local, $__qname);
      
      break;
    case 0: 
      revertToParentFromLeaveAttribute(this.result, this._cookie, $__uri, $__local, $__qname);
      
      break;
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
    case 2: 
      this.$_ngcc_current_state = 1;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 6: 
      NGCCHandler h = new foreignAttributes(this, this._source, this.$runtime, 184, null);
      spawnChildFromText(h, $value);
      
      break;
    case 1: 
      action0();
      this.$_ngcc_current_state = 0;
      this.$runtime.sendText(this._cookie, $value);
      
      break;
    case 4: 
      this.$_ngcc_current_state = 2;
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
    case 179: 
      this.childParticle = ((ParticleImpl)$__result__);
      action1();
      this.$_ngcc_current_state = 1;
      
      break;
    case 184: 
      this.fa = ((ForeignAttributesImpl)$__result__);
      this.$_ngcc_current_state = 4;
      
      break;
    case 178: 
      this.childParticle = ((ParticleImpl)$__result__);
      action1();
      this.$_ngcc_current_state = 1;
      
      break;
    case 182: 
      this.annotation = ((AnnotationImpl)$__result__);
      this.$_ngcc_current_state = 2;
    }
  }
  
  public boolean accepted()
  {
    return (this.$_ngcc_current_state == 0) || (this.$_ngcc_current_state == 4) || (this.$_ngcc_current_state == 1) || (this.$_ngcc_current_state == 2);
  }
  
  private final List particles = new ArrayList();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\state\modelGroupBody.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */