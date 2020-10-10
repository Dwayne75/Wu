package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;

class AnnotationsHost
  extends Base
  implements Annotations
{
  final Annotations lhs;
  final Annotations rhs;
  
  AnnotationsHost(Annotations lhs, Annotations rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public void addAttribute(String ns, String localName, String prefix, String value, Location _loc)
    throws BuildException
  {
    LocationHost loc = cast(_loc);
    this.lhs.addAttribute(ns, localName, prefix, value, loc.lhs);
    this.rhs.addAttribute(ns, localName, prefix, value, loc.rhs);
  }
  
  public void addComment(CommentList _comments)
    throws BuildException
  {
    CommentListHost comments = (CommentListHost)_comments;
    this.lhs.addComment(comments == null ? null : comments.lhs);
    this.rhs.addComment(comments == null ? null : comments.rhs);
  }
  
  public void addElement(ParsedElementAnnotation _ea)
    throws BuildException
  {
    ParsedElementAnnotationHost ea = (ParsedElementAnnotationHost)_ea;
    this.lhs.addElement(ea.lhs);
    this.rhs.addElement(ea.rhs);
  }
  
  public void addLeadingComment(CommentList _comments)
    throws BuildException
  {
    CommentListHost comments = (CommentListHost)_comments;
    this.lhs.addLeadingComment(comments.lhs);
    this.rhs.addLeadingComment(comments.rhs);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\host\AnnotationsHost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */