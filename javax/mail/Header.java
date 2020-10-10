package javax.mail;

public class Header
{
  protected String name;
  protected String value;
  
  public Header(String name, String value)
  {
    this.name = name;
    this.value = value;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public String getValue()
  {
    return this.value;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\Header.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */