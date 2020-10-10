package javax.xml.stream;

public abstract interface Location
{
  public abstract int getLineNumber();
  
  public abstract int getColumnNumber();
  
  public abstract int getCharacterOffset();
  
  public abstract String getPublicId();
  
  public abstract String getSystemId();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\Location.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */