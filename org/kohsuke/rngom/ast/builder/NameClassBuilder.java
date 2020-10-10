package org.kohsuke.rngom.ast.builder;

import java.util.List;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedNameClass;

public abstract interface NameClassBuilder<N extends ParsedNameClass, E extends ParsedElementAnnotation, L extends Location, A extends Annotations<E, L, CL>, CL extends CommentList<L>>
{
  public abstract N annotate(N paramN, A paramA)
    throws BuildException;
  
  public abstract N annotateAfter(N paramN, E paramE)
    throws BuildException;
  
  public abstract N commentAfter(N paramN, CL paramCL)
    throws BuildException;
  
  public abstract N makeChoice(List<N> paramList, L paramL, A paramA);
  
  public abstract N makeName(String paramString1, String paramString2, String paramString3, L paramL, A paramA);
  
  public abstract N makeNsName(String paramString, L paramL, A paramA);
  
  public abstract N makeNsName(String paramString, N paramN, L paramL, A paramA);
  
  public abstract N makeAnyName(L paramL, A paramA);
  
  public abstract N makeAnyName(N paramN, L paramL, A paramA);
  
  public abstract N makeErrorNameClass();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\ast\builder\NameClassBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */