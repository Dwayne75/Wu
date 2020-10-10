package javax.activation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class DataHandlerDataSource
  implements DataSource
{
  DataHandler dataHandler = null;
  
  public DataHandlerDataSource(DataHandler dh)
  {
    this.dataHandler = dh;
  }
  
  public InputStream getInputStream()
    throws IOException
  {
    return this.dataHandler.getInputStream();
  }
  
  public OutputStream getOutputStream()
    throws IOException
  {
    return this.dataHandler.getOutputStream();
  }
  
  public String getContentType()
  {
    return this.dataHandler.getContentType();
  }
  
  public String getName()
  {
    return this.dataHandler.getName();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\activation\DataHandlerDataSource.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */