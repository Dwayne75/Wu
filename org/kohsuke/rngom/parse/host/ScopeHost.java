package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedPattern;

public class ScopeHost
  extends GrammarSectionHost
  implements Scope
{
  protected final Scope lhs;
  protected final Scope rhs;
  
  protected ScopeHost(Scope lhs, Scope rhs)
  {
    super(lhs, rhs);
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public ParsedPattern makeParentRef(String name, Location _loc, Annotations _anno)
    throws BuildException
  {
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeParentRef(name, loc.lhs, anno.lhs), this.rhs.makeParentRef(name, loc.rhs, anno.rhs));
  }
  
  public ParsedPattern makeRef(String name, Location _loc, Annotations _anno)
    throws BuildException
  {
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makeRef(name, loc.lhs, anno.lhs), this.rhs.makeRef(name, loc.rhs, anno.rhs));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\host\ScopeHost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */