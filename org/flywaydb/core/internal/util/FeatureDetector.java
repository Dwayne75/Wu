package org.flywaydb.core.internal.util;

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public final class FeatureDetector
{
  private static final Log LOG = LogFactory.getLog(FeatureDetector.class);
  private ClassLoader classLoader;
  private Boolean apacheCommonsLoggingAvailable;
  private Boolean slf4jAvailable;
  private Boolean springJdbcAvailable;
  private Boolean jbossVFSv2Available;
  private Boolean jbossVFSv3Available;
  private Boolean osgiFrameworkAvailable;
  private Boolean androidAvailable;
  
  public FeatureDetector(ClassLoader classLoader)
  {
    this.classLoader = classLoader;
  }
  
  public boolean isApacheCommonsLoggingAvailable()
  {
    if (this.apacheCommonsLoggingAvailable == null) {
      this.apacheCommonsLoggingAvailable = Boolean.valueOf(ClassUtils.isPresent("org.apache.commons.logging.Log", this.classLoader));
    }
    return this.apacheCommonsLoggingAvailable.booleanValue();
  }
  
  public boolean isSlf4jAvailable()
  {
    if (this.slf4jAvailable == null) {
      this.slf4jAvailable = Boolean.valueOf(ClassUtils.isPresent("org.slf4j.Logger", this.classLoader));
    }
    return this.slf4jAvailable.booleanValue();
  }
  
  public boolean isSpringJdbcAvailable()
  {
    if (this.springJdbcAvailable == null)
    {
      this.springJdbcAvailable = Boolean.valueOf(ClassUtils.isPresent("org.springframework.jdbc.core.JdbcTemplate", this.classLoader));
      LOG.debug("Spring Jdbc available: " + this.springJdbcAvailable);
    }
    return this.springJdbcAvailable.booleanValue();
  }
  
  public boolean isJBossVFSv2Available()
  {
    if (this.jbossVFSv2Available == null)
    {
      this.jbossVFSv2Available = Boolean.valueOf(ClassUtils.isPresent("org.jboss.virtual.VFS", this.classLoader));
      LOG.debug("JBoss VFS v2 available: " + this.jbossVFSv2Available);
    }
    return this.jbossVFSv2Available.booleanValue();
  }
  
  public boolean isJBossVFSv3Available()
  {
    if (this.jbossVFSv3Available == null)
    {
      this.jbossVFSv3Available = Boolean.valueOf(ClassUtils.isPresent("org.jboss.vfs.VFS", this.classLoader));
      LOG.debug("JBoss VFS v3 available: " + this.jbossVFSv3Available);
    }
    return this.jbossVFSv3Available.booleanValue();
  }
  
  public boolean isOsgiFrameworkAvailable()
  {
    if (this.osgiFrameworkAvailable == null)
    {
      this.osgiFrameworkAvailable = Boolean.valueOf(ClassUtils.isPresent("org.osgi.framework.Bundle", FeatureDetector.class.getClassLoader()));
      LOG.debug("OSGi framework available: " + this.osgiFrameworkAvailable);
    }
    return this.osgiFrameworkAvailable.booleanValue();
  }
  
  public boolean isAndroidAvailable()
  {
    if (this.androidAvailable == null) {
      this.androidAvailable = Boolean.valueOf("Android Runtime".equals(System.getProperty("java.runtime.name")));
    }
    return this.androidAvailable.booleanValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\FeatureDetector.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */