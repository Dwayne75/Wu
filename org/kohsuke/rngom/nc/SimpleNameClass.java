package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

public class SimpleNameClass
  extends NameClass
{
  public final QName name;
  
  public SimpleNameClass(QName name)
  {
    this.name = name;
  }
  
  public SimpleNameClass(String nsUri, String localPart)
  {
    this(new QName(nsUri, localPart));
  }
  
  public boolean contains(QName name)
  {
    return this.name.equals(name);
  }
  
  public int containsSpecificity(QName name)
  {
    return contains(name) ? 2 : -1;
  }
  
  public int hashCode()
  {
    return this.name.hashCode();
  }
  
  public boolean equals(Object obj)
  {
    if ((obj == null) || (!(obj instanceof SimpleNameClass))) {
      return false;
    }
    SimpleNameClass other = (SimpleNameClass)obj;
    return this.name.equals(other.name);
  }
  
  public <V> V accept(NameClassVisitor<V> visitor)
  {
    return (V)visitor.visitName(this.name);
  }
  
  public boolean isOpen()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\nc\SimpleNameClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */