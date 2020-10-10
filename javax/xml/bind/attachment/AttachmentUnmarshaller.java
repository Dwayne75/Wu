package javax.xml.bind.attachment;

import javax.activation.DataHandler;

public abstract class AttachmentUnmarshaller
{
  public abstract DataHandler getAttachmentAsDataHandler(String paramString);
  
  public abstract byte[] getAttachmentAsByteArray(String paramString);
  
  public boolean isXOPPackage()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\attachment\AttachmentUnmarshaller.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */