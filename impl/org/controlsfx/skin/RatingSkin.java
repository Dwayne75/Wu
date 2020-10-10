package impl.org.controlsfx.skin;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import impl.org.controlsfx.behavior.RatingBehavior;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.Rating;
import org.controlsfx.tools.Utils;

public class RatingSkin
  extends BehaviorSkinBase<Rating, RatingBehavior>
{
  private static final String STRONG = "strong";
  private boolean updateOnHover;
  private boolean partialRating;
  private Pane backgroundContainer;
  private Pane foregroundContainer;
  private double rating = -1.0D;
  private Rectangle forgroundClipRect;
  private final EventHandler<MouseEvent> mouseMoveHandler = new EventHandler()
  {
    public void handle(MouseEvent event)
    {
      if (RatingSkin.this.updateOnHover) {
        RatingSkin.this.updateRatingFromMouseEvent(event);
      }
    }
  };
  private final EventHandler<MouseEvent> mouseClickHandler = new EventHandler()
  {
    public void handle(MouseEvent event)
    {
      if (!RatingSkin.this.updateOnHover) {
        RatingSkin.this.updateRatingFromMouseEvent(event);
      }
    }
  };
  
  private void updateRatingFromMouseEvent(MouseEvent event)
  {
    Rating control = (Rating)getSkinnable();
    if (!control.ratingProperty().isBound())
    {
      Point2D mouseLocation = new Point2D(event.getSceneX(), event.getSceneY());
      control.setRating(calculateRating(mouseLocation));
    }
  }
  
  public RatingSkin(Rating control)
  {
    super(control, new RatingBehavior(control));
    
    this.updateOnHover = control.isUpdateOnHover();
    this.partialRating = control.isPartialRating();
    
    recreateButtons();
    updateRating();
    
    registerChangeListener(control.ratingProperty(), "RATING");
    registerChangeListener(control.maxProperty(), "MAX");
    registerChangeListener(control.orientationProperty(), "ORIENTATION");
    registerChangeListener(control.updateOnHoverProperty(), "UPDATE_ON_HOVER");
    registerChangeListener(control.partialRatingProperty(), "PARTIAL_RATING");
    
    registerChangeListener(control.boundsInLocalProperty(), "BOUNDS");
  }
  
  protected void handleControlPropertyChanged(String p)
  {
    super.handleControlPropertyChanged(p);
    if (p == "RATING")
    {
      updateRating();
    }
    else if (p == "MAX")
    {
      recreateButtons();
    }
    else if (p == "ORIENTATION")
    {
      recreateButtons();
    }
    else if (p == "PARTIAL_RATING")
    {
      this.partialRating = ((Rating)getSkinnable()).isPartialRating();
      recreateButtons();
    }
    else if (p == "UPDATE_ON_HOVER")
    {
      this.updateOnHover = ((Rating)getSkinnable()).isUpdateOnHover();
      recreateButtons();
    }
    else if ((p == "BOUNDS") && 
      (this.partialRating))
    {
      updateClip();
    }
  }
  
  private void recreateButtons()
  {
    this.backgroundContainer = null;
    this.foregroundContainer = null;
    
    this.backgroundContainer = (isVertical() ? new VBox() : new HBox());
    this.backgroundContainer.getStyleClass().add("container");
    getChildren().setAll(new Node[] { this.backgroundContainer });
    if ((this.updateOnHover) || (this.partialRating))
    {
      this.foregroundContainer = (isVertical() ? new VBox() : new HBox());
      this.foregroundContainer.getStyleClass().add("container");
      this.foregroundContainer.setMouseTransparent(true);
      getChildren().add(this.foregroundContainer);
      
      this.forgroundClipRect = new Rectangle();
      this.foregroundContainer.setClip(this.forgroundClipRect);
    }
    for (int index = 0; index <= ((Rating)getSkinnable()).getMax(); index++)
    {
      Node backgroundNode = createButton();
      if (index > 0)
      {
        if (isVertical()) {
          this.backgroundContainer.getChildren().add(0, backgroundNode);
        } else {
          this.backgroundContainer.getChildren().add(backgroundNode);
        }
        if (this.partialRating)
        {
          Node foregroundNode = createButton();
          foregroundNode.getStyleClass().add("strong");
          foregroundNode.setMouseTransparent(true);
          if (isVertical()) {
            this.foregroundContainer.getChildren().add(0, foregroundNode);
          } else {
            this.foregroundContainer.getChildren().add(foregroundNode);
          }
        }
      }
    }
    updateRating();
  }
  
  private double calculateRating(Point2D sceneLocation)
  {
    Point2D b = this.backgroundContainer.sceneToLocal(sceneLocation);
    
    double x = b.getX();
    double y = b.getY();
    
    Rating control = (Rating)getSkinnable();
    
    int max = control.getMax();
    double w = control.getWidth() - (snappedLeftInset() + snappedRightInset());
    double h = control.getHeight() - (snappedTopInset() + snappedBottomInset());
    
    double newRating = -1.0D;
    if (isVertical()) {
      newRating = (h - y) / h * max;
    } else {
      newRating = x / w * max;
    }
    if (!this.partialRating) {
      newRating = Utils.clamp(1.0D, Math.ceil(newRating), control.getMax());
    }
    return newRating;
  }
  
  private void updateClip()
  {
    Rating control = (Rating)getSkinnable();
    double h = control.getHeight() - (snappedTopInset() + snappedBottomInset());
    double w = control.getWidth() - (snappedLeftInset() + snappedRightInset());
    if (isVertical())
    {
      double y = h * this.rating / control.getMax();
      this.forgroundClipRect.relocate(0.0D, h - y);
      this.forgroundClipRect.setWidth(control.getWidth());
      this.forgroundClipRect.setHeight(y);
    }
    else
    {
      double x = w * this.rating / control.getMax();
      this.forgroundClipRect.setWidth(x);
      this.forgroundClipRect.setHeight(control.getHeight());
    }
  }
  
  private Node createButton()
  {
    Region btn = new Region();
    btn.getStyleClass().add("button");
    
    btn.setOnMouseMoved(this.mouseMoveHandler);
    btn.setOnMouseClicked(this.mouseClickHandler);
    return btn;
  }
  
  private void updateRating()
  {
    double newRating = ((Rating)getSkinnable()).getRating();
    if (newRating == this.rating) {
      return;
    }
    this.rating = Utils.clamp(0.0D, newRating, ((Rating)getSkinnable()).getMax());
    if (this.partialRating) {
      updateClip();
    } else {
      updateButtonStyles();
    }
  }
  
  private void updateButtonStyles()
  {
    int max = ((Rating)getSkinnable()).getMax();
    
    List<Node> buttons = new ArrayList(this.backgroundContainer.getChildren());
    if (isVertical()) {
      Collections.reverse(buttons);
    }
    for (int i = 0; i < max; i++)
    {
      Node button = (Node)buttons.get(i);
      
      List<String> styleClass = button.getStyleClass();
      boolean containsStrong = styleClass.contains("strong");
      if (i < this.rating)
      {
        if (!containsStrong) {
          styleClass.add("strong");
        }
      }
      else if (containsStrong) {
        styleClass.remove("strong");
      }
    }
  }
  
  private boolean isVertical()
  {
    return ((Rating)getSkinnable()).getOrientation() == Orientation.VERTICAL;
  }
  
  protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\RatingSkin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */