package javax.xml.bind;

import java.security.PrivilegedAction;

final class GetPropertyAction
  implements PrivilegedAction<String>
{
  private final String propertyName;
  
  public GetPropertyAction(String propertyName)
  {
    this.propertyName = propertyName;
  }
  
  public String run()
  {
    return System.getProperty(this.propertyName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\GetPropertyAction.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */