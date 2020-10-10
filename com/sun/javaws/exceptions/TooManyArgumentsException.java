package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class TooManyArgumentsException
  extends JNLPException
{
  private String[] _arguments;
  
  public TooManyArgumentsException(String[] paramArrayOfString)
  {
    super(ResourceManager.getString("launch.error.category.arguments"));
    this._arguments = paramArrayOfString;
  }
  
  public String getRealMessage()
  {
    StringBuffer localStringBuffer = new StringBuffer("{");
    for (int i = 0; i < this._arguments.length - 1; i++)
    {
      localStringBuffer.append(this._arguments[i]);
      localStringBuffer.append(", ");
    }
    localStringBuffer.append(this._arguments[(this._arguments.length - 1)]);
    localStringBuffer.append(" }");
    
    return ResourceManager.getString("launch.error.toomanyargs", localStringBuffer.toString());
  }
  
  public String getField()
  {
    return getMessage();
  }
  
  public String toString()
  {
    return "TooManyArgumentsException[ " + getRealMessage() + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\TooManyArgumentsException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */