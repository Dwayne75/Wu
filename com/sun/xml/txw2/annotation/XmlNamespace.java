package com.sun.xml.txw2.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.PACKAGE})
public @interface XmlNamespace
{
  String value();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\annotation\XmlNamespace.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */