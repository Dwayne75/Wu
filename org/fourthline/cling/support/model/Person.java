package org.fourthline.cling.support.model;

public class Person
{
  private String name;
  
  public Person(String name)
  {
    this.name = name;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    Person person = (Person)o;
    if (!this.name.equals(person.name)) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    return this.name.hashCode();
  }
  
  public String toString()
  {
    return getName();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\Person.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */