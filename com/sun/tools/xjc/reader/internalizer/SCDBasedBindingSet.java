package com.sun.tools.xjc.reader.internalizer;

import com.sun.istack.NotNull;
import com.sun.istack.SAXParseException2;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.SchemaCache;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.util.DOMUtils;
import com.sun.tools.xjc.util.ForkContentHandler;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import com.sun.xml.xsom.SCD;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSSchemaSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.validation.ValidatorHandler;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class SCDBasedBindingSet
{
  private Target topLevel;
  private final DOMForest forest;
  private ErrorReceiver errorReceiver;
  private UnmarshallerHandler unmarshaller;
  private ForkContentHandler loader;
  
  final class Target
  {
    private Target firstChild;
    private final Target nextSibling;
    @NotNull
    private final SCD scd;
    @NotNull
    private final Element src;
    private final List<Element> bindings = new ArrayList();
    
    private Target(Target parent, Element src, SCD scd)
    {
      if (parent == null)
      {
        this.nextSibling = SCDBasedBindingSet.this.topLevel;
        SCDBasedBindingSet.this.topLevel = this;
      }
      else
      {
        this.nextSibling = parent.firstChild;
        parent.firstChild = this;
      }
      this.src = src;
      this.scd = scd;
    }
    
    void addBinidng(Element binding)
    {
      this.bindings.add(binding);
    }
    
    private void applyAll(Collection<? extends XSComponent> contextNode)
    {
      for (Target self = this; self != null; self = self.nextSibling) {
        self.apply(contextNode);
      }
    }
    
    private void apply(Collection<? extends XSComponent> contextNode)
    {
      Collection<XSComponent> childNodes = this.scd.select(contextNode);
      if (childNodes.isEmpty())
      {
        if (this.src.getAttributeNode("if-exists") != null) {
          return;
        }
        SCDBasedBindingSet.this.reportError(this.src, Messages.format("ERR_SCD_EVALUATED_EMPTY", new Object[] { this.scd }));
        return;
      }
      if (this.firstChild != null) {
        this.firstChild.applyAll(childNodes);
      }
      XSComponent target;
      if (!this.bindings.isEmpty())
      {
        Iterator<XSComponent> itr = childNodes.iterator();
        target = (XSComponent)itr.next();
        if (itr.hasNext())
        {
          SCDBasedBindingSet.this.reportError(this.src, Messages.format("ERR_SCD_MATCHED_MULTIPLE_NODES", new Object[] { this.scd, Integer.valueOf(childNodes.size()) }));
          SCDBasedBindingSet.this.errorReceiver.error(target.getLocator(), Messages.format("ERR_SCD_MATCHED_MULTIPLE_NODES_FIRST", new Object[0]));
          SCDBasedBindingSet.this.errorReceiver.error(((XSComponent)itr.next()).getLocator(), Messages.format("ERR_SCD_MATCHED_MULTIPLE_NODES_SECOND", new Object[0]));
        }
        for (Element binding : this.bindings) {
          for (Element item : DOMUtils.getChildElements(binding))
          {
            String localName = item.getLocalName();
            if (!"bindings".equals(localName)) {
              try
              {
                new DOMForestScanner(SCDBasedBindingSet.this.forest).scan(item, SCDBasedBindingSet.this.loader);
                BIDeclaration decl = (BIDeclaration)SCDBasedBindingSet.this.unmarshaller.getResult();
                
                XSAnnotation ann = target.getAnnotation(true);
                BindInfo bi = (BindInfo)ann.getAnnotation();
                if (bi == null)
                {
                  bi = new BindInfo();
                  ann.setAnnotation(bi);
                }
                bi.addDecl(decl);
              }
              catch (SAXException e) {}catch (JAXBException e)
              {
                throw new AssertionError(e);
              }
            }
          }
        }
      }
    }
  }
  
  SCDBasedBindingSet(DOMForest forest)
  {
    this.forest = forest;
  }
  
  Target createNewTarget(Target parent, Element src, SCD scd)
  {
    return new Target(parent, src, scd, null);
  }
  
  public void apply(XSSchemaSet schema, ErrorReceiver errorReceiver)
  {
    if (this.topLevel != null)
    {
      this.errorReceiver = errorReceiver;
      UnmarshallerImpl u = BindInfo.getJAXBContext().createUnmarshaller();
      this.unmarshaller = u.getUnmarshallerHandler();
      ValidatorHandler v = BindInfo.bindingFileSchema.newValidator();
      v.setErrorHandler(errorReceiver);
      this.loader = new ForkContentHandler(v, this.unmarshaller);
      
      this.topLevel.applyAll(schema.getSchemas());
      
      this.loader = null;
      this.unmarshaller = null;
      this.errorReceiver = null;
    }
  }
  
  private void reportError(Element errorSource, String formattedMsg)
  {
    reportError(errorSource, formattedMsg, null);
  }
  
  private void reportError(Element errorSource, String formattedMsg, Exception nestedException)
  {
    SAXParseException e = new SAXParseException2(formattedMsg, this.forest.locatorTable.getStartLocation(errorSource), nestedException);
    
    this.errorReceiver.error(e);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\internalizer\SCDBasedBindingSet.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */