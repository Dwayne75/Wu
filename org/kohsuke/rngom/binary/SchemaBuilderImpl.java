package org.kohsuke.rngom.binary;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.xml.namespace.QName;
import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.DataPatternBuilder;
import org.kohsuke.rngom.ast.builder.Div;
import org.kohsuke.rngom.ast.builder.ElementAnnotationBuilder;
import org.kohsuke.rngom.ast.builder.Grammar;
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
import org.kohsuke.rngom.ast.util.LocatorImpl;
import org.kohsuke.rngom.dt.CascadingDatatypeLibraryFactory;
import org.kohsuke.rngom.dt.builtin.BuiltinDatatypeLibraryFactory;
import org.kohsuke.rngom.nc.NameClass;
import org.kohsuke.rngom.nc.NameClassBuilderImpl;
import org.kohsuke.rngom.parse.Context;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.util.Localizer;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.ValidationContext;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SchemaBuilderImpl
  implements SchemaBuilder, ElementAnnotationBuilder, CommentList
{
  private final SchemaBuilderImpl parent;
  private boolean hadError = false;
  private final SchemaPatternBuilder pb;
  private final DatatypeLibraryFactory datatypeLibraryFactory;
  private final String inheritNs;
  private final ErrorHandler eh;
  private final OpenIncludes openIncludes;
  private final NameClassBuilder ncb = new NameClassBuilderImpl();
  static final Localizer localizer = new Localizer(SchemaBuilderImpl.class);
  
  static class OpenIncludes
  {
    final String uri;
    final OpenIncludes parent;
    
    OpenIncludes(String uri, OpenIncludes parent)
    {
      this.uri = uri;
      this.parent = parent;
    }
  }
  
  public ParsedPattern expandPattern(ParsedPattern _pattern)
    throws BuildException, IllegalSchemaException
  {
    Pattern pattern = (Pattern)_pattern;
    if (!this.hadError) {
      try
      {
        pattern.checkRecursion(0);
        pattern = pattern.expand(this.pb);
        pattern.checkRestrictions(0, null, null);
        if (!this.hadError) {
          return pattern;
        }
      }
      catch (SAXParseException e)
      {
        error(e);
      }
      catch (SAXException e)
      {
        throw new BuildException(e);
      }
      catch (RestrictionViolationException e)
      {
        if (e.getName() != null) {
          error(e.getMessageId(), e.getName().toString(), e.getLocator());
        } else {
          error(e.getMessageId(), e.getLocator());
        }
      }
    }
    throw new IllegalSchemaException();
  }
  
  public SchemaBuilderImpl(ErrorHandler eh)
  {
    this(eh, new CascadingDatatypeLibraryFactory(new DatatypeLibraryLoader(), new BuiltinDatatypeLibraryFactory(new DatatypeLibraryLoader())), new SchemaPatternBuilder());
  }
  
  public SchemaBuilderImpl(ErrorHandler eh, DatatypeLibraryFactory datatypeLibraryFactory, SchemaPatternBuilder pb)
  {
    this.parent = null;
    this.eh = eh;
    this.datatypeLibraryFactory = datatypeLibraryFactory;
    this.pb = pb;
    this.inheritNs = "";
    this.openIncludes = null;
  }
  
  private SchemaBuilderImpl(String inheritNs, String uri, SchemaBuilderImpl parent)
  {
    this.parent = parent;
    this.eh = parent.eh;
    this.datatypeLibraryFactory = parent.datatypeLibraryFactory;
    this.pb = parent.pb;
    this.inheritNs = inheritNs;
    this.openIncludes = new OpenIncludes(uri, parent.openIncludes);
  }
  
  public NameClassBuilder getNameClassBuilder()
  {
    return this.ncb;
  }
  
  public ParsedPattern makeChoice(List patterns, Location loc, Annotations anno)
    throws BuildException
  {
    if (patterns.isEmpty()) {
      throw new IllegalArgumentException();
    }
    Pattern result = (Pattern)patterns.get(0);
    for (int i = 1; i < patterns.size(); i++) {
      result = this.pb.makeChoice(result, (Pattern)patterns.get(i));
    }
    return result;
  }
  
  public ParsedPattern makeInterleave(List patterns, Location loc, Annotations anno)
    throws BuildException
  {
    if (patterns.isEmpty()) {
      throw new IllegalArgumentException();
    }
    Pattern result = (Pattern)patterns.get(0);
    for (int i = 1; i < patterns.size(); i++) {
      result = this.pb.makeInterleave(result, (Pattern)patterns.get(i));
    }
    return result;
  }
  
  public ParsedPattern makeGroup(List patterns, Location loc, Annotations anno)
    throws BuildException
  {
    if (patterns.isEmpty()) {
      throw new IllegalArgumentException();
    }
    Pattern result = (Pattern)patterns.get(0);
    for (int i = 1; i < patterns.size(); i++) {
      result = this.pb.makeGroup(result, (Pattern)patterns.get(i));
    }
    return result;
  }
  
  public ParsedPattern makeOneOrMore(ParsedPattern p, Location loc, Annotations anno)
    throws BuildException
  {
    return this.pb.makeOneOrMore((Pattern)p);
  }
  
  public ParsedPattern makeZeroOrMore(ParsedPattern p, Location loc, Annotations anno)
    throws BuildException
  {
    return this.pb.makeZeroOrMore((Pattern)p);
  }
  
  public ParsedPattern makeOptional(ParsedPattern p, Location loc, Annotations anno)
    throws BuildException
  {
    return this.pb.makeOptional((Pattern)p);
  }
  
  public ParsedPattern makeList(ParsedPattern p, Location loc, Annotations anno)
    throws BuildException
  {
    return this.pb.makeList((Pattern)p, (Locator)loc);
  }
  
  public ParsedPattern makeMixed(ParsedPattern p, Location loc, Annotations anno)
    throws BuildException
  {
    return this.pb.makeMixed((Pattern)p);
  }
  
  public ParsedPattern makeEmpty(Location loc, Annotations anno)
  {
    return this.pb.makeEmpty();
  }
  
  public ParsedPattern makeNotAllowed(Location loc, Annotations anno)
  {
    return this.pb.makeUnexpandedNotAllowed();
  }
  
  public ParsedPattern makeText(Location loc, Annotations anno)
  {
    return this.pb.makeText();
  }
  
  public ParsedPattern makeErrorPattern()
  {
    return this.pb.makeError();
  }
  
  public ParsedPattern makeAttribute(ParsedNameClass nc, ParsedPattern p, Location loc, Annotations anno)
    throws BuildException
  {
    return this.pb.makeAttribute((NameClass)nc, (Pattern)p, (Locator)loc);
  }
  
  public ParsedPattern makeElement(ParsedNameClass nc, ParsedPattern p, Location loc, Annotations anno)
    throws BuildException
  {
    return this.pb.makeElement((NameClass)nc, (Pattern)p, (Locator)loc);
  }
  
  private class DummyDataPatternBuilder
    implements DataPatternBuilder
  {
    private DummyDataPatternBuilder() {}
    
    public void addParam(String name, String value, Context context, String ns, Location loc, Annotations anno)
      throws BuildException
    {}
    
    public ParsedPattern makePattern(Location loc, Annotations anno)
      throws BuildException
    {
      return SchemaBuilderImpl.this.pb.makeError();
    }
    
    public ParsedPattern makePattern(ParsedPattern except, Location loc, Annotations anno)
      throws BuildException
    {
      return SchemaBuilderImpl.this.pb.makeError();
    }
    
    public void annotation(ParsedElementAnnotation ea) {}
  }
  
  private class ValidationContextImpl
    implements ValidationContext
  {
    private ValidationContext vc;
    private String ns;
    
    ValidationContextImpl(ValidationContext vc, String ns)
    {
      this.vc = vc;
      this.ns = (ns.length() == 0 ? null : ns);
    }
    
    public String resolveNamespacePrefix(String prefix)
    {
      return prefix.length() == 0 ? this.ns : this.vc.resolveNamespacePrefix(prefix);
    }
    
    public String getBaseUri()
    {
      return this.vc.getBaseUri();
    }
    
    public boolean isUnparsedEntity(String entityName)
    {
      return this.vc.isUnparsedEntity(entityName);
    }
    
    public boolean isNotation(String notationName)
    {
      return this.vc.isNotation(notationName);
    }
  }
  
  private class DataPatternBuilderImpl
    implements DataPatternBuilder
  {
    private DatatypeBuilder dtb;
    
    DataPatternBuilderImpl(DatatypeBuilder dtb)
    {
      this.dtb = dtb;
    }
    
    public void addParam(String name, String value, Context context, String ns, Location loc, Annotations anno)
      throws BuildException
    {
      try
      {
        this.dtb.addParameter(name, value, new SchemaBuilderImpl.ValidationContextImpl(SchemaBuilderImpl.this, context, ns));
      }
      catch (DatatypeException e)
      {
        String detail = e.getMessage();
        int pos = e.getIndex();
        String displayedParam;
        String displayedParam;
        if (pos == -1) {
          displayedParam = null;
        } else {
          displayedParam = displayParam(value, pos);
        }
        if (displayedParam != null)
        {
          if (detail != null) {
            SchemaBuilderImpl.this.error("invalid_param_detail_display", detail, displayedParam, (Locator)loc);
          } else {
            SchemaBuilderImpl.this.error("invalid_param_display", displayedParam, (Locator)loc);
          }
        }
        else if (detail != null) {
          SchemaBuilderImpl.this.error("invalid_param_detail", detail, (Locator)loc);
        } else {
          SchemaBuilderImpl.this.error("invalid_param", (Locator)loc);
        }
      }
    }
    
    String displayParam(String value, int pos)
    {
      if (pos < 0) {
        pos = 0;
      } else if (pos > value.length()) {
        pos = value.length();
      }
      return SchemaBuilderImpl.localizer.message("display_param", value.substring(0, pos), value.substring(pos));
    }
    
    public ParsedPattern makePattern(Location loc, Annotations anno)
      throws BuildException
    {
      try
      {
        return SchemaBuilderImpl.this.pb.makeData(this.dtb.createDatatype());
      }
      catch (DatatypeException e)
      {
        String detail = e.getMessage();
        if (detail != null) {
          SchemaBuilderImpl.this.error("invalid_params_detail", detail, (Locator)loc);
        } else {
          SchemaBuilderImpl.this.error("invalid_params", (Locator)loc);
        }
      }
      return SchemaBuilderImpl.this.pb.makeError();
    }
    
    public ParsedPattern makePattern(ParsedPattern except, Location loc, Annotations anno)
      throws BuildException
    {
      try
      {
        return SchemaBuilderImpl.this.pb.makeDataExcept(this.dtb.createDatatype(), (Pattern)except, (Locator)loc);
      }
      catch (DatatypeException e)
      {
        String detail = e.getMessage();
        if (detail != null) {
          SchemaBuilderImpl.this.error("invalid_params_detail", detail, (Locator)loc);
        } else {
          SchemaBuilderImpl.this.error("invalid_params", (Locator)loc);
        }
      }
      return SchemaBuilderImpl.this.pb.makeError();
    }
    
    public void annotation(ParsedElementAnnotation ea) {}
  }
  
  public DataPatternBuilder makeDataPatternBuilder(String datatypeLibrary, String type, Location loc)
    throws BuildException
  {
    DatatypeLibrary dl = this.datatypeLibraryFactory.createDatatypeLibrary(datatypeLibrary);
    if (dl == null) {
      error("unrecognized_datatype_library", datatypeLibrary, (Locator)loc);
    } else {
      try
      {
        return new DataPatternBuilderImpl(dl.createDatatypeBuilder(type));
      }
      catch (DatatypeException e)
      {
        String detail = e.getMessage();
        if (detail != null) {
          error("unsupported_datatype_detail", datatypeLibrary, type, detail, (Locator)loc);
        } else {
          error("unrecognized_datatype", datatypeLibrary, type, (Locator)loc);
        }
      }
    }
    return new DummyDataPatternBuilder(null);
  }
  
  public ParsedPattern makeValue(String datatypeLibrary, String type, String value, Context context, String ns, Location loc, Annotations anno)
    throws BuildException
  {
    DatatypeLibrary dl = this.datatypeLibraryFactory.createDatatypeLibrary(datatypeLibrary);
    if (dl == null) {
      error("unrecognized_datatype_library", datatypeLibrary, (Locator)loc);
    } else {
      try
      {
        DatatypeBuilder dtb = dl.createDatatypeBuilder(type);
        try
        {
          Datatype dt = dtb.createDatatype();
          Object obj = dt.createValue(value, new ValidationContextImpl(context, ns));
          if (obj != null) {
            return this.pb.makeValue(dt, obj);
          }
          error("invalid_value", value, (Locator)loc);
        }
        catch (DatatypeException e)
        {
          String detail = e.getMessage();
          if (detail != null) {
            error("datatype_requires_param_detail", detail, (Locator)loc);
          } else {
            error("datatype_requires_param", (Locator)loc);
          }
        }
      }
      catch (DatatypeException e)
      {
        error("unrecognized_datatype", datatypeLibrary, type, (Locator)loc);
      }
    }
    return this.pb.makeError();
  }
  
  static class GrammarImpl
    implements Grammar, Div, IncludedGrammar
  {
    private final SchemaBuilderImpl sb;
    private final Hashtable defines;
    private final RefPattern startRef;
    private final Scope parent;
    
    private GrammarImpl(SchemaBuilderImpl sb, Scope parent)
    {
      this.sb = sb;
      this.parent = parent;
      this.defines = new Hashtable();
      this.startRef = new RefPattern(null);
    }
    
    protected GrammarImpl(SchemaBuilderImpl sb, GrammarImpl g)
    {
      this.sb = sb;
      this.parent = g.parent;
      this.startRef = g.startRef;
      this.defines = g.defines;
    }
    
    public ParsedPattern endGrammar(Location loc, Annotations anno)
      throws BuildException
    {
      Enumeration e = this.defines.keys();
      while (e.hasMoreElements())
      {
        String name = (String)e.nextElement();
        RefPattern rp = (RefPattern)this.defines.get(name);
        if (rp.getPattern() == null)
        {
          this.sb.error("reference_to_undefined", name, rp.getRefLocator());
          rp.setPattern(this.sb.pb.makeError());
        }
      }
      Pattern start = this.startRef.getPattern();
      if (start == null)
      {
        this.sb.error("missing_start_element", (Locator)loc);
        start = this.sb.pb.makeError();
      }
      return start;
    }
    
    public void endDiv(Location loc, Annotations anno)
      throws BuildException
    {}
    
    public ParsedPattern endIncludedGrammar(Location loc, Annotations anno)
      throws BuildException
    {
      return null;
    }
    
    public void define(String name, GrammarSection.Combine combine, ParsedPattern pattern, Location loc, Annotations anno)
      throws BuildException
    {
      define(lookup(name), combine, pattern, loc);
    }
    
    private void define(RefPattern rp, GrammarSection.Combine combine, ParsedPattern pattern, Location loc)
      throws BuildException
    {
      switch (rp.getReplacementStatus())
      {
      case 0: 
        if (combine == null)
        {
          if (rp.isCombineImplicit())
          {
            if (rp.getName() == null) {
              this.sb.error("duplicate_start", (Locator)loc);
            } else {
              this.sb.error("duplicate_define", rp.getName(), (Locator)loc);
            }
          }
          else {
            rp.setCombineImplicit();
          }
        }
        else
        {
          byte combineType = combine == COMBINE_CHOICE ? 1 : 2;
          if ((rp.getCombineType() != 0) && (rp.getCombineType() != combineType)) {
            if (rp.getName() == null) {
              this.sb.error("conflict_combine_start", (Locator)loc);
            } else {
              this.sb.error("conflict_combine_define", rp.getName(), (Locator)loc);
            }
          }
          rp.setCombineType(combineType);
        }
        Pattern p = (Pattern)pattern;
        if (rp.getPattern() == null) {
          rp.setPattern(p);
        } else if (rp.getCombineType() == 2) {
          rp.setPattern(this.sb.pb.makeInterleave(rp.getPattern(), p));
        } else {
          rp.setPattern(this.sb.pb.makeChoice(rp.getPattern(), p));
        }
        break;
      case 1: 
        rp.setReplacementStatus((byte)2);
        break;
      }
    }
    
    public void topLevelAnnotation(ParsedElementAnnotation ea)
      throws BuildException
    {}
    
    public void topLevelComment(CommentList comments)
      throws BuildException
    {}
    
    private RefPattern lookup(String name)
    {
      if (name == "\000#start\000") {
        return this.startRef;
      }
      return lookup1(name);
    }
    
    private RefPattern lookup1(String name)
    {
      RefPattern p = (RefPattern)this.defines.get(name);
      if (p == null)
      {
        p = new RefPattern(name);
        this.defines.put(name, p);
      }
      return p;
    }
    
    public ParsedPattern makeRef(String name, Location loc, Annotations anno)
      throws BuildException
    {
      RefPattern p = lookup1(name);
      if ((p.getRefLocator() == null) && (loc != null)) {
        p.setRefLocator((Locator)loc);
      }
      return p;
    }
    
    public ParsedPattern makeParentRef(String name, Location loc, Annotations anno)
      throws BuildException
    {
      if (this.parent == null)
      {
        this.sb.error("parent_ref_outside_grammar", (Locator)loc);
        return this.sb.makeErrorPattern();
      }
      return this.parent.makeRef(name, loc, anno);
    }
    
    public Div makeDiv()
    {
      return this;
    }
    
    public Include makeInclude()
    {
      return new SchemaBuilderImpl.IncludeImpl(this.sb, this, null);
    }
  }
  
  static class Override
  {
    RefPattern prp;
    Override next;
    byte replacementStatus;
    
    Override(RefPattern prp, Override next)
    {
      this.prp = prp;
      this.next = next;
    }
  }
  
  private static class IncludeImpl
    implements Include, Div
  {
    private SchemaBuilderImpl sb;
    private SchemaBuilderImpl.Override overrides;
    private SchemaBuilderImpl.GrammarImpl grammar;
    
    private IncludeImpl(SchemaBuilderImpl sb, SchemaBuilderImpl.GrammarImpl grammar)
    {
      this.sb = sb;
      this.grammar = grammar;
    }
    
    public void define(String name, GrammarSection.Combine combine, ParsedPattern pattern, Location loc, Annotations anno)
      throws BuildException
    {
      RefPattern rp = SchemaBuilderImpl.GrammarImpl.access$600(this.grammar, name);
      this.overrides = new SchemaBuilderImpl.Override(rp, this.overrides);
      SchemaBuilderImpl.GrammarImpl.access$700(this.grammar, rp, combine, pattern, loc);
    }
    
    public void endDiv(Location loc, Annotations anno)
      throws BuildException
    {}
    
    public void topLevelAnnotation(ParsedElementAnnotation ea)
      throws BuildException
    {}
    
    public void topLevelComment(CommentList comments)
      throws BuildException
    {}
    
    public Div makeDiv()
    {
      return this;
    }
    
    public void endInclude(Parseable current, String uri, String ns, Location loc, Annotations anno)
      throws BuildException
    {
      for (SchemaBuilderImpl.OpenIncludes inc = this.sb.openIncludes; inc != null; inc = inc.parent) {
        if (inc.uri.equals(uri))
        {
          this.sb.error("recursive_include", uri, (Locator)loc);
          return;
        }
      }
      for (SchemaBuilderImpl.Override o = this.overrides; o != null; o = o.next)
      {
        o.replacementStatus = o.prp.getReplacementStatus();
        o.prp.setReplacementStatus((byte)1);
      }
      try
      {
        SchemaBuilderImpl isb = new SchemaBuilderImpl(ns, uri, this.sb, null);
        current.parseInclude(uri, isb, new SchemaBuilderImpl.GrammarImpl(isb, this.grammar), ns);
        for (SchemaBuilderImpl.Override o = this.overrides; o != null; o = o.next) {
          if (o.prp.getReplacementStatus() == 1) {
            if (o.prp.getName() == null) {
              this.sb.error("missing_start_replacement", (Locator)loc);
            } else {
              this.sb.error("missing_define_replacement", o.prp.getName(), (Locator)loc);
            }
          }
        }
      }
      catch (IllegalSchemaException e)
      {
        SchemaBuilderImpl.Override o;
        this.sb.noteError();
      }
      finally
      {
        SchemaBuilderImpl.Override o;
        for (SchemaBuilderImpl.Override o = this.overrides; o != null; o = o.next) {
          o.prp.setReplacementStatus(o.replacementStatus);
        }
      }
    }
    
    public Include makeInclude()
    {
      return null;
    }
  }
  
  public Grammar makeGrammar(Scope parent)
  {
    return new GrammarImpl(this, parent, null);
  }
  
  public ParsedPattern annotate(ParsedPattern p, Annotations anno)
    throws BuildException
  {
    return p;
  }
  
  public ParsedPattern annotateAfter(ParsedPattern p, ParsedElementAnnotation e)
    throws BuildException
  {
    return p;
  }
  
  public ParsedPattern commentAfter(ParsedPattern p, CommentList comments)
    throws BuildException
  {
    return p;
  }
  
  public ParsedPattern makeExternalRef(Parseable current, String uri, String ns, Scope scope, Location loc, Annotations anno)
    throws BuildException
  {
    for (OpenIncludes inc = this.openIncludes; inc != null; inc = inc.parent) {
      if (inc.uri.equals(uri))
      {
        error("recursive_include", uri, (Locator)loc);
        return this.pb.makeError();
      }
    }
    try
    {
      return current.parseExternal(uri, new SchemaBuilderImpl(ns, uri, this), scope, ns);
    }
    catch (IllegalSchemaException e)
    {
      noteError();
    }
    return this.pb.makeError();
  }
  
  public Location makeLocation(String systemId, int lineNumber, int columnNumber)
  {
    return new LocatorImpl(systemId, lineNumber, columnNumber);
  }
  
  public Annotations makeAnnotations(CommentList comments, Context context)
  {
    return this;
  }
  
  public ElementAnnotationBuilder makeElementAnnotationBuilder(String ns, String localName, String prefix, Location loc, CommentList comments, Context context)
  {
    return this;
  }
  
  public CommentList makeCommentList()
  {
    return this;
  }
  
  public void addComment(String value, Location loc)
    throws BuildException
  {}
  
  public void addAttribute(String ns, String localName, String prefix, String value, Location loc) {}
  
  public void addElement(ParsedElementAnnotation ea) {}
  
  public void addComment(CommentList comments)
    throws BuildException
  {}
  
  public void addLeadingComment(CommentList comments)
    throws BuildException
  {}
  
  public ParsedElementAnnotation makeElementAnnotation()
  {
    return null;
  }
  
  public void addText(String value, Location loc, CommentList comments)
    throws BuildException
  {}
  
  public boolean usesComments()
  {
    return false;
  }
  
  private void error(SAXParseException message)
    throws BuildException
  {
    noteError();
    try
    {
      if (this.eh != null) {
        this.eh.error(message);
      }
    }
    catch (SAXException e)
    {
      throw new BuildException(e);
    }
  }
  
  private void warning(SAXParseException message)
    throws BuildException
  {
    try
    {
      if (this.eh != null) {
        this.eh.warning(message);
      }
    }
    catch (SAXException e)
    {
      throw new BuildException(e);
    }
  }
  
  private void error(String key, Locator loc)
    throws BuildException
  {
    error(new SAXParseException(localizer.message(key), loc));
  }
  
  private void error(String key, String arg, Locator loc)
    throws BuildException
  {
    error(new SAXParseException(localizer.message(key, arg), loc));
  }
  
  private void error(String key, String arg1, String arg2, Locator loc)
    throws BuildException
  {
    error(new SAXParseException(localizer.message(key, arg1, arg2), loc));
  }
  
  private void error(String key, String arg1, String arg2, String arg3, Locator loc)
    throws BuildException
  {
    error(new SAXParseException(localizer.message(key, new Object[] { arg1, arg2, arg3 }), loc));
  }
  
  private void noteError()
  {
    if ((!this.hadError) && (this.parent != null)) {
      this.parent.noteError();
    }
    this.hadError = true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\SchemaBuilderImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */