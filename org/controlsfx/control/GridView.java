package org.controlsfx.control;

import impl.org.controlsfx.skin.GridViewSkin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Callback;

public class GridView<T>
  extends ControlsFXControl
{
  private DoubleProperty horizontalCellSpacing;
  private DoubleProperty verticalCellSpacing;
  private DoubleProperty cellWidth;
  private DoubleProperty cellHeight;
  private ObjectProperty<Callback<GridView<T>, GridCell<T>>> cellFactory;
  private ObjectProperty<ObservableList<T>> items;
  private static final String DEFAULT_STYLE_CLASS = "grid-view";
  
  public GridView()
  {
    this(FXCollections.observableArrayList());
  }
  
  public GridView(ObservableList<T> items)
  {
    getStyleClass().add("grid-view");
    setItems(items);
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new GridViewSkin(this);
  }
  
  public String getUserAgentStylesheet()
  {
    return getUserAgentStylesheet(GridView.class, "gridview.css");
  }
  
  public final DoubleProperty horizontalCellSpacingProperty()
  {
    if (this.horizontalCellSpacing == null) {
      this.horizontalCellSpacing = new StyleableDoubleProperty(12.0D)
      {
        public CssMetaData<GridView<?>, Number> getCssMetaData()
        {
          return GridView.StyleableProperties.HORIZONTAL_CELL_SPACING;
        }
        
        public Object getBean()
        {
          return GridView.this;
        }
        
        public String getName()
        {
          return "horizontalCellSpacing";
        }
      };
    }
    return this.horizontalCellSpacing;
  }
  
  public final void setHorizontalCellSpacing(double value)
  {
    horizontalCellSpacingProperty().set(value);
  }
  
  public final double getHorizontalCellSpacing()
  {
    return this.horizontalCellSpacing == null ? 12.0D : this.horizontalCellSpacing.get();
  }
  
  public final DoubleProperty verticalCellSpacingProperty()
  {
    if (this.verticalCellSpacing == null) {
      this.verticalCellSpacing = new StyleableDoubleProperty(12.0D)
      {
        public CssMetaData<GridView<?>, Number> getCssMetaData()
        {
          return GridView.StyleableProperties.VERTICAL_CELL_SPACING;
        }
        
        public Object getBean()
        {
          return GridView.this;
        }
        
        public String getName()
        {
          return "verticalCellSpacing";
        }
      };
    }
    return this.verticalCellSpacing;
  }
  
  public final void setVerticalCellSpacing(double value)
  {
    verticalCellSpacingProperty().set(value);
  }
  
  public final double getVerticalCellSpacing()
  {
    return this.verticalCellSpacing == null ? 12.0D : this.verticalCellSpacing.get();
  }
  
  public final DoubleProperty cellWidthProperty()
  {
    if (this.cellWidth == null) {
      this.cellWidth = new StyleableDoubleProperty(64.0D)
      {
        public CssMetaData<GridView<?>, Number> getCssMetaData()
        {
          return GridView.StyleableProperties.CELL_WIDTH;
        }
        
        public Object getBean()
        {
          return GridView.this;
        }
        
        public String getName()
        {
          return "cellWidth";
        }
      };
    }
    return this.cellWidth;
  }
  
  public final void setCellWidth(double value)
  {
    cellWidthProperty().set(value);
  }
  
  public final double getCellWidth()
  {
    return this.cellWidth == null ? 64.0D : this.cellWidth.get();
  }
  
  public final DoubleProperty cellHeightProperty()
  {
    if (this.cellHeight == null) {
      this.cellHeight = new StyleableDoubleProperty(64.0D)
      {
        public CssMetaData<GridView<?>, Number> getCssMetaData()
        {
          return GridView.StyleableProperties.CELL_HEIGHT;
        }
        
        public Object getBean()
        {
          return GridView.this;
        }
        
        public String getName()
        {
          return "cellHeight";
        }
      };
    }
    return this.cellHeight;
  }
  
  public final void setCellHeight(double value)
  {
    cellHeightProperty().set(value);
  }
  
  public final double getCellHeight()
  {
    return this.cellHeight == null ? 64.0D : this.cellHeight.get();
  }
  
  public final ObjectProperty<Callback<GridView<T>, GridCell<T>>> cellFactoryProperty()
  {
    if (this.cellFactory == null) {
      this.cellFactory = new SimpleObjectProperty(this, "cellFactory");
    }
    return this.cellFactory;
  }
  
  public final void setCellFactory(Callback<GridView<T>, GridCell<T>> value)
  {
    cellFactoryProperty().set(value);
  }
  
  public final Callback<GridView<T>, GridCell<T>> getCellFactory()
  {
    return this.cellFactory == null ? null : (Callback)this.cellFactory.get();
  }
  
  public final ObjectProperty<ObservableList<T>> itemsProperty()
  {
    if (this.items == null) {
      this.items = new SimpleObjectProperty(this, "items");
    }
    return this.items;
  }
  
  public final void setItems(ObservableList<T> value)
  {
    itemsProperty().set(value);
  }
  
  public final ObservableList<T> getItems()
  {
    return this.items == null ? null : (ObservableList)this.items.get();
  }
  
  private static class StyleableProperties
  {
    private static final CssMetaData<GridView<?>, Number> HORIZONTAL_CELL_SPACING = new CssMetaData("-fx-horizontal-cell-spacing", 
      StyleConverter.getSizeConverter(), Double.valueOf(12.0D))
    {
      public Double getInitialValue(GridView<?> node)
      {
        return Double.valueOf(node.getHorizontalCellSpacing());
      }
      
      public boolean isSettable(GridView<?> n)
      {
        return (n.horizontalCellSpacing == null) || (!n.horizontalCellSpacing.isBound());
      }
      
      public StyleableProperty<Number> getStyleableProperty(GridView<?> n)
      {
        return (StyleableProperty)n.horizontalCellSpacingProperty();
      }
    };
    private static final CssMetaData<GridView<?>, Number> VERTICAL_CELL_SPACING = new CssMetaData("-fx-vertical-cell-spacing", 
      StyleConverter.getSizeConverter(), Double.valueOf(12.0D))
    {
      public Double getInitialValue(GridView<?> node)
      {
        return Double.valueOf(node.getVerticalCellSpacing());
      }
      
      public boolean isSettable(GridView<?> n)
      {
        return (n.verticalCellSpacing == null) || (!n.verticalCellSpacing.isBound());
      }
      
      public StyleableProperty<Number> getStyleableProperty(GridView<?> n)
      {
        return (StyleableProperty)n.verticalCellSpacingProperty();
      }
    };
    private static final CssMetaData<GridView<?>, Number> CELL_WIDTH = new CssMetaData("-fx-cell-width", 
      StyleConverter.getSizeConverter(), Double.valueOf(64.0D))
    {
      public Double getInitialValue(GridView<?> node)
      {
        return Double.valueOf(node.getCellWidth());
      }
      
      public boolean isSettable(GridView<?> n)
      {
        return (n.cellWidth == null) || (!n.cellWidth.isBound());
      }
      
      public StyleableProperty<Number> getStyleableProperty(GridView<?> n)
      {
        return (StyleableProperty)n.cellWidthProperty();
      }
    };
    private static final CssMetaData<GridView<?>, Number> CELL_HEIGHT = new CssMetaData("-fx-cell-height", 
      StyleConverter.getSizeConverter(), Double.valueOf(64.0D))
    {
      public Double getInitialValue(GridView<?> node)
      {
        return Double.valueOf(node.getCellHeight());
      }
      
      public boolean isSettable(GridView<?> n)
      {
        return (n.cellHeight == null) || (!n.cellHeight.isBound());
      }
      
      public StyleableProperty<Number> getStyleableProperty(GridView<?> n)
      {
        return (StyleableProperty)n.cellHeightProperty();
      }
    };
    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
    
    static
    {
      List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList(Control.getClassCssMetaData());
      styleables.add(HORIZONTAL_CELL_SPACING);
      styleables.add(VERTICAL_CELL_SPACING);
      styleables.add(CELL_WIDTH);
      styleables.add(CELL_HEIGHT);
      
      STYLEABLES = Collections.unmodifiableList(styleables);
    }
  }
  
  public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData()
  {
    return StyleableProperties.STYLEABLES;
  }
  
  public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData()
  {
    return getClassCssMetaData();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\GridView.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */