package javax.activation;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;

public class DataHandler
  implements Transferable
{
  private DataSource dataSource = null;
  private DataSource objDataSource = null;
  private Object object = null;
  private String objectMimeType = null;
  private CommandMap currentCommandMap = null;
  private static final DataFlavor[] emptyFlavors = new DataFlavor[0];
  private DataFlavor[] transferFlavors = emptyFlavors;
  private DataContentHandler dataContentHandler = null;
  private DataContentHandler factoryDCH = null;
  private static DataContentHandlerFactory factory = null;
  private DataContentHandlerFactory oldFactory = null;
  private String shortType = null;
  
  public DataHandler(DataSource ds)
  {
    this.dataSource = ds;
    this.oldFactory = factory;
  }
  
  public DataHandler(Object obj, String mimeType)
  {
    this.object = obj;
    this.objectMimeType = mimeType;
    this.oldFactory = factory;
  }
  
  public DataHandler(URL url)
  {
    this.dataSource = new URLDataSource(url);
    this.oldFactory = factory;
  }
  
  private synchronized CommandMap getCommandMap()
  {
    if (this.currentCommandMap != null) {
      return this.currentCommandMap;
    }
    return CommandMap.getDefaultCommandMap();
  }
  
  public DataSource getDataSource()
  {
    if (this.dataSource == null)
    {
      if (this.objDataSource == null) {
        this.objDataSource = new DataHandlerDataSource(this);
      }
      return this.objDataSource;
    }
    return this.dataSource;
  }
  
  public String getName()
  {
    if (this.dataSource != null) {
      return this.dataSource.getName();
    }
    return null;
  }
  
  public String getContentType()
  {
    if (this.dataSource != null) {
      return this.dataSource.getContentType();
    }
    return this.objectMimeType;
  }
  
  public InputStream getInputStream()
    throws IOException
  {
    InputStream ins = null;
    if (this.dataSource != null)
    {
      ins = this.dataSource.getInputStream();
    }
    else
    {
      DataContentHandler dch = getDataContentHandler();
      if (dch == null) {
        throw new UnsupportedDataTypeException("no DCH for MIME type " + getBaseType());
      }
      if (((dch instanceof ObjectDataContentHandler)) && 
        (((ObjectDataContentHandler)dch).getDCH() == null)) {
        throw new UnsupportedDataTypeException("no object DCH for MIME type " + getBaseType());
      }
      final DataContentHandler fdch = dch;
      
      final PipedOutputStream pos = new PipedOutputStream();
      PipedInputStream pin = new PipedInputStream(pos);
      new Thread(new Runnable()
      {
        public void run()
        {
          try
          {
            fdch.writeTo(DataHandler.this.object, DataHandler.this.objectMimeType, pos); return;
          }
          catch (IOException e) {}finally
          {
            try
            {
              pos.close();
            }
            catch (IOException ie) {}
          }
        }
      }, "DataHandler.getInputStream").start();
      
      ins = pin;
    }
    return ins;
  }
  
  public void writeTo(OutputStream os)
    throws IOException
  {
    if (this.dataSource != null)
    {
      InputStream is = null;
      byte[] data = new byte[' '];
      
      is = this.dataSource.getInputStream();
      try
      {
        int bytes_read;
        while ((bytes_read = is.read(data)) > 0) {
          os.write(data, 0, bytes_read);
        }
      }
      finally
      {
        is.close();
        is = null;
      }
    }
    else
    {
      DataContentHandler dch = getDataContentHandler();
      dch.writeTo(this.object, this.objectMimeType, os);
    }
  }
  
  public OutputStream getOutputStream()
    throws IOException
  {
    if (this.dataSource != null) {
      return this.dataSource.getOutputStream();
    }
    return null;
  }
  
  public synchronized DataFlavor[] getTransferDataFlavors()
  {
    if (factory != this.oldFactory) {
      this.transferFlavors = emptyFlavors;
    }
    if (this.transferFlavors == emptyFlavors) {
      this.transferFlavors = getDataContentHandler().getTransferDataFlavors();
    }
    return this.transferFlavors;
  }
  
  public boolean isDataFlavorSupported(DataFlavor flavor)
  {
    DataFlavor[] lFlavors = getTransferDataFlavors();
    for (int i = 0; i < lFlavors.length; i++) {
      if (lFlavors[i].equals(flavor)) {
        return true;
      }
    }
    return false;
  }
  
  public Object getTransferData(DataFlavor flavor)
    throws UnsupportedFlavorException, IOException
  {
    return getDataContentHandler().getTransferData(flavor, this.dataSource);
  }
  
  public synchronized void setCommandMap(CommandMap commandMap)
  {
    if ((commandMap != this.currentCommandMap) || (commandMap == null))
    {
      this.transferFlavors = emptyFlavors;
      this.dataContentHandler = null;
      
      this.currentCommandMap = commandMap;
    }
  }
  
  public CommandInfo[] getPreferredCommands()
  {
    if (this.dataSource != null) {
      return getCommandMap().getPreferredCommands(getBaseType(), this.dataSource);
    }
    return getCommandMap().getPreferredCommands(getBaseType());
  }
  
  public CommandInfo[] getAllCommands()
  {
    if (this.dataSource != null) {
      return getCommandMap().getAllCommands(getBaseType(), this.dataSource);
    }
    return getCommandMap().getAllCommands(getBaseType());
  }
  
  public CommandInfo getCommand(String cmdName)
  {
    if (this.dataSource != null) {
      return getCommandMap().getCommand(getBaseType(), cmdName, this.dataSource);
    }
    return getCommandMap().getCommand(getBaseType(), cmdName);
  }
  
  public Object getContent()
    throws IOException
  {
    if (this.object != null) {
      return this.object;
    }
    return getDataContentHandler().getContent(getDataSource());
  }
  
  public Object getBean(CommandInfo cmdinfo)
  {
    Object bean = null;
    try
    {
      ClassLoader cld = null;
      
      cld = SecuritySupport.getContextClassLoader();
      if (cld == null) {
        cld = getClass().getClassLoader();
      }
      bean = cmdinfo.getCommandObject(this, cld);
    }
    catch (IOException e) {}catch (ClassNotFoundException e) {}
    return bean;
  }
  
  private synchronized DataContentHandler getDataContentHandler()
  {
    if (factory != this.oldFactory)
    {
      this.oldFactory = factory;
      this.factoryDCH = null;
      this.dataContentHandler = null;
      this.transferFlavors = emptyFlavors;
    }
    if (this.dataContentHandler != null) {
      return this.dataContentHandler;
    }
    String simpleMT = getBaseType();
    if ((this.factoryDCH == null) && (factory != null)) {
      this.factoryDCH = factory.createDataContentHandler(simpleMT);
    }
    if (this.factoryDCH != null) {
      this.dataContentHandler = this.factoryDCH;
    }
    if (this.dataContentHandler == null) {
      if (this.dataSource != null) {
        this.dataContentHandler = getCommandMap().createDataContentHandler(simpleMT, this.dataSource);
      } else {
        this.dataContentHandler = getCommandMap().createDataContentHandler(simpleMT);
      }
    }
    if (this.dataSource != null) {
      this.dataContentHandler = new DataSourceDataContentHandler(this.dataContentHandler, this.dataSource);
    } else {
      this.dataContentHandler = new ObjectDataContentHandler(this.dataContentHandler, this.object, this.objectMimeType);
    }
    return this.dataContentHandler;
  }
  
  private synchronized String getBaseType()
  {
    if (this.shortType == null)
    {
      String ct = getContentType();
      try
      {
        MimeType mt = new MimeType(ct);
        this.shortType = mt.getBaseType();
      }
      catch (MimeTypeParseException e)
      {
        this.shortType = ct;
      }
    }
    return this.shortType;
  }
  
  public static synchronized void setDataContentHandlerFactory(DataContentHandlerFactory newFactory)
  {
    if (factory != null) {
      throw new Error("DataContentHandlerFactory already defined");
    }
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
      try
      {
        security.checkSetFactory();
      }
      catch (SecurityException ex)
      {
        if (DataHandler.class.getClassLoader() != newFactory.getClass().getClassLoader()) {
          throw ex;
        }
      }
    }
    factory = newFactory;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\activation\DataHandler.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */