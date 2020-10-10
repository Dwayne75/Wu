package javax.mail;

public class MessageContext
{
  private Part part;
  
  public MessageContext(Part part)
  {
    this.part = part;
  }
  
  public Part getPart()
  {
    return this.part;
  }
  
  public Message getMessage()
  {
    try
    {
      return getMessage(this.part);
    }
    catch (MessagingException ex) {}
    return null;
  }
  
  private static Message getMessage(Part p)
    throws MessagingException
  {
    while (p != null)
    {
      if ((p instanceof Message)) {
        return (Message)p;
      }
      BodyPart bp = (BodyPart)p;
      Multipart mp = bp.getParent();
      if (mp == null) {
        return null;
      }
      p = mp.getParent();
    }
    return null;
  }
  
  public Session getSession()
  {
    Message msg = getMessage();
    return msg != null ? msg.session : null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\MessageContext.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */