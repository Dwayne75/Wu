package com.sun.javaws;

import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.jnl.LaunchDesc;
import java.awt.Frame;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SplashScreen
{
  private static boolean _alreadyHidden = false;
  private static final int HIDE_SPASH_SCREEN_TOKEN = 90;
  
  public static void hide()
  {
    hide(JnlpxArgs.getSplashPort());
  }
  
  private static void hide(int paramInt)
  {
    if ((paramInt <= 0) || (_alreadyHidden)) {
      return;
    }
    _alreadyHidden = true;
    
    Socket localSocket = null;
    try
    {
      localSocket = new Socket("127.0.0.1", paramInt);
      if (localSocket != null)
      {
        OutputStream localOutputStream = localSocket.getOutputStream();
        try
        {
          localOutputStream.write(90);
          localOutputStream.flush();
        }
        catch (IOException localIOException3) {}
        localOutputStream.close();
      }
    }
    catch (IOException localIOException1) {}catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    if (localSocket != null) {
      try
      {
        localSocket.close();
      }
      catch (IOException localIOException2)
      {
        Trace.println("exception closing socket: " + localIOException2, TraceLevel.BASIC);
      }
    }
  }
  
  public static void generateCustomSplash(Frame paramFrame, LaunchDesc paramLaunchDesc, boolean paramBoolean)
  {
    SplashGenerator localSplashGenerator = new SplashGenerator(paramFrame, paramLaunchDesc);
    if ((paramBoolean) || (localSplashGenerator.needsCustomSplash())) {
      localSplashGenerator.start();
    }
  }
  
  public static void removeCustomSplash(LaunchDesc paramLaunchDesc)
  {
    SplashGenerator localSplashGenerator = new SplashGenerator(null, paramLaunchDesc);
    localSplashGenerator.remove();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\SplashScreen.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */