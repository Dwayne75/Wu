package org.fourthline.cling.support.shared;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.UIManager;
import org.fourthline.cling.UpnpService;
import org.seamless.swing.AbstractController;
import org.seamless.swing.Application;
import org.seamless.swing.Controller;
import org.seamless.swing.logging.LogCategory;
import org.seamless.swing.logging.LogController;
import org.seamless.swing.logging.LogMessage;
import org.seamless.swing.logging.LoggingHandler;
import org.seamless.util.logging.LoggingUtil;

public abstract class MainController
  extends AbstractController<JFrame>
{
  private final LogController logController;
  private final JPanel logPanel;
  
  public MainController(JFrame view, List<LogCategory> logCategories)
  {
    super(view);
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception ex)
    {
      System.out.println("Unable to load native look and feel: " + ex.toString());
    }
    System.setProperty("sun.awt.exception.handler", AWTExceptionHandler.class.getName());
    
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
        if (MainController.this.getUpnpService() != null) {
          MainController.this.getUpnpService().shutdown();
        }
      }
    });
    this.logController = new LogController(this, logCategories)
    {
      protected void expand(LogMessage logMessage)
      {
        fireEventGlobal(new TextExpandEvent(logMessage
          .getMessage()));
      }
      
      protected Frame getParentWindow()
      {
        return (Frame)MainController.this.getView();
      }
    };
    this.logPanel = ((JPanel)this.logController.getView());
    this.logPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    
    Handler handler = new LoggingHandler()
    {
      protected void log(LogMessage msg)
      {
        MainController.this.logController.pushMessage(msg);
      }
    };
    if (System.getProperty("java.util.logging.config.file") == null) {
      LoggingUtil.resetRootHandler(new Handler[] { handler });
    } else {
      LogManager.getLogManager().getLogger("").addHandler(handler);
    }
  }
  
  public LogController getLogController()
  {
    return this.logController;
  }
  
  public JPanel getLogPanel()
  {
    return this.logPanel;
  }
  
  public void log(Level level, String msg)
  {
    log(new LogMessage(level, msg));
  }
  
  public void log(LogMessage message)
  {
    getLogController().pushMessage(message);
  }
  
  public void dispose()
  {
    super.dispose();
    ShutdownWindow.INSTANCE.setVisible(true);
    new Thread()
    {
      public void run()
      {
        System.exit(0);
      }
    }.start();
  }
  
  public abstract UpnpService getUpnpService();
  
  public static class ShutdownWindow
    extends JWindow
  {
    public static final JWindow INSTANCE = new ShutdownWindow();
    
    protected ShutdownWindow()
    {
      JLabel shutdownLabel = new JLabel("Shutting down, please wait...");
      shutdownLabel.setHorizontalAlignment(0);
      getContentPane().add(shutdownLabel);
      setPreferredSize(new Dimension(300, 30));
      pack();
      Application.center(this);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\shared\MainController.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */