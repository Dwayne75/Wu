package com.sun.jnlp;

import com.sun.deploy.config.Config;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Vector;
import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public final class FileOpenServiceImpl
  implements FileOpenService
{
  static FileOpenServiceImpl _sharedInstance = null;
  static FileSaveServiceImpl _fileSaveServiceImpl;
  
  private FileOpenServiceImpl(FileSaveServiceImpl paramFileSaveServiceImpl)
  {
    _fileSaveServiceImpl = paramFileSaveServiceImpl;
  }
  
  public static synchronized FileOpenService getInstance()
  {
    if (_sharedInstance == null) {
      _sharedInstance = new FileOpenServiceImpl((FileSaveServiceImpl)FileSaveServiceImpl.getInstance());
    }
    return _sharedInstance;
  }
  
  public static FileSystemView getFileSystemView()
  {
    Object localObject = FileSystemView.getFileSystemView();
    if (Config.getInstance().useAltFileSystemView())
    {
      String str = System.getProperty("java.version");
      if ((str.startsWith("1.2")) || (str.startsWith("1.3"))) {
        localObject = new WindowsAltFileSystemView();
      }
    }
    return (FileSystemView)localObject;
  }
  
  public FileContents openFileDialog(String paramString, String[] paramArrayOfString)
    throws IOException
  {
    try
    {
      if (!_fileSaveServiceImpl.askUser())
      {
        localFileContents1 = null;
        
        return localFileContents1;
      }
      FileContents localFileContents1 = (FileContents)AccessController.doPrivileged(new PrivilegedAction()
      {
        private final String val$pathHint;
        
        public Object run()
        {
          JFileChooser localJFileChooser = null;
          
          FileSystemView localFileSystemView = FileOpenServiceImpl.getFileSystemView();
          if (this.val$pathHint != null) {
            localJFileChooser = new JFileChooser(this.val$pathHint, localFileSystemView);
          } else {
            localJFileChooser = new JFileChooser(FileOpenServiceImpl._fileSaveServiceImpl.getLastPath(), localFileSystemView);
          }
          localJFileChooser.setFileSelectionMode(0);
          localJFileChooser.setDialogType(0);
          localJFileChooser.setMultiSelectionEnabled(false);
          int i = localJFileChooser.showOpenDialog(null);
          if (i == 1) {
            return null;
          }
          File localFile = localJFileChooser.getSelectedFile();
          if (localFile != null) {
            try
            {
              FileOpenServiceImpl._fileSaveServiceImpl.setLastPath(localFile.getPath());
              return new FileContentsImpl(localFile, FileSaveServiceImpl.computeMaxLength(localFile.length()));
            }
            catch (FileNotFoundException localFileNotFoundException) {}catch (IOException localIOException) {}
          }
          return null;
        }
      });
      FileContents localFileContents2 = localFileContents1;
      
      return localFileContents2;
    }
    finally {}
  }
  
  public FileContents[] openMultiFileDialog(String paramString, String[] paramArrayOfString)
    throws IOException
  {
    try
    {
      if (!_fileSaveServiceImpl.askUser())
      {
        arrayOfFileContents1 = null;
        
        return arrayOfFileContents1;
      }
      FileContents[] arrayOfFileContents1 = (FileContents[])AccessController.doPrivileged(new PrivilegedAction()
      {
        private final String val$pathHint;
        
        public Object run()
        {
          JFileChooser localJFileChooser = null;
          FileSystemView localFileSystemView = FileOpenServiceImpl.getFileSystemView();
          if (this.val$pathHint != null) {
            localJFileChooser = new JFileChooser(this.val$pathHint, localFileSystemView);
          } else {
            localJFileChooser = new JFileChooser(FileOpenServiceImpl._fileSaveServiceImpl.getLastPath(), localFileSystemView);
          }
          localJFileChooser.setFileSelectionMode(0);
          localJFileChooser.setDialogType(0);
          localJFileChooser.setMultiSelectionEnabled(true);
          int i = localJFileChooser.showOpenDialog(null);
          if (i == 1) {
            return null;
          }
          File[] arrayOfFile = localJFileChooser.getSelectedFiles();
          if ((arrayOfFile != null) && (arrayOfFile.length > 0))
          {
            FileContents[] arrayOfFileContents = new FileContents[arrayOfFile.length];
            for (int j = 0; j < arrayOfFile.length; j++) {
              try
              {
                arrayOfFileContents[j] = new FileContentsImpl(arrayOfFile[j], FileSaveServiceImpl.computeMaxLength(arrayOfFile[j].length()));
                FileOpenServiceImpl._fileSaveServiceImpl.setLastPath(arrayOfFile[j].getPath());
              }
              catch (FileNotFoundException localFileNotFoundException) {}catch (IOException localIOException) {}
            }
            return arrayOfFileContents;
          }
          return null;
        }
      });
      FileContents[] arrayOfFileContents2 = arrayOfFileContents1;
      
      return arrayOfFileContents2;
    }
    finally {}
  }
  
  static class WindowsAltFileSystemView
    extends FileSystemView
  {
    private static final Object[] noArgs = new Object[0];
    private static final Class[] noArgTypes = new Class[0];
    private static Method listRootsMethod = null;
    private static boolean listRootsMethodChecked = false;
    
    public boolean isRoot(File paramFile)
    {
      if (!paramFile.isAbsolute()) {
        return false;
      }
      String str = paramFile.getParent();
      if (str == null) {
        return true;
      }
      File localFile = new File(str);
      return localFile.equals(paramFile);
    }
    
    public File createNewFolder(File paramFile)
      throws IOException
    {
      if (paramFile == null) {
        throw new IOException("Containing directory is null:");
      }
      File localFile = null;
      
      localFile = createFileObject(paramFile, "New Folder");
      int i = 2;
      while ((localFile.exists()) && (i < 100))
      {
        localFile = createFileObject(paramFile, "New Folder (" + i + ")");
        i++;
      }
      if (localFile.exists()) {
        throw new IOException("Directory already exists:" + localFile.getAbsolutePath());
      }
      localFile.mkdirs();
      
      return localFile;
    }
    
    public boolean isHiddenFile(File paramFile)
    {
      return false;
    }
    
    public File[] getRoots()
    {
      Vector localVector = new Vector();
      
      FileSystemRoot localFileSystemRoot1 = new FileSystemRoot("A:\\");
      localVector.addElement(localFileSystemRoot1);
      for (int i = 67; i <= 90; i = (char)(i + 1))
      {
        char[] arrayOfChar = { i, ':', '\\' };
        String str = new String(arrayOfChar);
        FileSystemRoot localFileSystemRoot2 = new FileSystemRoot(str);
        if ((localFileSystemRoot2 != null) && (localFileSystemRoot2.exists())) {
          localVector.addElement(localFileSystemRoot2);
        }
      }
      File[] arrayOfFile = new File[localVector.size()];
      localVector.copyInto(arrayOfFile);
      return arrayOfFile;
    }
    
    class FileSystemRoot
      extends File
    {
      public FileSystemRoot(File paramFile)
      {
        super("");
      }
      
      public FileSystemRoot(String paramString)
      {
        super();
      }
      
      public boolean isDirectory()
      {
        return true;
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\FileOpenServiceImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */