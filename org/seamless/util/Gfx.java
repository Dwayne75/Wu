package org.seamless.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Gfx
{
  public static byte[] resizeProportionally(ImageIcon icon, String contentType, int newWidth, int newHeight)
    throws IOException
  {
    double widthRatio = newWidth != icon.getIconWidth() ? newWidth / icon.getIconWidth() : 1.0D;
    
    double heightRatio = newHeight != icon.getIconHeight() ? newHeight / icon.getIconHeight() : 1.0D;
    if (widthRatio < heightRatio) {
      newHeight = (int)(icon.getIconHeight() * widthRatio);
    } else {
      newWidth = (int)(icon.getIconWidth() * heightRatio);
    }
    int imageType = "image/png".equals(contentType) ? 2 : 1;
    
    BufferedImage bImg = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), imageType);
    Graphics2D g2d = bImg.createGraphics();
    g2d.drawImage(icon.getImage(), 0, 0, icon.getIconWidth(), icon.getIconHeight(), null);
    g2d.dispose();
    
    BufferedImage scaledImg = getScaledInstance(bImg, newWidth, newHeight, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, false);
    
    String formatName = "";
    if ("image/png".equals(contentType)) {
      formatName = "png";
    } else if (("image/jpeg".equals(contentType)) || ("image/jpg".equals(contentType))) {
      formatName = "jpeg";
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
    ImageIO.write(scaledImg, formatName, baos);
    return baos.toByteArray();
  }
  
  public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality)
  {
    int type = img.getTransparency() == 1 ? 1 : 2;
    
    BufferedImage ret = img;
    int h;
    int w;
    int h;
    if (higherQuality)
    {
      int w = img.getWidth();
      h = img.getHeight();
    }
    else
    {
      w = targetWidth;
      h = targetHeight;
    }
    do
    {
      if ((higherQuality) && (w > targetWidth))
      {
        w /= 2;
        if (w < targetWidth) {
          w = targetWidth;
        }
      }
      if ((higherQuality) && (h > targetHeight))
      {
        h /= 2;
        if (h < targetHeight) {
          h = targetHeight;
        }
      }
      BufferedImage tmp = new BufferedImage(w, h, type);
      Graphics2D g2 = tmp.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
      g2.drawImage(ret, 0, 0, w, h, null);
      g2.dispose();
      
      ret = tmp;
    } while ((w != targetWidth) || (h != targetHeight));
    return ret;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\Gfx.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */