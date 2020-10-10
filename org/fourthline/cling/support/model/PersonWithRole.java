package org.fourthline.cling.support.model;

import org.w3c.dom.Element;

public class PersonWithRole
  extends Person
{
  private String role;
  
  public PersonWithRole(String name)
  {
    super(name);
  }
  
  public PersonWithRole(String name, String role)
  {
    super(name);
    this.role = role;
  }
  
  public String getRole()
  {
    return this.role;
  }
  
  public void setOnElement(Element element)
  {
    element.setTextContent(toString());
    if (getRole() != null) {
      element.setAttribute("role", getRole());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\PersonWithRole.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */