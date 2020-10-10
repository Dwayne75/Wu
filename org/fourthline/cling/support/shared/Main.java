package org.fourthline.cling.support.shared;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.fourthline.cling.support.shared.log.LogView.Presenter;
import org.seamless.swing.Application;
import org.seamless.swing.logging.LogMessage;
import org.seamless.swing.logging.LoggingHandler;
import org.seamless.util.OS;
import org.seamless.util.logging.LoggingUtil;

public abstract class Main
  implements ShutdownHandler, Thread.UncaughtExceptionHandler
{
  @Inject
  LogView.Presenter logPresenter;
  protected final JFrame errorWindow = new JFrame();
  protected final LoggingHandler loggingHandler = new LoggingHandler()
  {
    protected void log(LogMessage msg)
    {
      Main.this.logPresenter.pushMessage(msg);
    }
  };
  protected boolean isRegularShutdown;
  
  public void init()
  {
    try
    {
      if (OS.checkForMac()) {
        NewPlatformApple.setup(this, getAppName());
      }
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception localException) {}
    this.errorWindow.setPreferredSize(new Dimension(900, 400));
    this.errorWindow.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent windowEvent)
      {
        Main.this.errorWindow.dispose();
      }
    });
    Thread.setDefaultUncaughtExceptionHandler(this);
    
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
        if (!Main.this.isRegularShutdown) {
          Main.this.shutdown();
        }
      }
    });
    if (System.getProperty("java.util.logging.config.file") == null) {
      LoggingUtil.resetRootHandler(new Handler[] { this.loggingHandler });
    } else {
      LogManager.getLogManager().getLogger("").addHandler(this.loggingHandler);
    }
  }
  
  public void shutdown()
  {
    this.isRegularShutdown = true;
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        Main.this.errorWindow.dispose();
      }
    });
  }
  
  public void uncaughtException(Thread thread, final Throwable throwable)
  {
    System.err.println("In thread '" + thread + "' uncaught exception: " + throwable);
    throwable.printStackTrace(System.err);
    
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        Main.this.errorWindow.getContentPane().removeAll();
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        StringBuilder text = new StringBuilder();
        
        text.append("An exceptional error occurred!\nYou can try to continue or exit the application.\n\n");
        text.append("Please tell us about this here:\nhttp://www.4thline.org/projects/mailinglists-cling.html\n\n");
        text.append("-------------------------------------------------------------------------------------------------------------\n\n");
        Writer stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace));
        text.append(stackTrace.toString());
        
        textArea.setText(text.toString());
        JScrollPane pane = new JScrollPane(textArea);
        Main.this.errorWindow.getContentPane().add(pane, "Center");
        
        JButton exitButton = new JButton("Exit Application");
        exitButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            System.exit(1);
          }
        });
        Main.this.errorWindow.getContentPane().add(exitButton, "South");
        
        Main.this.errorWindow.pack();
        Application.center(Main.this.errorWindow);
        textArea.setCaretPosition(0);
        
        Main.this.errorWindow.setVisible(true);
      }
    });
  }
  
  protected void removeLoggingHandler()
  {
    LogManager.getLogManager().getLogger("").removeHandler(this.loggingHandler);
  }
  
  protected abstract String getAppName();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\shared\Main.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */