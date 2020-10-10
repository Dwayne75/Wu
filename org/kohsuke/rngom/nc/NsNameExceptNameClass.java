package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

public class NsNameExceptNameClass
  extends NameClass
{
  private final NameClass nameClass;
  private final String namespaceURI;
  
  public NsNameExceptNameClass(String namespaceURI, NameClass nameClass)
  {
    this.namespaceURI = namespaceURI;
    this.nameClass = nameClass;
  }
  
  public boolean contains(QName name)
  {
    return (this.namespaceURI.equals(name.getNamespaceURI())) && (!this.nameClass.contains(name));
  }
  
  public int containsSpecificity(QName name)
  {
    return contains(name) ? 1 : -1;
  }
  
  public boolean equals(Object obj)
  {
    if ((obj == null) || (!(obj instanceof NsNameExceptNameClass))) {
      return false;
    }
    NsNameExceptNameClass other = (NsNameExceptNameClass)obj;
    return (this.namespaceURI.equals(other.namespaceURI)) && (this.nameClass.equals(other.nameClass));
  }
  
  public int hashCode()
  {
    return this.namespaceURI.hashCode() ^ this.nameClass.hashCode();
  }
  
  public <V> V accept(NameClassVisitor<V> visitor)
  {
    return (V)visitor.visitNsNameExcept(this.namespaceURI, this.nameClass);
  }
  
  public boolean isOpen()
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\nc\NsNameExceptNameClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */