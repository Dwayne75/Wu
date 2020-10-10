package com.sun.javaws.ui;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.DeployUIManager;
import com.sun.deploy.util.Trace;
import com.sun.javaws.Globals;
import com.sun.javaws.LaunchDownload.DownloadProgress;
import com.sun.javaws.Main;
import com.sun.javaws.cache.CacheImageLoader;
import com.sun.javaws.cache.CacheImageLoaderCallback;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;

public class DownloadWindow
  extends WindowAdapter
  implements ActionListener, LaunchDownload.DownloadProgress, CacheImageLoaderCallback
{
  private JFrame _frame = null;
  private String _title;
  private String _vendor;
  private long _estimatedDownloadSize = 0L;
  private long _totalDownloadedBytes = 0L;
  private URL _currentUrl = null;
  static final int TIMER_UPDATE_RATE = 1000;
  static final int TIMER_INITIAL_DELAY = 10;
  static final int TIMER_AVERAGE_SIZE = 10;
  Timer _timerObject = null;
  long[] _timerDownloadAverage = new long[10];
  int _timerCount = 0;
  long _timerLastBytesCount = 0L;
  boolean _timerOn = false;
  static final int HEART_BEAT_RATE = 250;
  static final boolean[] HEART_BEAT_RYTHM = { false, false, false, true, false, true };
  Timer _heartbeatTimer = null;
  Object _heartbeatLock = new Object();
  int _heartbeatCount = 0;
  boolean _heartbeatOn = false;
  boolean _isCanceled = false;
  boolean _exitOnCancel = true;
  private Image _appImage;
  private JButton _cancelButton = null;
  private JLabel _titleLabel = null;
  private JLabel _vendorLabel = null;
  private JLabel _infoStatus = null;
  private JLabel _infoProgressTxt = null;
  private JLabel _infoEstimatedTime = null;
  private JProgressBar _infoProgressBar = null;
  private JLabel _imageLabel = null;
  private static final int _yRestriction = 20;
  private static final int MAX_DISPLAY = 20;
  private static final String LEAD = "...";
  private DefaultBoundedRangeModel _loadingModel;
  private ActionListener _cancelActionListener;
  
  public DownloadWindow(LaunchDesc paramLaunchDesc, boolean paramBoolean)
  {
    setLaunchDesc(paramLaunchDesc, paramBoolean);
  }
  
  public DownloadWindow() {}
  
  public void setLaunchDesc(LaunchDesc paramLaunchDesc, boolean paramBoolean)
  {
    InformationDesc localInformationDesc = paramLaunchDesc.getInformation();
    this._title = localInformationDesc.getTitle();
    this._vendor = localInformationDesc.getVendor();
    if (this._titleLabel != null)
    {
      this._titleLabel.setText(this._title);
      this._vendorLabel.setText(this._vendor);
    }
    this._isCanceled = false;
    this._exitOnCancel = paramBoolean;
    if (localInformationDesc != null)
    {
      IconDesc localIconDesc = localInformationDesc.getIconLocation(2, 0);
      if (localIconDesc != null) {
        CacheImageLoader.getInstance().loadImage(localIconDesc, this);
      }
    }
  }
  
  public void imageAvailable(IconDesc paramIconDesc, Image paramImage, File paramFile)
  {
    updateImage(paramImage, true);
  }
  
  public void finalImageAvailable(IconDesc paramIconDesc, Image paramImage, File paramFile) {}
  
  public JFrame getFrame()
  {
    return this._frame;
  }
  
  public void buildIntroScreen()
  {
    LookAndFeel localLookAndFeel = null;
    try
    {
      localLookAndFeel = DeployUIManager.setLookAndFeel();
      
      this._frame = new JFrame(ResourceManager.getString("product.javaws.name", ""));
      
      this._frame.addWindowListener(this);
      
      JPanel localJPanel1 = new JPanel(new BorderLayout());
      Container localContainer = this._frame.getContentPane();
      localContainer.setLayout(new BorderLayout());
      localContainer.add(localJPanel1, "Center");
      
      localJPanel1.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8), new BevelBorder(1)));
      
      JPanel localJPanel2 = new JPanel(new BorderLayout());
      localJPanel1.add(localJPanel2, "North");
      
      JPanel localJPanel3 = new JPanel(new BorderLayout());
      localJPanel1.add(localJPanel3, "Center");
      
      JPanel localJPanel4 = new JPanel(new BorderLayout());
      this._imageLabel = new BLabel();
      localJPanel4.add(this._imageLabel, "Center");
      localJPanel4.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
      updateImage(ResourceManager.getIcon("java48.image").getImage(), false);
      
      localJPanel2.add(localJPanel4, "West");
      
      Font localFont1 = ResourceManager.getUIFont();
      Font localFont2 = localFont1.deriveFont(22.0F);
      Font localFont3 = localFont1.deriveFont(18.0F);
      
      JPanel localJPanel5 = new JPanel(new GridLayout(2, 3));
      localJPanel2.add(localJPanel5, "Center");
      
      this._titleLabel = new BLabel(this._title, 360, 0);
      this._titleLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
      this._titleLabel.setFont(localFont2);
      localJPanel5.add(this._titleLabel);
      
      this._vendorLabel = new BLabel(this._vendor, 0, 0);
      this._vendorLabel.setFont(localFont3);
      this._vendorLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
      localJPanel5.add(this._vendorLabel);
      
      JPanel localJPanel6 = new JPanel(new BorderLayout());
      localContainer.add(localJPanel6, "South");
      
      this._cancelButton = new JButton(ResourceManager.getString("launch.cancel"));
      
      this._cancelButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent paramAnonymousActionEvent)
        {
          DownloadWindow.this.cancelAction();
        }
      });
      this._infoStatus = new BLabel(ResourceManager.getString("launch.initializing", this._title, this._vendor), 0, 0);
      
      this._infoProgressTxt = new BLabel(" ", 0, 0);
      
      this._infoEstimatedTime = new BLabel(" ", 0, 0);
      
      this._loadingModel = new DefaultBoundedRangeModel(0, 1, 0, 100);
      this._infoProgressBar = new JProgressBar(this._loadingModel);
      this._infoProgressBar.setOpaque(true);
      this._infoProgressBar.setVisible(true);
      
      localJPanel3.add(this._infoStatus, "North");
      localJPanel3.add(this._infoProgressTxt, "Center");
      localJPanel3.add(this._infoEstimatedTime, "South");
      localJPanel3.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
      
      this._infoProgressBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
      
      localJPanel6.add(this._infoProgressBar, "Center");
      localJPanel6.add(this._cancelButton, "East");
      localJPanel6.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 10));
      
      this._frame.pack();
      setIndeterminedProgressBar(true);
      
      Dimension localDimension = Toolkit.getDefaultToolkit().getScreenSize();
      int i = (localDimension.width - this._frame.getWidth()) / 2;
      int j = (localDimension.height - this._frame.getHeight()) / 2;
      this._frame.setLocation(i, j);
    }
    finally
    {
      DeployUIManager.restoreLookAndFeel(localLookAndFeel);
    }
  }
  
  public void showLoadingProgressScreen()
  {
    setStatus(ResourceManager.getString("launch.progressScreen"));
    
    this._timerObject = new Timer(1000, this);
    this._timerObject.start();
  }
  
  public void setStatus(String paramString)
  {
    Runnable local2 = new Runnable()
    {
      private final String val$text;
      
      public void run()
      {
        if (DownloadWindow.this._infoStatus != null) {
          DownloadWindow.this._infoStatus.setText(this.val$text == null ? " " : this.val$text);
        }
      }
    };
    if ((this._infoStatus != null) && (this._infoStatus.isShowing())) {
      SwingUtilities.invokeLater(local2);
    } else {
      local2.run();
    }
  }
  
  public void setProgressText(String paramString)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      private final String val$text;
      
      public void run()
      {
        if (DownloadWindow.this._infoProgressTxt != null) {
          DownloadWindow.this._infoProgressTxt.setText(this.val$text == null ? " " : this.val$text);
        }
      }
    });
  }
  
  public void setProgressBarVisible(boolean paramBoolean)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      private final boolean val$isVisible;
      
      public void run()
      {
        if (DownloadWindow.this._infoProgressBar != null) {
          DownloadWindow.this._infoProgressBar.setVisible(this.val$isVisible);
        }
      }
    });
  }
  
  public void setProgressBarValue(int paramInt)
  {
    if (this._heartbeatOn) {
      setIndeterminedProgressBar(false);
    }
    if (this._loadingModel != null) {
      this._loadingModel.setValue(paramInt);
    }
    setProgressBarVisible(paramInt != 0);
  }
  
  public void setIndeterminedProgressBar(boolean paramBoolean)
  {
    if (this._heartbeatTimer == null) {
      this._heartbeatTimer = new Timer(250, new ActionListener()
      {
        public void actionPerformed(ActionEvent paramAnonymousActionEvent)
        {
          synchronized (DownloadWindow.this._heartbeatLock)
          {
            if ((DownloadWindow.this._heartbeatOn) && (DownloadWindow.this._heartbeatTimer != null))
            {
              DownloadWindow.this._heartbeatCount = ((DownloadWindow.this._heartbeatCount + 1) % DownloadWindow.HEART_BEAT_RYTHM.length);
              int i = DownloadWindow.HEART_BEAT_RYTHM[DownloadWindow.this._heartbeatCount];
              if (i != 0) {
                DownloadWindow.this._loadingModel.setValue(100);
              } else {
                DownloadWindow.this._loadingModel.setValue(0);
              }
            }
          }
        }
      });
    }
    synchronized (this._heartbeatLock)
    {
      if (paramBoolean)
      {
        setProgressBarVisible(true);
        this._loadingModel.setValue(0);
        this._heartbeatTimer.start();
        this._heartbeatOn = true;
      }
      else
      {
        setProgressBarVisible(false);
        this._heartbeatTimer.stop();
        this._heartbeatOn = false;
      }
    }
  }
  
  public void showLaunchingApplication(String paramString)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      private final String val$title;
      
      public void run()
      {
        if (DownloadWindow.this._loadingModel != null)
        {
          DownloadWindow.this._infoStatus.setText(this.val$title);
          DownloadWindow.this._infoProgressTxt.setText(" ");
          DownloadWindow.this._infoEstimatedTime.setText(" ");
          DownloadWindow.this._loadingModel.setValue(0);
        }
      }
    });
  }
  
  private void setEstimatedTime(String paramString)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      private final String val$title;
      
      public void run()
      {
        if (DownloadWindow.this._infoEstimatedTime != null) {
          DownloadWindow.this._infoEstimatedTime.setText(this.val$title == null ? " " : this.val$title);
        }
      }
    });
  }
  
  public void clearWindow()
  {
    if (SwingUtilities.isEventDispatchThread()) {
      clearWindowHelper();
    } else {
      try
      {
        SwingUtilities.invokeAndWait(new Runnable()
        {
          public void run()
          {
            DownloadWindow.this.clearWindowHelper();
          }
        });
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
    }
  }
  
  private void clearWindowHelper()
  {
    if (this._timerObject != null)
    {
      this._timerObject.stop();
      this._timerObject = null;
      this._timerDownloadAverage = null;
    }
    if (this._heartbeatTimer != null) {
      synchronized (this._heartbeatLock)
      {
        this._heartbeatTimer.stop();
        this._heartbeatTimer = null;
      }
    }
    if (this._frame != null)
    {
      this._infoStatus = null;
      this._infoProgressTxt = null;
      this._infoProgressBar = null;
      this._loadingModel = null;
      this._infoEstimatedTime = null;
      this._cancelButton.removeActionListener(this._cancelActionListener);
      this._cancelButton = null;
      this._cancelActionListener = null;
      this._frame.getContentPane().removeAll();
    }
  }
  
  public void disposeWindow()
  {
    if (this._frame != null)
    {
      clearWindow();
      this._frame.removeWindowListener(this);
      this._frame.setVisible(false);
      this._frame.dispose();
      this._frame = null;
    }
  }
  
  public void reset()
  {
    setStatus(null);
    setProgressText(null);
    setProgressBarVisible(false);
  }
  
  public void actionPerformed(ActionEvent paramActionEvent)
  {
    if (!this._timerOn) {
      return;
    }
    if (this._estimatedDownloadSize <= 0L) {
      return;
    }
    long l = this._totalDownloadedBytes - this._timerLastBytesCount;
    this._timerLastBytesCount = this._totalDownloadedBytes;
    this._timerDownloadAverage[(this._timerCount % 10)] = l;
    if (this._totalDownloadedBytes > this._estimatedDownloadSize) {
      this._estimatedDownloadSize = this._totalDownloadedBytes;
    }
    if (this._timerCount > 10)
    {
      float f = 0.0F;
      for (int i = 0; i < 10; i++) {
        f += (float)this._timerDownloadAverage[i];
      }
      f /= 10.0F;
      f /= 1.0F;
      if (f == 0.0F)
      {
        setEstimatedTime(ResourceManager.getString("launch.stalledDownload"));
      }
      else if (this._estimatedDownloadSize > 0L)
      {
        i = (int)((float)(this._estimatedDownloadSize - this._totalDownloadedBytes) / f);
        int j = i / 3600;
        i -= j * 3600;
        int k = i / 60;
        i -= k * 60;
        int m = i;
        
        setEstimatedTime(ResourceManager.getString("launch.estimatedTimeLeft", j, k, m));
      }
    }
    this._timerCount += 1;
  }
  
  public void resetDownloadTimer()
  {
    this._timerCount = 0;
    this._timerLastBytesCount = 0L;
  }
  
  public void progress(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt)
  {
    this._timerOn = true;
    this._totalDownloadedBytes = Math.max(0L, paramLong1);
    this._estimatedDownloadSize = paramLong2;
    if ((paramURL != this._currentUrl) && (paramURL != null))
    {
      String str1 = paramURL.getHost();
      String str2 = paramURL.getFile();
      int i = str2.lastIndexOf('/');
      if (i != -1) {
        str2 = str2.substring(i + 1);
      }
      if (str2.length() + str1.length() > 40)
      {
        str2 = maxDisplay(str2);
        str1 = maxDisplay(str1);
      }
      setStatus(ResourceManager.getString("launch.loadingNetStatus", str2, str1));
      this._currentUrl = paramURL;
    }
    if (paramLong2 == -1L)
    {
      setProgressText(ResourceManager.getString("launch.loadingNetProgress", bytesToString(this._totalDownloadedBytes)));
    }
    else
    {
      setProgressText(ResourceManager.getString("launch.loadingNetProgressPercent", bytesToString(this._totalDownloadedBytes), bytesToString(paramLong2), new Long(Math.max(0, paramInt)).toString()));
      
      setProgressBarValue(paramInt);
    }
  }
  
  public void patching(URL paramURL, String paramString, int paramInt1, int paramInt2)
  {
    this._timerOn = false;
    setEstimatedTime(null);
    if ((this._currentUrl != paramURL) || (paramInt1 == 0))
    {
      String str1 = paramURL.getHost();
      String str2 = paramURL.getFile();
      int i = str2.lastIndexOf('/');
      if (i != -1) {
        str2 = str2.substring(i + 1);
      }
      if (str2.length() + str1.length() > 40)
      {
        str2 = maxDisplay(str2);
        str1 = maxDisplay(str1);
      }
      setStatus(ResourceManager.getString("launch.patchingStatus", str2, str1));
      
      this._currentUrl = paramURL;
    }
    setProgressText(null);
    setProgressBarValue(paramInt2);
  }
  
  private String maxDisplay(String paramString)
  {
    int i = paramString.length();
    if (i > 20) {
      paramString = "..." + paramString.substring(i - (20 - "...".length()), i);
    }
    return paramString;
  }
  
  public void validating(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt)
  {
    this._timerOn = false;
    setEstimatedTime(null);
    
    long l = paramLong2 == 0L ? 0L : paramLong1 * 100L / paramLong2;
    if ((this._currentUrl != paramURL) || (paramLong1 == 0L))
    {
      String str1 = paramURL.getHost();
      String str2 = paramURL.getFile();
      int i = str2.lastIndexOf('/');
      if (i != -1) {
        str2 = str2.substring(i + 1);
      }
      if (str2.length() + str1.length() > 40)
      {
        str2 = maxDisplay(str2);
        str1 = maxDisplay(str1);
      }
      setStatus(ResourceManager.getString("launch.validatingStatus", str2, str1));
      this._currentUrl = paramURL;
    }
    if (paramLong1 != 0L) {
      setProgressText(ResourceManager.getString("launch.validatingProgress", (int)l));
    } else {
      setProgressText(null);
    }
    setProgressBarValue(paramInt);
  }
  
  public void downloadFailed(URL paramURL, String paramString)
  {
    this._timerOn = false;
    setEstimatedTime(null);
    
    setStatus(ResourceManager.getString("launch.loadingResourceFailedSts", paramURL.toString()));
    setProgressText(ResourceManager.getString("launch.loadingResourceFailed"));
    setProgressBarVisible(false);
  }
  
  public void extensionDownload(String paramString, int paramInt)
  {
    this._timerOn = false;
    setEstimatedTime(null);
    if (paramString != null) {
      setStatus(ResourceManager.getString("launch.extensiondownload-name", paramString, paramInt));
    } else {
      setStatus(ResourceManager.getString("launch.extensiondownload", paramString, paramInt));
    }
  }
  
  public void jreDownload(String paramString, URL paramURL)
  {
    this._timerOn = false;
    setEstimatedTime(null);
    
    String str = paramURL.getHost();
    
    str = maxDisplay(str);
    
    setStatus(ResourceManager.getString("launch.downloadingJRE", paramString, str));
  }
  
  private void loadingFromNet(URL paramURL, int paramInt1, int paramInt2) {}
  
  private void setAppImage(Image paramImage)
  {
    updateImage(paramImage, true);
  }
  
  private void updateImage(Image paramImage, boolean paramBoolean)
  {
    if (paramImage != null)
    {
      int i = paramImage.getWidth(null);
      int j = paramImage.getHeight(null);
      Object localObject1;
      if ((i > 64) || (j > 64))
      {
        int k = 64;
        if ((j > i) && (j < 2 * i)) {
          k = 64 * i / j;
        }
        localObject1 = new BufferedImage(64, 64, 1);
        if (!Globals.isHeadless())
        {
          Graphics localGraphics = ((Image)localObject1).getGraphics();
          try
          {
            if (this._imageLabel != null)
            {
              localGraphics.setColor(this._imageLabel.getBackground());
              localGraphics.fillRect(0, 0, 64, 64);
            }
            localGraphics.drawImage(paramImage, (64 - k) / 2, 0, k, 64, null);
          }
          finally
          {
            localGraphics.dispose();
          }
        }
        paramImage = (Image)localObject1;
      }
      else if ((i < 64) || (j < 64))
      {
        BufferedImage localBufferedImage = new BufferedImage(64, 64, 1);
        
        localObject1 = localBufferedImage.getGraphics();
        try
        {
          if (this._imageLabel != null)
          {
            ((Graphics)localObject1).setColor(this._imageLabel.getBackground());
            ((Graphics)localObject1).fillRect(0, 0, 64, 64);
          }
          ((Graphics)localObject1).drawImage(paramImage, (64 - i) / 2, (64 - j) / 2, i, j, null);
        }
        finally
        {
          ((Graphics)localObject1).dispose();
        }
        paramImage = localBufferedImage;
      }
    }
    synchronized (this)
    {
      if ((this._appImage == null) || (paramBoolean)) {
        this._appImage = paramImage;
      }
    }
    if (this._imageLabel != null)
    {
      if (this._appImage != null) {
        this._imageLabel.setIcon(new ImageIcon(this._appImage));
      }
      this._imageLabel.repaint();
    }
  }
  
  private String bytesToString(long paramLong)
  {
    String str = "";
    double d = paramLong;
    int i = 0;
    if (paramLong > 1073741824L)
    {
      d /= 1.073741824E9D;
      str = "G";
      i = 1;
    }
    else if (paramLong > 1048576L)
    {
      d /= 1048576.0D;
      str = "M";
      i = 1;
    }
    else if (paramLong > 1024L)
    {
      d /= 1024.0D;
      str = "K";
      i = 0;
    }
    return ResourceManager.formatDouble(d, i) + str;
  }
  
  public void windowClosing(WindowEvent paramWindowEvent)
  {
    cancelAction();
  }
  
  private void cancelAction()
  {
    if (this._exitOnCancel) {
      AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          Main.systemExit(-1);
          return null;
        }
      });
    } else {
      this._isCanceled = true;
    }
  }
  
  public boolean isCanceled()
  {
    return this._isCanceled;
  }
  
  public void resetCancled()
  {
    this._isCanceled = false;
  }
  
  public void setVisible(boolean paramBoolean)
  {
    JFrame localJFrame = this._frame;
    if (localJFrame != null) {
      SwingUtilities.invokeLater(new Runnable()
      {
        private final Frame val$f;
        private final boolean val$show;
        
        public void run()
        {
          this.val$f.setVisible(this.val$show);
        }
      });
    }
  }
  
  class BLabel
    extends JLabel
  {
    int _w;
    int _h;
    
    public BLabel()
    {
      this._w = 0;
      this._h = 0;
      setOpaque(true);
      setForeground(UIManager.getColor("textText"));
    }
    
    public BLabel(String paramString, int paramInt1, int paramInt2)
    {
      super();
      this._w = paramInt1;
      this._h = paramInt2;
      setOpaque(true);
      setForeground(UIManager.getColor("textText"));
    }
    
    public Dimension getPreferredSize()
    {
      Dimension localDimension = super.getPreferredSize();
      if (this._w > localDimension.width) {
        localDimension.width = this._w;
      }
      if (this._h > localDimension.height) {
        localDimension.height = this._h;
      }
      return localDimension;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\ui\DownloadWindow.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */