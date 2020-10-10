package com.sun.javaws.cache;

import com.sun.deploy.config.Config;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.javaws.Globals;
import com.sun.javaws.JavawsFactory;
import com.sun.javaws.exceptions.BadJARFileException;
import com.sun.javaws.exceptions.BadMimeTypeResponseException;
import com.sun.javaws.exceptions.BadVersionResponseException;
import com.sun.javaws.exceptions.ErrorCodeResponseException;
import com.sun.javaws.exceptions.FailedDownloadingResourceException;
import com.sun.javaws.exceptions.InvalidJarDiffException;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.exceptions.MissingVersionResponseException;
import com.sun.javaws.jardiff.JarDiffPatcher;
import com.sun.javaws.net.CanceledDownloadException;
import com.sun.javaws.net.HttpDownload;
import com.sun.javaws.net.HttpDownloadListener;
import com.sun.javaws.net.HttpRequest;
import com.sun.javaws.net.HttpResponse;
import com.sun.javaws.security.SigningInfo;
import com.sun.javaws.util.VersionID;
import com.sun.javaws.util.VersionString;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

public class DownloadProtocol
{
  public static final int JAR_DOWNLOAD = 0;
  public static final int NATIVE_DOWNLOAD = 1;
  public static final int IMAGE_DOWNLOAD = 2;
  public static final int APPLICATION_JNLP_DOWNLOAD = 3;
  public static final int EXTENSION_JNLP_DOWNLOAD = 4;
  private static final String JNLP_MIME_TYPE = "application/x-java-jnlp-file";
  private static final String ERROR_MIME_TYPE = "application/x-java-jnlp-error";
  private static final String JAR_MIME_TYPE = "application/x-java-archive";
  private static final String JARDIFF_MIME_TYPE = "application/x-java-archive-diff";
  private static final String GIF_MIME_TYPE = "image/gif";
  private static final String JPEG_MIME_TYPE = "image/jpeg";
  private static final String ARG_ARCH = "arch";
  private static final String ARG_OS = "os";
  private static final String ARG_LOCALE = "locale";
  private static final String ARG_VERSION_ID = "version-id";
  private static final String ARG_CURRENT_VERSION_ID = "current-version-id";
  private static final String ARG_PLATFORM_VERSION_ID = "platform-version-id";
  private static final String ARG_KNOWN_PLATFORMS = "known-platforms";
  private static final String REPLY_JNLP_VERSION = "x-java-jnlp-version-id";
  
  static class DownloadInfo
  {
    private URL _location;
    private String _version;
    private int _kind;
    private boolean _isCacheOk;
    private String _knownPlatforms = null;
    private boolean _isPlatformVersion = false;
    
    public DownloadInfo(URL paramURL, String paramString, int paramInt, boolean paramBoolean)
    {
      this._location = paramURL;
      this._version = paramString;
      this._kind = paramInt;
      this._isCacheOk = paramBoolean;
    }
    
    public DownloadInfo(URL paramURL, String paramString1, boolean paramBoolean1, String paramString2, boolean paramBoolean2)
    {
      this._location = paramURL;
      this._version = paramString1;
      this._kind = 4;
      this._isCacheOk = paramBoolean1;
      this._knownPlatforms = paramString2;
      this._isPlatformVersion = paramBoolean2;
    }
    
    URL getLocation()
    {
      return this._location;
    }
    
    String getVersion()
    {
      return this._version;
    }
    
    int getKind()
    {
      return this._kind;
    }
    
    char getEntryType()
    {
      switch (this._kind)
      {
      case 0: 
        return 'R';
      case 2: 
        return 'R';
      case 1: 
        return 'R';
      case 3: 
        return 'A';
      case 4: 
        return 'E';
      }
      return 'a';
    }
    
    boolean isCacheOk(DiskCacheEntry paramDiskCacheEntry, boolean paramBoolean)
    {
      return (paramBoolean) && ((this._version != null) || (this._isCacheOk)) && (paramDiskCacheEntry.getTimeStamp() != 0L);
    }
    
    URL getRequestURL(DiskCacheEntry paramDiskCacheEntry)
    {
      StringBuffer localStringBuffer = new StringBuffer();
      if ((this._version != null) && (this._kind != 4))
      {
        addURLArgument(localStringBuffer, "version-id", this._version);
        if (((this._kind == 0) || (this._kind == 1)) && (paramDiskCacheEntry != null) && (paramDiskCacheEntry.getVersionId() != null)) {
          addURLArgument(localStringBuffer, "current-version-id", paramDiskCacheEntry.getVersionId());
        }
      }
      if ((this._kind == 4) && (this._version != null))
      {
        if (this._isPlatformVersion) {
          addURLArgument(localStringBuffer, "platform-version-id", this._version);
        } else {
          addURLArgument(localStringBuffer, "version-id", this._version);
        }
        addURLArgument(localStringBuffer, "arch", Config.getOSArch());
        addURLArgument(localStringBuffer, "os", Config.getOSName());
        addURLArgument(localStringBuffer, "locale", Globals.getDefaultLocaleString());
        if (this._knownPlatforms != null) {
          addURLArgument(localStringBuffer, "known-platforms", this._knownPlatforms);
        }
      }
      if (localStringBuffer.length() > 0) {
        localStringBuffer.setLength(localStringBuffer.length() - 1);
      }
      if (localStringBuffer.length() > 0) {
        localStringBuffer.insert(0, '?');
      }
      try
      {
        if ((Globals.getCodebaseOverride() != null) && (Globals.getCodebase() != null)) {
          return new URL(Globals.getCodebaseOverride() + this._location.getFile().substring(Globals.getCodebase().getFile().length()) + localStringBuffer);
        }
        return new URL(this._location.getProtocol(), this._location.getHost(), this._location.getPort(), this._location.getFile() + localStringBuffer);
      }
      catch (MalformedURLException localMalformedURLException)
      {
        Trace.ignoredException(localMalformedURLException);
      }
      return null;
    }
    
    private void addURLArgument(StringBuffer paramStringBuffer, String paramString1, String paramString2)
    {
      paramStringBuffer.append(URLEncoder.encode(paramString1));paramStringBuffer.append('=');
      paramStringBuffer.append(URLEncoder.encode(paramString2));paramStringBuffer.append('&');
    }
    
    boolean needsReplyVersion(DiskCacheEntry paramDiskCacheEntry)
    {
      return this._version != null;
    }
    
    boolean isPlatformRequest()
    {
      return this._isPlatformVersion;
    }
    
    boolean isValidMimeType(String paramString, DiskCacheEntry paramDiskCacheEntry)
    {
      if (paramString == null) {
        return false;
      }
      if ((this._kind == 0) || (this._kind == 1))
      {
        if (paramString.equalsIgnoreCase("application/x-java-archive-diff")) {
          return (paramDiskCacheEntry != null) && (paramDiskCacheEntry.getVersionId() != null);
        }
        return true;
      }
      if (this._kind == 2) {
        return (paramString.equalsIgnoreCase("image/jpeg")) || (paramString.equalsIgnoreCase("image/gif"));
      }
      return true;
    }
    
    boolean isWebNewer(DiskCacheEntry paramDiskCacheEntry, long paramLong1, long paramLong2, String paramString)
    {
      if (this._version == null) {
        return ((paramLong2 == 0L) && (paramLong1 > 0L)) || (paramLong2 > paramDiskCacheEntry.getTimeStamp());
      }
      return true;
    }
  }
  
  private static class UpdateAvailableAction
    implements DownloadProtocol.DownloadAction
  {
    UpdateAvailableAction(DownloadProtocol.1 param1)
    {
      this();
    }
    
    private boolean _result = false;
    
    public boolean getResult()
    {
      return this._result;
    }
    
    public void actionInCache(DiskCacheEntry paramDiskCacheEntry)
      throws IOException, JNLPException
    {
      this._result = false;
    }
    
    public void actionOffline(DiskCacheEntry paramDiskCacheEntry, boolean paramBoolean)
      throws IOException, JNLPException
    {
      this._result = false;
    }
    
    public boolean skipDownloadStep()
    {
      return false;
    }
    
    public void actionDownload(DiskCacheEntry paramDiskCacheEntry, DownloadProtocol.DownloadInfo paramDownloadInfo, long paramLong, int paramInt, String paramString1, String paramString2, HttpResponse paramHttpResponse)
      throws IOException, JNLPException
    {
      this._result = true;
    }
    
    public boolean useHeadRequest()
    {
      return true;
    }
    
    private UpdateAvailableAction() {}
  }
  
  private static class IsInCacheAction
    implements DownloadProtocol.DownloadAction
  {
    IsInCacheAction(DownloadProtocol.1 param1)
    {
      this();
    }
    
    private DiskCacheEntry _dce = null;
    
    public DiskCacheEntry getResult()
    {
      return this._dce;
    }
    
    public void actionInCache(DiskCacheEntry paramDiskCacheEntry)
      throws IOException, JNLPException
    {
      this._dce = paramDiskCacheEntry;
    }
    
    public void actionOffline(DiskCacheEntry paramDiskCacheEntry, boolean paramBoolean)
      throws IOException, JNLPException
    {
      this._dce = (paramBoolean ? paramDiskCacheEntry : null);
    }
    
    public boolean skipDownloadStep()
    {
      return true;
    }
    
    public boolean useHeadRequest()
    {
      return false;
    }
    
    private IsInCacheAction() {}
    
    public void actionDownload(DiskCacheEntry paramDiskCacheEntry, DownloadProtocol.DownloadInfo paramDownloadInfo, long paramLong, int paramInt, String paramString1, String paramString2, HttpResponse paramHttpResponse)
      throws IOException, JNLPException
    {}
  }
  
  private static class DownloadSizeAction
    implements DownloadProtocol.DownloadAction
  {
    DownloadSizeAction(DownloadProtocol.1 param1)
    {
      this();
    }
    
    private long _result = -1L;
    
    public long getResult()
    {
      return this._result;
    }
    
    public void actionInCache(DiskCacheEntry paramDiskCacheEntry)
      throws IOException, JNLPException
    {
      this._result = 0L;
    }
    
    public void actionOffline(DiskCacheEntry paramDiskCacheEntry, boolean paramBoolean)
      throws IOException, JNLPException
    {
      this._result = (paramBoolean ? 0L : -1L);
    }
    
    public boolean skipDownloadStep()
    {
      return false;
    }
    
    public void actionDownload(DiskCacheEntry paramDiskCacheEntry, DownloadProtocol.DownloadInfo paramDownloadInfo, long paramLong, int paramInt, String paramString1, String paramString2, HttpResponse paramHttpResponse)
      throws IOException, JNLPException
    {
      this._result = paramInt;
    }
    
    public boolean useHeadRequest()
    {
      return true;
    }
    
    private DownloadSizeAction() {}
  }
  
  private static class RetrieveAction
    implements DownloadProtocol.DownloadAction
  {
    private DiskCacheEntry _result = null;
    private DownloadProtocol.DownloadDelegate _delegate = null;
    
    public DiskCacheEntry getResult()
    {
      return this._result;
    }
    
    public RetrieveAction(DownloadProtocol.DownloadDelegate paramDownloadDelegate)
    {
      this._delegate = paramDownloadDelegate;
    }
    
    public void actionInCache(DiskCacheEntry paramDiskCacheEntry)
      throws IOException, JNLPException
    {
      this._result = paramDiskCacheEntry;
    }
    
    public void actionOffline(DiskCacheEntry paramDiskCacheEntry, boolean paramBoolean)
      throws IOException, JNLPException
    {
      this._result = (paramBoolean ? paramDiskCacheEntry : null);
    }
    
    public boolean skipDownloadStep()
    {
      return false;
    }
    
    public void actionDownload(DiskCacheEntry paramDiskCacheEntry, DownloadProtocol.DownloadInfo paramDownloadInfo, long paramLong, int paramInt, String paramString1, String paramString2, HttpResponse paramHttpResponse)
      throws IOException, JNLPException
    {
      URL localURL = paramDownloadInfo.getLocation();
      
      boolean bool = paramString2.equalsIgnoreCase("application/x-java-archive-diff");
      String str1 = paramDownloadInfo.getVersion();
      String str2 = str1 != null ? paramString1 : null;
      
      Trace.println("Doing download", TraceLevel.NETWORK);
      
      HttpDownloadListener local1 = this._delegate == null ? null : new HttpDownloadListener()
      {
        private final URL val$location;
        private final String val$responseVersion;
        private final boolean val$willPatch;
        
        public boolean downloadProgress(int paramAnonymousInt1, int paramAnonymousInt2)
        {
          DownloadProtocol.RetrieveAction.this._delegate.downloading(this.val$location, this.val$responseVersion, paramAnonymousInt1, paramAnonymousInt2, this.val$willPatch);
          
          return true;
        }
      };
      File localFile1 = null;
      try
      {
        localFile1 = Cache.getTempCacheFile(localURL, str2);
        JavawsFactory.getHttpDownloadImpl().download(paramHttpResponse, localFile1, local1);
      }
      catch (IOException localIOException)
      {
        Trace.println("Got exception while downloading resource: " + localIOException, TraceLevel.NETWORK);
        if (this._delegate != null) {
          this._delegate.downloadFailed(localURL, paramString1);
        }
        throw new FailedDownloadingResourceException(localURL, paramString1, localIOException);
      }
      catch (CanceledDownloadException localCanceledDownloadException)
      {
        Trace.ignoredException(localCanceledDownloadException);
      }
      if (bool) {
        localFile1 = DownloadProtocol.applyPatch(paramDiskCacheEntry.getFile(), localFile1, localURL, paramString1, this._delegate);
      }
      if ((paramDownloadInfo.getKind() == 3) || (paramDownloadInfo.getKind() == 4) || (paramDownloadInfo.getKind() == 2))
      {
        Cache.insertEntry(paramDownloadInfo.getEntryType(), localURL, str2, localFile1, paramLong);
        
        localFile1 = null;
      }
      else if ((paramDownloadInfo.getKind() == 0) || (paramDownloadInfo.getKind() == 1))
      {
        File localFile2 = paramDownloadInfo.getKind() == 1 ? Cache.createNativeLibDir(localURL, str2) : null;
        
        JarFile localJarFile = new JarFile(localFile1);
        try
        {
          SigningInfo.checkSigning(localURL, str2, localJarFile, this._delegate, localFile2);
          
          localJarFile.close();localJarFile = null;
          Cache.insertEntry(paramDownloadInfo.getEntryType(), localURL, str2, localFile1, paramLong);
          
          localFile1 = null;
        }
        finally
        {
          if (localJarFile != null) {
            localJarFile.close();
          }
          if (localFile1 != null) {
            localFile1.delete();
          }
        }
      }
      this._result = Cache.getCacheEntry(paramDownloadInfo.getEntryType(), localURL, str2);
    }
    
    public boolean useHeadRequest()
    {
      return false;
    }
  }
  
  private static void doDownload(DownloadInfo paramDownloadInfo, DownloadAction paramDownloadAction)
    throws JNLPException
  {
    try
    {
      boolean[] arrayOfBoolean = new boolean[1];
      DiskCacheEntry localDiskCacheEntry = findBestDiskCacheEntry(paramDownloadInfo.getEntryType(), paramDownloadInfo.getLocation(), paramDownloadInfo.getVersion(), arrayOfBoolean);
      
      int i = arrayOfBoolean[0];
      if ((localDiskCacheEntry != null) && (paramDownloadInfo.isCacheOk(localDiskCacheEntry, i)))
      {
        Trace.println("Found in cache: " + localDiskCacheEntry, TraceLevel.NETWORK);
        
        paramDownloadAction.actionInCache(localDiskCacheEntry);
        return;
      }
      if (Globals.isOffline())
      {
        Trace.println("Offline mode. No Web check. Cache lookup: " + localDiskCacheEntry, TraceLevel.NETWORK);
        
        paramDownloadAction.actionOffline(localDiskCacheEntry, i);
        return;
      }
      if (paramDownloadAction.skipDownloadStep())
      {
        Trace.println("Skipping download step", TraceLevel.NETWORK);
        
        return;
      }
      URL localURL = paramDownloadInfo.getRequestURL(localDiskCacheEntry);
      
      Trace.println("Connection to: " + localURL, TraceLevel.NETWORK);
      
      HttpRequest localHttpRequest = JavawsFactory.getHttpRequestImpl();
      
      HttpResponse localHttpResponse = null;
      try
      {
        localHttpResponse = paramDownloadAction.useHeadRequest() ? localHttpRequest.doHeadRequest(localURL) : localHttpRequest.doGetRequest(localURL);
      }
      catch (IOException localIOException)
      {
        localHttpResponse = paramDownloadAction.useHeadRequest() ? localHttpRequest.doHeadRequest(localURL, false) : localHttpRequest.doGetRequest(localURL, false);
      }
      if (localHttpResponse.getStatusCode() == 404) {
        throw new FailedDownloadingResourceException(paramDownloadInfo.getLocation(), paramDownloadInfo.getVersion(), new IOException("HTTP response 404"));
      }
      int j = localHttpResponse.getContentLength();
      long l = localHttpResponse.getLastModified();
      Object localObject1 = localHttpResponse.getResponseHeader("x-java-jnlp-version-id");
      String str1 = paramDownloadInfo.getVersion();
      if ((str1 != null) && (localObject1 == null) && (Globals.getCodebaseOverride() != null) && (new VersionID(str1).isSimpleVersion())) {
        localObject1 = str1;
      }
      String str2 = localHttpResponse.getContentType();
      
      Trace.println("Sever response: (length: " + j + ", lastModified: " + new Date(l) + ", downloadVersion " + (String)localObject1 + ", mimeType: " + str2 + ")", TraceLevel.NETWORK);
      Object localObject2;
      if ((str2 != null) && (str2.equalsIgnoreCase("application/x-java-jnlp-error")))
      {
        localObject2 = localHttpResponse.getInputStream();
        
        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader((InputStream)localObject2));
        String str3 = localBufferedReader.readLine();
        throw new ErrorCodeResponseException(paramDownloadInfo.getLocation(), paramDownloadInfo.getVersion(), str3);
      }
      if (!paramDownloadInfo.isValidMimeType(str2, localDiskCacheEntry)) {
        throw new BadMimeTypeResponseException(paramDownloadInfo.getLocation(), paramDownloadInfo.getVersion(), str2);
      }
      if (paramDownloadInfo.needsReplyVersion(localDiskCacheEntry))
      {
        if (localObject1 == null) {
          throw new MissingVersionResponseException(paramDownloadInfo.getLocation(), paramDownloadInfo.getVersion());
        }
        if (!paramDownloadInfo.isPlatformRequest())
        {
          if (!new VersionString(paramDownloadInfo.getVersion()).contains((String)localObject1)) {
            throw new BadVersionResponseException(paramDownloadInfo.getLocation(), paramDownloadInfo.getVersion(), (String)localObject1);
          }
          localObject2 = new VersionID((String)localObject1);
          if (!((VersionID)localObject2).isSimpleVersion()) {
            throw new BadVersionResponseException(paramDownloadInfo.getLocation(), paramDownloadInfo.getVersion(), (String)localObject1);
          }
        }
      }
      if ((localDiskCacheEntry != null) && (!paramDownloadInfo.isWebNewer(localDiskCacheEntry, j, l, (String)localObject1)))
      {
        paramDownloadAction.actionInCache(localDiskCacheEntry);
        localHttpResponse.disconnect();
        return;
      }
      paramDownloadAction.actionDownload(localDiskCacheEntry, paramDownloadInfo, l, j, (String)localObject1, str2, localHttpResponse);
      localHttpResponse.disconnect();
    }
    catch (ZipException localZipException)
    {
      throw new BadJARFileException(paramDownloadInfo.getLocation(), paramDownloadInfo.getVersion(), localZipException);
    }
    catch (JNLPException localJNLPException)
    {
      throw localJNLPException;
    }
    catch (Exception localException)
    {
      throw new FailedDownloadingResourceException(paramDownloadInfo.getLocation(), paramDownloadInfo.getVersion(), localException);
    }
  }
  
  private static File applyPatch(File paramFile1, File paramFile2, URL paramURL, String paramString, DownloadDelegate paramDownloadDelegate)
    throws JNLPException
  {
    JarDiffPatcher localJarDiffPatcher = new JarDiffPatcher();
    
    File localFile = null;
    FileOutputStream localFileOutputStream = null;
    int i = 0;
    try
    {
      localFile = Cache.getTempCacheFile(paramURL, paramString);
      localFileOutputStream = new FileOutputStream(localFile);
      Patcher.PatchDelegate local1 = null;
      if (paramDownloadDelegate != null)
      {
        paramDownloadDelegate.patching(paramURL, paramString, 0);
        local1 = new Patcher.PatchDelegate()
        {
          private final DownloadProtocol.DownloadDelegate val$delegate;
          private final URL val$location;
          private final String val$newVersionId;
          
          public void patching(int paramAnonymousInt)
          {
            this.val$delegate.patching(this.val$location, this.val$newVersionId, paramAnonymousInt);
          }
        };
      }
      try
      {
        localJarDiffPatcher.applyPatch(local1, paramFile1.getPath(), paramFile2.getPath(), localFileOutputStream);
      }
      catch (IOException localIOException3)
      {
        throw new InvalidJarDiffException(paramURL, paramString, localIOException3);
      }
      i = 1;
    }
    catch (IOException localIOException2)
    {
      Trace.println("Got exception while patching: " + localIOException2, TraceLevel.NETWORK);
      
      throw new FailedDownloadingResourceException(paramURL, paramString, localIOException2);
    }
    finally
    {
      try
      {
        if (localFileOutputStream != null) {
          localFileOutputStream.close();
        }
      }
      catch (IOException localIOException4)
      {
        Trace.ignoredException(localIOException4);
      }
      if (i == 0) {
        localFile.delete();
      }
      paramFile2.delete();
      if ((paramDownloadDelegate != null) && (i == 0)) {
        paramDownloadDelegate.downloadFailed(paramURL, paramString);
      }
    }
    return localFile;
  }
  
  public static DiskCacheEntry getJRE(URL paramURL, String paramString1, boolean paramBoolean, String paramString2)
    throws JNLPException
  {
    DownloadInfo localDownloadInfo = new DownloadInfo(paramURL, paramString1, false, paramString2, paramBoolean);
    RetrieveAction localRetrieveAction = new RetrieveAction(null);
    doDownload(localDownloadInfo, localRetrieveAction);
    DiskCacheEntry localDiskCacheEntry = localRetrieveAction.getResult();
    if (localDiskCacheEntry == null) {
      throw new FailedDownloadingResourceException(paramURL, paramString1, null);
    }
    return localDiskCacheEntry;
  }
  
  public static DiskCacheEntry getLaunchFile(URL paramURL, boolean paramBoolean)
    throws JNLPException
  {
    DownloadInfo localDownloadInfo = new DownloadInfo(paramURL, null, 3, false);
    RetrieveAction localRetrieveAction = new RetrieveAction(null);
    doDownload(localDownloadInfo, localRetrieveAction);
    DiskCacheEntry localDiskCacheEntry = localRetrieveAction.getResult();
    if (localDiskCacheEntry == null) {
      throw new FailedDownloadingResourceException(paramURL, null, null);
    }
    return localDiskCacheEntry;
  }
  
  public static DiskCacheEntry getCachedLaunchedFile(URL paramURL)
    throws JNLPException
  {
    DownloadInfo localDownloadInfo = new DownloadInfo(paramURL, null, 3, true);
    IsInCacheAction localIsInCacheAction = new IsInCacheAction(null);
    doDownload(localDownloadInfo, localIsInCacheAction);
    DiskCacheEntry localDiskCacheEntry = localIsInCacheAction.getResult();
    return localDiskCacheEntry;
  }
  
  public static boolean isLaunchFileUpdateAvailable(URL paramURL)
    throws JNLPException
  {
    if (Globals.isOffline()) {
      return false;
    }
    DownloadInfo localDownloadInfo = new DownloadInfo(paramURL, null, 3, false);
    UpdateAvailableAction localUpdateAvailableAction = new UpdateAvailableAction(null);
    doDownload(localDownloadInfo, localUpdateAvailableAction);
    
    return localUpdateAvailableAction.getResult();
  }
  
  public static DiskCacheEntry getExtension(URL paramURL, String paramString1, String paramString2, boolean paramBoolean)
    throws JNLPException
  {
    DownloadInfo localDownloadInfo = new DownloadInfo(paramURL, paramString1, paramBoolean, paramString2, false);
    RetrieveAction localRetrieveAction = new RetrieveAction(null);
    doDownload(localDownloadInfo, localRetrieveAction);
    DiskCacheEntry localDiskCacheEntry = localRetrieveAction.getResult();
    if (localDiskCacheEntry == null) {
      throw new FailedDownloadingResourceException(paramURL, paramString1, null);
    }
    return localDiskCacheEntry;
  }
  
  public static DiskCacheEntry getCachedExtension(URL paramURL, String paramString1, String paramString2)
    throws JNLPException
  {
    DownloadInfo localDownloadInfo = new DownloadInfo(paramURL, paramString1, true, paramString2, false);
    IsInCacheAction localIsInCacheAction = new IsInCacheAction(null);
    doDownload(localDownloadInfo, localIsInCacheAction);
    DiskCacheEntry localDiskCacheEntry = localIsInCacheAction.getResult();
    return localDiskCacheEntry;
  }
  
  public static boolean isExtensionUpdateAvailable(URL paramURL, String paramString1, String paramString2)
    throws JNLPException
  {
    if (Globals.isOffline()) {
      return false;
    }
    DownloadInfo localDownloadInfo = new DownloadInfo(paramURL, paramString1, false, paramString2, false);
    UpdateAvailableAction localUpdateAvailableAction = new UpdateAvailableAction(null);
    doDownload(localDownloadInfo, localUpdateAvailableAction);
    return localUpdateAvailableAction.getResult();
  }
  
  public static DiskCacheEntry getResource(URL paramURL, String paramString, int paramInt, boolean paramBoolean, DownloadDelegate paramDownloadDelegate)
    throws JNLPException
  {
    DownloadInfo localDownloadInfo = new DownloadInfo(paramURL, paramString, paramInt, paramBoolean);
    RetrieveAction localRetrieveAction = new RetrieveAction(paramDownloadDelegate);
    doDownload(localDownloadInfo, localRetrieveAction);
    DiskCacheEntry localDiskCacheEntry = localRetrieveAction.getResult();
    if (localDiskCacheEntry == null) {
      throw new FailedDownloadingResourceException(paramURL, paramString, null);
    }
    return localDiskCacheEntry;
  }
  
  public static boolean isInCache(URL paramURL, String paramString, int paramInt)
  {
    return getCachedVersion(paramURL, paramString, paramInt) != null;
  }
  
  public static long getCachedSize(URL paramURL, String paramString, int paramInt)
  {
    DiskCacheEntry localDiskCacheEntry = getCachedVersion(paramURL, paramString, paramInt);
    return localDiskCacheEntry != null ? localDiskCacheEntry.getSize() : 0L;
  }
  
  public static DiskCacheEntry getCachedVersion(URL paramURL, String paramString, int paramInt)
  {
    try
    {
      DownloadInfo localDownloadInfo = new DownloadInfo(paramURL, paramString, paramInt, true);
      IsInCacheAction localIsInCacheAction = new IsInCacheAction(null);
      doDownload(localDownloadInfo, localIsInCacheAction);
      
      return localIsInCacheAction.getResult();
    }
    catch (JNLPException localJNLPException)
    {
      Trace.ignoredException(localJNLPException);
    }
    return null;
  }
  
  public static boolean isUpdateAvailable(URL paramURL, String paramString, int paramInt)
    throws JNLPException
  {
    if (Globals.isOffline()) {
      return false;
    }
    DownloadInfo localDownloadInfo = new DownloadInfo(paramURL, paramString, paramInt, false);
    UpdateAvailableAction localUpdateAvailableAction = new UpdateAvailableAction(null);
    doDownload(localDownloadInfo, localUpdateAvailableAction);
    return localUpdateAvailableAction.getResult();
  }
  
  public static long getDownloadSize(URL paramURL, String paramString, int paramInt)
    throws JNLPException
  {
    DownloadInfo localDownloadInfo = new DownloadInfo(paramURL, paramString, paramInt, false);
    DownloadSizeAction localDownloadSizeAction = new DownloadSizeAction(null);
    doDownload(localDownloadInfo, localDownloadSizeAction);
    return localDownloadSizeAction.getResult();
  }
  
  private static DiskCacheEntry findBestDiskCacheEntry(char paramChar, URL paramURL, String paramString, boolean[] paramArrayOfBoolean)
    throws IOException
  {
    if (paramString == null)
    {
      paramArrayOfBoolean[0] = true;
      return Cache.getCacheEntry(paramChar, paramURL, null);
    }
    VersionString localVersionString = new VersionString(paramString);
    if (localVersionString.isSimpleVersion())
    {
      localDiskCacheEntry1 = Cache.getCacheEntry(paramChar, paramURL, paramString);
      if (localDiskCacheEntry1 != null)
      {
        paramArrayOfBoolean[0] = true;
        return localDiskCacheEntry1;
      }
    }
    DiskCacheEntry localDiskCacheEntry1 = null;
    DiskCacheEntry localDiskCacheEntry2 = null;
    
    String[] arrayOfString = Cache.getCacheVersions(paramChar, paramURL);
    for (int i = 0; i < arrayOfString.length; i++)
    {
      if (localVersionString.contains(arrayOfString[i]))
      {
        localDiskCacheEntry2 = arrayOfString[i];
        break;
      }
      if ((localVersionString.containsGreaterThan(arrayOfString[i])) && 
        (localDiskCacheEntry1 == null)) {
        localDiskCacheEntry1 = arrayOfString[i];
      }
    }
    if (localDiskCacheEntry2 == null)
    {
      paramArrayOfBoolean[0] = false;
      if (localDiskCacheEntry1 == null) {
        return null;
      }
      localDiskCacheEntry2 = localDiskCacheEntry1;
    }
    else
    {
      paramArrayOfBoolean[0] = true;
    }
    return Cache.getCacheEntry(paramChar, paramURL, localDiskCacheEntry2);
  }
  
  private static abstract interface DownloadAction
  {
    public abstract void actionInCache(DiskCacheEntry paramDiskCacheEntry)
      throws IOException, JNLPException;
    
    public abstract void actionOffline(DiskCacheEntry paramDiskCacheEntry, boolean paramBoolean)
      throws IOException, JNLPException;
    
    public abstract boolean skipDownloadStep();
    
    public abstract void actionDownload(DiskCacheEntry paramDiskCacheEntry, DownloadProtocol.DownloadInfo paramDownloadInfo, long paramLong, int paramInt, String paramString1, String paramString2, HttpResponse paramHttpResponse)
      throws IOException, JNLPException;
    
    public abstract boolean useHeadRequest();
  }
  
  public static abstract interface DownloadDelegate
  {
    public abstract void downloading(URL paramURL, String paramString, int paramInt1, int paramInt2, boolean paramBoolean);
    
    public abstract void validating(URL paramURL, int paramInt1, int paramInt2);
    
    public abstract void patching(URL paramURL, String paramString, int paramInt);
    
    public abstract void downloadFailed(URL paramURL, String paramString);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\cache\DownloadProtocol.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */