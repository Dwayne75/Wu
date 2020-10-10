package org.fourthline.cling.support.messagebox.model;

import org.fourthline.cling.support.messagebox.parser.MessageElement;

public class MessageScheduleReminder
  extends Message
{
  private final DateTime startTime;
  private final NumberName owner;
  private final String subject;
  private final DateTime endTime;
  private final String location;
  private final String body;
  
  public MessageScheduleReminder(DateTime startTime, NumberName owner, String subject, DateTime endTime, String location, String body)
  {
    this(Message.DisplayType.MAXIMUM, startTime, owner, subject, endTime, location, body);
  }
  
  public MessageScheduleReminder(Message.DisplayType displayType, DateTime startTime, NumberName owner, String subject, DateTime endTime, String location, String body)
  {
    super(Message.Category.SCHEDULE_REMINDER, displayType);
    this.startTime = startTime;
    this.owner = owner;
    this.subject = subject;
    this.endTime = endTime;
    this.location = location;
    this.body = body;
  }
  
  public DateTime getStartTime()
  {
    return this.startTime;
  }
  
  public NumberName getOwner()
  {
    return this.owner;
  }
  
  public String getSubject()
  {
    return this.subject;
  }
  
  public DateTime getEndTime()
  {
    return this.endTime;
  }
  
  public String getLocation()
  {
    return this.location;
  }
  
  public String getBody()
  {
    return this.body;
  }
  
  public void appendMessageElements(MessageElement parent)
  {
    getStartTime().appendMessageElements((MessageElement)parent.createChild("StartTime"));
    getOwner().appendMessageElements((MessageElement)parent.createChild("Owner"));
    ((MessageElement)parent.createChild("Subject")).setContent(getSubject());
    getEndTime().appendMessageElements((MessageElement)parent.createChild("EndTime"));
    ((MessageElement)parent.createChild("Location")).setContent(getLocation());
    ((MessageElement)parent.createChild("Body")).setContent(getBody());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\messagebox\model\MessageScheduleReminder.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */