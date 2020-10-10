package com.wurmonline.server.banks;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class BankUnavailableException
  extends WurmServerException
{
  public static final String VERSION = "$Revision: 1.4 $";
  private static final long serialVersionUID = -5632991262062075642L;
  
  public BankUnavailableException(String message)
  {
    super(message);
  }
  
  public BankUnavailableException(Throwable cause)
  {
    super(cause);
  }
  
  public BankUnavailableException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\banks\BankUnavailableException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */