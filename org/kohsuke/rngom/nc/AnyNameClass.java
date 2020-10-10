package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

final class AnyNameClass
  extends NameClass
{
  public boolean contains(QName name)
  {
    return true;
  }
  
  public int containsSpecificity(QName name)
  {
    return 0;
  }
  
  public boolean equals(Object obj)
  {
    return obj == this;
  }
  
  public int hashCode()
  {
    return AnyNameClass.class.hashCode();
  }
  
  public <V> V accept(NameClassVisitor<V> visitor)
  {
    return (V)visitor.visitAnyName();
  }
  
  public boolean isOpen()
  {
    return true;
  }
  
  private static Object readReplace()
  {
    return NameClass.ANY;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\nc\AnyNameClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */