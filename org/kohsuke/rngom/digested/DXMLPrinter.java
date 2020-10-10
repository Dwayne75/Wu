package org.kohsuke.rngom.digested;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.util.CheckingSchemaBuilder;
import org.kohsuke.rngom.nc.NameClass;
import org.kohsuke.rngom.nc.NameClassVisitor;
import org.kohsuke.rngom.nc.SimpleNameClass;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.parse.compact.CompactParseable;
import org.kohsuke.rngom.parse.xml.SAXParseable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class DXMLPrinter
{
  protected XMLStreamWriter out;
  protected String indentStep = "\t";
  protected String newLine = System.getProperty("line.separator");
  protected int indent;
  protected boolean afterEnd = false;
  protected DXMLPrinterVisitor visitor;
  protected NameClassXMLPrinterVisitor ncVisitor;
  protected DOMPrinter domPrinter;
  
  public DXMLPrinter(XMLStreamWriter out)
  {
    this.out = out;
    this.visitor = new DXMLPrinterVisitor();
    this.ncVisitor = new NameClassXMLPrinterVisitor();
    this.domPrinter = new DOMPrinter(out);
  }
  
  public void printDocument(DGrammarPattern grammar)
    throws XMLStreamException
  {
    try
    {
      this.visitor.startDocument();
      this.visitor.on(grammar);
      this.visitor.endDocument();
    }
    catch (XMLWriterException e)
    {
      throw ((XMLStreamException)e.getCause());
    }
  }
  
  public void print(DPattern pattern)
    throws XMLStreamException
  {
    try
    {
      pattern.accept(this.visitor);
    }
    catch (XMLWriterException e)
    {
      throw ((XMLStreamException)e.getCause());
    }
  }
  
  public void print(NameClass nc)
    throws XMLStreamException
  {
    try
    {
      nc.accept(this.ncVisitor);
    }
    catch (XMLWriterException e)
    {
      throw ((XMLStreamException)e.getCause());
    }
  }
  
  public void print(Node node)
    throws XMLStreamException
  {
    this.domPrinter.print(node);
  }
  
  protected class XMLWriterException
    extends RuntimeException
  {
    protected XMLWriterException(Throwable cause)
    {
      super();
    }
  }
  
  protected class XMLWriter
  {
    protected XMLWriter() {}
    
    protected void newLine()
    {
      try
      {
        DXMLPrinter.this.out.writeCharacters(DXMLPrinter.this.newLine);
      }
      catch (XMLStreamException e)
      {
        throw new DXMLPrinter.XMLWriterException(DXMLPrinter.this, e);
      }
    }
    
    protected void indent()
    {
      try
      {
        for (int i = 0; i < DXMLPrinter.this.indent; i++) {
          DXMLPrinter.this.out.writeCharacters(DXMLPrinter.this.indentStep);
        }
      }
      catch (XMLStreamException e)
      {
        throw new DXMLPrinter.XMLWriterException(DXMLPrinter.this, e);
      }
    }
    
    public void startDocument()
    {
      try
      {
        DXMLPrinter.this.out.writeStartDocument();
      }
      catch (XMLStreamException e)
      {
        throw new DXMLPrinter.XMLWriterException(DXMLPrinter.this, e);
      }
    }
    
    public void endDocument()
    {
      try
      {
        DXMLPrinter.this.out.writeEndDocument();
      }
      catch (XMLStreamException e)
      {
        throw new DXMLPrinter.XMLWriterException(DXMLPrinter.this, e);
      }
    }
    
    public final void start(String element)
    {
      try
      {
        newLine();
        indent();
        DXMLPrinter.this.out.writeStartElement(element);
        DXMLPrinter.this.indent += 1;
        DXMLPrinter.this.afterEnd = false;
      }
      catch (XMLStreamException e)
      {
        throw new DXMLPrinter.XMLWriterException(DXMLPrinter.this, e);
      }
    }
    
    public void end()
    {
      try
      {
        DXMLPrinter.this.indent -= 1;
        if (DXMLPrinter.this.afterEnd)
        {
          newLine();
          indent();
        }
        DXMLPrinter.this.out.writeEndElement();
        DXMLPrinter.this.afterEnd = true;
      }
      catch (XMLStreamException e)
      {
        throw new DXMLPrinter.XMLWriterException(DXMLPrinter.this, e);
      }
    }
    
    public void attr(String prefix, String ns, String name, String value)
    {
      try
      {
        DXMLPrinter.this.out.writeAttribute(prefix, ns, name, value);
      }
      catch (XMLStreamException e)
      {
        throw new DXMLPrinter.XMLWriterException(DXMLPrinter.this, e);
      }
    }
    
    public void attr(String name, String value)
    {
      try
      {
        DXMLPrinter.this.out.writeAttribute(name, value);
      }
      catch (XMLStreamException e)
      {
        throw new DXMLPrinter.XMLWriterException(DXMLPrinter.this, e);
      }
    }
    
    public void ns(String prefix, String uri)
    {
      try
      {
        DXMLPrinter.this.out.writeNamespace(prefix, uri);
      }
      catch (XMLStreamException e)
      {
        throw new DXMLPrinter.XMLWriterException(DXMLPrinter.this, e);
      }
    }
    
    public void body(String text)
    {
      try
      {
        DXMLPrinter.this.out.writeCharacters(text);
        DXMLPrinter.this.afterEnd = false;
      }
      catch (XMLStreamException e)
      {
        throw new DXMLPrinter.XMLWriterException(DXMLPrinter.this, e);
      }
    }
  }
  
  protected class DXMLPrinterVisitor
    extends DXMLPrinter.XMLWriter
    implements DPatternVisitor<Void>
  {
    protected DXMLPrinterVisitor()
    {
      super();
    }
    
    protected void on(DPattern p)
    {
      p.accept(this);
    }
    
    protected void unwrapGroup(DPattern p)
    {
      if (((p instanceof DGroupPattern)) && (p.getAnnotation() == DAnnotation.EMPTY)) {
        for (DPattern d : (DGroupPattern)p) {
          on(d);
        }
      } else {
        on(p);
      }
    }
    
    protected void unwrapChoice(DPattern p)
    {
      if (((p instanceof DChoicePattern)) && (p.getAnnotation() == DAnnotation.EMPTY)) {
        for (DPattern d : (DChoicePattern)p) {
          on(d);
        }
      } else {
        on(p);
      }
    }
    
    protected void on(NameClass nc)
    {
      if ((nc instanceof SimpleNameClass))
      {
        QName qname = ((SimpleNameClass)nc).name;
        String name = qname.getLocalPart();
        if (!qname.getPrefix().equals("")) {
          name = qname.getPrefix() + ":";
        }
        attr("name", name);
      }
      else
      {
        nc.accept(DXMLPrinter.this.ncVisitor);
      }
    }
    
    protected void on(DAnnotation ann)
    {
      if (ann == DAnnotation.EMPTY) {
        return;
      }
      for (DAnnotation.Attribute attr : ann.getAttributes().values()) {
        attr(attr.getPrefix(), attr.getNs(), attr.getLocalName(), attr.getValue());
      }
      for (Element elem : ann.getChildren()) {
        try
        {
          newLine();
          indent();
          DXMLPrinter.this.print(elem);
        }
        catch (XMLStreamException e)
        {
          throw new DXMLPrinter.XMLWriterException(DXMLPrinter.this, e);
        }
      }
    }
    
    public Void onAttribute(DAttributePattern p)
    {
      start("attribute");
      on(p.getName());
      on(p.getAnnotation());
      DPattern child = p.getChild();
      if (!(child instanceof DTextPattern)) {
        on(p.getChild());
      }
      end();
      return null;
    }
    
    public Void onChoice(DChoicePattern p)
    {
      start("choice");
      on(p.getAnnotation());
      for (DPattern d : p) {
        on(d);
      }
      end();
      return null;
    }
    
    public Void onData(DDataPattern p)
    {
      List<DDataPattern.Param> params = p.getParams();
      DPattern except = p.getExcept();
      start("data");
      attr("datatypeLibrary", p.getDatatypeLibrary());
      attr("type", p.getType());
      on(p.getAnnotation());
      for (DDataPattern.Param param : params)
      {
        start("param");
        attr("ns", param.getNs());
        attr("name", param.getName());
        body(param.getValue());
        end();
      }
      if (except != null)
      {
        start("except");
        unwrapChoice(except);
        end();
      }
      end();
      return null;
    }
    
    public Void onElement(DElementPattern p)
    {
      start("element");
      on(p.getName());
      on(p.getAnnotation());
      unwrapGroup(p.getChild());
      end();
      return null;
    }
    
    public Void onEmpty(DEmptyPattern p)
    {
      start("empty");
      on(p.getAnnotation());
      end();
      return null;
    }
    
    public Void onGrammar(DGrammarPattern p)
    {
      start("grammar");
      ns(null, "http://relaxng.org/ns/structure/1.0");
      on(p.getAnnotation());
      start("start");
      on(p.getStart());
      end();
      for (DDefine d : p)
      {
        start("define");
        attr("name", d.getName());
        on(d.getAnnotation());
        unwrapGroup(d.getPattern());
        end();
      }
      end();
      return null;
    }
    
    public Void onGroup(DGroupPattern p)
    {
      start("group");
      on(p.getAnnotation());
      for (DPattern d : p) {
        on(d);
      }
      end();
      return null;
    }
    
    public Void onInterleave(DInterleavePattern p)
    {
      start("interleave");
      on(p.getAnnotation());
      for (DPattern d : p) {
        on(d);
      }
      end();
      return null;
    }
    
    public Void onList(DListPattern p)
    {
      start("list");
      on(p.getAnnotation());
      unwrapGroup(p.getChild());
      end();
      return null;
    }
    
    public Void onMixed(DMixedPattern p)
    {
      start("mixed");
      on(p.getAnnotation());
      unwrapGroup(p.getChild());
      end();
      return null;
    }
    
    public Void onNotAllowed(DNotAllowedPattern p)
    {
      start("notAllowed");
      on(p.getAnnotation());
      end();
      return null;
    }
    
    public Void onOneOrMore(DOneOrMorePattern p)
    {
      start("oneOrMore");
      on(p.getAnnotation());
      unwrapGroup(p.getChild());
      end();
      return null;
    }
    
    public Void onOptional(DOptionalPattern p)
    {
      start("optional");
      on(p.getAnnotation());
      unwrapGroup(p.getChild());
      end();
      return null;
    }
    
    public Void onRef(DRefPattern p)
    {
      start("ref");
      attr("name", p.getName());
      on(p.getAnnotation());
      end();
      return null;
    }
    
    public Void onText(DTextPattern p)
    {
      start("text");
      on(p.getAnnotation());
      end();
      return null;
    }
    
    public Void onValue(DValuePattern p)
    {
      start("value");
      if (!p.getNs().equals("")) {
        attr("ns", p.getNs());
      }
      attr("datatypeLibrary", p.getDatatypeLibrary());
      attr("type", p.getType());
      on(p.getAnnotation());
      body(p.getValue());
      end();
      return null;
    }
    
    public Void onZeroOrMore(DZeroOrMorePattern p)
    {
      start("zeroOrMore");
      on(p.getAnnotation());
      unwrapGroup(p.getChild());
      end();
      return null;
    }
  }
  
  protected class NameClassXMLPrinterVisitor
    extends DXMLPrinter.XMLWriter
    implements NameClassVisitor<Void>
  {
    protected NameClassXMLPrinterVisitor()
    {
      super();
    }
    
    public Void visitChoice(NameClass nc1, NameClass nc2)
    {
      start("choice");
      nc1.accept(this);
      nc2.accept(this);
      end();
      return null;
    }
    
    public Void visitNsName(String ns)
    {
      start("nsName");
      attr("ns", ns);
      end();
      return null;
    }
    
    public Void visitNsNameExcept(String ns, NameClass nc)
    {
      start("nsName");
      attr("ns", ns);
      start("except");
      nc.accept(this);
      end();
      end();
      return null;
    }
    
    public Void visitAnyName()
    {
      start("anyName");
      end();
      return null;
    }
    
    public Void visitAnyNameExcept(NameClass nc)
    {
      start("anyName");
      start("except");
      nc.accept(this);
      end();
      end();
      return null;
    }
    
    public Void visitName(QName name)
    {
      start("name");
      if (!name.getPrefix().equals("")) {
        body(name.getPrefix() + ":");
      }
      body(name.getLocalPart());
      end();
      return null;
    }
    
    public Void visitNull()
    {
      throw new UnsupportedOperationException("visitNull");
    }
  }
  
  public static void main(String[] args)
    throws Exception
  {
    ErrorHandler eh = new DefaultHandler()
    {
      public void error(SAXParseException e)
        throws SAXException
      {
        throw e;
      }
    };
    Parseable p;
    Parseable p;
    if (args[0].endsWith(".rng")) {
      p = new SAXParseable(new InputSource(args[0]), eh);
    } else {
      p = new CompactParseable(new InputSource(args[0]), eh);
    }
    SchemaBuilder sb = new CheckingSchemaBuilder(new DSchemaBuilderImpl(), eh);
    try
    {
      DGrammarPattern grammar = (DGrammarPattern)p.parse(sb);
      OutputStream out = new FileOutputStream(args[1]);
      XMLOutputFactory factory = XMLOutputFactory.newInstance();
      XMLStreamWriter output = factory.createXMLStreamWriter(out);
      DXMLPrinter printer = new DXMLPrinter(output);
      printer.printDocument(grammar);
      output.close();
      out.close();
    }
    catch (BuildException e)
    {
      if ((e.getCause() instanceof SAXParseException))
      {
        SAXParseException se = (SAXParseException)e.getCause();
        System.out.println("(" + se.getLineNumber() + "," + se.getColumnNumber() + "): " + se.getMessage());
        
        return;
      }
      if ((e.getCause() instanceof SAXException))
      {
        SAXException se = (SAXException)e.getCause();
        if (se.getException() != null) {
          se.getException().printStackTrace();
        }
      }
      throw e;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DXMLPrinter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */