package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.SchemaCache;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.annotation.XmlLocation;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.marshaller.MinimumEscapeHandler;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.xsom.XSComponent;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import org.xml.sax.Locator;

@XmlRootElement(namespace="http://www.w3.org/2001/XMLSchema", name="annotation")
@XmlType(namespace="http://www.w3.org/2001/XMLSchema", name="foobar")
public final class BindInfo
  implements Iterable<BIDeclaration>
{
  private BGMBuilder builder;
  @XmlLocation
  private Locator location;
  @XmlElement(namespace="http://www.w3.org/2001/XMLSchema")
  private Documentation documentation;
  private final List<BIDeclaration> decls;
  private XSComponent owner;
  
  public boolean isPointless()
  {
    if (size() > 0) {
      return false;
    }
    if ((this.documentation != null) && (!this.documentation.contents.isEmpty())) {
      return false;
    }
    return true;
  }
  
  private static final class Documentation
  {
    @XmlAnyElement
    @XmlMixed
    List<Object> contents = new ArrayList();
    
    void addAll(Documentation rhs)
    {
      if (rhs == null) {
        return;
      }
      if (this.contents == null) {
        this.contents = new ArrayList();
      }
      if (!this.contents.isEmpty()) {
        this.contents.add("\n\n");
      }
      this.contents.addAll(rhs.contents);
    }
  }
  
  public BindInfo()
  {
    this.decls = new ArrayList();
  }
  
  private static final class AppInfo
  {
    @XmlAnyElement(lax=true, value=DomHandlerEx.class)
    List<Object> contents = new ArrayList();
    
    public void addTo(BindInfo bi)
    {
      if (this.contents == null) {
        return;
      }
      for (Object o : this.contents)
      {
        if ((o instanceof BIDeclaration)) {
          bi.addDecl((BIDeclaration)o);
        }
        if ((o instanceof DomHandlerEx.DomAndLocation))
        {
          DomHandlerEx.DomAndLocation e = (DomHandlerEx.DomAndLocation)o;
          String nsUri = e.element.getNamespaceURI();
          if ((nsUri != null) && (!nsUri.equals("")) && (!nsUri.equals("http://www.w3.org/2001/XMLSchema"))) {
            bi.addDecl(new BIXPluginCustomization(e.element, e.loc));
          }
        }
      }
    }
  }
  
  @XmlElement(namespace="http://www.w3.org/2001/XMLSchema")
  void setAppinfo(AppInfo aib)
  {
    aib.addTo(this);
  }
  
  public Locator getSourceLocation()
  {
    return this.location;
  }
  
  public void setOwner(BGMBuilder _builder, XSComponent _owner)
  {
    this.owner = _owner;
    this.builder = _builder;
    for (BIDeclaration d : this.decls) {
      d.onSetOwner();
    }
  }
  
  public XSComponent getOwner()
  {
    return this.owner;
  }
  
  public BGMBuilder getBuilder()
  {
    return this.builder;
  }
  
  public void addDecl(BIDeclaration decl)
  {
    if (decl == null) {
      throw new IllegalArgumentException();
    }
    decl.setParent(this);
    this.decls.add(decl);
  }
  
  public <T extends BIDeclaration> T get(Class<T> kind)
  {
    for (BIDeclaration decl : this.decls) {
      if (kind.isInstance(decl)) {
        return (BIDeclaration)kind.cast(decl);
      }
    }
    return null;
  }
  
  public BIDeclaration[] getDecls()
  {
    return (BIDeclaration[])this.decls.toArray(new BIDeclaration[this.decls.size()]);
  }
  
  public String getDocumentation()
  {
    if ((this.documentation == null) || (this.documentation.contents == null)) {
      return null;
    }
    StringBuilder buf = new StringBuilder();
    for (Object c : this.documentation.contents)
    {
      if ((c instanceof String)) {
        buf.append(c.toString());
      }
      if ((c instanceof Element))
      {
        Transformer t = this.builder.getIdentityTransformer();
        StringWriter w = new StringWriter();
        try
        {
          Writer fw = new FilterWriter(w)
          {
            char[] buf = new char[1];
            
            public void write(int c)
              throws IOException
            {
              this.buf[0] = ((char)c);
              write(this.buf, 0, 1);
            }
            
            public void write(char[] cbuf, int off, int len)
              throws IOException
            {
              MinimumEscapeHandler.theInstance.escape(cbuf, off, len, false, this.out);
            }
            
            public void write(String str, int off, int len)
              throws IOException
            {
              write(str.toCharArray(), off, len);
            }
          };
          t.transform(new DOMSource((Element)c), new StreamResult(fw));
        }
        catch (TransformerException e)
        {
          throw new Error(e);
        }
        buf.append("\n<pre>\n");
        buf.append(w);
        buf.append("\n</pre>\n");
      }
    }
    return buf.toString();
  }
  
  public void absorb(BindInfo bi)
  {
    for (BIDeclaration d : bi) {
      d.setParent(this);
    }
    this.decls.addAll(bi.decls);
    if (this.documentation == null) {
      this.documentation = bi.documentation;
    } else {
      this.documentation.addAll(bi.documentation);
    }
  }
  
  public int size()
  {
    return this.decls.size();
  }
  
  public BIDeclaration get(int idx)
  {
    return (BIDeclaration)this.decls.get(idx);
  }
  
  public Iterator<BIDeclaration> iterator()
  {
    return this.decls.iterator();
  }
  
  public CCustomizations toCustomizationList()
  {
    CCustomizations r = null;
    for (BIDeclaration d : this) {
      if ((d instanceof BIXPluginCustomization))
      {
        BIXPluginCustomization pc = (BIXPluginCustomization)d;
        pc.markAsAcknowledged();
        if (((Model)Ring.get(Model.class)).options.pluginURIs.contains(pc.getName().getNamespaceURI()))
        {
          if (r == null) {
            r = new CCustomizations();
          }
          r.add(new CPluginCustomization(pc.element, pc.getLocation()));
        }
      }
    }
    if (r == null) {
      r = CCustomizations.EMPTY;
    }
    return new CCustomizations(r);
  }
  
  public static final BindInfo empty = new BindInfo();
  private static JAXBContextImpl customizationContext;
  
  public static JAXBContextImpl getJAXBContext()
  {
    synchronized (AnnotationParserFactoryImpl.class)
    {
      try
      {
        if (customizationContext == null) {
          customizationContext = new JAXBContextImpl(new Class[] { BindInfo.class, BIClass.class, BIConversion.User.class, BIConversion.UserAdapter.class, BIDom.class, BIFactoryMethod.class, BIInlineBinaryData.class, BIXDom.class, BIXSubstitutable.class, BIEnum.class, BIEnumMember.class, BIGlobalBinding.class, BIProperty.class, BISchemaBinding.class }, Collections.emptyList(), Collections.emptyMap(), null, false, null, false, false);
        }
        return customizationContext;
      }
      catch (JAXBException e)
      {
        throw new AssertionError(e);
      }
    }
  }
  
  public static final SchemaCache bindingFileSchema = new SchemaCache(BindInfo.class.getResource("binding.xsd"));
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\BindInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */