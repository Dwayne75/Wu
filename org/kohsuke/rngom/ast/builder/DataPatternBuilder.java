package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.Context;

public abstract interface DataPatternBuilder<P extends ParsedPattern, E extends ParsedElementAnnotation, L extends Location, A extends Annotations<E, L, CL>, CL extends CommentList<L>>
{
  public abstract void addParam(String paramString1, String paramString2, Context paramContext, String paramString3, L paramL, A paramA)
    throws BuildException;
  
  public abstract void annotation(E paramE);
  
  public abstract P makePattern(L paramL, A paramA)
    throws BuildException;
  
  public abstract P makePattern(P paramP, L paramL, A paramA)
    throws BuildException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\ast\builder\DataPatternBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */