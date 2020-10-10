package com.sun.activation.viewers;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

public class ImageViewerCanvas
  extends Canvas
{
  private Image canvas_image = null;
  
  public void setImage(Image new_image)
  {
    this.canvas_image = new_image;
    invalidate();
    repaint();
  }
  
  public Dimension getPreferredSize()
  {
    Dimension d = null;
    if (this.canvas_image == null) {
      d = new Dimension(200, 200);
    } else {
      d = new Dimension(this.canvas_image.getWidth(this), this.canvas_image.getHeight(this));
    }
    return d;
  }
  
  public void paint(Graphics g)
  {
    if (this.canvas_image != null) {
      g.drawImage(this.canvas_image, 0, 0, this);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\activation\viewers\ImageViewerCanvas.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */