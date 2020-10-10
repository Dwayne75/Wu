package org.controlsfx.control;

import impl.org.controlsfx.skin.ToggleSwitchSkin;
import java.net.URL;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.control.Labeled;
import javafx.scene.control.Skin;

public class ToggleSwitch
  extends Labeled
{
  private BooleanProperty selected;
  private static final String DEFAULT_STYLE_CLASS = "toggle-switch";
  
  public ToggleSwitch()
  {
    initialize();
  }
  
  public ToggleSwitch(String text)
  {
    super(text);
    initialize();
  }
  
  private void initialize()
  {
    getStyleClass().setAll(new String[] { "toggle-switch" });
  }
  
  public final void setSelected(boolean value)
  {
    selectedProperty().set(value);
  }
  
  public final boolean isSelected()
  {
    return this.selected == null ? false : this.selected.get();
  }
  
  public final BooleanProperty selectedProperty()
  {
    if (this.selected == null) {
      this.selected = new BooleanPropertyBase()
      {
        protected void invalidated()
        {
          Boolean v = Boolean.valueOf(get());
          ToggleSwitch.this.pseudoClassStateChanged(ToggleSwitch.PSEUDO_CLASS_SELECTED, v.booleanValue());
        }
        
        public Object getBean()
        {
          return ToggleSwitch.this;
        }
        
        public String getName()
        {
          return "selected";
        }
      };
    }
    return this.selected;
  }
  
  public void fire()
  {
    if (!isDisabled())
    {
      setSelected(!isSelected());
      fireEvent(new ActionEvent());
    }
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new ToggleSwitchSkin(this);
  }
  
  private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");
  
  public String getUserAgentStylesheet()
  {
    return ToggleSwitch.class.getResource("toggleswitch.css").toExternalForm();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\ToggleSwitch.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */