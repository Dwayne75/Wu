package org.fourthline.cling.support.shared;

import java.awt.Component;

public abstract interface View<P>
{
  public abstract Component asUIComponent();
  
  public abstract void setPresenter(P paramP);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\shared\View.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */