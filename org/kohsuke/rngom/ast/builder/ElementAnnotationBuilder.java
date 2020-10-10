package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedPattern;

public abstract interface ElementAnnotationBuilder<P extends ParsedPattern, E extends ParsedElementAnnotation, L extends Location, A extends Annotations<E, L, CL>, CL extends CommentList<L>>
  extends Annotations<E, L, CL>
{
  public abstract void addText(String paramString, L paramL, CL paramCL)
    throws BuildException;
  
  public abstract E makeElementAnnotation()
    throws BuildException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\ast\builder\ElementAnnotationBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */