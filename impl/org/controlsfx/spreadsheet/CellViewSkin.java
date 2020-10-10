package impl.org.controlsfx.spreadsheet;

import com.sun.javafx.scene.control.skin.TableCellSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.event.WeakEventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import org.controlsfx.control.spreadsheet.Filter;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCell.CornerPosition;

public class CellViewSkin
  extends TableCellSkin<ObservableList<SpreadsheetCell>, SpreadsheetCell>
{
  public static final EventType FILTER_EVENT_TYPE = new EventType("FilterEventType");
  private static final String TOP_LEFT_CLASS = "top-left";
  private static final String TOP_RIGHT_CLASS = "top-right";
  private static final String BOTTOM_RIGHT_CLASS = "bottom-right";
  private static final String BOTTOM_LEFT_CLASS = "bottom-left";
  private static final int TRIANGLE_SIZE = 8;
  private Region topLeftRegion = null;
  private Region topRightRegion = null;
  private Region bottomRightRegion = null;
  private Region bottomLeftRegion = null;
  private MenuButton filterButton = null;
  
  public CellViewSkin(CellView tableCell)
  {
    super(tableCell);
    tableCell.itemProperty().addListener(this.weakItemChangeListener);
    tableCell.tableColumnProperty().addListener(this.weakColumnChangeListener);
    tableCell.getTableColumn().addEventHandler(FILTER_EVENT_TYPE, this.weakTriangleEventHandler);
    if (tableCell.getItem() != null) {
      ((SpreadsheetCell)tableCell.getItem()).addEventHandler(SpreadsheetCell.CORNER_EVENT_TYPE, this.weakTriangleEventHandler);
    }
  }
  
  protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    Node graphic = ((TableCell)getSkinnable()).getGraphic();
    if ((graphic != null) && ((graphic instanceof ImageView)))
    {
      ImageView view = (ImageView)graphic;
      if (view.getImage() != null) {
        return view.getImage().getHeight();
      }
    }
    return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
  }
  
  protected void layoutChildren(double x, double y, double w, double h)
  {
    super.layoutChildren(x, y, w, h);
    if (((TableCell)getSkinnable()).getItem() != null)
    {
      layoutTriangle();
      handleFilter(x, y, w, h);
    }
  }
  
  private void layoutTriangle()
  {
    SpreadsheetCell cell = (SpreadsheetCell)((TableCell)getSkinnable()).getItem();
    
    handleTopLeft(cell);
    handleTopRight(cell);
    handleBottomLeft(cell);
    handleBottomRight(cell);
    
    ((TableCell)getSkinnable()).requestLayout();
  }
  
  private void handleFilter(double x, double y, double w, double h)
  {
    Filter filter = ((CellView)getSkinnable()).getFilter();
    if (filter != null)
    {
      removeMenuButton();
      this.filterButton = filter.getMenuButton();
      if (!getChildren().contains(this.filterButton)) {
        getChildren().add(this.filterButton);
      }
      layoutInArea(this.filterButton, x, y, w, h, 0.0D, HPos.RIGHT, VPos.BOTTOM);
    }
    else if (this.filterButton != null)
    {
      removeMenuButton();
    }
  }
  
  private void removeMenuButton()
  {
    if ((this.filterButton != null) && (getChildren().contains(this.filterButton)))
    {
      getChildren().remove(this.filterButton);
      this.filterButton = null;
    }
  }
  
  private void handleTopLeft(SpreadsheetCell cell)
  {
    if (cell.isCornerActivated(SpreadsheetCell.CornerPosition.TOP_LEFT))
    {
      if (this.topLeftRegion == null) {
        this.topLeftRegion = getRegion(SpreadsheetCell.CornerPosition.TOP_LEFT);
      }
      if (!getChildren().contains(this.topLeftRegion)) {
        getChildren().add(this.topLeftRegion);
      }
      this.topLeftRegion.relocate(0.0D, 0.0D);
    }
    else if (this.topLeftRegion != null)
    {
      getChildren().remove(this.topLeftRegion);
      this.topLeftRegion = null;
    }
  }
  
  private void handleTopRight(SpreadsheetCell cell)
  {
    if (cell.isCornerActivated(SpreadsheetCell.CornerPosition.TOP_RIGHT))
    {
      if (this.topRightRegion == null) {
        this.topRightRegion = getRegion(SpreadsheetCell.CornerPosition.TOP_RIGHT);
      }
      if (!getChildren().contains(this.topRightRegion)) {
        getChildren().add(this.topRightRegion);
      }
      this.topRightRegion.relocate(((TableCell)getSkinnable()).getWidth() - 8.0D, 0.0D);
    }
    else if (this.topRightRegion != null)
    {
      getChildren().remove(this.topRightRegion);
      this.topRightRegion = null;
    }
  }
  
  private void handleBottomRight(SpreadsheetCell cell)
  {
    if (cell.isCornerActivated(SpreadsheetCell.CornerPosition.BOTTOM_RIGHT))
    {
      if (this.bottomRightRegion == null) {
        this.bottomRightRegion = getRegion(SpreadsheetCell.CornerPosition.BOTTOM_RIGHT);
      }
      if (!getChildren().contains(this.bottomRightRegion)) {
        getChildren().add(this.bottomRightRegion);
      }
      this.bottomRightRegion.relocate(((TableCell)getSkinnable()).getWidth() - 8.0D, ((TableCell)getSkinnable()).getHeight() - 8.0D);
    }
    else if (this.bottomRightRegion != null)
    {
      getChildren().remove(this.bottomRightRegion);
      this.bottomRightRegion = null;
    }
  }
  
  private void handleBottomLeft(SpreadsheetCell cell)
  {
    if (cell.isCornerActivated(SpreadsheetCell.CornerPosition.BOTTOM_LEFT))
    {
      if (this.bottomLeftRegion == null) {
        this.bottomLeftRegion = getRegion(SpreadsheetCell.CornerPosition.BOTTOM_LEFT);
      }
      if (!getChildren().contains(this.bottomLeftRegion)) {
        getChildren().add(this.bottomLeftRegion);
      }
      this.bottomLeftRegion.relocate(0.0D, ((TableCell)getSkinnable()).getHeight() - 8.0D);
    }
    else if (this.bottomLeftRegion != null)
    {
      getChildren().remove(this.bottomLeftRegion);
      this.bottomLeftRegion = null;
    }
  }
  
  private static Region getRegion(SpreadsheetCell.CornerPosition position)
  {
    Region region = new Region();
    region.resize(8.0D, 8.0D);
    region.getStyleClass().add("cell-corner");
    switch (position)
    {
    case TOP_LEFT: 
      region.getStyleClass().add("top-left");
      break;
    case TOP_RIGHT: 
      region.getStyleClass().add("top-right");
      break;
    case BOTTOM_RIGHT: 
      region.getStyleClass().add("bottom-right");
      break;
    case BOTTOM_LEFT: 
      region.getStyleClass().add("bottom-left");
    }
    return region;
  }
  
  private final EventHandler<Event> triangleEventHandler = new EventHandler()
  {
    public void handle(Event event)
    {
      ((TableCell)CellViewSkin.this.getSkinnable()).requestLayout();
    }
  };
  private final WeakEventHandler weakTriangleEventHandler = new WeakEventHandler(this.triangleEventHandler);
  private final ChangeListener<SpreadsheetCell> itemChangeListener = new ChangeListener()
  {
    public void changed(ObservableValue<? extends SpreadsheetCell> arg0, SpreadsheetCell oldCell, SpreadsheetCell newCell)
    {
      if (oldCell != null) {
        oldCell.removeEventHandler(SpreadsheetCell.CORNER_EVENT_TYPE, CellViewSkin.this.weakTriangleEventHandler);
      }
      if (newCell != null) {
        newCell.addEventHandler(SpreadsheetCell.CORNER_EVENT_TYPE, CellViewSkin.this.weakTriangleEventHandler);
      }
      if (((TableCell)CellViewSkin.this.getSkinnable()).getItem() != null) {
        CellViewSkin.this.layoutTriangle();
      }
    }
  };
  private final WeakChangeListener<SpreadsheetCell> weakItemChangeListener = new WeakChangeListener(this.itemChangeListener);
  private final ChangeListener<TableColumn> columnChangeListener = new ChangeListener()
  {
    public void changed(ObservableValue<? extends TableColumn> arg0, TableColumn oldCell, TableColumn newCell)
    {
      if (oldCell != null) {
        oldCell.removeEventHandler(CellViewSkin.FILTER_EVENT_TYPE, CellViewSkin.this.weakTriangleEventHandler);
      }
      if (newCell != null) {
        newCell.addEventHandler(CellViewSkin.FILTER_EVENT_TYPE, CellViewSkin.this.weakTriangleEventHandler);
      }
    }
  };
  private final WeakChangeListener<TableColumn> weakColumnChangeListener = new WeakChangeListener(this.columnChangeListener);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\spreadsheet\CellViewSkin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */