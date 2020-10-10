package com.wurmonline.server.gui.propertysheet;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.gui.PlayerData;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.PropertyEditor;

public class PlayerPropertySheet
  extends VBox
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(PlayerPropertySheet.class.getName());
  private PlayerData current;
  private final ObservableList<PropertySheet.Item> list;
  
  private static enum PropertyType
  {
    NAME,  POSX,  POSY,  POWER,  CURRENTSERVER,  UNDEAD;
    
    private PropertyType() {}
  }
  
  private Set<PropertyType> changedProperties = new HashSet();
  
  public PlayerPropertySheet(PlayerData entry)
  {
    this.current = entry;
    this.list = FXCollections.observableArrayList();
    this.list.add(new CustomPropertyItem(PropertyType.NAME, "Name", "Player Name", "Name", true, entry
      .getName()));
    this.list.add(new CustomPropertyItem(PropertyType.POSX, "Position X", "Position in X", "The X position of the player", true, 
      Float.valueOf(entry.getPosx())));
    this.list.add(new CustomPropertyItem(PropertyType.POSY, "Position Y", "Position in Y", "The Y position of the player", true, 
      Float.valueOf(entry.getPosy())));
    this.list.add(new CustomPropertyItem(PropertyType.POWER, "Power", "Player Game Management Power", "Power from 0 to 5. 2 is Game Manager, 4 is Head GM and 5 Implementor", true, 
      Integer.valueOf(entry.getPower())));
    this.list.add(new CustomPropertyItem(PropertyType.CURRENTSERVER, "Current server", "Server id of the player", "The id of the server that the player is on", true, 
      Integer.valueOf(entry.getServer())));
    this.list.add(new CustomPropertyItem(PropertyType.UNDEAD, "Undead", "Whether the player is undead", "Lets the player play as undead", true, 
      Boolean.valueOf(entry.isUndead())));
    PropertySheet propertySheet = new PropertySheet(this.list);
    VBox.setVgrow(propertySheet, Priority.ALWAYS);
    getChildren().add(propertySheet);
  }
  
  public PlayerData getCurrentData()
  {
    return this.current;
  }
  
  class CustomPropertyItem
    implements PropertySheet.Item
  {
    private PlayerPropertySheet.PropertyType type;
    private String category;
    private String name;
    private String description;
    private boolean editable = true;
    private Object value;
    
    CustomPropertyItem(PlayerPropertySheet.PropertyType aType, String aCategory, String aName, String aDescription, boolean aEditable, Object aValue)
    {
      this.type = aType;
      this.category = aCategory;
      this.name = aName;
      this.description = aDescription;
      this.editable = aEditable;
      this.value = aValue;
    }
    
    public PlayerPropertySheet.PropertyType getPropertyType()
    {
      return this.type;
    }
    
    public Class<?> getType()
    {
      return this.value.getClass();
    }
    
    public String getCategory()
    {
      return this.category;
    }
    
    public String getName()
    {
      return this.name;
    }
    
    public String getDescription()
    {
      return this.description;
    }
    
    public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass()
    {
      return super.getPropertyEditorClass();
    }
    
    public boolean isEditable()
    {
      return this.editable;
    }
    
    public Object getValue()
    {
      return this.value;
    }
    
    public void setValue(Object aValue)
    {
      if (!this.value.equals(aValue)) {
        PlayerPropertySheet.this.changedProperties.add(this.type);
      }
      this.value = aValue;
    }
    
    public Optional<ObservableValue<? extends Object>> getObservableValue()
    {
      return Optional.of(new SimpleObjectProperty(this.value));
    }
  }
  
  public final String save()
  {
    String toReturn = "";
    boolean saveAtAll = false;
    for (CustomPropertyItem item : (CustomPropertyItem[])this.list.toArray(new CustomPropertyItem[this.list.size()])) {
      if (this.changedProperties.contains(item.getPropertyType()))
      {
        saveAtAll = true;
        try
        {
          switch (item.getPropertyType())
          {
          case NAME: 
            this.current.setName(item.getValue().toString());
            break;
          case POSX: 
            this.current.setPosx(((Float)item.getValue()).floatValue());
            break;
          case POSY: 
            this.current.setPosy(((Float)item.getValue()).floatValue());
            break;
          case POWER: 
            this.current.setPower(((Integer)item.getValue()).intValue());
            break;
          case CURRENTSERVER: 
            this.current.setServer(((Integer)item.getValue()).intValue());
            break;
          case UNDEAD: 
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\gui\propertysheet\PlayerPropertySheet.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */