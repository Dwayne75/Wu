package org.fourthline.cling.support.messagebox.model;

import org.fourthline.cling.support.messagebox.parser.MessageElement;

public class NumberName
  implements ElementAppender
{
  private String number;
  private String name;
  
  public NumberName(String number, String name)
  {
    this.number = number;
    this.name = name;
  }
  
  public String getNumber()
  {
    return this.number;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public void appendMessageElements(MessageElement parent)
  {
    ((MessageElement)parent.createChild("Number")).setContent(getNumber());
    ((MessageElement)parent.createChild("Name")).setContent(getName());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\messagebox\model\NumberName.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */