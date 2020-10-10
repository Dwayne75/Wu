package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.ElementAnnotationBuilder;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class ElementAnnotationBuilderImpl
  implements ElementAnnotationBuilder
{
  private final Element e;
  
  public ElementAnnotationBuilderImpl(Element e)
  {
    this.e = e;
  }
  
  public void addText(String value, Location loc, CommentList comments)
    throws BuildException
  {
    this.e.appendChild(this.e.getOwnerDocument().createTextNode(value));
  }
  
  public ParsedElementAnnotation makeElementAnnotation()
    throws BuildException
  {
    return new ElementWrapper(this.e);
  }
  
  public void addAttribute(String ns, String localName, String prefix, String value, Location loc)
    throws BuildException
  {
    this.e.setAttributeNS(ns, localName, value);
  }
  
  public void addElement(ParsedElementAnnotation ea)
    throws BuildException
  {
    this.e.appendChild(((ElementWrapper)ea).element);
  }
  
  public void addComment(CommentList comments)
    throws BuildException
  {}
  
  public void addLeadingComment(CommentList comments)
    throws BuildException
  {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\ElementAnnotationBuilderImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */