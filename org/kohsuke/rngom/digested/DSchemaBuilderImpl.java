package org.kohsuke.rngom.digested;

import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.DataPatternBuilder;
import org.kohsuke.rngom.ast.builder.ElementAnnotationBuilder;
import org.kohsuke.rngom.ast.builder.Grammar;
import org.kohsuke.rngom.ast.builder.NameClassBuilder;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.ast.util.LocatorImpl;
import org.kohsuke.rngom.nc.NameClass;
import org.kohsuke.rngom.nc.NameClassBuilderImpl;
import org.kohsuke.rngom.parse.Context;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;
import org.w3c.dom.Document;

public class DSchemaBuilderImpl
  implements SchemaBuilder<NameClass, DPattern, ElementWrapper, LocatorImpl, Annotation, CommentListImpl>
{
  private final NameClassBuilder ncb = new NameClassBuilderImpl();
  private final Document dom;
  
  public DSchemaBuilderImpl()
  {
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      this.dom = dbf.newDocumentBuilder().newDocument();
    }
    catch (ParserConfigurationException e)
    {
      throw new InternalError(e.getMessage());
    }
  }
  
  public NameClassBuilder getNameClassBuilder()
    throws BuildException
  {
    return this.ncb;
  }
  
  static DPattern wrap(DPattern p, LocatorImpl loc, Annotation anno)
  {
    p.location = loc;
    if (anno != null) {
      p.annotation = anno.getResult();
    }
    return p;
  }
  
  static DContainerPattern addAll(DContainerPattern parent, List<DPattern> children)
  {
    for (DPattern c : children) {
      parent.add(c);
    }
    return parent;
  }
  
  static DUnaryPattern addBody(DUnaryPattern parent, ParsedPattern _body, LocatorImpl loc)
  {
    parent.setChild((DPattern)_body);
    return parent;
  }
  
  public DPattern makeChoice(List<DPattern> patterns, LocatorImpl loc, Annotation anno)
    throws BuildException
  {
    return wrap(addAll(new DChoicePattern(), patterns), loc, anno);
  }
  
  public DPattern makeInterleave(List<DPattern> patterns, LocatorImpl loc, Annotation anno)
    throws BuildException
  {
    return wrap(addAll(new DInterleavePattern(), patterns), loc, anno);
  }
  
  public DPattern makeGroup(List<DPattern> patterns, LocatorImpl loc, Annotation anno)
    throws BuildException
  {
    return wrap(addAll(new DGroupPattern(), patterns), loc, anno);
  }
  
  public DPattern makeOneOrMore(DPattern p, LocatorImpl loc, Annotation anno)
    throws BuildException
  {
    return wrap(addBody(new DOneOrMorePattern(), p, loc), loc, anno);
  }
  
  public DPattern makeZeroOrMore(DPattern p, LocatorImpl loc, Annotation anno)
    throws BuildException
  {
    return wrap(addBody(new DZeroOrMorePattern(), p, loc), loc, anno);
  }
  
  public DPattern makeOptional(DPattern p, LocatorImpl loc, Annotation anno)
    throws BuildException
  {
    return wrap(addBody(new DOptionalPattern(), p, loc), loc, anno);
  }
  
  public DPattern makeList(DPattern p, LocatorImpl loc, Annotation anno)
    throws BuildException
  {
    return wrap(addBody(new DListPattern(), p, loc), loc, anno);
  }
  
  public DPattern makeMixed(DPattern p, LocatorImpl loc, Annotation anno)
    throws BuildException
  {
    return wrap(addBody(new DMixedPattern(), p, loc), loc, anno);
  }
  
  public DPattern makeEmpty(LocatorImpl loc, Annotation anno)
  {
    return wrap(new DEmptyPattern(), loc, anno);
  }
  
  public DPattern makeNotAllowed(LocatorImpl loc, Annotation anno)
  {
    return wrap(new DNotAllowedPattern(), loc, anno);
  }
  
  public DPattern makeText(LocatorImpl loc, Annotation anno)
  {
    return wrap(new DTextPattern(), loc, anno);
  }
  
  public DPattern makeAttribute(NameClass nc, DPattern p, LocatorImpl loc, Annotation anno)
    throws BuildException
  {
    return wrap(addBody(new DAttributePattern(nc), p, loc), loc, anno);
  }
  
  public DPattern makeElement(NameClass nc, DPattern p, LocatorImpl loc, Annotation anno)
    throws BuildException
  {
    return wrap(addBody(new DElementPattern(nc), p, loc), loc, anno);
  }
  
  public DataPatternBuilder makeDataPatternBuilder(String datatypeLibrary, String type, LocatorImpl loc)
    throws BuildException
  {
    return new DataPatternBuilderImpl(datatypeLibrary, type, loc);
  }
  
  public DPattern makeValue(String datatypeLibrary, String type, String value, Context c, String ns, LocatorImpl loc, Annotation anno)
    throws BuildException
  {
    return wrap(new DValuePattern(datatypeLibrary, type, value, c.copy(), ns), loc, anno);
  }
  
  public Grammar makeGrammar(Scope parent)
  {
    return new GrammarBuilderImpl(new DGrammarPattern(), parent, this);
  }
  
  public DPattern annotate(DPattern p, Annotation anno)
    throws BuildException
  {
    return p;
  }
  
  public DPattern annotateAfter(DPattern p, ElementWrapper e)
    throws BuildException
  {
    return p;
  }
  
  public DPattern commentAfter(DPattern p, CommentListImpl comments)
    throws BuildException
  {
    return p;
  }
  
  public DPattern makeExternalRef(Parseable current, String uri, String ns, Scope<DPattern, ElementWrapper, LocatorImpl, Annotation, CommentListImpl> scope, LocatorImpl loc, Annotation anno)
    throws BuildException, IllegalSchemaException
  {
    return null;
  }
  
  public LocatorImpl makeLocation(String systemId, int lineNumber, int columnNumber)
  {
    return new LocatorImpl(systemId, lineNumber, columnNumber);
  }
  
  public Annotation makeAnnotations(CommentListImpl comments, Context context)
  {
    return new Annotation();
  }
  
  public ElementAnnotationBuilder makeElementAnnotationBuilder(String ns, String localName, String prefix, LocatorImpl loc, CommentListImpl comments, Context context)
  {
    String qname;
    String qname;
    if (prefix == null) {
      qname = localName;
    } else {
      qname = prefix + ':' + localName;
    }
    return new ElementAnnotationBuilderImpl(this.dom.createElementNS(ns, qname));
  }
  
  public CommentListImpl makeCommentList()
  {
    return null;
  }
  
  public DPattern makeErrorPattern()
  {
    return new DNotAllowedPattern();
  }
  
  public boolean usesComments()
  {
    return false;
  }
  
  public DPattern expandPattern(DPattern p)
    throws BuildException, IllegalSchemaException
  {
    return p;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DSchemaBuilderImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */