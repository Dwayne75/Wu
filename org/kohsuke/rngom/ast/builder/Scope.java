package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedPattern;

public abstract interface Scope<P extends ParsedPattern, E extends ParsedElementAnnotation, L extends Location, A extends Annotations<E, L, CL>, CL extends CommentList<L>>
  extends GrammarSection<P, E, L, A, CL>
{
  public abstract P makeParentRef(String paramString, L paramL, A paramA)
    throws BuildException;
  
  public abstract P makeRef(String paramString, L paramL, A paramA)
    throws BuildException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\ast\builder\Scope.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */