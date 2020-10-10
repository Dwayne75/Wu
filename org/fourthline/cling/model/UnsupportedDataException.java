package org.fourthline.cling.model;

public class UnsupportedDataException
  extends RuntimeException
{
  private static final long serialVersionUID = 661795454401413339L;
  protected Object data;
  
  public UnsupportedDataException(String s)
  {
    super(s);
  }
  
  public UnsupportedDataException(String s, Throwable throwable)
  {
    super(s, throwable);
  }
  
  public UnsupportedDataException(String s, Throwable throwable, Object data)
  {
    super(s, throwable);
    this.data = data;
  }
  
  public Object getData()
  {
    return this.data;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\UnsupportedDataException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */