package org.controlsfx.control;

import impl.org.controlsfx.skin.PropertySheetSkin;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

public class PropertySheet
  extends ControlsFXControl
{
  private final ObservableList<Item> items;
  
  public static enum Mode
  {
    NAME,  CATEGORY;
    
    private Mode() {}
  }
  
  public static abstract interface Item
  {
    public abstract Class<?> getType();
    
    public abstract String getCategory();
    
    public abstract String getName();
    
    public abstract String getDescription();
    
    public abstract Object getValue();
    
    public abstract void setValue(Object paramObject);
    
    public abstract Optional<ObservableValue<? extends Object>> getObservableValue();
    
    public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass()
    {
      return Optional.empty();
    }
    
    public boolean isEditable()
    {
      return true;
    }
  }
  
  public PropertySheet()
  {
    this(null);
  }
  
  public PropertySheet(ObservableList<Item> items)
  {
    getStyleClass().add("property-sheet");
    
    this.items = (items == null ? FXCollections.observableArrayList() : items);
  }
  
  public ObservableList<Item> getItems()
  {
    return this.items;
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new PropertySheetSkin(this);
  }
  
  public String getUserAgentStylesheet()
  {
    return getUserAgentStylesheet(PropertySheet.class, "propertysheet.css");
  }
  
  private final SimpleObjectProperty<Mode> modeProperty = new SimpleObjectProperty(this, "mode", Mode.NAME);
  
  public final SimpleObjectProperty<Mode> modeProperty()
  {
    return this.modeProperty;
  }
  
  public final Mode getMode()
  {
    return (Mode)this.modeProperty.get();
  }
  
  public final void setMode(Mode mode)
  {
    this.modeProperty.set(mode);
  }
  
  private final SimpleObjectProperty<Callback<Item, PropertyEditor<?>>> propertyEditorFactory = new SimpleObjectProperty(this, "propertyEditor", new DefaultPropertyEditorFactory());
  
  public final SimpleObjectProperty<Callback<Item, PropertyEditor<?>>> propertyEditorFactory()
  {
    return this.propertyEditorFactory;
  }
  
  public final Callback<Item, PropertyEditor<?>> getPropertyEditorFactory()
  {
    return (Callback)this.propertyEditorFactory.get();
  }
  
  public final void setPropertyEditorFactory(Callback<Item, PropertyEditor<?>> factory)
  {
    this.propertyEditorFactory.set(factory == null ? new DefaultPropertyEditorFactory() : factory);
  }
  
  private final SimpleBooleanProperty modeSwitcherVisible = new SimpleBooleanProperty(this, "modeSwitcherVisible", true);
  
  public final SimpleBooleanProperty modeSwitcherVisibleProperty()
  {
    return this.modeSwitcherVisible;
  }
  
  public final boolean isModeSwitcherVisible()
  {
    return this.modeSwitcherVisible.get();
  }
  
  public final void setModeSwitcherVisible(boolean visible)
  {
    this.modeSwitcherVisible.set(visible);
  }
  
  private final SimpleBooleanProperty searchBoxVisible = new SimpleBooleanProperty(this, "searchBoxVisible", true);
  
  public final SimpleBooleanProperty searchBoxVisibleProperty()
  {
    return this.searchBoxVisible;
  }
  
  public final boolean isSearchBoxVisible()
  {
    return this.searchBoxVisible.get();
  }
  
  public final void setSearchBoxVisible(boolean visible)
  {
    this.searchBoxVisible.set(visible);
  }
  
  private final SimpleStringProperty titleFilterProperty = new SimpleStringProperty(this, "titleFilter", "");
  private static final String DEFAULT_STYLE_CLASS = "property-sheet";
  
  public final SimpleStringProperty titleFilter()
  {
    return this.titleFilterProperty;
  }
  
  public final String getTitleFilter()
  {
    return this.titleFilterProperty.get();
  }
  
  public final void setTitleFilter(String filter)
  {
    this.titleFilterProperty.set(filter);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\PropertySheet.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */