package impl.org.controlsfx.skin;

import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.skin.TextFieldSkin;
import com.sun.javafx.scene.text.HitInfo;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public abstract class CustomTextFieldSkin
  extends TextFieldSkin
{
  private static final PseudoClass HAS_NO_SIDE_NODE = PseudoClass.getPseudoClass("no-side-nodes");
  private static final PseudoClass HAS_LEFT_NODE = PseudoClass.getPseudoClass("left-node-visible");
  private static final PseudoClass HAS_RIGHT_NODE = PseudoClass.getPseudoClass("right-node-visible");
  private Node left;
  private StackPane leftPane;
  private Node right;
  private StackPane rightPane;
  private final TextField control;
  
  public CustomTextFieldSkin(TextField control)
  {
    super(control, new TextFieldBehavior(control));
    
    this.control = control;
    updateChildren();
    
    registerChangeListener(leftProperty(), "LEFT_NODE");
    registerChangeListener(rightProperty(), "RIGHT_NODE");
    registerChangeListener(control.focusedProperty(), "FOCUSED");
  }
  
  public abstract ObjectProperty<Node> leftProperty();
  
  public abstract ObjectProperty<Node> rightProperty();
  
  protected void handleControlPropertyChanged(String p)
  {
    super.handleControlPropertyChanged(p);
    if ((p == "LEFT_NODE") || (p == "RIGHT_NODE")) {
      updateChildren();
    }
  }
  
  private void updateChildren()
  {
    Node newLeft = (Node)leftProperty().get();
    if (newLeft != null)
    {
      getChildren().remove(this.leftPane);
      this.leftPane = new StackPane(new Node[] { newLeft });
      this.leftPane.setAlignment(Pos.CENTER_LEFT);
      this.leftPane.getStyleClass().add("left-pane");
      getChildren().add(this.leftPane);
      this.left = newLeft;
    }
    Node newRight = (Node)rightProperty().get();
    if (newRight != null)
    {
      getChildren().remove(this.rightPane);
      this.rightPane = new StackPane(new Node[] { newRight });
      this.rightPane.setAlignment(Pos.CENTER_RIGHT);
      this.rightPane.getStyleClass().add("right-pane");
      getChildren().add(this.rightPane);
      this.right = newRight;
    }
    this.control.pseudoClassStateChanged(HAS_LEFT_NODE, this.left != null);
    this.control.pseudoClassStateChanged(HAS_RIGHT_NODE, this.right != null);
    this.control.pseudoClassStateChanged(HAS_NO_SIDE_NODE, (this.left == null) && (this.right == null));
  }
  
  protected void layoutChildren(double x, double y, double w, double h)
  {
    double fullHeight = h + snappedTopInset() + snappedBottomInset();
    
    double leftWidth = this.leftPane == null ? 0.0D : snapSize(this.leftPane.prefWidth(fullHeight));
    double rightWidth = this.rightPane == null ? 0.0D : snapSize(this.rightPane.prefWidth(fullHeight));
    
    double textFieldStartX = snapPosition(x) + snapSize(leftWidth);
    double textFieldWidth = w - snapSize(leftWidth) - snapSize(rightWidth);
    
    super.layoutChildren(textFieldStartX, 0.0D, textFieldWidth, fullHeight);
    if (this.leftPane != null)
    {
      double leftStartX = 0.0D;
      this.leftPane.resizeRelocate(0.0D, 0.0D, leftWidth, fullHeight);
    }
    if (this.rightPane != null)
    {
      double rightStartX = this.rightPane == null ? 0.0D : w - rightWidth + snappedLeftInset();
      this.rightPane.resizeRelocate(rightStartX, 0.0D, rightWidth, fullHeight);
    }
  }
  
  public HitInfo getIndex(double x, double y)
  {
    double leftWidth = this.leftPane == null ? 0.0D : snapSize(this.leftPane.prefWidth(((TextField)getSkinnable()).getHeight()));
    return super.getIndex(x - leftWidth, y);
  }
  
  protected double computePrefWidth(double h, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    double pw = super.computePrefWidth(h, topInset, rightInset, bottomInset, leftInset);
    double leftWidth = this.leftPane == null ? 0.0D : snapSize(this.leftPane.prefWidth(h));
    double rightWidth = this.rightPane == null ? 0.0D : snapSize(this.rightPane.prefWidth(h));
    
    return pw + leftWidth + rightWidth;
  }
  
  protected double computePrefHeight(double w, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    double ph = super.computePrefHeight(w, topInset, rightInset, bottomInset, leftInset);
    double leftHeight = this.leftPane == null ? 0.0D : snapSize(this.leftPane.prefHeight(-1.0D));
    double rightHeight = this.rightPane == null ? 0.0D : snapSize(this.rightPane.prefHeight(-1.0D));
    
    return Math.max(ph, Math.max(leftHeight, rightHeight));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\CustomTextFieldSkin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */