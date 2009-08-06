/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.http;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.logging.Log;
import org.apache.http.*;
import org.apache.http.impl.*;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.*;
import org.apache.http.protocol.*;
import org.apache.http.util.EntityUtils;

/**
 * Use this class to make http requests
 */
public class HttpClient {

    private DefaultHttpClientConnection conn;
    private final DefaultConnectionReuseStrategy connStrategy;
    private final HttpRequestExecutor httpexecutor;
    private final BasicHttpContext context;
    private final HttpHost host;
    private final BasicHttpParams params;
    private final BasicHttpProcessor httpproc;

    /**
     * Connects to the given host
     * @param toHost
     * @param port
     * @throws UnknownHostException
     * @throws IOException
     * @throws HttpException
     */
    public HttpClient(String toHost, int port) throws UnknownHostException, IOException, HttpException {
        params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUserAgent(params, "HttpComponents/1.1");
        HttpProtocolParams.setUseExpectContinue(params, true);

        httpproc = new BasicHttpProcessor();
        // Required protocol interceptors
        httpproc.addInterceptor(new RequestContent());
        httpproc.addInterceptor(new RequestTargetHost());
        // Recommended protocol interceptors
        httpproc.addInterceptor(new RequestConnControl());
        httpproc.addInterceptor(new RequestUserAgent());
        httpproc.addInterceptor(new RequestExpectContinue());
        httpexecutor = new HttpRequestExecutor();
        context = new BasicHttpContext(null);
        host = new HttpHost(toHost, port);
        conn = new DefaultHttpClientConnection();
        connStrategy = new DefaultConnectionReuseStrategy();
        context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, host);
    }

    /**
     * Runs a GET request
     * @param srequest The request, eg /servlets-examples/servlet/RequestInfoExample"
     * @return
     * @throws UnknownHostException
     * @throws IOException
     * @throws HttpException
     */
    public HttpResponse request(String srequest) throws UnknownHostException, IOException, HttpException {

        if (!conn.isOpen()) {
            Socket socket = new Socket(host.getHostName(), host.getPort());
            conn.bind(socket, params);

            BasicHttpRequest request = new BasicHttpRequest("GET", srequest);
            Log.Debug(this, ">> Request URI: " + request.getRequestLine().getUri());

            request.setParams(params);
            httpexecutor.preProcess(request, httpproc, context);
            HttpResponse response = httpexecutor.execute(request, conn, context);
            response.setParams(params);
            httpexecutor.postProcess(response, httpproc, context);

//                Log.Debug(this, "<< Response: " + response.getStatusLine());
//                Log.Debug(this, EntityUtils.toString(response.getEntity()));
//                Log.Debug(this, "==============");
//                if (!connStrategy.keepAlive(response, context)) {
//                    conn.close();
//                } else {
//                    Log.Debug(this, "Connection kept alive...");
//                }
//            } else {
//                Log.Debug(this, "Connection already closed.");
            return response;
        }
        return null;
    }
}
