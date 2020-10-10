package javax.activation;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.OutputStream;

class DataSourceDataContentHandler
  implements DataContentHandler
{
  private DataSource ds = null;
  private DataFlavor[] transferFlavors = null;
  private DataContentHandler dch = null;
  
  public DataSourceDataContentHandler(DataContentHandler dch, DataSource ds)
  {
    this.ds = ds;
    this.dch = dch;
  }
  
  public DataFlavor[] getTransferDataFlavors()
  {
    if (this.transferFlavors == null) {
      if (this.dch != null)
      {
        this.transferFlavors = this.dch.getTransferDataFlavors();
      }
      else
      {
        this.transferFlavors = new DataFlavor[1];
        this.transferFlavors[0] = new ActivationDataFlavor(this.ds.getContentType(), this.ds.getContentType());
      }
    }
    return this.transferFlavors;
  }
  
  public Object getTransferData(DataFlavor df, DataSource ds)
    throws UnsupportedFlavorException, IOException
  {
    if (this.dch != null) {
      return this.dch.getTransferData(df, ds);
    }
    if (df.equals(getTransferDataFlavors()[0])) {
      return ds.getInputStream();
    }
    throw new UnsupportedFlavorException(df);
  }
  
  public Object getContent(DataSource ds)
    throws IOException
  {
    if (this.dch != null) {
      return this.dch.getContent(ds);
    }
    return ds.getInputStream();
  }
  
  public void writeTo(Object obj, String mimeType, OutputStream os)
    throws IOException
  {
    if (this.dch != null) {
      this.dch.writeTo(obj, mimeType, os);
    } else {
      throw new UnsupportedDataTypeException("no DCH for content type " + this.ds.getContentType());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\activation\DataSourceDataContentHandler.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */