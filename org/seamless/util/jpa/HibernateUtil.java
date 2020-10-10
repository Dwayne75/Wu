package org.seamless.util.jpa;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil
{
  public static final Configuration configuration;
  public static final SessionFactory sessionFactory;
  
  static
  {
    try
    {
      configuration = new Configuration().configure();
      sessionFactory = configuration.buildSessionFactory();
    }
    catch (Throwable t)
    {
      throw new RuntimeException(t);
    }
  }
  
  public static Configuration getConfiguration()
  {
    return configuration;
  }
  
  public static SessionFactory getSessionFactory()
  {
    return sessionFactory;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\jpa\HibernateUtil.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */