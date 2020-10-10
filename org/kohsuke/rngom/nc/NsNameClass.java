package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

public final class NsNameClass
  extends NameClass
{
  private final String namespaceUri;
  
  public NsNameClass(String namespaceUri)
  {
    this.namespaceUri = namespaceUri;
  }
  
  public boolean contains(QName name)
  {
    return this.namespaceUri.equals(name.getNamespaceURI());
  }
  
  public int containsSpecificity(QName name)
  {
    return contains(name) ? 1 : -1;
  }
  
  public int hashCode()
  {
    return this.namespaceUri.hashCode();
  }
  
  public boolean equals(Object obj)
  {
    if ((obj == null) || (!(obj instanceof NsNameClass))) {
      return false;
    }
    return this.namespaceUri.equals(((NsNameClass)obj).namespaceUri);
  }
  
  public <V> V accept(NameClassVisitor<V> visitor)
  {
    return (V)visitor.visitNsName(this.namespaceUri);
  }
  
  public boolean isOpen()
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\nc\NsNameClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */