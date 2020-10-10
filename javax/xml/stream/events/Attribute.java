package javax.xml.stream.events;

import javax.xml.namespace.QName;

public abstract interface Attribute
  extends XMLEvent
{
  public abstract QName getName();
  
  public abstract String getValue();
  
  public abstract String getDTDType();
  
  public abstract boolean isSpecified();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\events\Attribute.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */