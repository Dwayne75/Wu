package org.fourthline.cling.model.message.control;

public abstract interface ActionMessage
{
  public abstract String getActionNamespace();
  
  public abstract boolean isBodyNonEmptyString();
  
  public abstract String getBodyString();
  
  public abstract void setBody(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\control\ActionMessage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */