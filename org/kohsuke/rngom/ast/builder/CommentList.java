package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;

public abstract interface CommentList<L extends Location>
{
  public abstract void addComment(String paramString, L paramL)
    throws BuildException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\ast\builder\CommentList.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */