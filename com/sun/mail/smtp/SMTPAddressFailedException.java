package com.sun.mail.smtp;

import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;

public class SMTPAddressFailedException
  extends SendFailedException
{
  protected InternetAddress addr;
  protected String cmd;
  protected int rc;
  private static final long serialVersionUID = 804831199768630097L;
  
  public SMTPAddressFailedException(InternetAddress addr, String cmd, int rc, String err)
  {
    super(err);
    this.addr = addr;
    this.cmd = cmd;
    this.rc = rc;
  }
  
  public InternetAddress getAddress()
  {
    return this.addr;
  }
  
  public String getCommand()
  {
    return this.cmd;
  }
  
  public int getReturnCode()
  {
    return this.rc;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\smtp\SMTPAddressFailedException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */