package com.wurmonline.server.gui.propertysheet;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.gui.PlayerData;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;

public class PlayerPropertySheet
  extends VBox
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(PlayerPropertySheet.class.getName());
  private PlayerData current;
  private final ObservableList<PropertySheet.Item> list;
  private Set<PlayerPropertySheet.PropertyType> changedProperties = new HashSet();
  
  public PlayerPropertySheet(PlayerData entry)
  {
    this.current = entry;
    this.list = FXCollections.observableArrayList();
    this.list.add(new PlayerPropertySheet.CustomPropertyItem(this, PlayerPropertySheet.PropertyType.NAME, "Name", "Player Name", "Name", true, entry
      .getName()));
    this.list.add(new PlayerPropertySheet.CustomPropertyItem(this, PlayerPropertySheet.PropertyType.POSX, "Position X", "Position in X", "The X position of the player", true, 
      Float.valueOf(entry.getPosx())));
    this.list.add(new PlayerPropertySheet.CustomPropertyItem(this, PlayerPropertySheet.PropertyType.POSY, "Position Y", "Position in Y", "The Y position of the player", true, 
      Float.valueOf(entry.getPosy())));
    this.list.add(new PlayerPropertySheet.CustomPropertyItem(this, PlayerPropertySheet.PropertyType.POWER, "Power", "Player Game Management Power", "Power from 0 to 5. 2 is Game Manager, 4 is Head GM and 5 Implementor", true, 
      Integer.valueOf(entry.getPower())));
    this.list.add(new PlayerPropertySheet.CustomPropertyItem(this, PlayerPropertySheet.PropertyType.CURRENTSERVER, "Current server", "Server id of the player", "The id of the server that the player is on", true, 
      Integer.valueOf(entry.getServer())));
    this.list.add(new PlayerPropertySheet.CustomPropertyItem(this, PlayerPropertySheet.PropertyType.UNDEAD, "Undead", "Whether the player is undead", "Lets the player play as undead", true, 
      Boolean.valueOf(entry.isUndead())));
    PropertySheet propertySheet = new PropertySheet(this.list);
    VBox.setVgrow(propertySheet, Priority.ALWAYS);
    getChildren().add(propertySheet);
  }
  
  public PlayerData getCurrentData()
  {
    return this.current;
  }
  
  public final String save()
  {
    String toReturn = "";
    boolean saveAtAll = false;
    for (PlayerPropertySheet.CustomPropertyItem item : (PlayerPropertySheet.CustomPropertyItem[])this.list.toArray(new PlayerPropertySheet.CustomPropertyItem[this.list.size()])) {
      if (this.changedProperties.contains(item.getPropertyType()))
      {
        saveAtAll = true;
        try
        {
          switch (PlayerPropertySheet.1.$SwitchMap$com$wurmonline$server$gui$propertysheet$PlayerPropertySheet$PropertyType[item.getPropertyType().ordinal()])
          {
          case 1: 
            this.current.setName(item.getValue().toString());
            break;
          case 2: 
            this.current.setPosx(((Float)item.getValue()).floatValue());
            break;
          case 3: 
            this.current.setPosy(((Float)item.getValue()).floatValue());
            break;
          case 4: 
            this.current.setPower(((Integer)item.getValue()).intValue());
            break;
          case 5: 
            this.current.setServer(((Integer)item.getValue()).intValue());
            break;
          case 6: 
            if (!this.current.isUndead()) {
              this.current.setUndeadType((byte)(1 + Server.rand.nextInt(3)));
            } else {
              this.current.setUndeadType((byte)0);
            }
            break;
          }
        }
        catch (Exception ex)
        {
          saveAtAll = false;
          toReturn = toReturn + "Invalid value " + item.getCategory() + ": " + item.getValue() + ". ";
          logger.log(Level.INFO, "Error " + ex.getMessage(), ex);
        }
      }
    }
    if (toReturn.length() == 0) {
      if (saveAtAll) {
        try
        {
          this.current.save();
          toReturn = "ok";
        }
        catch (Exception ex)
        {
          toReturn = ex.getMessage();
        }
      }
    }
    return toReturn;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\gui\propertysheet\PlayerPropertySheet.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */