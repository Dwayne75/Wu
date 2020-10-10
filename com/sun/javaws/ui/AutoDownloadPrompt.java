package com.sun.javaws.ui;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.DialogFactory;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.JREDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.ResourcesDesc;
import java.awt.Component;
import javax.swing.JButton;

public class AutoDownloadPrompt
{
  public static int _result = -1;
  
  public static boolean prompt(Component paramComponent, LaunchDesc paramLaunchDesc)
  {
    if (_result >= 0) {
      return _result == 0;
    }
    String str1 = paramLaunchDesc.getInformation().getTitle();
    String str2 = paramLaunchDesc.getResources().getSelectedJRE().getVersion();
    String str3 = ResourceManager.getString("download.jre.prompt.title");
    
    String[] arrayOfString = { ResourceManager.getString("download.jre.prompt.text1", str1, str2), "", ResourceManager.getString("download.jre.prompt.text2") };
    
    JButton[] arrayOfJButton = { new JButton(ResourceManager.getString("download.jre.prompt.okButton")), new JButton(ResourceManager.getString("download.jre.prompt.cancelButton")) };
    
    arrayOfJButton[0].setMnemonic(ResourceManager.getAcceleratorKey("download.jre.prompt.okButton"));
    
    arrayOfJButton[1].setMnemonic(ResourceManager.getAcceleratorKey("download.jre.prompt.cancelButton"));
    
    _result = DialogFactory.showOptionDialog(paramComponent, 4, arrayOfString, str3, arrayOfJButton, arrayOfJButton[0]);
    
    return _result == 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\ui\AutoDownloadPrompt.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */