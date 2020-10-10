package javax.mail;

import javax.activation.DataSource;

public abstract interface MultipartDataSource
  extends DataSource
{
  public abstract int getCount();
  
  public abstract BodyPart getBodyPart(int paramInt)
    throws MessagingException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\MultipartDataSource.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */