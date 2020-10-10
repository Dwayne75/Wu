package org.apache.http.impl.client;

import java.io.IOException;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class DecompressingHttpClient
  implements HttpClient
{
  private HttpClient backend;
  private HttpRequestInterceptor acceptEncodingInterceptor;
  private HttpResponseInterceptor contentEncodingInterceptor;
  
  public DecompressingHttpClient(HttpClient backend)
  {
    this(backend, new RequestAcceptEncoding(), new ResponseContentEncoding());
  }
  
  DecompressingHttpClient(HttpClient backend, HttpRequestInterceptor requestInterceptor, HttpResponseInterceptor responseInterceptor)
  {
    this.backend = backend;
    this.acceptEncodingInterceptor = requestInterceptor;
    this.contentEncodingInterceptor = responseInterceptor;
  }
  
  public HttpParams getParams()
  {
    return this.backend.getParams();
  }
  
  public ClientConnectionManager getConnectionManager()
  {
    return this.backend.getConnectionManager();
  }
  
  public HttpResponse execute(HttpUriRequest request)
    throws IOException, ClientProtocolException
  {
    return execute(getHttpHost(request), request, (HttpContext)null);
  }
  
  HttpHost getHttpHost(HttpUriRequest request)
  {
    URI uri = request.getURI();
    return URIUtils.extractHost(uri);
  }
  
  public HttpResponse execute(HttpUriRequest request, HttpContext context)
    throws IOException, ClientProtocolException
  {
    return execute(getHttpHost(request), request, context);
  }
  
  public HttpResponse execute(HttpHost target, HttpRequest request)
    throws IOException, ClientProtocolException
  {
    return execute(target, request, (HttpContext)null);
  }
  
  /* Error */
  public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
    throws IOException, ClientProtocolException
  {
    // Byte code:
    //   0: aload_3
    //   1: ifnonnull +11 -> 12
    //   4: new 17	org/apache/http/protocol/BasicHttpContext
    //   7: dup
    //   8: invokespecial 18	org/apache/http/protocol/BasicHttpContext:<init>	()V
    //   11: astore_3
    //   12: aload_2
    //   13: instanceof 19
    //   16: ifeq +19 -> 35
    //   19: new 20	org/apache/http/impl/client/EntityEnclosingRequestWrapper
    //   22: dup
    //   23: aload_2
    //   24: checkcast 19	org/apache/http/HttpEntityEnclosingRequest
    //   27: invokespecial 21	org/apache/http/impl/client/EntityEnclosingRequestWrapper:<init>	(Lorg/apache/http/HttpEntityEnclosingRequest;)V
    //   30: astore 4
    //   32: goto +13 -> 45
    //   35: new 22	org/apache/http/impl/client/RequestWrapper
    //   38: dup
    //   39: aload_2
    //   40: invokespecial 23	org/apache/http/impl/client/RequestWrapper:<init>	(Lorg/apache/http/HttpRequest;)V
    //   43: astore 4
    //   45: aload_0
    //   46: getfield 8	org/apache/http/impl/client/DecompressingHttpClient:acceptEncodingInterceptor	Lorg/apache/http/HttpRequestInterceptor;
    //   49: aload 4
    //   51: aload_3
    //   52: invokeinterface 24 3 0
    //   57: aload_0
    //   58: getfield 7	org/apache/http/impl/client/DecompressingHttpClient:backend	Lorg/apache/http/client/HttpClient;
    //   61: aload_1
    //   62: aload 4
    //   64: aload_3
    //   65: invokeinterface 25 4 0
    //   70: astore 5
    //   72: aload_0
    //   73: getfield 9	org/apache/http/impl/client/DecompressingHttpClient:contentEncodingInterceptor	Lorg/apache/http/HttpResponseInterceptor;
    //   76: aload 5
    //   78: aload_3
    //   79: invokeinterface 26 3 0
    //   84: getstatic 27	java/lang/Boolean:TRUE	Ljava/lang/Boolean;
    //   87: aload_3
    //   88: ldc 28
    //   90: invokeinterface 29 2 0
    //   95: invokevirtual 30	java/lang/Boolean:equals	(Ljava/lang/Object;)Z
    //   98: ifeq +30 -> 128
    //   101: aload 5
    //   103: ldc 31
    //   105: invokeinterface 32 2 0
    //   110: aload 5
    //   112: ldc 33
    //   114: invokeinterface 32 2 0
    //   119: aload 5
    //   121: ldc 34
    //   123: invokeinterface 32 2 0
    //   128: aload 5
    //   130: areturn
    //   131: astore 6
    //   133: aload 5
    //   135: invokeinterface 36 1 0
    //   140: invokestatic 37	org/apache/http/util/EntityUtils:consume	(Lorg/apache/http/HttpEntity;)V
    //   143: aload 6
    //   145: athrow
    //   146: astore 6
    //   148: aload 5
    //   150: invokeinterface 36 1 0
    //   155: invokestatic 37	org/apache/http/util/EntityUtils:consume	(Lorg/apache/http/HttpEntity;)V
    //   158: aload 6
    //   160: athrow
    //   161: astore 6
    //   163: aload 5
    //   165: invokeinterface 36 1 0
    //   170: invokestatic 37	org/apache/http/util/EntityUtils:consume	(Lorg/apache/http/HttpEntity;)V
    //   173: aload 6
    //   175: athrow
    //   176: astore 4
    //   178: new 40	org/apache/http/client/ClientProtocolException
    //   181: dup
    //   182: aload 4
    //   184: invokespecial 41	org/apache/http/client/ClientProtocolException:<init>	(Ljava/lang/Throwable;)V
    //   187: athrow
    // Line number table:
    //   Java source line #129	-> byte code offset #0
    //   Java source line #131	-> byte code offset #12
    //   Java source line #132	-> byte code offset #19
    //   Java source line #134	-> byte code offset #35
    //   Java source line #136	-> byte code offset #45
    //   Java source line #137	-> byte code offset #57
    //   Java source line #139	-> byte code offset #72
    //   Java source line #140	-> byte code offset #84
    //   Java source line #141	-> byte code offset #101
    //   Java source line #142	-> byte code offset #110
    //   Java source line #143	-> byte code offset #119
    //   Java source line #145	-> byte code offset #128
    //   Java source line #146	-> byte code offset #131
    //   Java source line #147	-> byte code offset #133
    //   Java source line #148	-> byte code offset #143
    //   Java source line #149	-> byte code offset #146
    //   Java source line #150	-> byte code offset #148
    //   Java source line #151	-> byte code offset #158
    //   Java source line #152	-> byte code offset #161
    //   Java source line #153	-> byte code offset #163
    //   Java source line #154	-> byte code offset #173
    //   Java source line #156	-> byte code offset #176
    //   Java source line #157	-> byte code offset #178
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	188	0	this	DecompressingHttpClient
    //   0	188	1	target	HttpHost
    //   0	188	2	request	HttpRequest
    //   0	188	3	context	HttpContext
    //   30	3	4	wrapped	HttpRequest
    //   43	20	4	wrapped	HttpRequest
    //   176	7	4	e	org.apache.http.HttpException
    //   70	94	5	response	HttpResponse
    //   131	13	6	ex	org.apache.http.HttpException
    //   146	13	6	ex	IOException
    //   161	13	6	ex	RuntimeException
    // Exception table:
    //   from	to	target	type
    //   72	130	131	org/apache/http/HttpException
    //   72	130	146	java/io/IOException
    //   72	130	161	java/lang/RuntimeException
    //   0	130	176	org/apache/http/HttpException
    //   131	176	176	org/apache/http/HttpException
  }
  
  public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler)
    throws IOException, ClientProtocolException
  {
    return (T)execute(getHttpHost(request), request, responseHandler);
  }
  
  public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
    throws IOException, ClientProtocolException
  {
    return (T)execute(getHttpHost(request), request, responseHandler, context);
  }
  
  public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler)
    throws IOException, ClientProtocolException
  {
    return (T)execute(target, request, responseHandler, null);
  }
  
  public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
    throws IOException, ClientProtocolException
  {
    HttpResponse response = execute(target, request, context);
    try
    {
      HttpEntity entity;
      return (T)responseHandler.handleResponse(response);
    }
    finally
    {
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        EntityUtils.consume(entity);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\client\DecompressingHttpClient.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */