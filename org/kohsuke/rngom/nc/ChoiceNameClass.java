package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

public class ChoiceNameClass
  extends NameClass
{
  private final NameClass nameClass1;
  private final NameClass nameClass2;
  
  public ChoiceNameClass(NameClass nameClass1, NameClass nameClass2)
  {
    this.nameClass1 = nameClass1;
    this.nameClass2 = nameClass2;
  }
  
  public boolean contains(QName name)
  {
    return (this.nameClass1.contains(name)) || (this.nameClass2.contains(name));
  }
  
  public int containsSpecificity(QName name)
  {
    return Math.max(this.nameClass1.containsSpecificity(name), this.nameClass2.containsSpecificity(name));
  }
  
  public int hashCode()
  {
    return this.nameClass1.hashCode() ^ this.nameClass2.hashCode();
  }
  
  public boolean equals(Object obj)
  {
    if ((obj == null) || (!(obj instanceof ChoiceNameClass))) {
      return false;
    }
    ChoiceNameClass other = (ChoiceNameClass)obj;
    return (this.nameClass1.equals(other.nameClass1)) && (this.nameClass2.equals(other.nameClass2));
  }
  
  public <V> V accept(NameClassVisitor<V> visitor)
  {
    return (V)visitor.visitChoice(this.nameClass1, this.nameClass2);
  }
  
  public boolean isOpen()
  {
    return (this.nameClass1.isOpen()) || (this.nameClass2.isOpen());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\nc\ChoiceNameClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */