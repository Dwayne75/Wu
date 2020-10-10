package org.fourthline.cling.support.shared.log.impl;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import org.fourthline.cling.support.shared.TextExpand;
import org.fourthline.cling.support.shared.log.LogView;
import org.fourthline.cling.support.shared.log.LogView.Presenter;
import org.seamless.swing.logging.LogMessage;

@ApplicationScoped
public class LogPresenter
  implements LogView.Presenter
{
  @Inject
  protected LogView view;
  @Inject
  protected Event<TextExpand> textExpandEvent;
  
  public void init()
  {
    this.view.setPresenter(this);
  }
  
  public void onExpand(LogMessage logMessage)
  {
    this.textExpandEvent.fire(new TextExpand(logMessage.getMessage()));
  }
  
  @PreDestroy
  public void destroy()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        LogPresenter.this.view.dispose();
      }
    });
  }
  
  public void pushMessage(final LogMessage message)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        LogPresenter.this.view.pushMessage(message);
      }
    });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\shared\log\impl\LogPresenter.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */