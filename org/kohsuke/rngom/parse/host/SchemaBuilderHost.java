package org.kohsuke.rngom.parse.host;

import java.util.ArrayList;
import java.util.List;
import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.DataPatternBuilder;
import org.kohsuke.rngom.ast.builder.ElementAnnotationBuilder;
import org.kohsuke.rngom.ast.builder.Grammar;
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

public class SchemaBuilderHost
  extends Base
  implements SchemaBuilder
{
  final SchemaBuilder lhs;
  final SchemaBuilder rhs;
  
  public SchemaBuilderHost(SchemaBuilder lhs, SchemaBuilder rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public ParsedPattern annotate(ParsedPattern _p, Annotations _anno)
    throws BuildException
  {
    ParsedPatternHost p = (ParsedPatternHost)_p;
    AnnotationsHost a = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.annotate(p.lhs, a.lhs), this.rhs.annotate(p.lhs, a.lhs));
  }
  
  public ParsedPattern annotateAfter(ParsedPattern _p, ParsedElementAnnotation _e)
    throws BuildException
  {
    ParsedPatternHost p = (ParsedPatternHost)_p;
    ParsedElementAnnotationHost e = (ParsedElementAnnotationHost)_e;
    return new ParsedPatternHost(this.lhs.annotateAfter(p.lhs, e.lhs), this.rhs.annotateAfter(p.rhs, e.rhs));
  }
  
  public ParsedPattern commentAfter(ParsedPattern _p, CommentList _comments)
    throws BuildException
  {
    ParsedPatternHost p = (ParsedPatternHost)_p;
    CommentListHost comments = (CommentListHost)_comments;
    
    return new ParsedPatternHost(this.lhs.commentAfter(p.lhs, comments == null ? null : comments.lhs), this.rhs.commentAfter(p.rhs, comments == null ? null : comments.rhs));
  }
  
  public ParsedPattern expandPattern(ParsedPattern _p)
    throws BuildException, IllegalSchemaException
  {
    ParsedPatternHost p = (ParsedPatternHost)_p;
    return new ParsedPatternHost(this.lhs.expandPattern(p.lhs), this.rhs.expandPattern(p.rhs));
  }
  
  public NameClassBuilder getNameClassBuilder()
    throws BuildException
  {
    return new NameClassBuilderHost(this.lhs.getNameClassBuilder(), this.rhs.getNameClassBuilder());
  }
  
  public Annotations makeAnnotations(CommentList _comments, Context context)
  {
    CommentListHost comments = (CommentListHost)_comments;
    Annotations l = this.lhs.makeAnnotations(comments != null ? comments.lhs : null, context);
    Annotations r = this.rhs.makeAnnotations(comments != null ? comments.rhs : null, context);
    if ((l == null) || (r == null)) {
      throw new IllegalArgumentException("annotations cannot be null");
    }
    return new AnnotationsHost(l, r);
  }
  
  public ParsedPattern makeAttribute(ParsedNameClass _nc, ParsedPattern _p, Location _loc, Annotations _anno)
    throws BuildException
  {
    ParsedNameClassHost nc = (ParsedNameClassHost)_nc;
    ParsedPatternHost p = (ParsedPatternHost)_p;
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeAttribute(nc.lhs, p.lhs, loc.lhs, anno.lhs), this.rhs.makeAttribute(nc.rhs, p.rhs, loc.rhs, anno.rhs));
  }
  
  public ParsedPattern makeChoice(List patterns, Location _loc, Annotations _anno)
    throws BuildException
  {
    List<ParsedPattern> lp = new ArrayList();
    List<ParsedPattern> rp = new ArrayList();
    for (int i = 0; i < patterns.size(); i++)
    {
      lp.add(((ParsedPatternHost)patterns.get(i)).lhs);
      rp.add(((ParsedPatternHost)patterns.get(i)).rhs);
    }
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeChoice(lp, loc.lhs, anno.lhs), this.rhs.makeChoice(rp, loc.rhs, anno.rhs));
  }
  
  public CommentList makeCommentList()
  {
    return new CommentListHost(this.lhs.makeCommentList(), this.rhs.makeCommentList());
  }
  
  public DataPatternBuilder makeDataPatternBuilder(String datatypeLibrary, String type, Location _loc)
    throws BuildException
  {
    LocationHost loc = cast(_loc);
    
    return new DataPatternBuilderHost(this.lhs.makeDataPatternBuilder(datatypeLibrary, type, loc.lhs), this.rhs.makeDataPatternBuilder(datatypeLibrary, type, loc.rhs));
  }
  
  public ParsedPattern makeElement(ParsedNameClass _nc, ParsedPattern _p, Location _loc, Annotations _anno)
    throws BuildException
  {
    ParsedNameClassHost nc = (ParsedNameClassHost)_nc;
    ParsedPatternHost p = (ParsedPatternHost)_p;
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeElement(nc.lhs, p.lhs, loc.lhs, anno.lhs), this.rhs.makeElement(nc.rhs, p.rhs, loc.rhs, anno.rhs));
  }
  
  public ElementAnnotationBuilder makeElementAnnotationBuilder(String ns, String localName, String prefix, Location _loc, CommentList _comments, Context context)
  {
    LocationHost loc = cast(_loc);
    CommentListHost comments = (CommentListHost)_comments;
    
    return new ElementAnnotationBuilderHost(this.lhs.makeElementAnnotationBuilder(ns, localName, prefix, loc.lhs, comments == null ? null : comments.lhs, context), this.rhs.makeElementAnnotationBuilder(ns, localName, prefix, loc.rhs, comments == null ? null : comments.rhs, context));
  }
  
  public ParsedPattern makeEmpty(Location _loc, Annotations _anno)
  {
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeEmpty(loc.lhs, anno.lhs), this.rhs.makeEmpty(loc.rhs, anno.rhs));
  }
  
  public ParsedPattern makeErrorPattern()
  {
    return new ParsedPatternHost(this.lhs.makeErrorPattern(), this.rhs.makeErrorPattern());
  }
  
  public ParsedPattern makeExternalRef(Parseable current, String uri, String ns, Scope _scope, Location _loc, Annotations _anno)
    throws BuildException, IllegalSchemaException
  {
    ScopeHost scope = (ScopeHost)_scope;
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeExternalRef(current, uri, ns, scope.lhs, loc.lhs, anno.lhs), this.rhs.makeExternalRef(current, uri, ns, scope.rhs, loc.rhs, anno.rhs));
  }
  
  public Grammar makeGrammar(Scope _parent)
  {
    ScopeHost parent = (ScopeHost)_parent;
    
    return new GrammarHost(this.lhs.makeGrammar(parent != null ? parent.lhs : null), this.rhs.makeGrammar(parent != null ? parent.rhs : null));
  }
  
  public ParsedPattern makeGroup(List patterns, Location _loc, Annotations _anno)
    throws BuildException
  {
    List<ParsedPattern> lp = new ArrayList();
    List<ParsedPattern> rp = new ArrayList();
    for (int i = 0; i < patterns.size(); i++)
    {
      lp.add(((ParsedPatternHost)patterns.get(i)).lhs);
      rp.add(((ParsedPatternHost)patterns.get(i)).rhs);
    }
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeGroup(lp, loc.lhs, anno.lhs), this.rhs.makeGroup(rp, loc.rhs, anno.rhs));
  }
  
  public ParsedPattern makeInterleave(List patterns, Location _loc, Annotations _anno)
    throws BuildException
  {
    List<ParsedPattern> lp = new ArrayList();
    List<ParsedPattern> rp = new ArrayList();
    for (int i = 0; i < patterns.size(); i++)
    {
      lp.add(((ParsedPatternHost)patterns.get(i)).lhs);
      rp.add(((ParsedPatternHost)patterns.get(i)).rhs);
    }
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeInterleave(lp, loc.lhs, anno.lhs), this.rhs.makeInterleave(rp, loc.rhs, anno.rhs));
  }
  
  public ParsedPattern makeList(ParsedPattern _p, Location _loc, Annotations _anno)
    throws BuildException
  {
    ParsedPatternHost p = (ParsedPatternHost)_p;
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeList(p.lhs, loc.lhs, anno.lhs), this.rhs.makeList(p.rhs, loc.rhs, anno.rhs));
  }
  
  public Location makeLocation(String systemId, int lineNumber, int columnNumber)
  {
    return new LocationHost(this.lhs.makeLocation(systemId, lineNumber, columnNumber), this.rhs.makeLocation(systemId, lineNumber, columnNumber));
  }
  
  public ParsedPattern makeMixed(ParsedPattern _p, Location _loc, Annotations _anno)
    throws BuildException
  {
    ParsedPatternHost p = (ParsedPatternHost)_p;
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeMixed(p.lhs, loc.lhs, anno.lhs), this.rhs.makeMixed(p.rhs, loc.rhs, anno.rhs));
  }
  
  public ParsedPattern makeNotAllowed(Location _loc, Annotations _anno)
  {
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeNotAllowed(loc.lhs, anno.lhs), this.rhs.makeNotAllowed(loc.rhs, anno.rhs));
  }
  
  public ParsedPattern makeOneOrMore(ParsedPattern _p, Location _loc, Annotations _anno)
    throws BuildException
  {
    ParsedPatternHost p = (ParsedPatternHost)_p;
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeOneOrMore(p.lhs, loc.lhs, anno.lhs), this.rhs.makeOneOrMore(p.rhs, loc.rhs, anno.rhs));
  }
  
  public ParsedPattern makeZeroOrMore(ParsedPattern _p, Location _loc, Annotations _anno)
    throws BuildException
  {
    ParsedPatternHost p = (ParsedPatternHost)_p;
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeZeroOrMore(p.lhs, loc.lhs, anno.lhs), this.rhs.makeZeroOrMore(p.rhs, loc.rhs, anno.rhs));
  }
  
  public ParsedPattern makeOptional(ParsedPattern _p, Location _loc, Annotations _anno)
    throws BuildException
  {
    ParsedPatternHost p = (ParsedPatternHost)_p;
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeOptional(p.lhs, loc.lhs, anno.lhs), this.rhs.makeOptional(p.rhs, loc.rhs, anno.rhs));
  }
  
  public ParsedPattern makeText(Location _loc, Annotations _anno)
  {
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeText(loc.lhs, anno.lhs), this.rhs.makeText(loc.rhs, anno.rhs));
  }
  
  public ParsedPattern makeValue(String datatypeLibrary, String type, String value, Context c, String ns, Location _loc, Annotations _anno)
    throws BuildException
  {
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeValue(datatypeLibrary, type, value, c, ns, loc.lhs, anno.lhs), this.rhs.makeValue(datatypeLibrary, type, value, c, ns, loc.rhs, anno.rhs));
  }
  
  public boolean usesComments()
  {
    return (this.lhs.usesComments()) || (this.rhs.usesComments());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\host\SchemaBuilderHost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */