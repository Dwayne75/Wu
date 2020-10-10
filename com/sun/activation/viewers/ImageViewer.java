package com.sun.activation.viewers;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Panel;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import javax.activation.CommandObject;
import javax.activation.DataHandler;

public class ImageViewer
  extends Panel
  implements CommandObject
{
  private ImageViewerCanvas canvas = null;
  private Image image = null;
  private DataHandler _dh = null;
  private boolean DEBUG = false;
  
  public ImageViewer()
  {
    this.canvas = new ImageViewerCanvas();
    add(this.canvas);
  }
  
  public void setCommandContext(String verb, DataHandler dh)
    throws IOException
  {
    this._dh = dh;
    setInputStream(this._dh.getInputStream());
  }
  
  private void setInputStream(InputStream ins)
    throws IOException
  {
    MediaTracker mt = new MediaTracker(this);
    int bytes_read = 0;
    byte[] data = new byte['Ѐ'];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while ((bytes_read = ins.read(data)) > 0) {
      baos.write(data, 0, bytes_read);
    }
    ins.close();
    
    this.image = getToolkit().createImage(baos.toByteArray());
    
    mt.addImage(this.image, 0);
    try
    {
      mt.waitForID(0);
      mt.waitForAll();
      if (mt.statusID(0, true) != 8) {
        System.out.println("Error occured in image loading = " + mt.getErrorsID(0));
      }
    }
    catch (InterruptedException e)
    {
      throw new IOException("Error reading image data");
    }
    this.canvas.setImage(this.image);
    if (this.DEBUG) {
      System.out.println("calling invalidate");
    }
  }
  
  public void addNotify()
  {
    super.addNotify();
    invalidate();
    validate();
    doLayout();
  }
  
  public Dimension getPreferredSize()
  {
    return this.canvas.getPreferredSize();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\activation\viewers\ImageViewer.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */