package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedPattern;

public abstract interface GrammarSection<P extends ParsedPattern, E extends ParsedElementAnnotation, L extends Location, A extends Annotations<E, L, CL>, CL extends CommentList<L>>
{
  public abstract void define(String paramString, Combine paramCombine, P paramP, L paramL, A paramA)
    throws BuildException;
  
  public abstract void topLevelAnnotation(E paramE)
    throws BuildException;
  
  public abstract void topLevelComment(CL paramCL)
    throws BuildException;
  
  public abstract Div<P, E, L, A, CL> makeDiv();
  
  public abstract Include<P, E, L, A, CL> makeInclude();
  
  public static final class Combine
  {
    private final String name;
    
    private Combine(String name)
    {
      this.name = name;
    }
    
    public final String toString()
    {
      return this.name;
    }
  }
  
  public static final Combine COMBINE_CHOICE = new Combine("choice", null);
  public static final Combine COMBINE_INTERLEAVE = new Combine("interleave", null);
  public static final String START = "\000#start\000";
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\ast\builder\GrammarSection.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */