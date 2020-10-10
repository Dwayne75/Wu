package org.fourthline.cling.support.messagebox.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.fourthline.cling.support.messagebox.parser.MessageElement;

public class DateTime
  implements ElementAppender
{
  private final String date;
  private final String time;
  
  public DateTime()
  {
    this(getCurrentDate(), getCurrentTime());
  }
  
  public DateTime(String date, String time)
  {
    this.date = date;
    this.time = time;
  }
  
  public String getDate()
  {
    return this.date;
  }
  
  public String getTime()
  {
    return this.time;
  }
  
  public void appendMessageElements(MessageElement parent)
  {
    ((MessageElement)parent.createChild("Date")).setContent(getDate());
    ((MessageElement)parent.createChild("Time")).setContent(getTime());
  }
  
  public static String getCurrentDate()
  {
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    return fmt.format(new Date());
  }
  
  public static String getCurrentTime()
  {
    SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
    return fmt.format(new Date());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\messagebox\model\DateTime.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */