package com.lianyubo.utils;

import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.*;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * HttpClient工具类
 */

public class HttpClientUtil {
	private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static CookieStore cookieStore = new BasicCookieStore();
	private static CloseableHttpClient httpClient;
	private final static String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.110 Safari/537.36";
	private static HttpHost proxy;
	private static RequestConfig requestConfig;

	static{
		init();
	}
	private static void init() {
        try {
            SSLContext sslContext =
                    SSLContexts.custom()
                            .loadTrustMaterial(KeyStore.getInstance(KeyStore.getDefaultType()), new TrustStrategy() {
                                @Override
                                public boolean isTrusted(X509Certificate[] chain, String authType)
                                        throws CertificateException {
                                    return true;
                                }
                            }).build();

            SSLConnectionSocketFactory sslSFactory =
                    new SSLConnectionSocketFactory(sslContext);
            Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.INSTANCE).register("https", sslSFactory)
                            .build();


            PoolingHttpClientConnectionManager connManager =
                    new PoolingHttpClientConnectionManager(socketFactoryRegistry);

            SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(Constants.TIMEOUT).setTcpNoDelay(true).build();
            connManager.setDefaultSocketConfig(socketConfig);

            ConnectionConfig connectionConfig =
                    ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE)
                            .setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).build();
            connManager.setDefaultConnectionConfig(connectionConfig);
            connManager.setMaxTotal(500);
            connManager.setDefaultMaxPerRoute(300);
			HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {

				@Override
				public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
					if (executionCount > 2) {
						return false;
					}
					if (exception instanceof InterruptedIOException) {
						return true;
					}
					if (exception instanceof ConnectTimeoutException) {
						return true;
					}
					if (exception instanceof UnknownHostException) {
						return true;
					}
					if (exception instanceof SSLException) {
						return true;
					}
					HttpRequest request = HttpClientContext.adapt(context).getRequest();
					if (!(request instanceof HttpEntityEnclosingRequest)) {
						return true;
					}
					return false;
				}
			};

            HttpClientBuilder httpClientBuilder =
                    HttpClients.custom().setConnectionManager(connManager)
							.setRetryHandler(retryHandler)
                            .setDefaultCookieStore(new BasicCookieStore()).setUserAgent(userAgent);
            if (proxy != null) {
                httpClientBuilder.setRoutePlanner(new DefaultProxyRoutePlanner(proxy)).build();
            }
            httpClient = httpClientBuilder.build();

            requestConfig = RequestConfig.custom().setSocketTimeout(Constants.TIMEOUT).
					setConnectTimeout(Constants.TIMEOUT).
					setConnectionRequestTimeout(Constants.TIMEOUT).
					setCookieSpec(CookieSpecs.STANDARD).
					build();
        } catch (Exception e) {
            logger.error("Exception:", e);
        }
    }

	public static String getWebPage(String url) throws IOException {
		HttpGet request = new HttpGet(url);
		return getWebPage(request, "utf-8");
	}
	public static String getWebPage(HttpRequestBase request) throws IOException {
		return getWebPage(request, "utf-8");
	}
	public static String postRequest(String postUrl, Map<String, String> params) throws IOException {
		HttpPost post = new HttpPost(postUrl);
		setHttpPostParams(post, params);
		return getWebPage(post, "utf-8");
	}


	/**
	 * @param encoding 字符编码
     * @return 网页内容
     */

	public static String getWebPage(HttpRequestBase request
			, String encoding) throws IOException {
		CloseableHttpResponse response = null;
		response = getResponse(request);
		logger.info("status---" + response.getStatusLine().getStatusCode());
		String content = EntityUtils.toString(response.getEntity(),encoding);
		request.releaseConnection();
		return content;
	}

	public static CloseableHttpResponse getResponse(HttpRequestBase request) throws IOException {
		if (request.getConfig() == null){
			request.setConfig(requestConfig);
		}
		request.setHeader("User-Agent", Constants.userAgentArray[new Random().nextInt(Constants.userAgentArray.length)]);
		HttpClientContext httpClientContext = HttpClientContext.create();
		httpClientContext.setCookieStore(cookieStore);
		CloseableHttpResponse response = httpClient.execute(request, httpClientContext);
//		int statusCode = response.getStatusLine().getStatusCode();
//		if(statusCode != 200){
//			throw new IOException("status code is:" + statusCode);
//		}
		return response;
	}
	public static CloseableHttpResponse getResponse(String url) throws IOException {
		HttpGet request = new HttpGet(url);
		return getResponse(request);
	}
	/**
	 * 序列化对象
	 * @param object
	 * @throws Exception
	 */
	public static void serializeObject(Object object,String filePath){
		OutputStream fos = null;
		try {
			fos = new FileOutputStream(filePath, false);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
			logger.info("序列化成功");
			oos.flush();
			fos.close();
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 反序列化对象
	 * @param path
	 * @throws Exception
	 */
	public static Object deserializeObject(String path) throws Exception {
//		InputStream fis = HttpClientUtil.class.getResourceAsStream(name);
		File file = new File(path);
		InputStream fis = new FileInputStream(file);
		ObjectInputStream ois = null;
		Object object = null;
		ois = new ObjectInputStream(fis);
		object = ois.readObject();
		fis.close();
		ois.close();
		return object;
	}


	public static CookieStore getCookieStore() {
		return cookieStore;
	}

	public static void setCookieStore(CookieStore cookieStore) {
		HttpClientUtil.cookieStore = cookieStore;
	}


	/**
	 * 设置request请求参数
	 * @param request
	 * @param params
     */

	public static void setHttpPostParams(HttpPost request,Map<String,String> params){
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		for (String key : params.keySet()) {
			formParams.add(new BasicNameValuePair(key,params.get(key)));
		}
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(formParams, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		request.setEntity(entity);
	}
	public static RequestConfig.Builder getRequestConfigBuilder(){
		return RequestConfig.custom().setSocketTimeout(Constants.TIMEOUT).
				setConnectTimeout(Constants.TIMEOUT).
				setConnectionRequestTimeout(Constants.TIMEOUT).
				setCookieSpec(CookieSpecs.STANDARD);
	}

}
