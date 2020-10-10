package com.sun.javaws.security;

import com.sun.jnlp.JNLPClassLoader;
import com.sun.jnlp.PrintServiceImpl;

public class JavaWebStartSecurity
  extends SecurityManager
{
  private JNLPClassLoader currentJNLPClassLoader()
  {
    Class[] arrayOfClass = getClassContext();
    for (int i = 0; i < arrayOfClass.length; i++)
    {
      localClassLoader = arrayOfClass[i].getClassLoader();
      if ((localClassLoader instanceof JNLPClassLoader)) {
        return (JNLPClassLoader)localClassLoader;
      }
    }
    ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
    if ((localClassLoader instanceof JNLPClassLoader)) {
      return (JNLPClassLoader)localClassLoader;
    }
    return (JNLPClassLoader)null;
  }
  
  public void checkAwtEventQueueAccess()
  {
    if ((!AppContextUtil.isApplicationAppContext()) && (currentJNLPClassLoader() != null)) {
      super.checkAwtEventQueueAccess();
    }
  }
  
  public Class[] getExecutionStackContext()
  {
    return super.getClassContext();
  }
  
  public void checkPrintJobAccess()
  {
    try
    {
      super.checkPrintJobAccess();
    }
    catch (SecurityException localSecurityException)
    {
      if (PrintServiceImpl.requestPrintPermission()) {
        return;
      }
      throw localSecurityException;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\security\JavaWebStartSecurity.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */