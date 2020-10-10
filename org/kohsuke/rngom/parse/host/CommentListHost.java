package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.om.Location;

class CommentListHost
  extends Base
  implements CommentList
{
  final CommentList lhs;
  final CommentList rhs;
  
  CommentListHost(CommentList lhs, CommentList rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public void addComment(String value, Location _loc)
    throws BuildException
  {
    LocationHost loc = cast(_loc);
    if (this.lhs != null) {
      this.lhs.addComment(value, loc.lhs);
    }
    if (this.rhs != null) {
      this.rhs.addComment(value, loc.rhs);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\host\CommentListHost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */