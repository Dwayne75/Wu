package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.zones.VolaTile;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public final class ChangeAppearanceQuestion
  extends Question
{
  private Item mirror;
  private byte gender;
  
  public ChangeAppearanceQuestion(Creature aResponder, Item aItem)
  {
    super(aResponder, "Golden Mirror", "This mirror allows you to change your gender and alter your appearance.", 51, aResponder.getWurmId());
    this.mirror = aItem;
    this.gender = Byte.MAX_VALUE;
  }
  
  private void handleGenderChange()
  {
    if (getResponder().getSex() != this.gender)
    {
      Player player = (Player)getResponder();
      try
      {
        player.getSaveFile().setFace(0L);
      }
      catch (IOException ex)
      {
        player.getCommunicator().sendAlertServerMessage("Something went wrong changing your gender. You remain as you were.", (byte)3);
        logger.warning("Error setting face for player " + player.getName() + ": " + ex.getMessage());
        return;
      }
      player.setVisible(false);
      getResponder().setSex(this.gender);
      if (player.getCurrentTile() != null) {
        player.getCurrentTile().setNewFace(player);
      }
      getResponder().setModelName("Human");
      player.setVisible(true);
      getResponder().getCommunicator().sendNewFace(getResponder().getWurmId(), getResponder().getFace());
      getResponder().getCommunicator().sendSafeServerMessage("You feel a strange sensation as Vynora's power alters your body. You are now " + (this.gender == 1 ? "female" : "male") + ".", (byte)2);
    }
    else
    {
      getResponder().getCommunicator().sendSafeServerMessage("Your gender remains the same.");
    }
    this.mirror.setAuxData((byte)1);
    this.mirror.sendUpdate();
    getResponder().getCommunicator().sendSafeServerMessage("The mirror's glow diminishes slightly as some of the magic is used.", (byte)2);
    getResponder().getCommunicator().sendCustomizeFace(getResponder().getFace(), this.mirror.getWurmId());
  }
  
  private void sendConfirmation()
  {
    if (this.mirror.getAuxData() == 1) {
      return;
    }
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    buf.append("harray{text{text=''}}text{type='bold';text='Are you sure? This mirror will not allow you to make this choice again.'}harray{text{text=''}}");
    if (this.gender == getResponder().getSex()) {
      buf.append("radio{group='confirm';id='yes';text='Yes, I wish to remain " + (this.gender == 1 ? "female" : "male") + "';}");
    } else {
      buf.append("radio{group='confirm';id='yes';text='Yes, I wish to become " + (this.gender == 1 ? "female" : "male") + "';}");
    }
    buf.append("radio{group='confirm';id='no';text='No, I do not wish to make this decision now.'}");
    buf.append("harray{text{text=''}}");
    buf.append(createAnswerButton2("Next"));
    getResponder().getCommunicator().sendBml(300, 250, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  public void answer(Properties answers)
  {
    if (this.mirror.getOwnerId() != getResponder().getWurmId())
    {
      getResponder().getCommunicator().sendAlertServerMessage("You are no longer in possession of this mirror.", (byte)3);
      return;
    }
    if (answers.getProperty("confirm", "").equals("yes"))
    {
      handleGenderChange();
    }
    else if ((answers.getProperty("gender", "").equals("male")) || (answers.getProperty("gender", "").equals("female")))
    {
      ChangeAppearanceQuestion question = new ChangeAppearanceQuestion(getResponder(), this.mirror);
      if (answers.getProperty("gender").equals("male")) {
        question.gender = 0;
      } else {
        question.gender = 1;
      }
      question.sendConfirmation();
    }
    else
    {
      getResponder().getCommunicator().sendSafeServerMessage("You put the mirror away, leaving your body as it was.", (byte)2);
    }
  }
  
  public void sendQuestion()
  {
    if (this.mirror.getAuxData() == 1) {
      return;
    }
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    buf.append("harray{text{text=''}}text{text='Before you may change your appearance, you must choose to select a new gender or keep your current one.'}harray{text{text=''}}text{type='bold';text='What will your gender be?'}");
    
    buf.append(femaleOption());
    buf.append(maleOption());
    buf.append("harray{text{text=''}}");
    buf.append(createAnswerButton2("Next"));
    getResponder().getCommunicator().sendBml(300, 250, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private final String maleOption()
  {
    if (getResponder().getSex() == 0) {
      return "harray{text{text=''}radio{ group='gender'; id='male';text='Male (current)';selected='true'}}";
    }
    return "harray{text{text=''}radio{ group='gender'; id='male';text='Male'}}";
  }
  
  private final String femaleOption()
  {
    if (getResponder().getSex() == 1) {
      return "harray{text{text=''}radio{ group='gender'; id='female';text='Female (current)';selected='true'}}";
    }
    return "harray{text{text=''}radio{ group='gender'; id='female';text='Female'}}";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\ChangeAppearanceQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */