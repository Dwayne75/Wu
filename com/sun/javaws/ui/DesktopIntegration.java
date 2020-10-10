package com.sun.javaws.ui;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.DeployUIManager;
import com.sun.deploy.util.DialogFactory;
import com.sun.javaws.Main;
import com.sun.javaws.SplashScreen;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.ShortcutDesc;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;

public class DesktopIntegration
  extends JOptionPane
{
  private int _answer = 2;
  
  public DesktopIntegration(Frame paramFrame, String paramString, boolean paramBoolean1, boolean paramBoolean2)
  {
    initComponents(paramString, paramBoolean1, paramBoolean2);
  }
  
  private void initComponents(String paramString, boolean paramBoolean1, boolean paramBoolean2)
  {
    Object[] arrayOfObject = new Object[2];
    JButton[] arrayOfJButton = new JButton[3];
    
    arrayOfJButton[0] = new JButton(ResourceManager.getString("install.yesButton"));
    
    arrayOfJButton[0].setMnemonic(ResourceManager.getVKCode("install.yesMnemonic"));
    
    JButton localJButton1 = arrayOfJButton[0];
    
    arrayOfJButton[1] = new JButton(ResourceManager.getString("install.noButton"));
    
    arrayOfJButton[1].setMnemonic(ResourceManager.getVKCode("install.noMnemonic"));
    
    JButton localJButton2 = arrayOfJButton[1];
    
    arrayOfJButton[2] = new JButton(ResourceManager.getString("install.configButton"));
    
    arrayOfJButton[2].setMnemonic(ResourceManager.getVKCode("install.configMnemonic"));
    
    JButton localJButton3 = arrayOfJButton[2];
    for (int i = 0; i < 3; i++) {
      arrayOfJButton[i].addActionListener(new ActionListener()
      {
        private final JButton val$configureButton;
        private final JButton val$yesButton;
        private final JButton val$noButton;
        
        public void actionPerformed(ActionEvent paramAnonymousActionEvent)
        {
          JButton localJButton = (JButton)paramAnonymousActionEvent.getSource();
          if (localJButton == this.val$configureButton)
          {
            Main.launchJavaControlPanel("advanced");
            return;
          }
          if (localJButton == this.val$yesButton) {
            DesktopIntegration.this._answer = 1;
          } else if (localJButton == this.val$noButton) {
            DesktopIntegration.this._answer = 0;
          }
          Object localObject = (Component)paramAnonymousActionEvent.getSource();
          JDialog localJDialog = null;
          while (((Component)localObject).getParent() != null)
          {
            if ((localObject instanceof JDialog)) {
              localJDialog = (JDialog)localObject;
            }
            localObject = ((Component)localObject).getParent();
          }
          if (localJDialog != null) {
            localJDialog.setVisible(false);
          }
        }
      });
    }
    String str = null;
    if (Config.getOSName().equalsIgnoreCase("Windows"))
    {
      if ((paramBoolean1) && (paramBoolean2)) {
        str = ResourceManager.getString("install.windows.both.message", paramString);
      } else if (paramBoolean1) {
        str = ResourceManager.getString("install.desktop.message", paramString);
      } else if (paramBoolean2) {
        str = ResourceManager.getString("install.windows.menu.message", paramString);
      }
    }
    else if ((paramBoolean1) && (paramBoolean2)) {
      str = ResourceManager.getString("install.gnome.both.message", paramString);
    } else if (paramBoolean1) {
      str = ResourceManager.getString("install.desktop.message", paramString);
    } else if (paramBoolean2) {
      str = ResourceManager.getString("install.gnome.menu.message", paramString);
    }
    setOptions(arrayOfJButton);
    setMessage(str);
    setMessageType(2);
    setInitialValue(arrayOfJButton[0]);
  }
  
  public static int showDTIDialog(Frame paramFrame, LaunchDesc paramLaunchDesc)
  {
    String str = paramLaunchDesc.getInformation().getTitle();
    ShortcutDesc localShortcutDesc = paramLaunchDesc.getInformation().getShortcut();
    
    boolean bool1 = localShortcutDesc == null ? true : localShortcutDesc.getDesktop();
    boolean bool2 = localShortcutDesc == null ? true : localShortcutDesc.getMenu();
    
    LookAndFeel localLookAndFeel = DeployUIManager.setLookAndFeel();
    
    DesktopIntegration localDesktopIntegration = new DesktopIntegration(paramFrame, str, bool1, bool2);
    JDialog localJDialog = localDesktopIntegration.createDialog(paramFrame, ResourceManager.getString("install.title", str));
    DialogFactory.positionDialog(localJDialog);
    SplashScreen.hide();
    localJDialog.setVisible(true);
    
    DeployUIManager.restoreLookAndFeel(localLookAndFeel);
    
    return localDesktopIntegration._answer;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\ui\DesktopIntegration.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */