package com.sun.javaws;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.ConsoleWindow;
import com.sun.deploy.util.DeployUIManager;
import com.sun.deploy.util.DialogFactory;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.util.JavawsConsoleController;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.GeneralSecurityException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

public class LaunchErrorDialog
  extends JDialog
{
  private LaunchErrorDialog(Frame paramFrame, Throwable paramThrowable)
  {
    super(paramFrame, true);
    
    JNLPException localJNLPException = null;
    if ((paramThrowable instanceof JNLPException)) {
      localJNLPException = (JNLPException)paramThrowable;
    }
    JTabbedPane localJTabbedPane = new JTabbedPane();
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add("Center", localJTabbedPane);
    
    localJTabbedPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    
    String str1 = getErrorCategory(paramThrowable);
    setTitle(ResourceManager.getString("launcherrordialog.title", str1));
    
    String str2 = getLaunchDescTitle();
    String str3 = getLaunchDescVendor();
    if (Globals.isImportMode()) {
      str4 = ResourceManager.getString("launcherrordialog.import.errorintro");
    } else {
      str4 = ResourceManager.getString("launcherrordialog.errorintro");
    }
    if (str2 != null) {
      str4 = str4 + ResourceManager.getString("launcherrordialog.errortitle", str2);
    }
    if (str3 != null) {
      str4 = str4 + ResourceManager.getString("launcherrordialog.errorvendor", str3);
    }
    String str4 = str4 + ResourceManager.getString("launcherrordialog.errorcategory", str1);
    str4 = str4 + getErrorDescription(paramThrowable);
    
    JTextArea localJTextArea = new JTextArea();
    localJTextArea.setFont(ResourceManager.getUIFont());
    localJTextArea.setEditable(false);
    localJTextArea.setLineWrap(true);
    localJTextArea.setText(str4);
    localJTabbedPane.add(ResourceManager.getString("launcherrordialog.generalTab"), new JScrollPane(localJTextArea));
    
    String str5 = null;
    String str6 = null;
    if (localJNLPException != null)
    {
      str5 = localJNLPException.getLaunchDescSource();
      if (str5 == null)
      {
        localObject1 = JNLPException.getDefaultLaunchDesc();
        if (localObject1 != null) {
          str5 = ((LaunchDesc)localObject1).getSource();
        }
      }
    }
    else if (JNLPException.getDefaultLaunchDesc() != null)
    {
      str5 = JNLPException.getDefaultLaunchDesc().getSource();
    }
    if (JNLPException.getDefaultLaunchDesc() != null) {
      str6 = JNLPException.getDefaultLaunchDesc().getSource();
    }
    if ((str6 != null) && (str6.equals(str5))) {
      str6 = null;
    }
    if (str5 != null)
    {
      localObject1 = new JTextArea();
      ((JTextArea)localObject1).setFont(ResourceManager.getUIFont());
      ((JTextArea)localObject1).setEditable(false);
      ((JTextArea)localObject1).setLineWrap(true);
      ((JTextArea)localObject1).setText(str5);
      localJTabbedPane.add(ResourceManager.getString("launcherrordialog.jnlpTab"), new JScrollPane((Component)localObject1));
    }
    if (str6 != null)
    {
      localObject1 = new JTextArea();
      ((JTextArea)localObject1).setFont(ResourceManager.getUIFont());
      ((JTextArea)localObject1).setEditable(false);
      ((JTextArea)localObject1).setLineWrap(true);
      ((JTextArea)localObject1).setText(str6);
      localJTabbedPane.add(ResourceManager.getString("launcherrordialog.jnlpMainTab"), new JScrollPane((Component)localObject1));
    }
    if (paramThrowable != null)
    {
      localObject1 = new JTextArea();
      ((JTextArea)localObject1).setFont(ResourceManager.getUIFont());
      ((JTextArea)localObject1).setEditable(false);
      ((JTextArea)localObject1).setLineWrap(true);
      ((JTextArea)localObject1).setWrapStyleWord(false);
      localObject2 = new StringWriter();
      localObject3 = new PrintWriter((Writer)localObject2);
      paramThrowable.printStackTrace((PrintWriter)localObject3);
      ((JTextArea)localObject1).setText(((StringWriter)localObject2).toString());
      localJTabbedPane.add(ResourceManager.getString("launcherrordialog.exceptionTab"), new JScrollPane((Component)localObject1));
    }
    if ((localJNLPException != null) && (localJNLPException.getWrappedException() != null))
    {
      localObject1 = new JTextArea();
      ((JTextArea)localObject1).setFont(ResourceManager.getUIFont());
      ((JTextArea)localObject1).setEditable(false);
      ((JTextArea)localObject1).setLineWrap(true);
      ((JTextArea)localObject1).setWrapStyleWord(false);
      localObject2 = new StringWriter();
      localObject3 = new PrintWriter((Writer)localObject2);
      localJNLPException.getWrappedException().printStackTrace((PrintWriter)localObject3);
      ((JTextArea)localObject1).setText(((StringWriter)localObject2).toString());
      localJTabbedPane.add(ResourceManager.getString("launcherrordialog.wrappedExceptionTab"), new JScrollPane((Component)localObject1));
    }
    Object localObject1 = null;
    Object localObject2 = JavawsConsoleController.getInstance().getConsole();
    if (localObject2 != null) {
      localObject1 = ((ConsoleWindow)localObject2).getTextArea().getDocument();
    }
    if (localObject1 != null)
    {
      localObject3 = new JTextArea((Document)localObject1);
      ((JTextArea)localObject3).setFont(ResourceManager.getUIFont());
      
      localJTabbedPane.add(ResourceManager.getString("launcherrordialog.consoleTab"), new JScrollPane((Component)localObject3));
    }
    Object localObject3 = new JButton(ResourceManager.getString("launcherrordialog.abort"));
    
    ((JButton)localObject3).setMnemonic(ResourceManager.getVKCode("launcherrordialog.abortMnemonic"));
    
    Box localBox = new Box(0);
    localBox.add(Box.createHorizontalGlue());
    localBox.add((Component)localObject3);
    localBox.add(Box.createHorizontalGlue());
    getContentPane().add("South", localBox);
    getRootPane().setDefaultButton((JButton)localObject3);
    
    ((JButton)localObject3).addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
      {
        LaunchErrorDialog.this.setVisible(false);
      }
    });
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent paramAnonymousWindowEvent)
      {
        LaunchErrorDialog.this.setVisible(false);
      }
    });
    pack();
    setSize(450, 300);
    
    Rectangle localRectangle = getBounds();
    Dimension localDimension = Toolkit.getDefaultToolkit().getScreenSize();
    localRectangle.width = Math.min(localDimension.width, localRectangle.width);
    localRectangle.height = Math.min(localDimension.height, localRectangle.height);
    setBounds((localDimension.width - localRectangle.width) / 2, (localDimension.height - localRectangle.height) / 2, localRectangle.width, localRectangle.height);
  }
  
  public static void show(Frame paramFrame, Throwable paramThrowable, boolean paramBoolean)
  {
    try
    {
      SwingUtilities.invokeAndWait(new Runnable()
      {
        private final Frame val$owner;
        private final Throwable val$e;
        
        public void run()
        {
          LaunchErrorDialog.showWarning(this.val$owner, this.val$e);
        }
      });
    }
    catch (Exception localException) {}
    if (paramBoolean) {
      Main.systemExit(0);
    }
  }
  
  private static void showWarning(Frame paramFrame, Throwable paramThrowable)
  {
    LookAndFeel localLookAndFeel = null;
    try
    {
      localLookAndFeel = DeployUIManager.setLookAndFeel();
      
      SplashScreen.hide();
      
      System.err.println("#### Java Web Start Error:");
      System.err.println("#### " + paramThrowable.getMessage());
      int i = (!Globals.TCKHarnessRun) && ((!Globals.isSilentMode()) || (Main.isViewer())) ? 1 : 0;
      if ((i != 0) && (wantsDetails(paramFrame, paramThrowable)))
      {
        LaunchErrorDialog localLaunchErrorDialog = new LaunchErrorDialog(paramFrame, paramThrowable);
        localLaunchErrorDialog.setVisible(true);
      }
    }
    finally
    {
      DeployUIManager.restoreLookAndFeel(localLookAndFeel);
    }
  }
  
  private static String getErrorCategory(Throwable paramThrowable)
  {
    String str = ResourceManager.getString("launch.error.category.unexpected");
    if ((paramThrowable instanceof JNLPException))
    {
      JNLPException localJNLPException = (JNLPException)paramThrowable;
      str = localJNLPException.getCategory();
    }
    else if (((paramThrowable instanceof SecurityException)) || ((paramThrowable instanceof GeneralSecurityException)))
    {
      str = ResourceManager.getString("launch.error.category.security");
    }
    else if ((paramThrowable instanceof OutOfMemoryError))
    {
      str = ResourceManager.getString("launch.error.category.memory");
    }
    return str;
  }
  
  private static String getErrorDescription(Throwable paramThrowable)
  {
    String str = paramThrowable.getMessage();
    if (str == null) {
      str = ResourceManager.getString("launcherrordialog.genericerror", paramThrowable.getClass().getName());
    }
    return str;
  }
  
  private static String getLaunchDescTitle()
  {
    LaunchDesc localLaunchDesc = JNLPException.getDefaultLaunchDesc();
    return localLaunchDesc == null ? null : localLaunchDesc.getInformation().getTitle();
  }
  
  private static String getLaunchDescVendor()
  {
    LaunchDesc localLaunchDesc = JNLPException.getDefaultLaunchDesc();
    return localLaunchDesc == null ? null : localLaunchDesc.getInformation().getVendor();
  }
  
  private static boolean wantsDetails(Frame paramFrame, Throwable paramThrowable)
  {
    String str1 = null;
    String str2 = getErrorCategory(paramThrowable);
    if ((paramThrowable instanceof JNLPException)) {
      str1 = ((JNLPException)paramThrowable).getBriefMessage();
    }
    if (str1 == null) {
      if (getLaunchDescTitle() == null)
      {
        if (Globals.isImportMode()) {
          str1 = ResourceManager.getString("launcherrordialog.import.brief.message");
        } else {
          str1 = ResourceManager.getString("launcherrordialog.brief.message");
        }
      }
      else if (Globals.isImportMode()) {
        str1 = ResourceManager.getString("launcherrordialog.import.brief.messageKnown", getLaunchDescTitle());
      } else {
        str1 = ResourceManager.getString("launcherrordialog.brief.messageKnown", getLaunchDescTitle());
      }
    }
    String[] arrayOfString = { ResourceManager.getString("launcherrordialog.brief.ok"), ResourceManager.getString("launcherrordialog.brief.details") };
    
    int i = DialogFactory.showOptionDialog(paramFrame, 1, str1, ResourceManager.getString("launcherrordialog.brief.title", str2), arrayOfString, arrayOfString[0]);
    if (i == 1) {
      return true;
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\LaunchErrorDialog.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */