package org.intellij.lang.annotations;

import java.lang.annotation.Annotation;

@Pattern("(?:[^%]|%%|(?:%(?:\\d+\\$)?(?:[-#+ 0,(<]*)?(?:\\d+)?(?:\\.\\d+)?(?:[tT])?(?:[a-zA-Z%])))*")
public @interface PrintFormat {}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\intellij\lang\annotations\PrintFormat.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */