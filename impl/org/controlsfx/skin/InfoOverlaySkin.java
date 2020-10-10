package impl.org.controlsfx.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import java.net.URL;
import java.util.Collections;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.controlsfx.control.InfoOverlay;

public class InfoOverlaySkin
  extends BehaviorSkinBase<InfoOverlay, BehaviorBase<InfoOverlay>>
{
  private final ImageView EXPAND_IMAGE = new ImageView(new Image(InfoOverlay.class.getResource("expand.png").toExternalForm()));
  private final ImageView COLLAPSE_IMAGE = new ImageView(new Image(InfoOverlay.class.getResource("collapse.png").toExternalForm()));
  private static final Duration TRANSITION_DURATION = new Duration(350.0D);
  private Node content;
  private Label infoLabel;
  private HBox infoPanel;
  private ToggleButton expandCollapseButton;
  private Timeline timeline;
  private DoubleProperty transition = new SimpleDoubleProperty(this, "transition", 0.0D)
  {
    protected void invalidated()
    {
      ((InfoOverlay)InfoOverlaySkin.this.getSkinnable()).requestLayout();
    }
  };
  
  public InfoOverlaySkin(final InfoOverlay control)
  {
    super(control, new BehaviorBase(control, Collections.emptyList()));
    
    this.content = control.getContent();
    control.hoverProperty().addListener(new ChangeListener()
    {
      public void changed(ObservableValue<? extends Boolean> o, Boolean wasHover, Boolean isHover)
      {
        if ((control.isShowOnHover()) && (
          ((isHover.booleanValue()) && (!InfoOverlaySkin.this.isExpanded())) || ((!isHover.booleanValue()) && (InfoOverlaySkin.this.isExpanded())))) {
          InfoOverlaySkin.this.doToggle();
        }
      }
    });
    this.infoLabel = new Label();
    this.infoLabel.setWrapText(true);
    this.infoLabel.setAlignment(Pos.TOP_LEFT);
    this.infoLabel.getStyleClass().add("info");
    this.infoLabel.textProperty().bind(control.textProperty());
    
    this.expandCollapseButton = new ToggleButton();
    this.expandCollapseButton.setMouseTransparent(true);
    this.expandCollapseButton.visibleProperty().bind(Bindings.not(control.showOnHoverProperty()));
    this.expandCollapseButton.managedProperty().bind(Bindings.not(control.showOnHoverProperty()));
    updateToggleButton();
    
    this.infoPanel = new HBox(new Node[] { this.infoLabel, this.expandCollapseButton });
    this.infoPanel.setAlignment(Pos.TOP_LEFT);
    this.infoPanel.setFillHeight(true);
    this.infoPanel.getStyleClass().add("info-panel");
    this.infoPanel.setCursor(Cursor.HAND);
    this.infoPanel.setOnMouseClicked(new EventHandler()
    {
      public void handle(MouseEvent e)
      {
        if (!control.isShowOnHover()) {
          InfoOverlaySkin.this.doToggle();
        }
      }
    });
    getChildren().addAll(new Node[] { this.content, this.infoPanel });
    
    registerChangeListener(control.contentProperty(), "CONTENT");
  }
  
  protected void handleControlPropertyChanged(String p)
  {
    super.handleControlPropertyChanged(p);
    if ("CONTENT".equals(p))
    {
      getChildren().remove(0);
      getChildren().add(0, ((InfoOverlay)getSkinnable()).getContent());
      ((InfoOverlay)getSkinnable()).requestLayout();
    }
  }
  
  private void doToggle()
  {
    this.expandCollapseButton.setSelected(!this.expandCollapseButton.isSelected());
    toggleInfoPanel();
    updateToggleButton();
  }
  
  private boolean isExpanded()
  {
    return this.expandCollapseButton.isSelected();
  }
  
  protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight)
  {
    double contentPrefHeight = this.content.prefHeight(contentWidth);
    
    double toggleButtonPrefWidth = this.expandCollapseButton.prefWidth(-1.0D);
    this.expandCollapseButton.setMinWidth(toggleButtonPrefWidth);
    
    Insets infoPanelPadding = this.infoPanel.getPadding();
    double infoLabelWidth = snapSize(contentWidth - toggleButtonPrefWidth - infoPanelPadding
      .getLeft() - infoPanelPadding.getRight());
    
    double prefInfoPanelHeight = (snapSize(this.infoLabel.prefHeight(infoLabelWidth)) + snapSpace(this.infoPanel.getPadding().getTop()) + snapSpace(this.infoPanel.getPadding().getBottom())) * this.transition.get();
    
    this.infoLabel.setMaxWidth(infoLabelWidth);
    this.infoLabel.setMaxHeight(prefInfoPanelHeight);
    
    layoutInArea(this.content, contentX, contentY, contentWidth, contentHeight, -1.0D, HPos.CENTER, VPos.TOP);
    
    layoutInArea(this.infoPanel, contentX, snapPosition(contentPrefHeight - prefInfoPanelHeight), contentWidth, prefInfoPanelHeight, 0.0D, HPos.CENTER, VPos.BOTTOM);
  }
  
  private void updateToggleButton()
  {
    if (this.expandCollapseButton.isSelected())
    {
      this.expandCollapseButton.getStyleClass().setAll(new String[] { "collapse-button" });
      this.expandCollapseButton.setGraphic(this.COLLAPSE_IMAGE);
    }
    else
    {
      this.expandCollapseButton.getStyleClass().setAll(new String[] { "expand-button" });
      this.expandCollapseButton.setGraphic(this.EXPAND_IMAGE);
    }
  }
  
  protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    double insets = topInset + bottomInset;
    return insets + (this.content == null ? 0.0D : this.content.prefHeight(width));
  }
  
  protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    double insets = leftInset + rightInset;
    return insets + (this.content == null ? 0.0D : this.content.prefWidth(height));
  }
  
  protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
  }
  
  protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
  }
  
  private void toggleInfoPanel()
  {
    if (this.content == null) {
      return;
    }
    Duration duration;
    if ((this.timeline != null) && (this.timeline.getStatus() != Animation.Status.STOPPED))
    {
      Duration duration = this.timeline.getCurrentTime();
      this.timeline.stop();
    }
    else
    {
      duration = TRANSITION_DURATION;
    }
    this.timeline = new Timeline();
    this.timeline.setCycleCount(1);
    KeyFrame k2;
    KeyFrame k1;
    KeyFrame k2;
    if (isExpanded())
    {
      KeyFrame k1 = new KeyFrame(Duration.ZERO, new KeyValue[] { new KeyValue(this.transition, Integer.valueOf(0)) });
      k2 = new KeyFrame(duration, new KeyValue[] { new KeyValue(this.transition, Integer.valueOf(1), Interpolator.LINEAR) });
    }
    else
    {
      k1 = new KeyFrame(Duration.ZERO, new KeyValue[] { new KeyValue(this.transition, Integer.valueOf(1)) });
      k2 = new KeyFrame(duration, new KeyValue[] { new KeyValue(this.transition, Integer.valueOf(0), Interpolator.LINEAR) });
    }
    this.timeline.getKeyFrames().setAll(new KeyFrame[] { k1, k2 });
    this.timeline.play();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\InfoOverlaySkin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */