package com.sun.xml.txw2;

import com.sun.xml.txw2.output.XmlSerializer;
import java.util.HashMap;
import java.util.Map;

public final class Document
{
  private final XmlSerializer out;
  private boolean started = false;
  private Content current = null;
  private final Map<Class, DatatypeWriter> datatypeWriters = new HashMap();
  private int iota = 1;
  private final NamespaceSupport inscopeNamespace = new NamespaceSupport();
  private NamespaceDecl activeNamespaces;
  
  Document(XmlSerializer out)
  {
    this.out = out;
    for (DatatypeWriter dw : DatatypeWriter.BUILDIN) {
      this.datatypeWriters.put(dw.getType(), dw);
    }
  }
  
  void flush()
  {
    this.out.flush();
  }
  
  void setFirstContent(Content c)
  {
    assert (this.current == null);
    this.current = new StartDocument();
    this.current.setNext(this, c);
  }
  
  public void addDatatypeWriter(DatatypeWriter<?> dw)
  {
    this.datatypeWriters.put(dw.getType(), dw);
  }
  
  void run()
  {
    for (;;)
    {
      Content next = this.current.getNext();
      if ((next == null) || (!next.isReadyToCommit())) {
        return;
      }
      next.accept(this.visitor);
      next.written();
      this.current = next;
    }
  }
  
  void writeValue(Object obj, NamespaceResolver nsResolver, StringBuilder buf)
  {
    if (obj == null) {
      throw new IllegalArgumentException("argument contains null");
    }
    if ((obj instanceof Object[]))
    {
      for (Object o : (Object[])obj) {
        writeValue(o, nsResolver, buf);
      }
      return;
    }
    if ((obj instanceof Iterable))
    {
      for (Object o : (Iterable)obj) {
        writeValue(o, nsResolver, buf);
      }
      return;
    }
    if (buf.length() > 0) {
      buf.append(' ');
    }
    Class c = obj.getClass();
    while (c != null)
    {
      DatatypeWriter dw = (DatatypeWriter)this.datatypeWriters.get(c);
      if (dw != null)
      {
        dw.print(obj, nsResolver, buf);
        return;
      }
      c = c.getSuperclass();
    }
    buf.append(obj);
  }
  
  private final ContentVisitor visitor = new ContentVisitor()
  {
    public void onStartDocument()
    {
      throw new IllegalStateException();
    }
    
    public void onEndDocument()
    {
      Document.this.out.endDocument();
    }
    
    public void onEndTag()
    {
      Document.this.out.endTag();
      Document.this.inscopeNamespace.popContext();
      Document.this.activeNamespaces = null;
    }
    
    public void onPcdata(StringBuilder buffer)
    {
      if (Document.this.activeNamespaces != null) {
        buffer = Document.this.fixPrefix(buffer);
      }
      Document.this.out.text(buffer);
    }
    
    public void onCdata(StringBuilder buffer)
    {
      if (Document.this.activeNamespaces != null) {
        buffer = Document.this.fixPrefix(buffer);
      }
      Document.this.out.cdata(buffer);
    }
    
    public void onComment(StringBuilder buffer)
    {
      if (Document.this.activeNamespaces != null) {
        buffer = Document.this.fixPrefix(buffer);
      }
      Document.this.out.comment(buffer);
    }
    
    public void onStartTag(String nsUri, String localName, Attribute attributes, NamespaceDecl namespaces)
    {
      assert (nsUri != null);
      assert (localName != null);
      
      Document.this.activeNamespaces = namespaces;
      if (!Document.this.started)
      {
        Document.this.started = true;
        Document.this.out.startDocument();
      }
      Document.this.inscopeNamespace.pushContext();
      for (NamespaceDecl ns = namespaces; ns != null; ns = ns.next)
      {
        ns.declared = false;
        if (ns.prefix != null)
        {
          String uri = Document.this.inscopeNamespace.getURI(ns.prefix);
          if ((uri == null) || (!uri.equals(ns.uri)))
          {
            Document.this.inscopeNamespace.declarePrefix(ns.prefix, ns.uri);
            ns.declared = true;
          }
        }
      }
      for (NamespaceDecl ns = namespaces; ns != null; ns = ns.next) {
        if (ns.prefix == null) {
          if (Document.this.inscopeNamespace.getURI("").equals(ns.uri))
          {
            ns.prefix = "";
          }
          else
          {
            String p = Document.this.inscopeNamespace.getPrefix(ns.uri);
            if (p == null)
            {
              while (Document.this.inscopeNamespace.getURI(p = Document.this.newPrefix()) != null) {}
              ns.declared = true;
              Document.this.inscopeNamespace.declarePrefix(p, ns.uri);
            }
            ns.prefix = p;
          }
        }
      }
      assert (namespaces.uri.equals(nsUri));
      assert (namespaces.prefix != null) : "a prefix must have been all allocated";
      Document.this.out.beginStartTag(nsUri, localName, namespaces.prefix);
      for (NamespaceDecl ns = namespaces; ns != null; ns = ns.next) {
        if (ns.declared) {
          Document.this.out.writeXmlns(ns.prefix, ns.uri);
        }
      }
      for (Attribute a = attributes; a != null; a = a.next)
      {
        String prefix;
        String prefix;
        if (a.nsUri.length() == 0) {
          prefix = "";
        } else {
          prefix = Document.this.inscopeNamespace.getPrefix(a.nsUri);
        }
        Document.this.out.writeAttribute(a.nsUri, a.localName, prefix, Document.this.fixPrefix(a.value));
      }
      Document.this.out.endStartTag(nsUri, localName, namespaces.prefix);
    }
  };
  private final StringBuilder prefixSeed = new StringBuilder("ns");
  private int prefixIota = 0;
  static final char MAGIC = '\000';
  
  private String newPrefix()
  {
    this.prefixSeed.setLength(2);
    this.prefixSeed.append(++this.prefixIota);
    return this.prefixSeed.toString();
  }
  
  private StringBuilder fixPrefix(StringBuilder buf)
  {
    assert (this.activeNamespaces != null);
    
    int len = buf.length();
    for (int i = 0; i < len; i++) {
      if (buf.charAt(i) == 0) {
        break;
      }
    }
    if (i == len) {
      return buf;
    }
    for (; i < len; goto 227)
    {
      char uriIdx = buf.charAt(i + 1);
      NamespaceDecl ns = this.activeNamespaces;
      while ((ns != null) && (ns.uniqueId != uriIdx)) {
        ns = ns.next;
      }
      if (ns == null) {
        throw new IllegalStateException("Unexpected use of prefixes " + buf);
      }
      int length = 2;
      String prefix = ns.prefix;
      if (prefix.length() == 0)
      {
        if ((buf.length() <= i + 2) || (buf.charAt(i + 2) != ':')) {
          throw new IllegalStateException("Unexpected use of prefixes " + buf);
        }
        length = 3;
      }
      buf.replace(i, i + length, prefix);
      len += prefix.length() - length;
      if ((i < len) && (buf.charAt(i) != 0)) {
        i++;
      }
    }
    return buf;
  }
  
  char assignNewId()
  {
    return (char)this.iota++;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\Document.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */