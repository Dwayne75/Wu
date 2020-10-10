package org.fourthline.cling.support.shared.log;

import java.util.List;
import org.fourthline.cling.support.shared.View;
import org.seamless.swing.logging.LogCategory;
import org.seamless.swing.logging.LogMessage;

public abstract interface LogView
  extends View<Presenter>
{
  public abstract void pushMessage(LogMessage paramLogMessage);
  
  public abstract void dispose();
  
  public static abstract interface LogCategories
    extends List<LogCategory>
  {}
  
  public static abstract interface Presenter
  {
    public abstract void init();
    
    public abstract void onExpand(LogMessage paramLogMessage);
    
    public abstract void pushMessage(LogMessage paramLogMessage);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\shared\log\LogView.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */