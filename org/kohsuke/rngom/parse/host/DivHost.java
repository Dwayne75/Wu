package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.Div;
import org.kohsuke.rngom.ast.om.Location;

public class DivHost
  extends GrammarSectionHost
  implements Div
{
  private final Div lhs;
  private final Div rhs;
  
  DivHost(Div lhs, Div rhs)
  {
    super(lhs, rhs);
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public void endDiv(Location _loc, Annotations _anno)
    throws BuildException
  {
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    this.lhs.endDiv(loc.lhs, anno.lhs);
    this.rhs.endDiv(loc.rhs, anno.rhs);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\host\DivHost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */