package org.controlsfx.control;

import impl.org.controlsfx.skin.CheckComboBoxSkin;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;

public class CheckComboBox<T>
  extends ControlsFXControl
{
  private final ObservableList<T> items;
  private final Map<T, BooleanProperty> itemBooleanMap;
  
  public CheckComboBox()
  {
    this(null);
  }
  
  public CheckComboBox(ObservableList<T> items)
  {
    int initialSize = items == null ? 32 : items.size();
    
    this.itemBooleanMap = new HashMap(initialSize);
    this.items = (items == null ? FXCollections.observableArrayList() : items);
    setCheckModel(new CheckComboBoxBitSetCheckModel(this.items, this.itemBooleanMap));
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new CheckComboBoxSkin(this);
  }
  
  public ObservableList<T> getItems()
  {
    return this.items;
  }
  
  public BooleanProperty getItemBooleanProperty(int index)
  {
    if ((index < 0) || (index >= this.items.size())) {
      return null;
    }
    return getItemBooleanProperty(getItems().get(index));
  }
  
  public BooleanProperty getItemBooleanProperty(T item)
  {
    return (BooleanProperty)this.itemBooleanMap.get(item);
  }
  
  private ObjectProperty<IndexedCheckModel<T>> checkModel = new SimpleObjectProperty(this, "checkModel");
  
  public final void setCheckModel(IndexedCheckModel<T> value)
  {
    checkModelProperty().set(value);
  }
  
  public final IndexedCheckModel<T> getCheckModel()
  {
    return this.checkModel == null ? null : (IndexedCheckModel)this.checkModel.get();
  }
  
  public final ObjectProperty<IndexedCheckModel<T>> checkModelProperty()
  {
    return this.checkModel;
  }
  
  private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty(this, "converter");
  
  public final ObjectProperty<StringConverter<T>> converterProperty()
  {
    return this.converter;
  }
  
  public final void setConverter(StringConverter<T> value)
  {
    converterProperty().set(value);
  }
  
  public final StringConverter<T> getConverter()
  {
    return (StringConverter)converterProperty().get();
  }
  
  private static class CheckComboBoxBitSetCheckModel<T>
    extends CheckBitSetModelBase<T>
  {
    private final ObservableList<T> items;
    
    CheckComboBoxBitSetCheckModel(ObservableList<T> items, Map<T, BooleanProperty> itemBooleanMap)
    {
      super();
      
      this.items = items;
      this.items.addListener(new ListChangeListener()
      {
        public void onChanged(ListChangeListener.Change<? extends T> c)
        {
          CheckComboBox.CheckComboBoxBitSetCheckModel.this.updateMap();
        }
      });
      updateMap();
    }
    
    public T getItem(int index)
    {
      return (T)this.items.get(index);
    }
    
    public int getItemCount()
    {
      return this.items.size();
    }
    
    public int getItemIndex(T item)
    {
      return this.items.indexOf(item);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\CheckComboBox.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */