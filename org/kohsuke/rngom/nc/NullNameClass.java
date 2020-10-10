package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

final class NullNameClass
  extends NameClass
{
  public boolean contains(QName name)
  {
    return false;
  }
  
  public int containsSpecificity(QName name)
  {
    return -1;
  }
  
  public int hashCode()
  {
    return NullNameClass.class.hashCode();
  }
  
  public boolean equals(Object obj)
  {
    return this == obj;
  }
  
  public <V> V accept(NameClassVisitor<V> visitor)
  {
    return (V)visitor.visitNull();
  }
  
  public boolean isOpen()
  {
    return false;
  }
  
  private Object readResolve()
  {
    return NameClass.NULL;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\nc\NullNameClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */