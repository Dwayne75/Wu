package com.sun.jnlp;

import com.sun.deploy.resources.ResourceManager;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.jnlp.ExtendedService;
import javax.jnlp.FileContents;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class ExtendedServiceImpl
  implements ExtendedService
{
  private static ExtendedServiceImpl _sharedInstance = null;
  private static int DEFAULT_FILESIZE = Integer.MAX_VALUE;
  
  public static synchronized ExtendedServiceImpl getInstance()
  {
    if (_sharedInstance == null) {
      _sharedInstance = new ExtendedServiceImpl();
    }
    return _sharedInstance;
  }
  
  public FileContents openFile(File paramFile)
    throws IOException
  {
    if (!askUser(paramFile.getPath())) {
      return null;
    }
    Object localObject = AccessController.doPrivileged(new PrivilegedAction()
    {
      private final File val$file;
      
      public Object run()
      {
        try
        {
          return new FileContentsImpl(this.val$file, ExtendedServiceImpl.DEFAULT_FILESIZE);
        }
        catch (IOException localIOException)
        {
          return localIOException;
        }
      }
    });
    if ((localObject instanceof IOException)) {
      throw ((IOException)localObject);
    }
    return (FileContents)localObject;
  }
  
  synchronized boolean askUser(String paramString)
  {
    SmartSecurityDialog localSmartSecurityDialog = new SmartSecurityDialog();
    
    JTextArea localJTextArea = new JTextArea(4, 30);
    localJTextArea.setFont(ResourceManager.getUIFont());
    localJTextArea.setEditable(false);
    
    localJTextArea.append(paramString);
    
    JScrollPane localJScrollPane = new JScrollPane(localJTextArea);
    
    String str1 = ResourceManager.getString("APIImpl.extended.fileOpen.message1");
    
    String str2 = ResourceManager.getString("APIImpl.extended.fileOpen.message2");
    
    Object[] arrayOfObject = { str1, localJScrollPane, str2 };
    
    return localSmartSecurityDialog.showDialog(arrayOfObject);
  }
  
  public FileContents[] openFiles(File[] paramArrayOfFile)
    throws IOException
  {
    if ((paramArrayOfFile == null) || (paramArrayOfFile.length <= 0)) {
      return null;
    }
    String str = "";
    for (int i = 0; i < paramArrayOfFile.length; i++) {
      str = str + paramArrayOfFile[i].getPath() + "\n";
    }
    if (!askUser(str)) {
      return null;
    }
    Object[] arrayOfObject = (Object[])AccessController.doPrivileged(new PrivilegedAction()
    {
      private final File[] val$files;
      
      public Object run()
      {
        FileContents[] arrayOfFileContents = new FileContents[this.val$files.length];
        try
        {
          for (int i = 0; i < this.val$files.length; i++) {
            arrayOfFileContents[i] = new FileContentsImpl(this.val$files[i], ExtendedServiceImpl.DEFAULT_FILESIZE);
          }
        }
        catch (IOException localIOException)
        {
          arrayOfFileContents[0] = localIOException;
        }
        return arrayOfFileContents;
      }
    });
    if ((arrayOfObject[0] instanceof IOException)) {
      throw ((IOException)arrayOfObject[0]);
    }
    return (FileContents[])arrayOfObject;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\ExtendedServiceImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */