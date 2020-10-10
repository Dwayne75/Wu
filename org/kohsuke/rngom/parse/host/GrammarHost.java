package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.Grammar;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedPattern;

public class GrammarHost
  extends ScopeHost
  implements Grammar
{
  final Grammar lhs;
  final Grammar rhs;
  
  public GrammarHost(Grammar lhs, Grammar rhs)
  {
    super(lhs, rhs);
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public ParsedPattern endGrammar(Location _loc, Annotations _anno)
    throws BuildException
  {
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.endGrammar(loc.lhs, anno.lhs), this.rhs.endGrammar(loc.rhs, anno.rhs));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\host\GrammarHost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */