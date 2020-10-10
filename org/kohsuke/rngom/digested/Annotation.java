package org.kohsuke.rngom.digested;

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.util.LocatorImpl;

class Annotation
  implements Annotations<ElementWrapper, LocatorImpl, CommentListImpl>
{
  private final DAnnotation a = new DAnnotation();
  
  public void addAttribute(String ns, String localName, String prefix, String value, LocatorImpl loc)
    throws BuildException
  {
    this.a.attributes.put(new QName(ns, localName, prefix), new DAnnotation.Attribute(ns, localName, prefix, value, loc));
  }
  
  public void addElement(ElementWrapper ea)
    throws BuildException
  {
    this.a.contents.add(ea.element);
  }
  
  public void addComment(CommentListImpl comments)
    throws BuildException
  {}
  
  public void addLeadingComment(CommentListImpl comments)
    throws BuildException
  {}
  
  DAnnotation getResult()
  {
    return this.a;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\Annotation.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */