package org.fourthline.cling.support.messagebox.model;

import java.util.Random;
import org.fourthline.cling.support.messagebox.parser.MessageDOM;
import org.fourthline.cling.support.messagebox.parser.MessageDOMParser;
import org.fourthline.cling.support.messagebox.parser.MessageElement;
import org.seamless.xml.ParserException;

public abstract class Message
  implements ElementAppender
{
  protected final Random randomGenerator = new Random();
  private final int id;
  private final Category category;
  private DisplayType displayType;
  
  public static enum Category
  {
    SMS("SMS"),  INCOMING_CALL("Incoming Call"),  SCHEDULE_REMINDER("Schedule Reminder");
    
    public String text;
    
    private Category(String text)
    {
      this.text = text;
    }
  }
  
  public static enum DisplayType
  {
    MINIMUM("Minimum"),  MAXIMUM("Maximum");
    
    public String text;
    
    private DisplayType(String text)
    {
      this.text = text;
    }
  }
  
  public Message(Category category, DisplayType displayType)
  {
    this(0, category, displayType);
  }
  
  public Message(int id, Category category, DisplayType displayType)
  {
    if (id == 0) {
      id = this.randomGenerator.nextInt(Integer.MAX_VALUE);
    }
    this.id = id;
    this.category = category;
    this.displayType = displayType;
  }
  
  public int getId()
  {
    return this.id;
  }
  
  public Category getCategory()
  {
    return this.category;
  }
  
  public DisplayType getDisplayType()
  {
    return this.displayType;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    Message message = (Message)o;
    if (this.id != message.id) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    return this.id;
  }
  
  public String toString()
  {
    try
    {
      MessageDOMParser mp = new MessageDOMParser();
      MessageDOM dom = (MessageDOM)mp.createDocument();
      
      MessageElement root = dom.createRoot(mp.createXPath(), "Message");
      ((MessageElement)root.createChild("Category")).setContent(getCategory().text);
      ((MessageElement)root.createChild("DisplayType")).setContent(getDisplayType().text);
      appendMessageElements(root);
      
      String s = mp.print(dom, 0, false);
      
      return s.replaceAll("<Message xmlns=\"urn:samsung-com:messagebox-1-0\">", "").replaceAll("</Message>", "");
    }
    catch (ParserException ex)
    {
      throw new RuntimeException(ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\messagebox\model\Message.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */