package com.sun.jnlp;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.DialogFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.jnlp.FileContents;
import javax.jnlp.FileSaveService;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public final class FileSaveServiceImpl
  implements FileSaveService
{
  static FileSaveServiceImpl _sharedInstance = null;
  private SmartSecurityDialog _securityDialog = null;
  private String _lastPath;
  
  private FileSaveServiceImpl()
  {
    this._securityDialog = new SmartSecurityDialog(ResourceManager.getString("APIImpl.file.save.message"));
  }
  
  public static synchronized FileSaveService getInstance()
  {
    if (_sharedInstance == null) {
      _sharedInstance = new FileSaveServiceImpl();
    }
    return _sharedInstance;
  }
  
  String getLastPath()
  {
    return this._lastPath;
  }
  
  void setLastPath(String paramString)
  {
    this._lastPath = paramString;
  }
  
  public FileContents saveAsFileDialog(String paramString, String[] paramArrayOfString, FileContents paramFileContents)
    throws IOException
  {
    return saveFileDialog(paramString, paramArrayOfString, paramFileContents.getInputStream(), paramFileContents.getName());
  }
  
  public FileContents saveFileDialog(String paramString1, String[] paramArrayOfString, InputStream paramInputStream, String paramString2)
    throws IOException
  {
    try
    {
      if (!askUser())
      {
        localObject1 = null;
        
        return (FileContents)localObject1;
      }
      Object localObject1 = AccessController.doPrivileged(new PrivilegedAction()
      {
        private final String val$pathHint;
        private final InputStream val$stream;
        
        public Object run()
        {
          JFileChooser localJFileChooser = null;
          FileSystemView localFileSystemView = FileOpenServiceImpl.getFileSystemView();
          if (this.val$pathHint != null) {
            localJFileChooser = new JFileChooser(this.val$pathHint, localFileSystemView);
          } else {
            localJFileChooser = new JFileChooser(FileSaveServiceImpl.this.getLastPath(), localFileSystemView);
          }
          localJFileChooser.setFileSelectionMode(0);
          localJFileChooser.setDialogType(1);
          localJFileChooser.setMultiSelectionEnabled(false);
          int i = localJFileChooser.showSaveDialog(null);
          if (i == 1) {
            return null;
          }
          File localFile = localJFileChooser.getSelectedFile();
          if (localFile != null)
          {
            if (!FileSaveServiceImpl.fileChk(localFile)) {
              return null;
            }
            try
            {
              byte[] arrayOfByte = new byte[' '];
              BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localFile));
              BufferedInputStream localBufferedInputStream = new BufferedInputStream(this.val$stream);
              int j = localBufferedInputStream.read(arrayOfByte);
              while (j != -1)
              {
                localBufferedOutputStream.write(arrayOfByte, 0, j);
                j = localBufferedInputStream.read(arrayOfByte);
              }
              localBufferedOutputStream.close();
              FileSaveServiceImpl.this.setLastPath(localFile.getPath());
              return new FileContentsImpl(localFile, FileSaveServiceImpl.computeMaxLength(localFile.length()));
            }
            catch (IOException localIOException)
            {
              return localIOException;
            }
          }
          return null;
        }
      });
      if ((localObject1 instanceof IOException)) {
        throw ((IOException)localObject1);
      }
      FileContents localFileContents = (FileContents)localObject1;
      
      return localFileContents;
    }
    finally {}
  }
  
  synchronized boolean askUser()
  {
    if (CheckServicePermission.hasFileAccessPermissions()) {
      return true;
    }
    return this._securityDialog.showDialog();
  }
  
  static long computeMaxLength(long paramLong)
  {
    return paramLong * 3L;
  }
  
  static boolean fileChk(File paramFile)
  {
    if (paramFile.exists())
    {
      String str1 = ResourceManager.getString("APIImpl.file.save.fileExist", paramFile.getPath());
      
      String str2 = ResourceManager.getMessage("APIImpl.file.save.fileExistTitle");
      
      int i = DialogFactory.showConfirmDialog(str1, str2);
      return i == 0;
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\FileSaveServiceImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */