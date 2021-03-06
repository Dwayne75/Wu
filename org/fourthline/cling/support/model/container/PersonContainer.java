package org.fourthline.cling.support.model.container;

import org.fourthline.cling.support.model.DIDLObject.Class;
import org.fourthline.cling.support.model.DIDLObject.Property.DC.LANGUAGE;

public class PersonContainer
  extends Container
{
  public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.container.person");
  
  public PersonContainer()
  {
    setClazz(CLASS);
  }
  
  public PersonContainer(Container other)
  {
    super(other);
  }
  
  public PersonContainer(String id, Container parent, String title, String creator, Integer childCount)
  {
    this(id, parent.getId(), title, creator, childCount);
  }
  
  public PersonContainer(String id, String parentID, String title, String creator, Integer childCount)
  {
    super(id, parentID, title, creator, CLASS, childCount);
  }
  
  public String getLanguage()
  {
    return (String)getFirstPropertyValue(DIDLObject.Property.DC.LANGUAGE.class);
  }
  
  public PersonContainer setLanguage(String language)
  {
    replaceFirstProperty(new DIDLObject.Property.DC.LANGUAGE(language));
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\container\PersonContainer.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */