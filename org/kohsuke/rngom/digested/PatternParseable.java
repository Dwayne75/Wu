package org.kohsuke.rngom.digested;

import java.util.ArrayList;
import java.util.List;
import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.IncludedGrammar;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedNameClass;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.nc.NameClass;
import org.kohsuke.rngom.parse.Parseable;
import org.xml.sax.Locator;

final class PatternParseable
  implements Parseable
{
  private final DPattern pattern;
  
  public PatternParseable(DPattern p)
  {
    this.pattern = p;
  }
  
  public ParsedPattern parse(SchemaBuilder sb)
    throws BuildException
  {
    return (ParsedPattern)this.pattern.accept(new Parser(sb));
  }
  
  public ParsedPattern parseInclude(String uri, SchemaBuilder f, IncludedGrammar g, String inheritedNs)
    throws BuildException
  {
    throw new UnsupportedOperationException();
  }
  
  public ParsedPattern parseExternal(String uri, SchemaBuilder f, Scope s, String inheritedNs)
    throws BuildException
  {
    throw new UnsupportedOperationException();
  }
  
  private static class Parser
    implements DPatternVisitor<ParsedPattern>
  {
    private final SchemaBuilder sb;
    
    public Parser(SchemaBuilder sb)
    {
      this.sb = sb;
    }
    
    private Annotations parseAnnotation(DPattern p)
    {
      return null;
    }
    
    private Location parseLocation(DPattern p)
    {
      Locator l = p.getLocation();
      return this.sb.makeLocation(l.getSystemId(), l.getLineNumber(), l.getColumnNumber());
    }
    
    private ParsedNameClass parseNameClass(NameClass name)
    {
      return name;
    }
    
    public ParsedPattern onAttribute(DAttributePattern p)
    {
      return this.sb.makeAttribute(parseNameClass(p.getName()), (ParsedPattern)p.getChild().accept(this), parseLocation(p), parseAnnotation(p));
    }
    
    public ParsedPattern onChoice(DChoicePattern p)
    {
      List<ParsedPattern> kids = new ArrayList();
      for (DPattern c = p.firstChild(); c != null; c = c.next) {
        kids.add((ParsedPattern)c.accept(this));
      }
      return this.sb.makeChoice(kids, parseLocation(p), null);
    }
    
    public ParsedPattern onData(DDataPattern p)
    {
      return null;
    }
    
    public ParsedPattern onElement(DElementPattern p)
    {
      return this.sb.makeElement(parseNameClass(p.getName()), (ParsedPattern)p.getChild().accept(this), parseLocation(p), parseAnnotation(p));
    }
    
    public ParsedPattern onEmpty(DEmptyPattern p)
    {
      return this.sb.makeEmpty(parseLocation(p), parseAnnotation(p));
    }
    
    public ParsedPattern onGrammar(DGrammarPattern p)
    {
      return null;
    }
    
    public ParsedPattern onGroup(DGroupPattern p)
    {
      List<ParsedPattern> kids = new ArrayList();
      for (DPattern c = p.firstChild(); c != null; c = c.next) {
        kids.add((ParsedPattern)c.accept(this));
      }
      return this.sb.makeGroup(kids, parseLocation(p), null);
    }
    
    public ParsedPattern onInterleave(DInterleavePattern p)
    {
      List<ParsedPattern> kids = new ArrayList();
      for (DPattern c = p.firstChild(); c != null; c = c.next) {
        kids.add((ParsedPattern)c.accept(this));
      }
      return this.sb.makeInterleave(kids, parseLocation(p), null);
    }
    
    public ParsedPattern onList(DListPattern p)
    {
      return this.sb.makeList((ParsedPattern)p.getChild().accept(this), parseLocation(p), parseAnnotation(p));
    }
    
    public ParsedPattern onMixed(DMixedPattern p)
    {
      return this.sb.makeMixed((ParsedPattern)p.getChild().accept(this), parseLocation(p), parseAnnotation(p));
    }
    
    public ParsedPattern onNotAllowed(DNotAllowedPattern p)
    {
      return this.sb.makeNotAllowed(parseLocation(p), parseAnnotation(p));
    }
    
    public ParsedPattern onOneOrMore(DOneOrMorePattern p)
    {
      return this.sb.makeOneOrMore((ParsedPattern)p.getChild().accept(this), parseLocation(p), parseAnnotation(p));
    }
    
    public ParsedPattern onOptional(DOptionalPattern p)
    {
      return this.sb.makeOptional((ParsedPattern)p.getChild().accept(this), parseLocation(p), parseAnnotation(p));
    }
    
    public ParsedPattern onRef(DRefPattern p)
    {
      return null;
    }
    
    public ParsedPattern onText(DTextPattern p)
    {
      return this.sb.makeText(parseLocation(p), parseAnnotation(p));
    }
    
    public ParsedPattern onValue(DValuePattern p)
    {
      return this.sb.makeValue(p.getDatatypeLibrary(), p.getType(), p.getValue(), p.getContext(), p.getNs(), parseLocation(p), parseAnnotation(p));
    }
    
    public ParsedPattern onZeroOrMore(DZeroOrMorePattern p)
    {
      return this.sb.makeZeroOrMore((ParsedPattern)p.getChild().accept(this), parseLocation(p), parseAnnotation(p));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\PatternParseable.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */