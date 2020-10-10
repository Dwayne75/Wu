package com.sun.xml.bind.api;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.bind.v2.ContextFactory;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

public abstract class JAXBRIContext
  extends JAXBContext
{
  public static final String DEFAULT_NAMESPACE_REMAP = "com.sun.xml.bind.defaultNamespaceRemap";
  public static final String TYPE_REFERENCES = "com.sun.xml.bind.typeReferences";
  public static final String CANONICALIZATION_SUPPORT = "com.sun.xml.bind.c14n";
  public static final String TREAT_EVERYTHING_NILLABLE = "com.sun.xml.bind.treatEverythingNillable";
  
  public static JAXBRIContext newInstance(@NotNull Class[] classes, @Nullable Collection<TypeReference> typeRefs, @Nullable Map<Class, Class> subclassReplacements, @Nullable String defaultNamespaceRemap, boolean c14nSupport, @Nullable RuntimeAnnotationReader ar)
    throws JAXBException
  {
    return ContextFactory.createContext(classes, typeRefs, subclassReplacements, defaultNamespaceRemap, c14nSupport, ar, false, false);
  }
  
  /**
   * @deprecated
   */
  public static JAXBRIContext newInstance(@NotNull Class[] classes, @Nullable Collection<TypeReference> typeRefs, @Nullable String defaultNamespaceRemap, boolean c14nSupport)
    throws JAXBException
  {
    return newInstance(classes, typeRefs, Collections.emptyMap(), defaultNamespaceRemap, c14nSupport, null);
  }
  
  public abstract boolean hasSwaRef();
  
  @Nullable
  public abstract QName getElementName(@NotNull Object paramObject)
    throws JAXBException;
  
  public abstract Bridge createBridge(@NotNull TypeReference paramTypeReference);
  
  @NotNull
  public abstract BridgeContext createBridgeContext();
  
  public abstract <B, V> RawAccessor<B, V> getElementPropertyAccessor(Class<B> paramClass, String paramString1, String paramString2)
    throws JAXBException;
  
  @NotNull
  public abstract List<String> getKnownNamespaceURIs();
  
  public abstract void generateSchema(@NotNull SchemaOutputResolver paramSchemaOutputResolver)
    throws IOException;
  
  public abstract QName getTypeName(@NotNull TypeReference paramTypeReference);
  
  @NotNull
  public abstract String getBuildId();
  
  public abstract void generateEpisode(Result paramResult);
  
  @NotNull
  public static String mangleNameToVariableName(@NotNull String localName)
  {
    return NameConverter.standard.toVariableName(localName);
  }
  
  @NotNull
  public static String mangleNameToClassName(@NotNull String localName)
  {
    return NameConverter.standard.toClassName(localName);
  }
  
  @NotNull
  public static String mangleNameToPropertyName(@NotNull String localName)
  {
    return NameConverter.standard.toPropertyName(localName);
  }
  
  @Nullable
  public static Type getBaseType(@NotNull Type type, @NotNull Class baseType)
  {
    return Navigator.REFLECTION.getBaseClass(type, baseType);
  }
  
  public static final String ANNOTATION_READER = RuntimeAnnotationReader.class.getName();
  public static final String ENABLE_XOP = "com.sun.xml.bind.XOP";
  public static final String SUBCLASS_REPLACEMENTS = "com.sun.xml.bind.subclassReplacements";
  public static final String XMLACCESSORFACTORY_SUPPORT = "com.sun.xml.bind.XmlAccessorFactory";
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\api\JAXBRIContext.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */