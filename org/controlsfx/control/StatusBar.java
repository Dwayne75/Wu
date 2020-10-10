package org.controlsfx.control;

import impl.org.controlsfx.i18n.Localization;
import impl.org.controlsfx.skin.StatusBarSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;

public class StatusBar
  extends ControlsFXControl
{
  public StatusBar()
  {
    getStyleClass().add("status-bar");
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new StatusBarSkin(this);
  }
  
  public String getUserAgentStylesheet()
  {
    return getUserAgentStylesheet(StatusBar.class, "statusbar.css");
  }
  
  private final StringProperty text = new SimpleStringProperty(this, "text", 
    Localization.localize(Localization.asKey("statusbar.ok")));
  
  public final StringProperty textProperty()
  {
    return this.text;
  }
  
  public final void setText(String text)
  {
    textProperty().set(text);
  }
  
  public final String getText()
  {
    return (String)textProperty().get();
  }
  
  private final ObjectProperty<Node> graphic = new SimpleObjectProperty(this, "graphic");
  
  public final ObjectProperty<Node> graphicProperty()
  {
    return this.graphic;
  }
  
  public final Node getGraphic()
  {
    return (Node)graphicProperty().get();
  }
  
  public final void setGraphic(Node node)
  {
    graphicProperty().set(node);
  }
  
  private final StringProperty styleTextProperty = new SimpleStringProperty();
  
  public void setStyleText(String style)
  {
    this.styleTextProperty.set(style);
  }
  
  public String getStyleText()
  {
    return (String)this.styleTextProperty.get();
  }
  
  public final StringProperty styleTextProperty()
  {
    return this.styleTextProperty;
  }
  
  private final ObservableList<Node> leftItems = FXCollections.observableArrayList();
  
  public final ObservableList<Node> getLeftItems()
  {
    return this.leftItems;
  }
  
  private final ObservableList<Node> rightItems = FXCollections.observableArrayList();
  
  public final ObservableList<Node> getRightItems()
  {
    return this.rightItems;
  }
  
  private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress");
  
  public final DoubleProperty progressProperty()
  {
    return this.progress;
  }
  
  public final void setProgress(double progress)
  {
    progressProperty().set(progress);
  }
  
  public final double getProgress()
  {
    return progressProperty().get();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\StatusBar.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */