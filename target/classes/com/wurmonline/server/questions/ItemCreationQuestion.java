package com.wurmonline.server.questions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;

public final class ItemCreationQuestion
  extends Question
{
  private LinkedList<ItemTemplate> itemplates = new LinkedList();
  private final String filter;
  
  public ItemCreationQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 5, aTarget);
    this.filter = "*";
  }
  
  public ItemCreationQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, String aFilter)
  {
    super(aResponder, aTitle, aQuestion, 5, aTarget);
    this.filter = aFilter;
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    
    String val = getAnswer().getProperty("filterme");
    if ((val != null) && (val.equals("true")))
    {
      val = getAnswer().getProperty("filtertext");
      if ((val == null) || (val.length() == 0)) {
        val = "*";
      }
      ItemCreationQuestion icq = new ItemCreationQuestion(getResponder(), this.title, this.question, this.target, val);
      
      icq.sendQuestion();
    }
    else
    {
      QuestionParser.parseItemCreationQuestion(this);
    }
  }
  
  public void sendQuestion()
  {
    int height = 225;
    this.itemplates = new LinkedList();
    StringBuilder buf = new StringBuilder(getBmlHeader());
    buf.append("harray{label{text=\"List shows name -material\"}}");
    ItemTemplate[] templates = ItemTemplateFactory.getInstance().getTemplates();
    
    Arrays.sort(templates);
    for (int x = 0; x < templates.length; x++) {
      if (!templates[x].isNoCreate()) {
        if (getResponder().getPower() != 5)
        {
          if (!templates[x].unique) {
            if ((!templates[x].isPuppet()) && 
              (templates[x].getTemplateId() != 175) && 
              (templates[x].getTemplateId() != 654) && 
              (templates[x].getTemplateId() != 738) && 
              (templates[x].getTemplateId() != 972) && 
              (templates[x].getTemplateId() != 1032) && 
              (templates[x].getTemplateId() != 1297) && 
              (templates[x].getTemplateId() != 1437) && (!templates[x].isRoyal)) {
              if (templates[x].isUnstableRift()) {}
            }
          }
        }
        else if ((getResponder().getPower() >= 2) || (templates[x].getTemplateId() == 781) || (
          (templates[x].isBulk()) && (!templates[x].isFood()) && 
          (templates[x].getTemplateId() != 683) && 
          (templates[x].getTemplateId() != 737) && 
          (templates[x].getTemplateId() != 175) && 
          (templates[x].getTemplateId() != 654) && 
          (templates[x].getTemplateId() != 738) && 
          (templates[x].getTemplateId() != 972) && 
          (templates[x].getTemplateId() != 1032))) {
          if (PlayerInfoFactory.wildCardMatch(templates[x].getName().toLowerCase(), this.filter.toLowerCase())) {
            this.itemplates.add(templates[x]);
          }
        }
      }
    }
    if (this.itemplates.size() != 1) {
      this.itemplates.add(0, null);
    }
    buf.append("harray{label{text=\"Item\"};dropdown{id=\"data1\";options=\"");
    for (int i = 0; i < this.itemplates.size(); i++)
    {
      if (i > 0) {
        buf.append(",");
      }
      ItemTemplate tp = (ItemTemplate)this.itemplates.get(i);
      if (tp == null) {
        buf.append("Nothing");
      } else if ((tp.isMetal()) || (tp.isWood()) || (tp.isOre) || (tp.isShard)) {
        buf.append(tp.getName() + " - " + tp.sizeString + Item.getMaterialString(tp.getMaterial()) + " ");
      } else if (tp.bowUnstringed) {
        buf.append(tp.getName() + " - " + tp.sizeString + " [unstringed]");
      } else {
        buf.append(tp.getName() + (tp.sizeString.isEmpty() ? "" : new StringBuilder().append(" - ").append(tp.sizeString).toString()));
      }
    }
    buf.append("\"}}");
    
    buf.append("harray{button{text=\"Filter list\";id=\"filterme\"};label{text=\" using \"};input{maxchars=\"30\";id=\"filtertext\";text=\"" + this.filter + "\";onenter=\"filterme\"}}");
    
    buf.append("harray{label{text=\"Material\"};dropdown{id=\"material\";options=\"");
    for (int x = 0; x <= 96; x++) {
      if (x == 0)
      {
        buf.append("standard");
      }
      else
      {
        buf.append(",");
        buf.append(Item.getMaterialString((byte)x));
      }
    }
    buf.append("\"}");
    if ((Servers.isThisATestServer()) && (getResponder().getPower() > 2))
    {
      buf.append("label{text=\"   \"}");
      buf.append("checkbox{id=\"alltypes\";text=\"All Types \";selected=\"false\";hover=\"If qty is 1 and standard material, makes one of each normal material type\"}");
    }
    buf.append("}");
    buf.append("harray{label{text=\"Number of items   \"};input{maxchars=\"3\"; id=\"number\"; text=\"1\"}}");
    buf.append("harray{label{text=\"Item qualitylevel \"};input{maxchars=\"2\"; id=\"data2\"; text=\"1\"}}");
    
    buf.append("harray{label{text=\"Custom size mod (float.eg. 0.3)\"};input{maxchars=\"4\"; id=\"sizemod\"; text=\"\"}}");
    if (getResponder().getPower() >= 4)
    {
      buf.append("table{rows=\"1\";cols=\"8\";");
      buf.append("radio{group=\"rare\";id=\"0\";selected=\"true\"};label{text=\"Common\"};");
      buf.append("radio{group=\"rare\";id=\"1\"};label{text=\"Rare\"};");
      buf.append("radio{group=\"rare\";id=\"2\"};label{text=\"Supreme\"};");
      buf.append("radio{group=\"rare\";id=\"3\"};label{text=\"Fantastic\"};");
      buf.append("}");
      buf.append("harray{label{text='Item Actual Name';hover=\"leave blank to use its base name\"};input{id='itemName'; maxchars='60'; text=''}}");
      
      buf.append("harray{label{text=\"Colour:\";hover=\"leave blank to use default\"};label{text='R'};input{id='c_red'; maxchars='3'; text=''}label{text='G'};input{id='c_green'; maxchars='3'; text=''}label{text='B'};input{id='c_blue'; maxchars='3'; text=''}}");
      
      height += 50;
    }
    else
    {
      buf.append("passthrough{id=\"rare\";text=\"0\"}");
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(250, height, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  ItemTemplate getTemplate(int aTemplateId)
  {
    return (ItemTemplate)this.itemplates.get(aTemplateId);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\ItemCreationQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */