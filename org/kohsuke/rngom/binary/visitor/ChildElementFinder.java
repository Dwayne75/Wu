package org.kohsuke.rngom.binary.visitor;

import java.util.HashSet;
import java.util.Set;
import org.kohsuke.rngom.binary.Pattern;
import org.kohsuke.rngom.nc.NameClass;

public class ChildElementFinder
  extends PatternWalker
{
  private final Set children;
  
  public ChildElementFinder()
  {
    this.children = new HashSet();
  }
  
  public static class Element
  {
    public final NameClass nc;
    public final Pattern content;
    
    public Element(NameClass nc, Pattern content)
    {
      this.nc = nc;
      this.content = content;
    }
    
    public boolean equals(Object o)
    {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Element)) {
        return false;
      }
      Element element = (Element)o;
      if (this.content != null ? !this.content.equals(element.content) : element.content != null) {
        return false;
      }
      if (this.nc != null ? !this.nc.equals(element.nc) : element.nc != null) {
        return false;
      }
      return true;
    }
    
    public int hashCode()
    {
      int result = this.nc != null ? this.nc.hashCode() : 0;
      result = 29 * result + (this.content != null ? this.content.hashCode() : 0);
      return result;
    }
  }
  
  public Set getChildren()
  {
    return this.children;
  }
  
  public void visitElement(NameClass nc, Pattern content)
  {
    this.children.add(new Element(nc, content));
  }
  
  public void visitAttribute(NameClass ns, Pattern value) {}
  
  public void visitList(Pattern p) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\visitor\ChildElementFinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */