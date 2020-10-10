package com.sun.xml.bind.v2.bytecode;

import com.sun.xml.bind.Util;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ClassTailor
{
  private static final Logger logger = Util.getClassLogger();
  
  public static String toVMClassName(Class c)
  {
    assert (!c.isPrimitive());
    if (c.isArray()) {
      return toVMTypeName(c);
    }
    return c.getName().replace('.', '/');
  }
  
  public static String toVMTypeName(Class c)
  {
    if (c.isArray()) {
      return '[' + toVMTypeName(c.getComponentType());
    }
    if (c.isPrimitive())
    {
      if (c == Boolean.TYPE) {
        return "Z";
      }
      if (c == Character.TYPE) {
        return "C";
      }
      if (c == Byte.TYPE) {
        return "B";
      }
      if (c == Double.TYPE) {
        return "D";
      }
      if (c == Float.TYPE) {
        return "F";
      }
      if (c == Integer.TYPE) {
        return "I";
      }
      if (c == Long.TYPE) {
        return "J";
      }
      if (c == Short.TYPE) {
        return "S";
      }
      throw new IllegalArgumentException(c.getName());
    }
    return 'L' + c.getName().replace('.', '/') + ';';
  }
  
  public static byte[] tailor(Class templateClass, String newClassName, String... replacements)
  {
    String vmname = toVMClassName(templateClass);
    return tailor(templateClass.getClassLoader().getResourceAsStream(vmname + ".class"), vmname, newClassName, replacements);
  }
  
  public static byte[] tailor(InputStream image, String templateClassName, String newClassName, String... replacements)
  {
    DataInputStream in = new DataInputStream(image);
    try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
      DataOutputStream out = new DataOutputStream(baos);
      
      long l = in.readLong();
      out.writeLong(l);
      
      short count = in.readShort();
      out.writeShort(count);
      for (int i = 0; i < count; i++)
      {
        byte tag = in.readByte();
        out.writeByte(tag);
        switch (tag)
        {
        case 0: 
          break;
        case 1: 
          String value = in.readUTF();
          if (value.equals(templateClassName)) {
            value = newClassName;
          } else {
            for (int j = 0; j < replacements.length; j += 2) {
              if (value.equals(replacements[j]))
              {
                value = replacements[(j + 1)];
                break;
              }
            }
          }
          out.writeUTF(value);
          
          break;
        case 3: 
        case 4: 
          out.writeInt(in.readInt());
          break;
        case 5: 
        case 6: 
          i++;
          out.writeLong(in.readLong());
          break;
        case 7: 
        case 8: 
          out.writeShort(in.readShort());
          break;
        case 9: 
        case 10: 
        case 11: 
        case 12: 
          out.writeInt(in.readInt());
          break;
        case 2: 
        default: 
          throw new IllegalArgumentException("Unknown constant type " + tag);
        }
      }
      byte[] buf = new byte['Ȁ'];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
      
      return baos.toByteArray();
    }
    catch (IOException e)
    {
      logger.log(Level.WARNING, "failed to tailor", e);
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\bytecode\ClassTailor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */