package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

public class AnyNameExceptNameClass
  extends NameClass
{
  private final NameClass nameClass;
  
  public AnyNameExceptNameClass(NameClass nameClass)
  {
    this.nameClass = nameClass;
  }
  
  public boolean contains(QName name)
  {
    return !this.nameClass.contains(name);
  }
  
  public int containsSpecificity(QName name)
  {
    return contains(name) ? 0 : -1;
  }
  
  public boolean equals(Object obj)
  {
    if ((obj == null) || (!(obj instanceof AnyNameExceptNameClass))) {
      return false;
    }
    return this.nameClass.equals(((AnyNameExceptNameClass)obj).nameClass);
  }
  
  public int hashCode()
  {
    return this.nameClass.hashCode() ^ 0xFFFFFFFF;
  }
  
  public <V> V accept(NameClassVisitor<V> visitor)
  {
    return (V)visitor.visitAnyNameExcept(this.nameClass);
  }
  
  public boolean isOpen()
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\nc\AnyNameExceptNameClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */