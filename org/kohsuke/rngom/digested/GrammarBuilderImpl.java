package org.kohsuke.rngom.digested;

import java.util.ArrayList;
import java.util.List;
import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.Div;
import org.kohsuke.rngom.ast.builder.Grammar;
import org.kohsuke.rngom.ast.builder.GrammarSection.Combine;
import org.kohsuke.rngom.ast.builder.Include;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.ast.util.LocatorImpl;
import org.w3c.dom.Element;

class GrammarBuilderImpl
  implements Grammar, Div
{
  protected final DGrammarPattern grammar;
  protected final Scope parent;
  protected final DSchemaBuilderImpl sb;
  private List<Element> additionalElementAnnotations;
  
  public GrammarBuilderImpl(DGrammarPattern p, Scope parent, DSchemaBuilderImpl sb)
  {
    this.grammar = p;
    this.parent = parent;
    this.sb = sb;
  }
  
  public ParsedPattern endGrammar(Location loc, Annotations anno)
    throws BuildException
  {
    if (anno != null) {
      this.grammar.annotation = ((Annotation)anno).getResult();
    }
    if (this.additionalElementAnnotations != null)
    {
      if (this.grammar.annotation == null) {
        this.grammar.annotation = new DAnnotation();
      }
      this.grammar.annotation.contents.addAll(this.additionalElementAnnotations);
    }
    return this.grammar;
  }
  
  public void endDiv(Location loc, Annotations anno)
    throws BuildException
  {}
  
  public void define(String name, GrammarSection.Combine combine, ParsedPattern pattern, Location loc, Annotations anno)
    throws BuildException
  {
    if (name == "\000#start\000")
    {
      this.grammar.start = ((DPattern)pattern);
    }
    else
    {
      DDefine d = this.grammar.getOrAdd(name);
      d.setPattern((DPattern)pattern);
      if (anno != null) {
        d.annotation = ((Annotation)anno).getResult();
      }
    }
  }
  
  public void topLevelAnnotation(ParsedElementAnnotation ea)
    throws BuildException
  {
    if (this.additionalElementAnnotations == null) {
      this.additionalElementAnnotations = new ArrayList();
    }
    this.additionalElementAnnotations.add(((ElementWrapper)ea).element);
  }
  
  public void topLevelComment(CommentList comments)
    throws BuildException
  {}
  
  public Div makeDiv()
  {
    return this;
  }
  
  public Include makeInclude()
  {
    return new IncludeImpl(this.grammar, this.parent, this.sb);
  }
  
  public ParsedPattern makeParentRef(String name, Location loc, Annotations anno)
    throws BuildException
  {
    return this.parent.makeRef(name, loc, anno);
  }
  
  public ParsedPattern makeRef(String name, Location loc, Annotations anno)
    throws BuildException
  {
    return DSchemaBuilderImpl.wrap(new DRefPattern(this.grammar.getOrAdd(name)), (LocatorImpl)loc, (Annotation)anno);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\GrammarBuilderImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */