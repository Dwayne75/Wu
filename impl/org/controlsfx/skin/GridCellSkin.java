package impl.org.controlsfx.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.CellSkinBase;
import java.util.Collections;
import org.controlsfx.control.GridCell;

public class GridCellSkin<T>
  extends CellSkinBase<GridCell<T>, BehaviorBase<GridCell<T>>>
{
  public GridCellSkin(GridCell<T> control)
  {
    super(control, new BehaviorBase(control, Collections.emptyList()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\GridCellSkin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */