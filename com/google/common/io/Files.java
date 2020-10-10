package com.google.common.io;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Beta
public final class Files
{
  private static final int TEMP_DIR_ATTEMPTS = 10000;
  
  public static BufferedReader newReader(File file, Charset charset)
    throws FileNotFoundException
  {
    Preconditions.checkNotNull(file);
    Preconditions.checkNotNull(charset);
    return new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
  }
  
  public static BufferedWriter newWriter(File file, Charset charset)
    throws FileNotFoundException
  {
    Preconditions.checkNotNull(file);
    Preconditions.checkNotNull(charset);
    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
  }
  
  public static ByteSource asByteSource(File file)
  {
    return new FileByteSource(file, null);
  }
  
  private static final class FileByteSource
    extends ByteSource
  {
    private final File file;
    
    private FileByteSource(File file)
    {
      this.file = ((File)Preconditions.checkNotNull(file));
    }
    
    public FileInputStream openStream()
      throws IOException
    {
      return new FileInputStream(this.file);
    }
    
    public long size()
      throws IOException
    {
      if (!this.file.isFile()) {
        throw new FileNotFoundException(this.file.toString());
      }
      return this.file.length();
    }
    
    public byte[] read()
      throws IOException
    {
      Closer closer = Closer.create();
      try
      {
        FileInputStream in = (FileInputStream)closer.register(openStream());
        return Files.readFile(in, in.getChannel().size());
      }
      catch (Throwable e)
      {
        throw closer.rethrow(e);
      }
      finally
      {
        closer.close();
      }
    }
    
    public String toString()
    {
      String str = String.valueOf(String.valueOf(this.file));return 20 + str.length() + "Files.asByteSource(" + str + ")";
    }
  }
  
  static byte[] readFile(InputStream in, long expectedSize)
    throws IOException
  {
    if (expectedSize > 2147483647L)
    {
      long l = expectedSize;throw new OutOfMemoryError(68 + "file is too large to fit in a byte array: " + l + " bytes");
    }
    return expectedSize == 0L ? ByteStreams.toByteArray(in) : ByteStreams.toByteArray(in, (int)expectedSize);
  }
  
  public static ByteSink asByteSink(File file, FileWriteMode... modes)
  {
    return new FileByteSink(file, modes, null);
  }
  
  private static final class FileByteSink
    extends ByteSink
  {
    private final File file;
    private final ImmutableSet<FileWriteMode> modes;
    
    private FileByteSink(File file, FileWriteMode... modes)
    {
      this.file = ((File)Preconditions.checkNotNull(file));
      this.modes = ImmutableSet.copyOf(modes);
    }
    
    public FileOutputStream openStream()
      throws IOException
    {
      return new FileOutputStream(this.file, this.modes.contains(FileWriteMode.APPEND));
    }
    
    public String toString()
    {
      String str1 = String.valueOf(String.valueOf(this.file));String str2 = String.valueOf(String.valueOf(this.modes));return 20 + str1.length() + str2.length() + "Files.asByteSink(" + str1 + ", " + str2 + ")";
    }
  }
  
  public static CharSource asCharSource(File file, Charset charset)
  {
    return asByteSource(file).asCharSource(charset);
  }
  
  public static CharSink asCharSink(File file, Charset charset, FileWriteMode... modes)
  {
    return asByteSink(file, modes).asCharSink(charset);
  }
  
  private static FileWriteMode[] modes(boolean append)
  {
    return append ? new FileWriteMode[] { FileWriteMode.APPEND } : new FileWriteMode[0];
  }
  
  public static byte[] toByteArray(File file)
    throws IOException
  {
    return asByteSource(file).read();
  }
  
  public static String toString(File file, Charset charset)
    throws IOException
  {
    return asCharSource(file, charset).read();
  }
  
  public static void write(byte[] from, File to)
    throws IOException
  {
    asByteSink(to, new FileWriteMode[0]).write(from);
  }
  
  public static void copy(File from, OutputStream to)
    throws IOException
  {
    asByteSource(from).copyTo(to);
  }
  
  public static void copy(File from, File to)
    throws IOException
  {
    Preconditions.checkArgument(!from.equals(to), "Source %s and destination %s must be different", new Object[] { from, to });
    
    asByteSource(from).copyTo(asByteSink(to, new FileWriteMode[0]));
  }
  
  public static void write(CharSequence from, File to, Charset charset)
    throws IOException
  {
    asCharSink(to, charset, new FileWriteMode[0]).write(from);
  }
  
  public static void append(CharSequence from, File to, Charset charset)
    throws IOException
  {
    write(from, to, charset, true);
  }
  
  private static void write(CharSequence from, File to, Charset charset, boolean append)
    throws IOException
  {
    asCharSink(to, charset, modes(append)).write(from);
  }
  
  public static void copy(File from, Charset charset, Appendable to)
    throws IOException
  {
    asCharSource(from, charset).copyTo(to);
  }
  
  public static boolean equal(File file1, File file2)
    throws IOException
  {
    Preconditions.checkNotNull(file1);
    Preconditions.checkNotNull(file2);
    if ((file1 == file2) || (file1.equals(file2))) {
      return true;
    }
    long len1 = file1.length();
    long len2 = file2.length();
    if ((len1 != 0L) && (len2 != 0L) && (len1 != len2)) {
      return false;
    }
    return asByteSource(file1).contentEquals(asByteSource(file2));
  }
  
  public static File createTempDir()
  {
    File baseDir = new File(System.getProperty("java.io.tmpdir"));
    long l = System.currentTimeMillis();String baseName = 21 + l + "-";
    for (int counter = 0; counter < 10000; counter++)
    {
      str1 = String.valueOf(String.valueOf(baseName));i = counter;tempDir = new File(baseDir, 11 + str1.length() + str1 + i);
      if (tempDir.mkdir()) {
        return tempDir;
      }
    }
    counter = String.valueOf(String.valueOf("Failed to create directory within 10000 attempts (tried "));File tempDir = String.valueOf(String.valueOf(baseName));String str1 = String.valueOf(String.valueOf(baseName));int i = 9999;throw new IllegalStateException(17 + counter.length() + tempDir.length() + str1.length() + counter + tempDir + "0 to " + str1 + i + ")");
  }
  
  public static void touch(File file)
    throws IOException
  {
    Preconditions.checkNotNull(file);
    if ((!file.createNewFile()) && (!file.setLastModified(System.currentTimeMillis())))
    {
      String str = String.valueOf(String.valueOf(file));throw new IOException(38 + str.length() + "Unable to update modification time of " + str);
    }
  }
  
  public static void createParentDirs(File file)
    throws IOException
  {
    Preconditions.checkNotNull(file);
    File parent = file.getCanonicalFile().getParentFile();
    if (parent == null) {
      return;
    }
    parent.mkdirs();
    if (!parent.isDirectory())
    {
      String str = String.valueOf(String.valueOf(file));throw new IOException(39 + str.length() + "Unable to create parent directories of " + str);
    }
  }
  
  public static void move(File from, File to)
    throws IOException
  {
    Preconditions.checkNotNull(from);
    Preconditions.checkNotNull(to);
    Preconditions.checkArgument(!from.equals(to), "Source %s and destination %s must be different", new Object[] { from, to });
    if (!from.renameTo(to))
    {
      copy(from, to);
      if (!from.delete())
      {
        if (!to.delete())
        {
          str = String.valueOf(String.valueOf(to));throw new IOException(17 + str.length() + "Unable to delete " + str);
        }
        String str = String.valueOf(String.valueOf(from));throw new IOException(17 + str.length() + "Unable to delete " + str);
      }
    }
  }
  
  public static String readFirstLine(File file, Charset charset)
    throws IOException
  {
    return asCharSource(file, charset).readFirstLine();
  }
  
  public static List<String> readLines(File file, Charset charset)
    throws IOException
  {
    (List)readLines(file, charset, new LineProcessor()
    {
      final List<String> result = Lists.newArrayList();
      
      public boolean processLine(String line)
      {
        this.result.add(line);
        return true;
      }
      
      public List<String> getResult()
      {
        return this.result;
      }
    });
  }
  
  public static <T> T readLines(File file, Charset charset, LineProcessor<T> callback)
    throws IOException
  {
    return (T)asCharSource(file, charset).readLines(callback);
  }
  
  public static <T> T readBytes(File file, ByteProcessor<T> processor)
    throws IOException
  {
    return (T)asByteSource(file).read(processor);
  }
  
  public static HashCode hash(File file, HashFunction hashFunction)
    throws IOException
  {
    return asByteSource(file).hash(hashFunction);
  }
  
  public static MappedByteBuffer map(File file)
    throws IOException
  {
    Preconditions.checkNotNull(file);
    return map(file, FileChannel.MapMode.READ_ONLY);
  }
  
  public static MappedByteBuffer map(File file, FileChannel.MapMode mode)
    throws IOException
  {
    Preconditions.checkNotNull(file);
    Preconditions.checkNotNull(mode);
    if (!file.exists()) {
      throw new FileNotFoundException(file.toString());
    }
    return map(file, mode, file.length());
  }
  
  public static MappedByteBuffer map(File file, FileChannel.MapMode mode, long size)
    throws FileNotFoundException, IOException
  {
    Preconditions.checkNotNull(file);
    Preconditions.checkNotNull(mode);
    
    Closer closer = Closer.create();
    try
    {
      RandomAccessFile raf = (RandomAccessFile)closer.register(new RandomAccessFile(file, mode == FileChannel.MapMode.READ_ONLY ? "r" : "rw"));
      
      return map(raf, mode, size);
    }
    catch (Throwable e)
    {
      throw closer.rethrow(e);
    }
    finally
    {
      closer.close();
    }
  }
  
  private static MappedByteBuffer map(RandomAccessFile raf, FileChannel.MapMode mode, long size)
    throws IOException
  {
    Closer closer = Closer.create();
    try
    {
      FileChannel channel = (FileChannel)closer.register(raf.getChannel());
      return channel.map(mode, 0L, size);
    }
    catch (Throwable e)
    {
      throw closer.rethrow(e);
    }
    finally
    {
      closer.close();
    }
  }
  
  /* Error */
  public static String simplifyPath(String pathname)
  {
    // Byte code:
    //   0: aload_0
    //   1: invokestatic 3	com/google/common/base/Preconditions:checkNotNull	(Ljava/lang/Object;)Ljava/lang/Object;
    //   4: pop
    //   5: aload_0
    //   6: invokevirtual 63	java/lang/String:length	()I
    //   9: ifne +6 -> 15
    //   12: ldc 114
    //   14: areturn
    //   15: bipush 47
    //   17: invokestatic 115	com/google/common/base/Splitter:on	(C)Lcom/google/common/base/Splitter;
    //   20: invokevirtual 116	com/google/common/base/Splitter:omitEmptyStrings	()Lcom/google/common/base/Splitter;
    //   23: aload_0
    //   24: invokevirtual 117	com/google/common/base/Splitter:split	(Ljava/lang/CharSequence;)Ljava/lang/Iterable;
    //   27: astore_1
    //   28: new 118	java/util/ArrayList
    //   31: dup
    //   32: invokespecial 119	java/util/ArrayList:<init>	()V
    //   35: astore_2
    //   36: aload_1
    //   37: invokeinterface 120 1 0
    //   42: astore_3
    //   43: aload_3
    //   44: invokeinterface 121 1 0
    //   49: ifeq +113 -> 162
    //   52: aload_3
    //   53: invokeinterface 122 1 0
    //   58: checkcast 123	java/lang/String
    //   61: astore 4
    //   63: aload 4
    //   65: ldc 114
    //   67: invokevirtual 124	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   70: ifeq +6 -> 76
    //   73: goto -30 -> 43
    //   76: aload 4
    //   78: ldc 125
    //   80: invokevirtual 124	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   83: ifeq +67 -> 150
    //   86: aload_2
    //   87: invokeinterface 126 1 0
    //   92: ifle +46 -> 138
    //   95: aload_2
    //   96: aload_2
    //   97: invokeinterface 126 1 0
    //   102: iconst_1
    //   103: isub
    //   104: invokeinterface 127 2 0
    //   109: checkcast 123	java/lang/String
    //   112: ldc 125
    //   114: invokevirtual 124	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   117: ifne +21 -> 138
    //   120: aload_2
    //   121: aload_2
    //   122: invokeinterface 126 1 0
    //   127: iconst_1
    //   128: isub
    //   129: invokeinterface 128 2 0
    //   134: pop
    //   135: goto +24 -> 159
    //   138: aload_2
    //   139: ldc 125
    //   141: invokeinterface 129 2 0
    //   146: pop
    //   147: goto +12 -> 159
    //   150: aload_2
    //   151: aload 4
    //   153: invokeinterface 129 2 0
    //   158: pop
    //   159: goto -116 -> 43
    //   162: bipush 47
    //   164: invokestatic 130	com/google/common/base/Joiner:on	(C)Lcom/google/common/base/Joiner;
    //   167: aload_2
    //   168: invokevirtual 131	com/google/common/base/Joiner:join	(Ljava/lang/Iterable;)Ljava/lang/String;
    //   171: astore_3
    //   172: aload_0
    //   173: iconst_0
    //   174: invokevirtual 132	java/lang/String:charAt	(I)C
    //   177: bipush 47
    //   179: if_icmpne +32 -> 211
    //   182: ldc -123
    //   184: aload_3
    //   185: invokestatic 62	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   188: dup
    //   189: invokevirtual 63	java/lang/String:length	()I
    //   192: ifeq +9 -> 201
    //   195: invokevirtual 134	java/lang/String:concat	(Ljava/lang/String;)Ljava/lang/String;
    //   198: goto +12 -> 210
    //   201: pop
    //   202: new 123	java/lang/String
    //   205: dup_x1
    //   206: swap
    //   207: invokespecial 135	java/lang/String:<init>	(Ljava/lang/String;)V
    //   210: astore_3
    //   211: aload_3
    //   212: ldc -120
    //   214: invokevirtual 137	java/lang/String:startsWith	(Ljava/lang/String;)Z
    //   217: ifeq +12 -> 229
    //   220: aload_3
    //   221: iconst_3
    //   222: invokevirtual 138	java/lang/String:substring	(I)Ljava/lang/String;
    //   225: astore_3
    //   226: goto -15 -> 211
    //   229: aload_3
    //   230: ldc -117
    //   232: invokevirtual 124	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   235: ifeq +9 -> 244
    //   238: ldc -123
    //   240: astore_3
    //   241: goto +15 -> 256
    //   244: ldc -116
    //   246: aload_3
    //   247: invokevirtual 124	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   250: ifeq +6 -> 256
    //   253: ldc 114
    //   255: astore_3
    //   256: aload_3
    //   257: areturn
    // Line number table:
    //   Java source line #719	-> byte code offset #0
    //   Java source line #720	-> byte code offset #5
    //   Java source line #721	-> byte code offset #12
    //   Java source line #725	-> byte code offset #15
    //   Java source line #727	-> byte code offset #28
    //   Java source line #730	-> byte code offset #36
    //   Java source line #731	-> byte code offset #63
    //   Java source line #732	-> byte code offset #73
    //   Java source line #733	-> byte code offset #76
    //   Java source line #734	-> byte code offset #86
    //   Java source line #735	-> byte code offset #120
    //   Java source line #737	-> byte code offset #138
    //   Java source line #740	-> byte code offset #150
    //   Java source line #742	-> byte code offset #159
    //   Java source line #745	-> byte code offset #162
    //   Java source line #746	-> byte code offset #172
    //   Java source line #747	-> byte code offset #182
    //   Java source line #750	-> byte code offset #211
    //   Java source line #751	-> byte code offset #220
    //   Java source line #753	-> byte code offset #229
    //   Java source line #754	-> byte code offset #238
    //   Java source line #755	-> byte code offset #244
    //   Java source line #756	-> byte code offset #253
    //   Java source line #759	-> byte code offset #256
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	258	0	pathname	String
    //   28	230	1	components	Iterable<String>
    //   36	222	2	path	List<String>
    //   43	119	3	i$	java.util.Iterator
    //   172	86	3	result	String
    //   63	96	4	component	String
  }
  
  public static String getFileExtension(String fullName)
  {
    Preconditions.checkNotNull(fullName);
    String fileName = new File(fullName).getName();
    int dotIndex = fileName.lastIndexOf('.');
    return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
  }
  
  public static String getNameWithoutExtension(String file)
  {
    Preconditions.checkNotNull(file);
    String fileName = new File(file).getName();
    int dotIndex = fileName.lastIndexOf('.');
    return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
  }
  
  public static TreeTraverser<File> fileTreeTraverser()
  {
    return FILE_TREE_TRAVERSER;
  }
  
  private static final TreeTraverser<File> FILE_TREE_TRAVERSER = new TreeTraverser()
  {
    public Iterable<File> children(File file)
    {
      if (file.isDirectory())
      {
        File[] files = file.listFiles();
        if (files != null) {
          return Collections.unmodifiableList(Arrays.asList(files));
        }
      }
      return Collections.emptyList();
    }
    
    public String toString()
    {
      return "Files.fileTreeTraverser()";
    }
  };
  
  public static Predicate<File> isDirectory()
  {
    return FilePredicate.IS_DIRECTORY;
  }
  
  public static Predicate<File> isFile()
  {
    return FilePredicate.IS_FILE;
  }
  
  private static abstract enum FilePredicate
    implements Predicate<File>
  {
    IS_DIRECTORY,  IS_FILE;
    
    private FilePredicate() {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\io\Files.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */