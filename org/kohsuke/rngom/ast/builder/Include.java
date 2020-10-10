package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;

public abstract interface Include<P extends ParsedPattern, E extends ParsedElementAnnotation, L extends Location, A extends Annotations<E, L, CL>, CL extends CommentList<L>>
  extends GrammarSection<P, E, L, A, CL>
{
  public abstract void endInclude(Parseable paramParseable, String paramString1, String paramString2, L paramL, A paramA)
    throws BuildException, IllegalSchemaException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\ast\builder\Include.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */