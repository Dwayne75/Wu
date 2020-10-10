package com.sun.javaws.ui;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.Trace;
import com.sun.javaws.LaunchDownload;
import com.sun.javaws.LocalApplicationProperties;
import com.sun.javaws.cache.Cache;
import com.sun.javaws.cache.CacheImageLoader;
import com.sun.javaws.cache.CacheImageLoaderCallback;
import com.sun.javaws.cache.DiskCacheEntry;
import com.sun.javaws.cache.DownloadProtocol;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;

class CacheObject
{
  private static final DateFormat _df = ;
  private static final String[] COLUMN_KEYS = { "jnlp.viewer.app.column", "jnlp.viewer.vendor.column", "jnlp.viewer.type.column", "jnlp.viewer.date.column", "jnlp.viewer.size.column", "jnlp.viewer.status.column" };
  private static final int _columns = COLUMN_KEYS.length;
  private static TLabel _title;
  private static TLabel _vendor;
  private static TLabel _type;
  private static TLabel _date;
  private static TLabel _size;
  private static TLabel _status;
  private static ImageIcon _onlineIcon;
  private static ImageIcon _offlineIcon;
  private static ImageIcon _noLaunchIcon;
  private static ImageIcon _java32;
  private final DiskCacheEntry _dce;
  private final AbstractTableModel _model;
  
  public CacheObject(DiskCacheEntry paramDiskCacheEntry, AbstractTableModel paramAbstractTableModel)
  {
    this._dce = paramDiskCacheEntry;
    this._model = paramAbstractTableModel;
    if (_title == null)
    {
      _title = new TLabel(2);
      _vendor = new TLabel(2);
      _type = new TLabel(0);
      _date = new TLabel(4);
      _size = new TLabel(4);
      _status = new TLabel(0);
      
      _java32 = new ViewerIcon(0, 0, ResourceManager.class.getResource("image/java32.png"));
      
      _onlineIcon = new ViewerIcon(0, 0, ResourceManager.class.getResource("image/online.gif"));
      
      _offlineIcon = new ViewerIcon(0, 0, ResourceManager.class.getResource("image/offline.gif"));
      
      _noLaunchIcon = null;
    }
  }
  
  public static String getColumnName(int paramInt)
  {
    return ResourceManager.getMessage(COLUMN_KEYS[paramInt]);
  }
  
  public static int getColumnCount()
  {
    return _columns;
  }
  
  public static String getHeaderToolTipText(int paramInt)
  {
    return ResourceManager.getString(COLUMN_KEYS[paramInt] + ".tooltip");
  }
  
  public static int getPreferredWidth(int paramInt)
  {
    if (paramInt < _columns) {
      switch (paramInt)
      {
      case 0: 
        return 192;
      case 1: 
        return 140;
      case 2: 
        return 70;
      case 3: 
        return 70;
      case 4: 
        return 64;
      case 5: 
        return 64;
      }
    }
    throw new ArrayIndexOutOfBoundsException("column index: " + paramInt);
  }
  
  public static Class getClass(int paramInt)
  {
    if (paramInt < _columns) {
      switch (paramInt)
      {
      case 0: 
        return JLabel.class;
      case 1: 
        return JLabel.class;
      case 2: 
        return JLabel.class;
      case 3: 
        return JLabel.class;
      case 4: 
        return JLabel.class;
      case 5: 
        return JLabel.class;
      }
    }
    throw new ArrayIndexOutOfBoundsException("column index: " + paramInt);
  }
  
  public Object getObject(int paramInt)
  {
    if (paramInt < _columns) {
      switch (paramInt)
      {
      case 0: 
        return getTitleLabel();
      case 1: 
        return getVendorLabel();
      case 2: 
        return getTypeLabel();
      case 3: 
        return getDateLabel();
      case 4: 
        return getSizeLabel();
      case 5: 
        return getStatusLabel();
      }
    }
    throw new ArrayIndexOutOfBoundsException("column index: " + paramInt);
  }
  
  public boolean isEditable(int paramInt)
  {
    return false;
  }
  
  private final int ICON_W = 32;
  private final int ICON_H = 32;
  private String _titleString = null;
  
  public void setValue(int paramInt, Object paramObject) {}
  
  public String getTitleString()
  {
    if (this._titleString == null) {
      this._titleString = getTitle();
    }
    return this._titleString;
  }
  
  private ImageIcon _icon = null;
  
  private JLabel getTitleLabel()
  {
    if (this._icon == null)
    {
      File localFile = getIconFile();
      if (localFile != null) {
        this._icon = new ViewerIcon(32, 32, localFile.getPath());
      }
      if (this._icon == null) {
        this._icon = _java32;
      }
    }
    if ((this._icon != null) && (this._icon.getIconWidth() > 0) && (this._icon.getIconHeight() > 0)) {
      _title.setIcon(this._icon);
    }
    _title.setText(getTitleString());
    return _title;
  }
  
  private String _vendorString = null;
  
  public String getVendorString()
  {
    if (this._vendorString == null) {
      this._vendorString = getVendor();
    }
    return this._vendorString;
  }
  
  private TLabel getVendorLabel()
  {
    _vendor.setText(getVendorString());
    return _vendor;
  }
  
  private String _typeString = null;
  
  public String getTypeString()
  {
    if (this._typeString == null) {
      this._typeString = getLaunchTypeString(getLaunchDesc().getLaunchType());
    }
    return this._typeString;
  }
  
  public static String getLaunchTypeString(int paramInt)
  {
    switch (paramInt)
    {
    case 1: 
      return ResourceManager.getMessage("jnlp.viewer.application");
    case 2: 
      return ResourceManager.getMessage("jnlp.viewer.applet");
    case 3: 
      return ResourceManager.getMessage("jnlp.viewer.extension");
    case 4: 
      return ResourceManager.getMessage("jnlp.viewer.installer");
    }
    return "";
  }
  
  private TLabel getTypeLabel()
  {
    _type.setText(getTypeString());
    return _type;
  }
  
  private Date _theDate = null;
  private String _dateString = null;
  
  public Date getDate()
  {
    if (this._dateString == null)
    {
      this._theDate = getLastAccesed();
      if (this._theDate != null) {
        this._dateString = _df.format(this._theDate);
      } else {
        this._dateString = "";
      }
    }
    return this._theDate;
  }
  
  private TLabel getDateLabel()
  {
    getDate();
    _date.setText(this._dateString);
    return _date;
  }
  
  private long _theSize = 0L;
  private String _sizeString = null;
  
  public long getSize()
  {
    if (this._sizeString == null)
    {
      this._theSize = getResourceSize();
      if (this._theSize > 10240L) {
        this._sizeString = (" " + this._theSize / 1024L + " KB");
      } else {
        this._sizeString = (" " + this._theSize / 1024L + "." + this._theSize % 1024L / 102L + " KB");
      }
    }
    return this._theSize;
  }
  
  private TLabel getSizeLabel()
  {
    getSize();
    _size.setText(this._sizeString);
    return _size;
  }
  
  private int _statusInt = -1;
  private ImageIcon _statusIcon = null;
  private String _statusText = "";
  
  public int getStatus()
  {
    if (this._statusInt < 0)
    {
      if (canLaunchOffline()) {
        this._statusInt = 2;
      } else {
        this._statusInt = (hasHref() ? 1 : 0);
      }
      switch (this._statusInt)
      {
      case 0: 
        this._statusIcon = _noLaunchIcon;
        if (getLaunchDesc().isApplicationDescriptor()) {
          this._statusText = ResourceManager.getString("jnlp.viewer.norun1.tooltip", getTypeString());
        } else {
          this._statusText = ResourceManager.getString("jnlp.viewer.norun2.tooltip");
        }
        break;
      case 1: 
        this._statusIcon = _onlineIcon;
        this._statusText = ResourceManager.getString("jnlp.viewer.online.tooltip", getTypeString());
        
        break;
      case 2: 
        this._statusIcon = _offlineIcon;
        this._statusText = ResourceManager.getString("jnlp.viewer.offline.tooltip", getTypeString());
      }
    }
    return this._statusInt;
  }
  
  private TLabel getStatusLabel()
  {
    getStatus();
    if ((this._statusIcon == null) || ((this._statusIcon.getIconWidth() > 0) && (this._statusIcon.getIconHeight() > 0)))
    {
      _status.setIcon(this._statusIcon);
      _status.setToolTipText(this._statusText);
    }
    return _status;
  }
  
  public static void hasFocus(Component paramComponent, boolean paramBoolean)
  {
    if ((paramComponent instanceof TLabel)) {
      ((TLabel)paramComponent).hasFocus(paramBoolean);
    }
  }
  
  public int compareColumns(CacheObject paramCacheObject, int paramInt)
  {
    switch (paramInt)
    {
    case 0: 
      return compareStrings(getTitleString(), paramCacheObject.getTitleString());
    case 1: 
      return compareStrings(getVendorString(), paramCacheObject.getVendorString());
    case 2: 
      return compareStrings(getTypeString(), paramCacheObject.getTypeString());
    case 3: 
      return compareDates(getDate(), paramCacheObject.getDate());
    case 4: 
      return compareLong(getSize(), paramCacheObject.getSize());
    }
    return compareInt(getStatus(), paramCacheObject.getStatus());
  }
  
  private static final float[] dash = { 1.0F, 2.0F };
  private static final BasicStroke _dashed = new BasicStroke(1.0F, 2, 0, 10.0F, dash, 0.0F);
  
  private class TLabel
    extends JLabel
  {
    boolean _focus = false;
    
    public TLabel(int paramInt)
    {
      setOpaque(true);
      setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
      setHorizontalAlignment(paramInt);
    }
    
    public void paint(Graphics paramGraphics)
    {
      super.paint(paramGraphics);
      if ((this._focus) && ((paramGraphics instanceof Graphics2D)))
      {
        Stroke localStroke = ((Graphics2D)paramGraphics).getStroke();
        ((Graphics2D)paramGraphics).setStroke(CacheObject._dashed);
        paramGraphics.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        ((Graphics2D)paramGraphics).setStroke(localStroke);
      }
    }
    
    public void hasFocus(boolean paramBoolean)
    {
      this._focus = paramBoolean;
    }
  }
  
  private int compareStrings(String paramString1, String paramString2)
  {
    if (paramString1 == paramString2) {
      return 0;
    }
    if (paramString1 == null) {
      return -1;
    }
    if (paramString2 == null) {
      return 1;
    }
    return paramString1.compareTo(paramString2);
  }
  
  private int compareDates(Date paramDate1, Date paramDate2)
  {
    if (paramDate1 == paramDate2) {
      return 0;
    }
    if (paramDate1 == null) {
      return -1;
    }
    if (paramDate2 == null) {
      return 1;
    }
    return compareLong(paramDate1.getTime(), paramDate2.getTime());
  }
  
  private int compareLong(long paramLong1, long paramLong2)
  {
    if (paramLong1 == paramLong2) {
      return 0;
    }
    return paramLong1 < paramLong2 ? -1 : 1;
  }
  
  private int compareInt(int paramInt1, int paramInt2)
  {
    if (paramInt1 == paramInt2) {
      return 0;
    }
    return paramInt1 < paramInt2 ? -1 : 1;
  }
  
  public DiskCacheEntry getDCE()
  {
    return this._dce;
  }
  
  LaunchDesc _ld = null;
  LocalApplicationProperties _lap = null;
  
  public LaunchDesc getLaunchDesc()
  {
    if (this._ld == null) {
      try
      {
        this._ld = LaunchDescFactory.buildDescriptor(this._dce.getFile());
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
    }
    return this._ld;
  }
  
  public LocalApplicationProperties getLocalApplicationProperties()
  {
    if (this._lap == null) {
      this._lap = Cache.getLocalApplicationProperties(this._dce, getLaunchDesc());
    }
    return this._lap;
  }
  
  public File getJnlpFile()
  {
    return this._dce.getFile();
  }
  
  public String getTitle()
  {
    try
    {
      return getLaunchDesc().getInformation().getTitle();
    }
    catch (Exception localException) {}
    return "";
  }
  
  public String getVendor()
  {
    try
    {
      return getLaunchDesc().getInformation().getVendor();
    }
    catch (Exception localException) {}
    return "";
  }
  
  public String getHref()
  {
    URL localURL = getLaunchDesc().getLocation();
    if (localURL != null) {
      return localURL.toString();
    }
    return null;
  }
  
  public File getIconFile()
  {
    try
    {
      IconDesc localIconDesc = getLaunchDesc().getInformation().getIconLocation(1, 0);
      
      DiskCacheEntry localDiskCacheEntry = DownloadProtocol.getCachedVersion(localIconDesc.getLocation(), localIconDesc.getVersion(), 2);
      if (localDiskCacheEntry != null) {
        return localDiskCacheEntry.getFile();
      }
    }
    catch (Exception localException) {}
    return null;
  }
  
  public Date getLastAccesed()
  {
    return getLocalApplicationProperties().getLastAccessed();
  }
  
  public long getResourceSize()
  {
    return LaunchDownload.getCachedSize(getLaunchDesc());
  }
  
  public boolean inFilter(int paramInt)
  {
    return (paramInt == 0) || (paramInt == getLaunchDesc().getLaunchType());
  }
  
  public boolean hasHref()
  {
    if (getLaunchDesc().isApplicationDescriptor()) {
      return this._ld.getLocation() != null;
    }
    return false;
  }
  
  public boolean canLaunchOffline()
  {
    if (getLaunchDesc().isApplicationDescriptor()) {
      return this._ld.getInformation().supportsOfflineOperation();
    }
    return false;
  }
  
  private class ViewerIcon
    extends ImageIcon
    implements CacheImageLoaderCallback
  {
    private int _width;
    private int _height;
    
    public ViewerIcon(int paramInt1, int paramInt2, String paramString)
    {
      this._width = paramInt1;
      this._height = paramInt2;
      try
      {
        URL localURL = new File(paramString).toURL();
        if (localURL != null) {
          CacheImageLoader.getInstance().loadImage(localURL, this);
        }
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
    }
    
    public ViewerIcon(int paramInt1, int paramInt2, URL paramURL)
    {
      this._width = paramInt1;
      this._height = paramInt2;
      if (paramURL != null) {
        CacheImageLoader.getInstance().loadImage(paramURL, this);
      }
    }
    
    public void imageAvailable(IconDesc paramIconDesc, Image paramImage, File paramFile)
    {
      int i = paramImage.getWidth(null);
      int j = paramImage.getHeight(null);
      Image localImage = paramImage;
      new Thread(new Runnable()
      {
        private final Image val$imageIn;
        private final int val$w;
        private final int val$h;
        
        public void run()
        {
          Image localImage = this.val$imageIn;
          if ((CacheObject.ViewerIcon.this._width > 0) && (CacheObject.ViewerIcon.this._height > 0) && ((CacheObject.ViewerIcon.this._width != this.val$w) || (CacheObject.ViewerIcon.this._height != this.val$h))) {
            localImage = this.val$imageIn.getScaledInstance(CacheObject.ViewerIcon.this._width, CacheObject.ViewerIcon.this._height, 1);
          }
          CacheObject.ViewerIcon.this.setImage(localImage);
          CacheObject.this._model.fireTableDataChanged();
        }
      }).start();
    }
    
    public void finalImageAvailable(IconDesc paramIconDesc, Image paramImage, File paramFile) {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\ui\CacheObject.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */