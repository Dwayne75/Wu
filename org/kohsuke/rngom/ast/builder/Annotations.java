package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;

public abstract interface Annotations<E extends ParsedElementAnnotation, L extends Location, CL extends CommentList<L>>
{
  public abstract void addAttribute(String paramString1, String paramString2, String paramString3, String paramString4, L paramL)
    throws BuildException;
  
  public abstract void addElement(E paramE)
    throws BuildException;
  
  public abstract void addComment(CL paramCL)
    throws BuildException;
  
  public abstract void addLeadingComment(CL paramCL)
    throws BuildException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\ast\builder\Annotations.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */