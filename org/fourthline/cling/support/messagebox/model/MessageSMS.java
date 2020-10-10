package org.fourthline.cling.support.messagebox.model;

import org.fourthline.cling.support.messagebox.parser.MessageElement;

public class MessageSMS
  extends Message
{
  private final DateTime receiveTime;
  private final NumberName receiver;
  private final NumberName sender;
  private final String body;
  
  public MessageSMS(NumberName receiver, NumberName sender, String body)
  {
    this(new DateTime(), receiver, sender, body);
  }
  
  public MessageSMS(DateTime receiveTime, NumberName receiver, NumberName sender, String body)
  {
    this(Message.DisplayType.MAXIMUM, receiveTime, receiver, sender, body);
  }
  
  public MessageSMS(Message.DisplayType displayType, DateTime receiveTime, NumberName receiver, NumberName sender, String body)
  {
    super(Message.Category.SMS, displayType);
    this.receiveTime = receiveTime;
    this.receiver = receiver;
    this.sender = sender;
    this.body = body;
  }
  
  public DateTime getReceiveTime()
  {
    return this.receiveTime;
  }
  
  public NumberName getReceiver()
  {
    return this.receiver;
  }
  
  public NumberName getSender()
  {
    return this.sender;
  }
  
  public String getBody()
  {
    return this.body;
  }
  
  public void appendMessageElements(MessageElement parent)
  {
    getReceiveTime().appendMessageElements((MessageElement)parent.createChild("ReceiveTime"));
    getReceiver().appendMessageElements((MessageElement)parent.createChild("Receiver"));
    getSender().appendMessageElements((MessageElement)parent.createChild("Sender"));
    ((MessageElement)parent.createChild("Body")).setContent(getBody());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\messagebox\model\MessageSMS.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */