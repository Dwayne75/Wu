package javax.activation;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.OutputStream;

public abstract interface DataContentHandler
{
  public abstract DataFlavor[] getTransferDataFlavors();
  
  public abstract Object getTransferData(DataFlavor paramDataFlavor, DataSource paramDataSource)
    throws UnsupportedFlavorException, IOException;
  
  public abstract Object getContent(DataSource paramDataSource)
    throws IOException;
  
  public abstract void writeTo(Object paramObject, String paramString, OutputStream paramOutputStream)
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\activation\DataContentHandler.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */