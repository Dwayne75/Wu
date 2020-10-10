package impl.org.controlsfx.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import java.util.Collections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;

public class NotificationPaneSkin
  extends BehaviorSkinBase<NotificationPane, BehaviorBase<NotificationPane>>
{
  private NotificationBar notificationBar;
  private Node content;
  private Rectangle clip = new Rectangle();
  
  public NotificationPaneSkin(final NotificationPane control)
  {
    super(control, new BehaviorBase(control, Collections.emptyList()));
    
    this.notificationBar = new NotificationBar()
    {
      public void requestContainerLayout()
      {
        control.requestLayout();
      }
      
      public String getText()
      {
        return control.getText();
      }
      
      public Node getGraphic()
      {
        return control.getGraphic();
      }
      
      public ObservableList<Action> getActions()
      {
        return control.getActions();
      }
      
      public boolean isShowing()
      {
        return control.isShowing();
      }
      
      public boolean isShowFromTop()
      {
        return control.isShowFromTop();
      }
      
      public void hide()
      {
        control.hide();
      }
      
      public boolean isCloseButtonVisible()
      {
        return control.isCloseButtonVisible();
      }
      
      public double getContainerHeight()
      {
        return control.getHeight();
      }
      
      public void relocateInParent(double x, double y)
      {
        NotificationPaneSkin.this.notificationBar.relocate(x, y);
      }
    };
    control.setClip(this.clip);
    updateContent();
    
    registerChangeListener(control.heightProperty(), "HEIGHT");
    registerChangeListener(control.contentProperty(), "CONTENT");
    registerChangeListener(control.textProperty(), "TEXT");
    registerChangeListener(control.graphicProperty(), "GRAPHIC");
    registerChangeListener(control.showingProperty(), "SHOWING");
    registerChangeListener(control.showFromTopProperty(), "SHOW_FROM_TOP");
    registerChangeListener(control.closeButtonVisibleProperty(), "CLOSE_BUTTON_VISIBLE");
    
    ParentTraversalEngine engine = new ParentTraversalEngine(getSkinnable());
    ((NotificationPane)getSkinnable()).setImpl_traversalEngine(engine);
    engine.setOverriddenFocusTraversability(Boolean.valueOf(false));
  }
  
  protected void handleControlPropertyChanged(String p)
  {
    super.handleControlPropertyChanged(p);
    if ("CONTENT".equals(p)) {
      updateContent();
    } else if ("TEXT".equals(p)) {
      this.notificationBar.label.setText(((NotificationPane)getSkinnable()).getText());
    } else if ("GRAPHIC".equals(p)) {
      this.notificationBar.label.setGraphic(((NotificationPane)getSkinnable()).getGraphic());
    } else if ("SHOWING".equals(p))
    {
      if (((NotificationPane)getSkinnable()).isShowing()) {
        this.notificationBar.doShow();
      } else {
        this.notificationBar.doHide();
      }
    }
    else if ("SHOW_FROM_TOP".equals(p))
    {
      if (((NotificationPane)getSkinnable()).isShowing()) {
        ((NotificationPane)getSkinnable()).requestLayout();
      }
    }
    else if ("CLOSE_BUTTON_VISIBLE".equals(p)) {
      this.notificationBar.updatePane();
    } else if ("HEIGHT".equals(p)) {
      if ((((NotificationPane)getSkinnable()).isShowing()) && (!((NotificationPane)getSkinnable()).isShowFromTop())) {
        this.notificationBar.requestLayout();
      }
    }
  }
  
  private void updateContent()
  {
    if (this.content != null) {
      getChildren().remove(this.content);
    }
    this.content = ((NotificationPane)getSkinnable()).getContent();
    if (this.content == null) {
      getChildren().setAll(new Node[] { this.notificationBar });
    } else {
      getChildren().setAll(new Node[] { this.content, this.notificationBar });
    }
  }
  
  protected void layoutChildren(double x, double y, double w, double h)
  {
    double notificationBarHeight = this.notificationBar.prefHeight(w);
    
    this.notificationBar.resize(w, notificationBarHeight);
    if (this.content != null) {
      this.content.resizeRelocate(x, y, w, h);
    }
    this.clip.setX(x);
    this.clip.setY(y);
    this.clip.setWidth(w);
    this.clip.setHeight(h);
  }
  
  protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    return this.content == null ? 0.0D : this.content.minWidth(height);
  }
  
  protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    return this.content == null ? 0.0D : this.content.minHeight(width);
  }
  
  protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    return this.content == null ? 0.0D : this.content.prefWidth(height);
  }
  
  protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    return this.content == null ? 0.0D : this.content.prefHeight(width);
  }
  
  protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    return this.content == null ? 0.0D : this.content.maxWidth(height);
  }
  
  protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    return this.content == null ? 0.0D : this.content.maxHeight(width);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\NotificationPaneSkin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */