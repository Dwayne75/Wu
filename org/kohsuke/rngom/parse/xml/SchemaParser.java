package org.kohsuke.rngom.parse.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.DataPatternBuilder;
import org.kohsuke.rngom.ast.builder.Div;
import org.kohsuke.rngom.ast.builder.ElementAnnotationBuilder;
import org.kohsuke.rngom.ast.builder.Grammar;
import org.kohsuke.rngom.ast.builder.GrammarSection;
import org.kohsuke.rngom.ast.builder.GrammarSection.Combine;
import org.kohsuke.rngom.ast.builder.Include;
import org.kohsuke.rngom.ast.builder.IncludedGrammar;
import org.kohsuke.rngom.ast.builder.NameClassBuilder;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedNameClass;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.Context;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.util.Localizer;
import org.kohsuke.rngom.util.Uri;
import org.kohsuke.rngom.xml.sax.AbstractLexicalHandler;
import org.kohsuke.rngom.xml.sax.XmlBaseHandler;
import org.kohsuke.rngom.xml.util.Naming;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

class SchemaParser
{
  private static final String relaxngURIPrefix = "http://relaxng.org/ns/structure/1.0".substring(0, "http://relaxng.org/ns/structure/1.0".lastIndexOf('/') + 1);
  static final String relaxng10URI = "http://relaxng.org/ns/structure/1.0";
  private static final Localizer localizer = new Localizer(new Localizer(Parseable.class), SchemaParser.class);
  private String relaxngURI;
  private final XMLReader xr;
  private final ErrorHandler eh;
  private final SchemaBuilder schemaBuilder;
  private final NameClassBuilder nameClassBuilder;
  private ParsedPattern startPattern;
  private Locator locator;
  private final XmlBaseHandler xmlBaseHandler = new XmlBaseHandler();
  private final ContextImpl context = new ContextImpl();
  private boolean hadError = false;
  private Hashtable patternTable;
  private Hashtable nameClassTable;
  private static final int INIT_CHILD_ALLOC = 5;
  private static final int PATTERN_CONTEXT = 0;
  private static final int ANY_NAME_CONTEXT = 1;
  private static final int NS_NAME_CONTEXT = 2;
  private SAXParseable parseable;
  
  static class PrefixMapping
  {
    final String prefix;
    final String uri;
    final PrefixMapping next;
    
    PrefixMapping(String prefix, String uri, PrefixMapping next)
    {
      this.prefix = prefix;
      this.uri = uri;
      this.next = next;
    }
  }
  
  static abstract class AbstractContext
    extends DtdContext
    implements Context
  {
    SchemaParser.PrefixMapping prefixMapping;
    
    AbstractContext()
    {
      this.prefixMapping = new SchemaParser.PrefixMapping("xml", "http://www.w3.org/XML/1998/namespace", null);
    }
    
    AbstractContext(AbstractContext context)
    {
      super();
      this.prefixMapping = context.prefixMapping;
    }
    
    public String resolveNamespacePrefix(String prefix)
    {
      for (SchemaParser.PrefixMapping p = this.prefixMapping; p != null; p = p.next) {
        if (p.prefix.equals(prefix)) {
          return p.uri;
        }
      }
      return null;
    }
    
    public Enumeration prefixes()
    {
      Vector v = new Vector();
      for (SchemaParser.PrefixMapping p = this.prefixMapping; p != null; p = p.next) {
        if (!v.contains(p.prefix)) {
          v.addElement(p.prefix);
        }
      }
      return v.elements();
    }
    
    public Context copy()
    {
      return new SchemaParser.SavedContext(this);
    }
  }
  
  static class SavedContext
    extends SchemaParser.AbstractContext
  {
    private final String baseUri;
    
    SavedContext(SchemaParser.AbstractContext context)
    {
      super();
      this.baseUri = context.getBaseUri();
    }
    
    public String getBaseUri()
    {
      return this.baseUri;
    }
  }
  
  class ContextImpl
    extends SchemaParser.AbstractContext
  {
    ContextImpl() {}
    
    public String getBaseUri()
    {
      return SchemaParser.this.xmlBaseHandler.getBaseUri();
    }
  }
  
  static abstract interface CommentHandler
  {
    public abstract void comment(String paramString);
  }
  
  abstract class Handler
    implements ContentHandler, SchemaParser.CommentHandler
  {
    CommentList comments;
    
    Handler() {}
    
    CommentList getComments()
    {
      CommentList tem = this.comments;
      this.comments = null;
      return tem;
    }
    
    public void comment(String value)
    {
      if (this.comments == null) {
        this.comments = SchemaParser.this.schemaBuilder.makeCommentList();
      }
      this.comments.addComment(value, SchemaParser.this.makeLocation());
    }
    
    public void processingInstruction(String target, String date) {}
    
    public void skippedEntity(String name) {}
    
    public void ignorableWhitespace(char[] ch, int start, int len) {}
    
    public void startDocument() {}
    
    public void endDocument() {}
    
    public void startPrefixMapping(String prefix, String uri)
    {
      SchemaParser.this.context.prefixMapping = new SchemaParser.PrefixMapping(prefix, uri, SchemaParser.this.context.prefixMapping);
    }
    
    public void endPrefixMapping(String prefix)
    {
      SchemaParser.this.context.prefixMapping = SchemaParser.this.context.prefixMapping.next;
    }
    
    public void setDocumentLocator(Locator loc)
    {
      SchemaParser.this.locator = loc;
      SchemaParser.this.xmlBaseHandler.setLocator(loc);
    }
  }
  
  abstract class State
    extends SchemaParser.Handler
  {
    State parent;
    String nsInherit;
    String ns;
    String datatypeLibrary;
    Scope scope;
    Location startLocation;
    Annotations annotations;
    
    State()
    {
      super();
    }
    
    void set()
    {
      SchemaParser.this.xr.setContentHandler(this);
    }
    
    abstract State create();
    
    abstract State createChildState(String paramString)
      throws SAXException;
    
    void setParent(State parent)
    {
      this.parent = parent;
      this.nsInherit = parent.getNs();
      this.datatypeLibrary = parent.datatypeLibrary;
      this.scope = parent.scope;
      this.startLocation = SchemaParser.this.makeLocation();
      if (parent.comments != null)
      {
        this.annotations = SchemaParser.this.schemaBuilder.makeAnnotations(parent.comments, SchemaParser.this.getContext());
        parent.comments = null;
      }
      else if ((parent instanceof SchemaParser.RootState))
      {
        this.annotations = SchemaParser.this.schemaBuilder.makeAnnotations(null, SchemaParser.this.getContext());
      }
    }
    
    String getNs()
    {
      return this.ns == null ? this.nsInherit : this.ns;
    }
    
    boolean isRelaxNGElement(String uri)
      throws SAXException
    {
      return uri.equals(SchemaParser.this.relaxngURI);
    }
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
      throws SAXException
    {
      SchemaParser.this.xmlBaseHandler.startElement();
      if (isRelaxNGElement(namespaceURI))
      {
        State state = createChildState(localName);
        if (state == null)
        {
          SchemaParser.this.xr.setContentHandler(new SchemaParser.Skipper(SchemaParser.this, this));
          return;
        }
        state.setParent(this);
        state.set();
        state.attributes(atts);
      }
      else
      {
        checkForeignElement();
        SchemaParser.ForeignElementHandler feh = new SchemaParser.ForeignElementHandler(SchemaParser.this, this, getComments());
        feh.startElement(namespaceURI, localName, qName, atts);
        SchemaParser.this.xr.setContentHandler(feh);
      }
    }
    
    public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException
    {
      SchemaParser.this.xmlBaseHandler.endElement();
      this.parent.set();
      end();
    }
    
    void setName(String name)
      throws SAXException
    {
      SchemaParser.this.error("illegal_name_attribute");
    }
    
    void setOtherAttribute(String name, String value)
      throws SAXException
    {
      SchemaParser.this.error("illegal_attribute_ignored", name);
    }
    
    void endAttributes()
      throws SAXException
    {}
    
    void checkForeignElement()
      throws SAXException
    {}
    
    void attributes(Attributes atts)
      throws SAXException
    {
      int len = atts.getLength();
      for (int i = 0; i < len; i++)
      {
        String uri = atts.getURI(i);
        if (uri.length() == 0)
        {
          String name = atts.getLocalName(i);
          if (name.equals("name"))
          {
            setName(atts.getValue(i).trim());
          }
          else if (name.equals("ns"))
          {
            this.ns = atts.getValue(i);
          }
          else if (name.equals("datatypeLibrary"))
          {
            this.datatypeLibrary = atts.getValue(i);
            SchemaParser.this.checkUri(this.datatypeLibrary);
            if ((!this.datatypeLibrary.equals("")) && (!Uri.isAbsolute(this.datatypeLibrary))) {
              SchemaParser.this.error("relative_datatype_library");
            }
            if (Uri.hasFragmentId(this.datatypeLibrary)) {
              SchemaParser.this.error("fragment_identifier_datatype_library");
            }
            this.datatypeLibrary = Uri.escapeDisallowedChars(this.datatypeLibrary);
          }
          else
          {
            setOtherAttribute(name, atts.getValue(i));
          }
        }
        else if (uri.equals(SchemaParser.this.relaxngURI))
        {
          SchemaParser.this.error("qualified_attribute", atts.getLocalName(i));
        }
        else if ((uri.equals("http://www.w3.org/XML/1998/namespace")) && (atts.getLocalName(i).equals("base")))
        {
          SchemaParser.this.xmlBaseHandler.xmlBaseAttribute(atts.getValue(i));
        }
        else
        {
          if (this.annotations == null) {
            this.annotations = SchemaParser.this.schemaBuilder.makeAnnotations(null, SchemaParser.this.getContext());
          }
          this.annotations.addAttribute(uri, atts.getLocalName(i), SchemaParser.this.findPrefix(atts.getQName(i), uri), atts.getValue(i), this.startLocation);
        }
      }
      endAttributes();
    }
    
    abstract void end()
      throws SAXException;
    
    void endChild(ParsedPattern pattern) {}
    
    void endChild(ParsedNameClass nc) {}
    
    public void startDocument() {}
    
    public void endDocument()
    {
      if ((this.comments != null) && (SchemaParser.this.startPattern != null))
      {
        SchemaParser.this.startPattern = SchemaParser.this.schemaBuilder.commentAfter(SchemaParser.this.startPattern, this.comments);
        this.comments = null;
      }
    }
    
    public void characters(char[] ch, int start, int len)
      throws SAXException
    {
      for (int i = 0; i < len; i++) {
        switch (ch[(start + i)])
        {
        case '\t': 
        case '\n': 
        case '\r': 
        case ' ': 
          break;
        default: 
          SchemaParser.this.error("illegal_characters_ignored");
        }
      }
    }
    
    boolean isPatternNamespaceURI(String s)
    {
      return s.equals(SchemaParser.this.relaxngURI);
    }
    
    void endForeignChild(ParsedElementAnnotation ea)
    {
      if (this.annotations == null) {
        this.annotations = SchemaParser.this.schemaBuilder.makeAnnotations(null, SchemaParser.this.getContext());
      }
      this.annotations.addElement(ea);
    }
    
    void mergeLeadingComments()
    {
      if (this.comments != null)
      {
        if (this.annotations == null) {
          this.annotations = SchemaParser.this.schemaBuilder.makeAnnotations(this.comments, SchemaParser.this.getContext());
        } else {
          this.annotations.addLeadingComment(this.comments);
        }
        this.comments = null;
      }
    }
  }
  
  class ForeignElementHandler
    extends SchemaParser.Handler
  {
    final SchemaParser.State nextState;
    ElementAnnotationBuilder builder;
    final Stack builderStack = new Stack();
    StringBuffer textBuf;
    Location textLoc;
    
    ForeignElementHandler(SchemaParser.State nextState, CommentList comments)
    {
      super();
      this.nextState = nextState;
      this.comments = comments;
    }
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
    {
      flushText();
      if (this.builder != null) {
        this.builderStack.push(this.builder);
      }
      Location loc = SchemaParser.this.makeLocation();
      this.builder = SchemaParser.this.schemaBuilder.makeElementAnnotationBuilder(namespaceURI, localName, SchemaParser.this.findPrefix(qName, namespaceURI), loc, getComments(), SchemaParser.this.getContext());
      
      int len = atts.getLength();
      for (int i = 0; i < len; i++)
      {
        String uri = atts.getURI(i);
        this.builder.addAttribute(uri, atts.getLocalName(i), SchemaParser.this.findPrefix(atts.getQName(i), uri), atts.getValue(i), loc);
      }
    }
    
    public void endElement(String namespaceURI, String localName, String qName)
    {
      flushText();
      if (this.comments != null) {
        this.builder.addComment(getComments());
      }
      ParsedElementAnnotation ea = this.builder.makeElementAnnotation();
      if (this.builderStack.empty())
      {
        this.nextState.endForeignChild(ea);
        this.nextState.set();
      }
      else
      {
        this.builder = ((ElementAnnotationBuilder)this.builderStack.pop());
        this.builder.addElement(ea);
      }
    }
    
    public void characters(char[] ch, int start, int length)
    {
      if (this.textBuf == null) {
        this.textBuf = new StringBuffer();
      }
      this.textBuf.append(ch, start, length);
      if (this.textLoc == null) {
        this.textLoc = SchemaParser.this.makeLocation();
      }
    }
    
    public void comment(String value)
    {
      flushText();
      super.comment(value);
    }
    
    void flushText()
    {
      if ((this.textBuf != null) && (this.textBuf.length() != 0))
      {
        this.builder.addText(this.textBuf.toString(), this.textLoc, getComments());
        this.textBuf.setLength(0);
      }
      this.textLoc = null;
    }
  }
  
  class Skipper
    extends DefaultHandler
    implements SchemaParser.CommentHandler
  {
    int level = 1;
    final SchemaParser.State nextState;
    
    Skipper(SchemaParser.State nextState)
    {
      this.nextState = nextState;
    }
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
      throws SAXException
    {
      this.level += 1;
    }
    
    public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException
    {
      if (--this.level == 0) {
        this.nextState.set();
      }
    }
    
    public void comment(String value) {}
  }
  
  abstract class EmptyContentState
    extends SchemaParser.State
  {
    EmptyContentState()
    {
      super();
    }
    
    SchemaParser.State createChildState(String localName)
      throws SAXException
    {
      SchemaParser.this.error("expected_empty", localName);
      return null;
    }
    
    abstract ParsedPattern makePattern()
      throws SAXException;
    
    void end()
      throws SAXException
    {
      if (this.comments != null)
      {
        if (this.annotations == null) {
          this.annotations = SchemaParser.this.schemaBuilder.makeAnnotations(null, SchemaParser.this.getContext());
        }
        this.annotations.addComment(this.comments);
        this.comments = null;
      }
      this.parent.endChild(makePattern());
    }
  }
  
  abstract class PatternContainerState
    extends SchemaParser.State
  {
    List<ParsedPattern> childPatterns;
    
    PatternContainerState()
    {
      super();
    }
    
    SchemaParser.State createChildState(String localName)
      throws SAXException
    {
      SchemaParser.State state = (SchemaParser.State)SchemaParser.this.patternTable.get(localName);
      if (state == null)
      {
        SchemaParser.this.error("expected_pattern", localName);
        return null;
      }
      return state.create();
    }
    
    ParsedPattern buildPattern(List<ParsedPattern> patterns, Location loc, Annotations anno)
      throws SAXException
    {
      if ((patterns.size() == 1) && (anno == null)) {
        return (ParsedPattern)patterns.get(0);
      }
      return SchemaParser.this.schemaBuilder.makeGroup(patterns, loc, anno);
    }
    
    void endChild(ParsedPattern pattern)
    {
      if (this.childPatterns == null) {
        this.childPatterns = new ArrayList(5);
      }
      this.childPatterns.add(pattern);
    }
    
    void endForeignChild(ParsedElementAnnotation ea)
    {
      if (this.childPatterns == null)
      {
        super.endForeignChild(ea);
      }
      else
      {
        int idx = this.childPatterns.size() - 1;
        this.childPatterns.set(idx, SchemaParser.this.schemaBuilder.annotateAfter((ParsedPattern)this.childPatterns.get(idx), ea));
      }
    }
    
    void end()
      throws SAXException
    {
      if (this.childPatterns == null)
      {
        SchemaParser.this.error("missing_children");
        endChild(SchemaParser.this.schemaBuilder.makeErrorPattern());
      }
      if (this.comments != null)
      {
        int idx = this.childPatterns.size() - 1;
        this.childPatterns.set(idx, SchemaParser.this.schemaBuilder.commentAfter((ParsedPattern)this.childPatterns.get(idx), this.comments));
        this.comments = null;
      }
      sendPatternToParent(buildPattern(this.childPatterns, this.startLocation, this.annotations));
    }
    
    void sendPatternToParent(ParsedPattern p)
    {
      this.parent.endChild(p);
    }
  }
  
  class GroupState
    extends SchemaParser.PatternContainerState
  {
    GroupState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new GroupState(SchemaParser.this);
    }
  }
  
  class ZeroOrMoreState
    extends SchemaParser.PatternContainerState
  {
    ZeroOrMoreState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new ZeroOrMoreState(SchemaParser.this);
    }
    
    ParsedPattern buildPattern(List<ParsedPattern> patterns, Location loc, Annotations anno)
      throws SAXException
    {
      return SchemaParser.this.schemaBuilder.makeZeroOrMore(super.buildPattern(patterns, loc, null), loc, anno);
    }
  }
  
  class OneOrMoreState
    extends SchemaParser.PatternContainerState
  {
    OneOrMoreState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new OneOrMoreState(SchemaParser.this);
    }
    
    ParsedPattern buildPattern(List<ParsedPattern> patterns, Location loc, Annotations anno)
      throws SAXException
    {
      return SchemaParser.this.schemaBuilder.makeOneOrMore(super.buildPattern(patterns, loc, null), loc, anno);
    }
  }
  
  class OptionalState
    extends SchemaParser.PatternContainerState
  {
    OptionalState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new OptionalState(SchemaParser.this);
    }
    
    ParsedPattern buildPattern(List<ParsedPattern> patterns, Location loc, Annotations anno)
      throws SAXException
    {
      return SchemaParser.this.schemaBuilder.makeOptional(super.buildPattern(patterns, loc, null), loc, anno);
    }
  }
  
  class ListState
    extends SchemaParser.PatternContainerState
  {
    ListState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new ListState(SchemaParser.this);
    }
    
    ParsedPattern buildPattern(List<ParsedPattern> patterns, Location loc, Annotations anno)
      throws SAXException
    {
      return SchemaParser.this.schemaBuilder.makeList(super.buildPattern(patterns, loc, null), loc, anno);
    }
  }
  
  class ChoiceState
    extends SchemaParser.PatternContainerState
  {
    ChoiceState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new ChoiceState(SchemaParser.this);
    }
    
    ParsedPattern buildPattern(List<ParsedPattern> patterns, Location loc, Annotations anno)
      throws SAXException
    {
      return SchemaParser.this.schemaBuilder.makeChoice(patterns, loc, anno);
    }
  }
  
  class InterleaveState
    extends SchemaParser.PatternContainerState
  {
    InterleaveState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new InterleaveState(SchemaParser.this);
    }
    
    ParsedPattern buildPattern(List<ParsedPattern> patterns, Location loc, Annotations anno)
    {
      return SchemaParser.this.schemaBuilder.makeInterleave(patterns, loc, anno);
    }
  }
  
  class MixedState
    extends SchemaParser.PatternContainerState
  {
    MixedState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new MixedState(SchemaParser.this);
    }
    
    ParsedPattern buildPattern(List<ParsedPattern> patterns, Location loc, Annotations anno)
      throws SAXException
    {
      return SchemaParser.this.schemaBuilder.makeMixed(super.buildPattern(patterns, loc, null), loc, anno);
    }
  }
  
  static abstract interface NameClassRef
  {
    public abstract void setNameClass(ParsedNameClass paramParsedNameClass);
  }
  
  class ElementState
    extends SchemaParser.PatternContainerState
    implements SchemaParser.NameClassRef
  {
    ParsedNameClass nameClass;
    boolean nameClassWasAttribute;
    String name;
    
    ElementState()
    {
      super();
    }
    
    void setName(String name)
    {
      this.name = name;
    }
    
    public void setNameClass(ParsedNameClass nc)
    {
      this.nameClass = nc;
    }
    
    void endAttributes()
      throws SAXException
    {
      if (this.name != null)
      {
        this.nameClass = SchemaParser.this.expandName(this.name, getNs(), null);
        this.nameClassWasAttribute = true;
      }
      else
      {
        new SchemaParser.NameClassChildState(SchemaParser.this, this, this).set();
      }
    }
    
    SchemaParser.State create()
    {
      return new ElementState(SchemaParser.this);
    }
    
    ParsedPattern buildPattern(List<ParsedPattern> patterns, Location loc, Annotations anno)
      throws SAXException
    {
      return SchemaParser.this.schemaBuilder.makeElement(this.nameClass, super.buildPattern(patterns, loc, null), loc, anno);
    }
    
    void endForeignChild(ParsedElementAnnotation ea)
    {
      if ((this.nameClassWasAttribute) || (this.childPatterns != null) || (this.nameClass == null)) {
        super.endForeignChild(ea);
      } else {
        this.nameClass = SchemaParser.this.nameClassBuilder.annotateAfter(this.nameClass, ea);
      }
    }
  }
  
  class RootState
    extends SchemaParser.PatternContainerState
  {
    IncludedGrammar grammar;
    
    RootState()
    {
      super();
    }
    
    RootState(IncludedGrammar grammar, Scope scope, String ns)
    {
      super();
      this.grammar = grammar;
      this.scope = scope;
      this.nsInherit = ns;
      this.datatypeLibrary = "";
    }
    
    SchemaParser.State create()
    {
      return new RootState(SchemaParser.this);
    }
    
    SchemaParser.State createChildState(String localName)
      throws SAXException
    {
      if (this.grammar == null) {
        return super.createChildState(localName);
      }
      if (localName.equals("grammar")) {
        return new SchemaParser.MergeGrammarState(SchemaParser.this, this.grammar);
      }
      SchemaParser.this.error("expected_grammar", localName);
      return null;
    }
    
    void checkForeignElement()
      throws SAXException
    {
      SchemaParser.this.error("root_bad_namespace_uri", "http://relaxng.org/ns/structure/1.0");
    }
    
    void endChild(ParsedPattern pattern)
    {
      SchemaParser.this.startPattern = pattern;
    }
    
    boolean isRelaxNGElement(String uri)
      throws SAXException
    {
      if (!uri.startsWith(SchemaParser.relaxngURIPrefix)) {
        return false;
      }
      if (!uri.equals("http://relaxng.org/ns/structure/1.0")) {
        SchemaParser.this.warning("wrong_uri_version", "http://relaxng.org/ns/structure/1.0".substring(SchemaParser.relaxngURIPrefix.length()), uri.substring(SchemaParser.relaxngURIPrefix.length()));
      }
      SchemaParser.this.relaxngURI = uri;
      return true;
    }
  }
  
  class NotAllowedState
    extends SchemaParser.EmptyContentState
  {
    NotAllowedState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new NotAllowedState(SchemaParser.this);
    }
    
    ParsedPattern makePattern()
    {
      return SchemaParser.this.schemaBuilder.makeNotAllowed(this.startLocation, this.annotations);
    }
  }
  
  class EmptyState
    extends SchemaParser.EmptyContentState
  {
    EmptyState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new EmptyState(SchemaParser.this);
    }
    
    ParsedPattern makePattern()
    {
      return SchemaParser.this.schemaBuilder.makeEmpty(this.startLocation, this.annotations);
    }
  }
  
  class TextState
    extends SchemaParser.EmptyContentState
  {
    TextState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new TextState(SchemaParser.this);
    }
    
    ParsedPattern makePattern()
    {
      return SchemaParser.this.schemaBuilder.makeText(this.startLocation, this.annotations);
    }
  }
  
  class ValueState
    extends SchemaParser.EmptyContentState
  {
    ValueState()
    {
      super();
    }
    
    final StringBuffer buf = new StringBuffer();
    String type;
    
    SchemaParser.State create()
    {
      return new ValueState(SchemaParser.this);
    }
    
    void setOtherAttribute(String name, String value)
      throws SAXException
    {
      if (name.equals("type")) {
        this.type = SchemaParser.this.checkNCName(value.trim());
      } else {
        super.setOtherAttribute(name, value);
      }
    }
    
    public void characters(char[] ch, int start, int len)
    {
      this.buf.append(ch, start, len);
    }
    
    void checkForeignElement()
      throws SAXException
    {
      SchemaParser.this.error("value_contains_foreign_element");
    }
    
    ParsedPattern makePattern()
      throws SAXException
    {
      if (this.type == null) {
        return makePattern("", "token");
      }
      return makePattern(this.datatypeLibrary, this.type);
    }
    
    void end()
      throws SAXException
    {
      mergeLeadingComments();
      super.end();
    }
    
    ParsedPattern makePattern(String datatypeLibrary, String type)
    {
      return SchemaParser.this.schemaBuilder.makeValue(datatypeLibrary, type, this.buf.toString(), SchemaParser.this.getContext(), getNs(), this.startLocation, this.annotations);
    }
  }
  
  class DataState
    extends SchemaParser.State
  {
    String type;
    
    DataState()
    {
      super();
    }
    
    ParsedPattern except = null;
    DataPatternBuilder dpb = null;
    
    SchemaParser.State create()
    {
      return new DataState(SchemaParser.this);
    }
    
    SchemaParser.State createChildState(String localName)
      throws SAXException
    {
      if (localName.equals("param"))
      {
        if (this.except != null) {
          SchemaParser.this.error("param_after_except");
        }
        return new SchemaParser.ParamState(SchemaParser.this, this.dpb);
      }
      if (localName.equals("except"))
      {
        if (this.except != null) {
          SchemaParser.this.error("multiple_except");
        }
        return new SchemaParser.ChoiceState(SchemaParser.this);
      }
      SchemaParser.this.error("expected_param_except", localName);
      return null;
    }
    
    void setOtherAttribute(String name, String value)
      throws SAXException
    {
      if (name.equals("type")) {
        this.type = SchemaParser.this.checkNCName(value.trim());
      } else {
        super.setOtherAttribute(name, value);
      }
    }
    
    void endAttributes()
      throws SAXException
    {
      if (this.type == null) {
        SchemaParser.this.error("missing_type_attribute");
      } else {
        this.dpb = SchemaParser.this.schemaBuilder.makeDataPatternBuilder(this.datatypeLibrary, this.type, this.startLocation);
      }
    }
    
    void endForeignChild(ParsedElementAnnotation ea)
    {
      this.dpb.annotation(ea);
    }
    
    void end()
      throws SAXException
    {
      ParsedPattern p;
      ParsedPattern p;
      if (this.dpb != null)
      {
        ParsedPattern p;
        if (this.except != null) {
          p = this.dpb.makePattern(this.except, this.startLocation, this.annotations);
        } else {
          p = this.dpb.makePattern(this.startLocation, this.annotations);
        }
      }
      else
      {
        p = SchemaParser.this.schemaBuilder.makeErrorPattern();
      }
      this.parent.endChild(p);
    }
    
    void endChild(ParsedPattern pattern)
    {
      this.except = pattern;
    }
  }
  
  class ParamState
    extends SchemaParser.State
  {
    private final StringBuffer buf = new StringBuffer();
    private final DataPatternBuilder dpb;
    private String name;
    
    ParamState(DataPatternBuilder dpb)
    {
      super();
      this.dpb = dpb;
    }
    
    SchemaParser.State create()
    {
      return new ParamState(SchemaParser.this, null);
    }
    
    void setName(String name)
      throws SAXException
    {
      this.name = SchemaParser.this.checkNCName(name);
    }
    
    void endAttributes()
      throws SAXException
    {
      if (this.name == null) {
        SchemaParser.this.error("missing_name_attribute");
      }
    }
    
    SchemaParser.State createChildState(String localName)
      throws SAXException
    {
      SchemaParser.this.error("expected_empty", localName);
      return null;
    }
    
    public void characters(char[] ch, int start, int len)
    {
      this.buf.append(ch, start, len);
    }
    
    void checkForeignElement()
      throws SAXException
    {
      SchemaParser.this.error("param_contains_foreign_element");
    }
    
    void end()
      throws SAXException
    {
      if (this.name == null) {
        return;
      }
      if (this.dpb == null) {
        return;
      }
      mergeLeadingComments();
      this.dpb.addParam(this.name, this.buf.toString(), SchemaParser.this.getContext(), getNs(), this.startLocation, this.annotations);
    }
  }
  
  class AttributeState
    extends SchemaParser.PatternContainerState
    implements SchemaParser.NameClassRef
  {
    ParsedNameClass nameClass;
    boolean nameClassWasAttribute;
    String name;
    
    AttributeState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new AttributeState(SchemaParser.this);
    }
    
    void setName(String name)
    {
      this.name = name;
    }
    
    public void setNameClass(ParsedNameClass nc)
    {
      this.nameClass = nc;
    }
    
    void endAttributes()
      throws SAXException
    {
      if (this.name != null)
      {
        String nsUse;
        String nsUse;
        if (this.ns != null) {
          nsUse = this.ns;
        } else {
          nsUse = "";
        }
        this.nameClass = SchemaParser.this.expandName(this.name, nsUse, null);
        this.nameClassWasAttribute = true;
      }
      else
      {
        new SchemaParser.NameClassChildState(SchemaParser.this, this, this).set();
      }
    }
    
    void endForeignChild(ParsedElementAnnotation ea)
    {
      if ((this.nameClassWasAttribute) || (this.childPatterns != null) || (this.nameClass == null)) {
        super.endForeignChild(ea);
      } else {
        this.nameClass = SchemaParser.this.nameClassBuilder.annotateAfter(this.nameClass, ea);
      }
    }
    
    void end()
      throws SAXException
    {
      if (this.childPatterns == null) {
        endChild(SchemaParser.this.schemaBuilder.makeText(this.startLocation, null));
      }
      super.end();
    }
    
    ParsedPattern buildPattern(List<ParsedPattern> patterns, Location loc, Annotations anno)
      throws SAXException
    {
      return SchemaParser.this.schemaBuilder.makeAttribute(this.nameClass, super.buildPattern(patterns, loc, null), loc, anno);
    }
    
    SchemaParser.State createChildState(String localName)
      throws SAXException
    {
      SchemaParser.State tem = super.createChildState(localName);
      if ((tem != null) && (this.childPatterns != null)) {
        SchemaParser.this.error("attribute_multi_pattern");
      }
      return tem;
    }
  }
  
  abstract class SinglePatternContainerState
    extends SchemaParser.PatternContainerState
  {
    SinglePatternContainerState()
    {
      super();
    }
    
    SchemaParser.State createChildState(String localName)
      throws SAXException
    {
      if (this.childPatterns == null) {
        return super.createChildState(localName);
      }
      SchemaParser.this.error("too_many_children");
      return null;
    }
  }
  
  class GrammarSectionState
    extends SchemaParser.State
  {
    GrammarSection section;
    
    GrammarSectionState()
    {
      super();
    }
    
    GrammarSectionState(GrammarSection section)
    {
      super();
      this.section = section;
    }
    
    SchemaParser.State create()
    {
      return new GrammarSectionState(SchemaParser.this, null);
    }
    
    SchemaParser.State createChildState(String localName)
      throws SAXException
    {
      if (localName.equals("define")) {
        return new SchemaParser.DefineState(SchemaParser.this, this.section);
      }
      if (localName.equals("start")) {
        return new SchemaParser.StartState(SchemaParser.this, this.section);
      }
      if (localName.equals("include"))
      {
        Include include = this.section.makeInclude();
        if (include != null) {
          return new SchemaParser.IncludeState(SchemaParser.this, include);
        }
      }
      if (localName.equals("div")) {
        return new SchemaParser.DivState(SchemaParser.this, this.section.makeDiv());
      }
      SchemaParser.this.error("expected_define", localName);
      
      return null;
    }
    
    void end()
      throws SAXException
    {
      if (this.comments != null)
      {
        this.section.topLevelComment(this.comments);
        this.comments = null;
      }
    }
    
    void endForeignChild(ParsedElementAnnotation ea)
    {
      this.section.topLevelAnnotation(ea);
    }
  }
  
  class DivState
    extends SchemaParser.GrammarSectionState
  {
    final Div div;
    
    DivState(Div div)
    {
      super(div);
      this.div = div;
    }
    
    void end()
      throws SAXException
    {
      super.end();
      this.div.endDiv(this.startLocation, this.annotations);
    }
  }
  
  class IncludeState
    extends SchemaParser.GrammarSectionState
  {
    String href;
    final Include include;
    
    IncludeState(Include include)
    {
      super(include);
      this.include = include;
    }
    
    void setOtherAttribute(String name, String value)
      throws SAXException
    {
      if (name.equals("href"))
      {
        this.href = value;
        SchemaParser.this.checkUri(this.href);
      }
      else
      {
        super.setOtherAttribute(name, value);
      }
    }
    
    void endAttributes()
      throws SAXException
    {
      if (this.href == null) {
        SchemaParser.this.error("missing_href_attribute");
      } else {
        this.href = SchemaParser.this.resolve(this.href);
      }
    }
    
    void end()
      throws SAXException
    {
      super.end();
      if (this.href != null) {
        try
        {
          this.include.endInclude(SchemaParser.this.parseable, this.href, getNs(), this.startLocation, this.annotations);
        }
        catch (IllegalSchemaException e) {}
      }
    }
  }
  
  class MergeGrammarState
    extends SchemaParser.GrammarSectionState
  {
    final IncludedGrammar grammar;
    
    MergeGrammarState(IncludedGrammar grammar)
    {
      super(grammar);
      this.grammar = grammar;
    }
    
    void end()
      throws SAXException
    {
      super.end();
      this.parent.endChild(this.grammar.endIncludedGrammar(this.startLocation, this.annotations));
    }
  }
  
  class GrammarState
    extends SchemaParser.GrammarSectionState
  {
    Grammar grammar;
    
    GrammarState()
    {
      super();
    }
    
    void setParent(SchemaParser.State parent)
    {
      super.setParent(parent);
      this.grammar = SchemaParser.this.schemaBuilder.makeGrammar(this.scope);
      this.section = this.grammar;
      this.scope = this.grammar;
    }
    
    SchemaParser.State create()
    {
      return new GrammarState(SchemaParser.this);
    }
    
    void end()
      throws SAXException
    {
      super.end();
      this.parent.endChild(this.grammar.endGrammar(this.startLocation, this.annotations));
    }
  }
  
  class RefState
    extends SchemaParser.EmptyContentState
  {
    String name;
    
    RefState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new RefState(SchemaParser.this);
    }
    
    void endAttributes()
      throws SAXException
    {
      if (this.name == null) {
        SchemaParser.this.error("missing_name_attribute");
      }
    }
    
    void setName(String name)
      throws SAXException
    {
      this.name = SchemaParser.this.checkNCName(name);
    }
    
    ParsedPattern makePattern()
      throws SAXException
    {
      if (this.name == null) {
        return SchemaParser.this.schemaBuilder.makeErrorPattern();
      }
      if (this.scope == null)
      {
        SchemaParser.this.error("ref_outside_grammar", this.name);
        return SchemaParser.this.schemaBuilder.makeErrorPattern();
      }
      return this.scope.makeRef(this.name, this.startLocation, this.annotations);
    }
  }
  
  class ParentRefState
    extends SchemaParser.RefState
  {
    ParentRefState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new ParentRefState(SchemaParser.this);
    }
    
    ParsedPattern makePattern()
      throws SAXException
    {
      if (this.name == null) {
        return SchemaParser.this.schemaBuilder.makeErrorPattern();
      }
      if (this.scope == null)
      {
        SchemaParser.this.error("parent_ref_outside_grammar", this.name);
        return SchemaParser.this.schemaBuilder.makeErrorPattern();
      }
      return this.scope.makeParentRef(this.name, this.startLocation, this.annotations);
    }
  }
  
  class ExternalRefState
    extends SchemaParser.EmptyContentState
  {
    String href;
    ParsedPattern includedPattern;
    
    ExternalRefState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new ExternalRefState(SchemaParser.this);
    }
    
    void setOtherAttribute(String name, String value)
      throws SAXException
    {
      if (name.equals("href"))
      {
        this.href = value;
        SchemaParser.this.checkUri(this.href);
      }
      else
      {
        super.setOtherAttribute(name, value);
      }
    }
    
    void endAttributes()
      throws SAXException
    {
      if (this.href == null) {
        SchemaParser.this.error("missing_href_attribute");
      } else {
        this.href = SchemaParser.this.resolve(this.href);
      }
    }
    
    ParsedPattern makePattern()
    {
      if (this.href != null) {
        try
        {
          return SchemaParser.this.schemaBuilder.makeExternalRef(SchemaParser.this.parseable, this.href, getNs(), this.scope, this.startLocation, this.annotations);
        }
        catch (IllegalSchemaException e) {}
      }
      return SchemaParser.this.schemaBuilder.makeErrorPattern();
    }
  }
  
  abstract class DefinitionState
    extends SchemaParser.PatternContainerState
  {
    GrammarSection.Combine combine = null;
    final GrammarSection section;
    
    DefinitionState(GrammarSection section)
    {
      super();
      this.section = section;
    }
    
    void setOtherAttribute(String name, String value)
      throws SAXException
    {
      if (name.equals("combine"))
      {
        value = value.trim();
        if (value.equals("choice")) {
          this.combine = GrammarSection.COMBINE_CHOICE;
        } else if (value.equals("interleave")) {
          this.combine = GrammarSection.COMBINE_INTERLEAVE;
        } else {
          SchemaParser.this.error("combine_attribute_bad_value", value);
        }
      }
      else
      {
        super.setOtherAttribute(name, value);
      }
    }
    
    ParsedPattern buildPattern(List<ParsedPattern> patterns, Location loc, Annotations anno)
      throws SAXException
    {
      return super.buildPattern(patterns, loc, null);
    }
  }
  
  class DefineState
    extends SchemaParser.DefinitionState
  {
    String name;
    
    DefineState(GrammarSection section)
    {
      super(section);
    }
    
    SchemaParser.State create()
    {
      return new DefineState(SchemaParser.this, null);
    }
    
    void setName(String name)
      throws SAXException
    {
      this.name = SchemaParser.this.checkNCName(name);
    }
    
    void endAttributes()
      throws SAXException
    {
      if (this.name == null) {
        SchemaParser.this.error("missing_name_attribute");
      }
    }
    
    void sendPatternToParent(ParsedPattern p)
    {
      if (this.name != null) {
        this.section.define(this.name, this.combine, p, this.startLocation, this.annotations);
      }
    }
  }
  
  class StartState
    extends SchemaParser.DefinitionState
  {
    StartState(GrammarSection section)
    {
      super(section);
    }
    
    SchemaParser.State create()
    {
      return new StartState(SchemaParser.this, null);
    }
    
    void sendPatternToParent(ParsedPattern p)
    {
      this.section.define("\000#start\000", this.combine, p, this.startLocation, this.annotations);
    }
    
    SchemaParser.State createChildState(String localName)
      throws SAXException
    {
      SchemaParser.State tem = super.createChildState(localName);
      if ((tem != null) && (this.childPatterns != null)) {
        SchemaParser.this.error("start_multi_pattern");
      }
      return tem;
    }
  }
  
  abstract class NameClassContainerState
    extends SchemaParser.State
  {
    NameClassContainerState()
    {
      super();
    }
    
    SchemaParser.State createChildState(String localName)
      throws SAXException
    {
      SchemaParser.State state = (SchemaParser.State)SchemaParser.this.nameClassTable.get(localName);
      if (state == null)
      {
        SchemaParser.this.error("expected_name_class", localName);
        return null;
      }
      return state.create();
    }
  }
  
  class NameClassChildState
    extends SchemaParser.NameClassContainerState
  {
    final SchemaParser.State prevState;
    final SchemaParser.NameClassRef nameClassRef;
    
    SchemaParser.State create()
    {
      return null;
    }
    
    NameClassChildState(SchemaParser.State prevState, SchemaParser.NameClassRef nameClassRef)
    {
      super();
      this.prevState = prevState;
      this.nameClassRef = nameClassRef;
      setParent(prevState.parent);
      this.ns = prevState.ns;
    }
    
    void endChild(ParsedNameClass nameClass)
    {
      this.nameClassRef.setNameClass(nameClass);
      this.prevState.set();
    }
    
    void endForeignChild(ParsedElementAnnotation ea)
    {
      this.prevState.endForeignChild(ea);
    }
    
    void end()
      throws SAXException
    {
      this.nameClassRef.setNameClass(SchemaParser.this.nameClassBuilder.makeErrorNameClass());
      SchemaParser.this.error("missing_name_class");
      this.prevState.set();
      this.prevState.end();
    }
  }
  
  abstract class NameClassBaseState
    extends SchemaParser.State
  {
    NameClassBaseState()
    {
      super();
    }
    
    abstract ParsedNameClass makeNameClass()
      throws SAXException;
    
    void end()
      throws SAXException
    {
      this.parent.endChild(makeNameClass());
    }
  }
  
  class NameState
    extends SchemaParser.NameClassBaseState
  {
    NameState()
    {
      super();
    }
    
    final StringBuffer buf = new StringBuffer();
    
    SchemaParser.State createChildState(String localName)
      throws SAXException
    {
      SchemaParser.this.error("expected_name", localName);
      return null;
    }
    
    SchemaParser.State create()
    {
      return new NameState(SchemaParser.this);
    }
    
    public void characters(char[] ch, int start, int len)
    {
      this.buf.append(ch, start, len);
    }
    
    void checkForeignElement()
      throws SAXException
    {
      SchemaParser.this.error("name_contains_foreign_element");
    }
    
    ParsedNameClass makeNameClass()
      throws SAXException
    {
      mergeLeadingComments();
      return SchemaParser.this.expandName(this.buf.toString().trim(), getNs(), this.annotations);
    }
  }
  
  class AnyNameState
    extends SchemaParser.NameClassBaseState
  {
    AnyNameState()
    {
      super();
    }
    
    ParsedNameClass except = null;
    
    SchemaParser.State create()
    {
      return new AnyNameState(SchemaParser.this);
    }
    
    SchemaParser.State createChildState(String localName)
      throws SAXException
    {
      if (localName.equals("except"))
      {
        if (this.except != null) {
          SchemaParser.this.error("multiple_except");
        }
        return new SchemaParser.NameClassChoiceState(SchemaParser.this, getContext());
      }
      SchemaParser.this.error("expected_except", localName);
      return null;
    }
    
    int getContext()
    {
      return 1;
    }
    
    ParsedNameClass makeNameClass()
    {
      if (this.except == null) {
        return makeNameClassNoExcept();
      }
      return makeNameClassExcept(this.except);
    }
    
    ParsedNameClass makeNameClassNoExcept()
    {
      return SchemaParser.this.nameClassBuilder.makeAnyName(this.startLocation, this.annotations);
    }
    
    ParsedNameClass makeNameClassExcept(ParsedNameClass except)
    {
      return SchemaParser.this.nameClassBuilder.makeAnyName(except, this.startLocation, this.annotations);
    }
    
    void endChild(ParsedNameClass nameClass)
    {
      this.except = nameClass;
    }
  }
  
  class NsNameState
    extends SchemaParser.AnyNameState
  {
    NsNameState()
    {
      super();
    }
    
    SchemaParser.State create()
    {
      return new NsNameState(SchemaParser.this);
    }
    
    ParsedNameClass makeNameClassNoExcept()
    {
      return SchemaParser.this.nameClassBuilder.makeNsName(getNs(), null, null);
    }
    
    ParsedNameClass makeNameClassExcept(ParsedNameClass except)
    {
      return SchemaParser.this.nameClassBuilder.makeNsName(getNs(), except, null, null);
    }
    
    int getContext()
    {
      return 2;
    }
  }
  
  class NameClassChoiceState
    extends SchemaParser.NameClassContainerState
  {
    private ParsedNameClass[] nameClasses;
    private int nNameClasses;
    private int context;
    
    NameClassChoiceState()
    {
      super();
      this.context = 0;
    }
    
    NameClassChoiceState(int context)
    {
      super();
      this.context = context;
    }
    
    void setParent(SchemaParser.State parent)
    {
      super.setParent(parent);
      if ((parent instanceof NameClassChoiceState)) {
        this.context = ((NameClassChoiceState)parent).context;
      }
    }
    
    SchemaParser.State create()
    {
      return new NameClassChoiceState(SchemaParser.this);
    }
    
    SchemaParser.State createChildState(String localName)
      throws SAXException
    {
      if (localName.equals("anyName"))
      {
        if (this.context >= 1)
        {
          SchemaParser.this.error(this.context == 1 ? "any_name_except_contains_any_name" : "ns_name_except_contains_any_name");
          
          return null;
        }
      }
      else if ((localName.equals("nsName")) && 
        (this.context == 2))
      {
        SchemaParser.this.error("ns_name_except_contains_ns_name");
        return null;
      }
      return super.createChildState(localName);
    }
    
    void endChild(ParsedNameClass nc)
    {
      if (this.nameClasses == null)
      {
        this.nameClasses = new ParsedNameClass[5];
      }
      else if (this.nNameClasses >= this.nameClasses.length)
      {
        ParsedNameClass[] newNameClasses = new ParsedNameClass[this.nameClasses.length * 2];
        System.arraycopy(this.nameClasses, 0, newNameClasses, 0, this.nameClasses.length);
        this.nameClasses = newNameClasses;
      }
      this.nameClasses[(this.nNameClasses++)] = nc;
    }
    
    void endForeignChild(ParsedElementAnnotation ea)
    {
      if (this.nNameClasses == 0) {
        super.endForeignChild(ea);
      } else {
        this.nameClasses[(this.nNameClasses - 1)] = SchemaParser.this.nameClassBuilder.annotateAfter(this.nameClasses[(this.nNameClasses - 1)], ea);
      }
    }
    
    void end()
      throws SAXException
    {
      if (this.nNameClasses == 0)
      {
        SchemaParser.this.error("missing_name_class");
        this.parent.endChild(SchemaParser.this.nameClassBuilder.makeErrorNameClass());
        return;
      }
      if (this.comments != null)
      {
        this.nameClasses[(this.nNameClasses - 1)] = SchemaParser.this.nameClassBuilder.commentAfter(this.nameClasses[(this.nNameClasses - 1)], this.comments);
        this.comments = null;
      }
      this.parent.endChild(SchemaParser.this.nameClassBuilder.makeChoice(Arrays.asList(this.nameClasses).subList(0, this.nNameClasses), this.startLocation, this.annotations));
    }
  }
  
  private void initPatternTable()
  {
    this.patternTable = new Hashtable();
    this.patternTable.put("zeroOrMore", new ZeroOrMoreState());
    this.patternTable.put("oneOrMore", new OneOrMoreState());
    this.patternTable.put("optional", new OptionalState());
    this.patternTable.put("list", new ListState());
    this.patternTable.put("choice", new ChoiceState());
    this.patternTable.put("interleave", new InterleaveState());
    this.patternTable.put("group", new GroupState());
    this.patternTable.put("mixed", new MixedState());
    this.patternTable.put("element", new ElementState());
    this.patternTable.put("attribute", new AttributeState());
    this.patternTable.put("empty", new EmptyState());
    this.patternTable.put("text", new TextState());
    this.patternTable.put("value", new ValueState());
    this.patternTable.put("data", new DataState());
    this.patternTable.put("notAllowed", new NotAllowedState());
    this.patternTable.put("grammar", new GrammarState());
    this.patternTable.put("ref", new RefState());
    this.patternTable.put("parentRef", new ParentRefState());
    this.patternTable.put("externalRef", new ExternalRefState());
  }
  
  private void initNameClassTable()
  {
    this.nameClassTable = new Hashtable();
    this.nameClassTable.put("name", new NameState());
    this.nameClassTable.put("anyName", new AnyNameState());
    this.nameClassTable.put("nsName", new NsNameState());
    this.nameClassTable.put("choice", new NameClassChoiceState());
  }
  
  public ParsedPattern getParsedPattern()
    throws IllegalSchemaException
  {
    if (this.hadError) {
      throw new IllegalSchemaException();
    }
    return this.startPattern;
  }
  
  private void error(String key)
    throws SAXException
  {
    error(key, this.locator);
  }
  
  private void error(String key, String arg)
    throws SAXException
  {
    error(key, arg, this.locator);
  }
  
  void error(String key, String arg1, String arg2)
    throws SAXException
  {
    error(key, arg1, arg2, this.locator);
  }
  
  private void error(String key, Locator loc)
    throws SAXException
  {
    error(new SAXParseException(localizer.message(key), loc));
  }
  
  private void error(String key, String arg, Locator loc)
    throws SAXException
  {
    error(new SAXParseException(localizer.message(key, arg), loc));
  }
  
  private void error(String key, String arg1, String arg2, Locator loc)
    throws SAXException
  {
    error(new SAXParseException(localizer.message(key, arg1, arg2), loc));
  }
  
  private void error(SAXParseException e)
    throws SAXException
  {
    this.hadError = true;
    if (this.eh != null) {
      this.eh.error(e);
    }
  }
  
  void warning(String key)
    throws SAXException
  {
    warning(key, this.locator);
  }
  
  private void warning(String key, String arg)
    throws SAXException
  {
    warning(key, arg, this.locator);
  }
  
  private void warning(String key, String arg1, String arg2)
    throws SAXException
  {
    warning(key, arg1, arg2, this.locator);
  }
  
  private void warning(String key, Locator loc)
    throws SAXException
  {
    warning(new SAXParseException(localizer.message(key), loc));
  }
  
  private void warning(String key, String arg, Locator loc)
    throws SAXException
  {
    warning(new SAXParseException(localizer.message(key, arg), loc));
  }
  
  private void warning(String key, String arg1, String arg2, Locator loc)
    throws SAXException
  {
    warning(new SAXParseException(localizer.message(key, arg1, arg2), loc));
  }
  
  private void warning(SAXParseException e)
    throws SAXException
  {
    if (this.eh != null) {
      this.eh.warning(e);
    }
  }
  
  SchemaParser(SAXParseable parseable, XMLReader xr, ErrorHandler eh, SchemaBuilder schemaBuilder, IncludedGrammar grammar, Scope scope, String inheritedNs)
    throws SAXException
  {
    this.parseable = parseable;
    this.xr = xr;
    this.eh = eh;
    this.schemaBuilder = schemaBuilder;
    this.nameClassBuilder = schemaBuilder.getNameClassBuilder();
    if (eh != null) {
      xr.setErrorHandler(eh);
    }
    xr.setDTDHandler(this.context);
    if (schemaBuilder.usesComments()) {
      try
      {
        xr.setProperty("http://xml.org/sax/properties/lexical-handler", new LexicalHandlerImpl());
      }
      catch (SAXNotRecognizedException e)
      {
        warning("no_comment_support", xr.getClass().getName());
      }
      catch (SAXNotSupportedException e)
      {
        warning("no_comment_support", xr.getClass().getName());
      }
    }
    initPatternTable();
    initNameClassTable();
    new RootState(grammar, scope, inheritedNs).set();
  }
  
  private Context getContext()
  {
    return this.context;
  }
  
  class LexicalHandlerImpl
    extends AbstractLexicalHandler
  {
    private boolean inDtd = false;
    
    LexicalHandlerImpl() {}
    
    public void startDTD(String s, String s1, String s2)
      throws SAXException
    {
      this.inDtd = true;
    }
    
    public void endDTD()
      throws SAXException
    {
      this.inDtd = false;
    }
    
    public void comment(char[] chars, int start, int length)
      throws SAXException
    {
      if (!this.inDtd) {
        ((SchemaParser.CommentHandler)SchemaParser.this.xr.getContentHandler()).comment(new String(chars, start, length));
      }
    }
  }
  
  private ParsedNameClass expandName(String name, String ns, Annotations anno)
    throws SAXException
  {
    int ic = name.indexOf(':');
    if (ic == -1) {
      return this.nameClassBuilder.makeName(ns, checkNCName(name), null, null, anno);
    }
    String prefix = checkNCName(name.substring(0, ic));
    String localName = checkNCName(name.substring(ic + 1));
    for (PrefixMapping tem = this.context.prefixMapping; tem != null; tem = tem.next) {
      if (tem.prefix.equals(prefix)) {
        return this.nameClassBuilder.makeName(tem.uri, localName, prefix, null, anno);
      }
    }
    error("undefined_prefix", prefix);
    return this.nameClassBuilder.makeName("", localName, null, null, anno);
  }
  
  private String findPrefix(String qName, String uri)
  {
    String prefix = null;
    if ((qName == null) || (qName.equals("")))
    {
      for (PrefixMapping p = this.context.prefixMapping; p != null; p = p.next) {
        if (p.uri.equals(uri))
        {
          prefix = p.prefix;
          break;
        }
      }
    }
    else
    {
      int off = qName.indexOf(':');
      if (off > 0) {
        prefix = qName.substring(0, off);
      }
    }
    return prefix;
  }
  
  private String checkNCName(String str)
    throws SAXException
  {
    if (!Naming.isNcname(str)) {
      error("invalid_ncname", str);
    }
    return str;
  }
  
  private String resolve(String systemId)
    throws SAXException
  {
    if (Uri.hasFragmentId(systemId)) {
      error("href_fragment_id");
    }
    systemId = Uri.escapeDisallowedChars(systemId);
    return Uri.resolve(this.xmlBaseHandler.getBaseUri(), systemId);
  }
  
  private Location makeLocation()
  {
    if (this.locator == null) {
      return null;
    }
    return this.schemaBuilder.makeLocation(this.locator.getSystemId(), this.locator.getLineNumber(), this.locator.getColumnNumber());
  }
  
  private void checkUri(String s)
    throws SAXException
  {
    if (!Uri.isValid(s)) {
      error("invalid_uri", s);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\xml\SchemaParser.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */