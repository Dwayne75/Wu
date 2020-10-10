package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.ElementAnnotationBuilder;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;

final class ElementAnnotationBuilderHost
  extends AnnotationsHost
  implements ElementAnnotationBuilder
{
  final ElementAnnotationBuilder lhs;
  final ElementAnnotationBuilder rhs;
  
  ElementAnnotationBuilderHost(ElementAnnotationBuilder lhs, ElementAnnotationBuilder rhs)
  {
    super(lhs, rhs);
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public void addText(String value, Location _loc, CommentList _comments)
    throws BuildException
  {
    LocationHost loc = cast(_loc);
    CommentListHost comments = (CommentListHost)_comments;
    
    this.lhs.addText(value, loc.lhs, comments == null ? null : comments.lhs);
    this.rhs.addText(value, loc.rhs, comments == null ? null : comments.rhs);
  }
  
  public ParsedElementAnnotation makeElementAnnotation()
    throws BuildException
  {
    return new ParsedElementAnnotationHost(this.lhs.makeElementAnnotation(), this.rhs.makeElementAnnotation());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\host\ElementAnnotationBuilderHost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */