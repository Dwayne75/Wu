package org.controlsfx.control;

import impl.org.controlsfx.i18n.Localization;
import impl.org.controlsfx.skin.ListSelectionViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;

public class ListSelectionView<T>
  extends ControlsFXControl
{
  private static final String DEFAULT_STYLECLASS = "list-selection-view";
  
  public ListSelectionView()
  {
    getStyleClass().add("list-selection-view");
    
    Label sourceHeader = new Label(Localization.localize(Localization.asKey("listSelectionView.header.source")));
    sourceHeader.getStyleClass().add("list-header-label");
    sourceHeader.setId("source-header-label");
    setSourceHeader(sourceHeader);
    
    Label targetHeader = new Label(Localization.localize(Localization.asKey("listSelectionView.header.target")));
    targetHeader.getStyleClass().add("list-header-label");
    targetHeader.setId("target-header-label");
    setTargetHeader(targetHeader);
  }
  
  protected Skin<ListSelectionView<T>> createDefaultSkin()
  {
    return new ListSelectionViewSkin(this);
  }
  
  public String getUserAgentStylesheet()
  {
    return getUserAgentStylesheet(ListSelectionView.class, "listselectionview.css");
  }
  
  private final ObjectProperty<Node> sourceHeader = new SimpleObjectProperty(this, "sourceHeader");
  
  public final ObjectProperty<Node> sourceHeaderProperty()
  {
    return this.sourceHeader;
  }
  
  public final Node getSourceHeader()
  {
    return (Node)this.sourceHeader.get();
  }
  
  public final void setSourceHeader(Node node)
  {
    this.sourceHeader.set(node);
  }
  
  private final ObjectProperty<Node> sourceFooter = new SimpleObjectProperty(this, "sourceFooter");
  
  public final ObjectProperty<Node> sourceFooterProperty()
  {
    return this.sourceFooter;
  }
  
  public final Node getSourceFooter()
  {
    return (Node)this.sourceFooter.get();
  }
  
  public final void setSourceFooter(Node node)
  {
    this.sourceFooter.set(node);
  }
  
  private final ObjectProperty<Node> targetHeader = new SimpleObjectProperty(this, "targetHeader");
  
  public final ObjectProperty<Node> targetHeaderProperty()
  {
    return this.targetHeader;
  }
  
  public final Node getTargetHeader()
  {
    return (Node)this.targetHeader.get();
  }
  
  public final void setTargetHeader(Node node)
  {
    this.targetHeader.set(node);
  }
  
  private final ObjectProperty<Node> targetFooter = new SimpleObjectProperty(this, "targetFooter");
  private ObjectProperty<ObservableList<T>> sourceItems;
  private ObjectProperty<ObservableList<T>> targetItems;
  
  public final ObjectProperty<Node> targetFooterProperty()
  {
    return this.targetFooter;
  }
  
  public final Node getTargetFooter()
  {
    return (Node)this.targetFooter.get();
  }
  
  public final void setTargetFooter(Node node)
  {
    this.targetFooter.set(node);
  }
  
  public final void setSourceItems(ObservableList<T> value)
  {
    sourceItemsProperty().set(value);
  }
  
  public final ObservableList<T> getSourceItems()
  {
    return (ObservableList)sourceItemsProperty().get();
  }
  
  public final ObjectProperty<ObservableList<T>> sourceItemsProperty()
  {
    if (this.sourceItems == null) {
      this.sourceItems = new SimpleObjectProperty(this, "sourceItems", FXCollections.observableArrayList());
    }
    return this.sourceItems;
  }
  
  public final void setTargetItems(ObservableList<T> value)
  {
    targetItemsProperty().set(value);
  }
  
  public final ObservableList<T> getTargetItems()
  {
    return (ObservableList)targetItemsProperty().get();
  }
  
  public final ObjectProperty<ObservableList<T>> targetItemsProperty()
  {
    if (this.targetItems == null) {
      this.targetItems = new SimpleObjectProperty(this, "targetItems", FXCollections.observableArrayList());
    }
    return this.targetItems;
  }
  
  private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty(this, "orientation", Orientation.HORIZONTAL);
  private ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactory;
  
  public final ObjectProperty<Orientation> orientationProperty()
  {
    return this.orientation;
  }
  
  public final void setOrientation(Orientation value)
  {
    orientationProperty().set(value);
  }
  
  public final Orientation getOrientation()
  {
    return (Orientation)this.orientation.get();
  }
  
  public final void setCellFactory(Callback<ListView<T>, ListCell<T>> value)
  {
    cellFactoryProperty().set(value);
  }
  
  public final Callback<ListView<T>, ListCell<T>> getCellFactory()
  {
    return this.cellFactory == null ? null : (Callback)this.cellFactory.get();
  }
  
  public final ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactoryProperty()
  {
    if (this.cellFactory == null) {
      this.cellFactory = new SimpleObjectProperty(this, "cellFactory");
    }
    return this.cellFactory;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\ListSelectionView.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */