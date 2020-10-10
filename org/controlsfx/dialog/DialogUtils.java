package org.controlsfx.dialog;

import javafx.collections.ObservableList;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

class DialogUtils
{
  static void forcefullyHideDialog(Dialog<?> dialog)
  {
    DialogPane dialogPane = dialog.getDialogPane();
    if (containsCancelButton(dialog))
    {
      dialog.hide();
      return;
    }
    dialogPane.getButtonTypes().add(ButtonType.CANCEL);
    dialog.hide();
    dialogPane.getButtonTypes().remove(ButtonType.CANCEL);
  }
  
  static boolean containsCancelButton(Dialog<?> dialog)
  {
    DialogPane dialogPane = dialog.getDialogPane();
    for (ButtonType type : dialogPane.getButtonTypes()) {
      if (type.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
        return true;
      }
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\dialog\DialogUtils.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */