package org.controlsfx.control.cell;

import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.controlsfx.control.GridCell;

public class MediaImageCell
  extends GridCell<Media>
{
  private MediaPlayer mediaPlayer;
  private final MediaView mediaView;
  
  public MediaImageCell()
  {
    getStyleClass().add("media-grid-cell");
    
    this.mediaView = new MediaView();
    this.mediaView.setMediaPlayer(this.mediaPlayer);
    this.mediaView.fitHeightProperty().bind(heightProperty());
    this.mediaView.fitWidthProperty().bind(widthProperty());
    this.mediaView.setMediaPlayer(this.mediaPlayer);
  }
  
  public void pause()
  {
    if (this.mediaPlayer != null) {
      this.mediaPlayer.pause();
    }
  }
  
  public void play()
  {
    if (this.mediaPlayer != null) {
      this.mediaPlayer.play();
    }
  }
  
  public void stop()
  {
    if (this.mediaPlayer != null) {
      this.mediaPlayer.stop();
    }
  }
  
  protected void updateItem(Media item, boolean empty)
  {
    super.updateItem(item, empty);
    
    getChildren().clear();
    if (this.mediaPlayer != null) {
      this.mediaPlayer.stop();
    }
    if (empty)
    {
      setGraphic(null);
    }
    else
    {
      this.mediaPlayer = new MediaPlayer(item);
      this.mediaView.setMediaPlayer(this.mediaPlayer);
      setGraphic(this.mediaView);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\cell\MediaImageCell.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */