/**
 * Story Stream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Story Stream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Story Stream. If not, see <http://www.gnu.org/licenses/>.
 */

package com.logtomobile.readerapp.net.request;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.io.Closer;
import com.logtomobile.readerapp.net.CancelableTask;
import com.logtomobile.readerapp.net.Constants;
import com.logtomobile.readerapp.net.RequestExecutor;
import com.logtomobile.readerapp.net.RequestResult;
import com.logtomobile.readerapp.net.util.NetUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Marcin Przepiórkowski
 *
 * This is a generic HTTP GET request. This class should be used to construct specialized
 * POST requests.
 */
/*package*/ class HttpGet<T> {
    /**
     * The interface of the server response parser.
     */
    public static interface ServerResponseParser<T> {
        /**
         * Invoked to parse server response.
         *
         * @param json  server response in the json format
         * @return      parsed server response
         */
        T parse(@NonNull String json);
    }

    private String mRequestUrl;
    private ServerResponseParser<T> mParser;
    private OnRequestFinishedListener<T> mExecutionListener;
    private boolean mUseSsl;

    /**
     * Default PostRequest constructor.
     *
     * @param requestUrl            http request url
     * @param parser                server response parser
     * @param executionListener     execution listener
     */
    public HttpGet(boolean useSsl,
                   @NonNull String requestUrl,
                   @Nullable ServerResponseParser<T> parser,
                   @Nullable OnRequestFinishedListener<T> executionListener) {
        checkNotNull(requestUrl, "requestUrl cannot be null");
        checkArgument(!requestUrl.isEmpty(), "requestUrl cannot be empty");

        mUseSsl = useSsl;
        mRequestUrl = requestUrl;
        mParser = parser;
        mExecutionListener = executionListener;
    }

    /**
     * Executes this request asynchronously in the RequestExecutor. When the execution finishes,
     * executionListener is invoked (if it's not null). When an error occurs, a NetErrorEvent instance
     * is posted on the application event bus.
     *
     * @return  the task id, that can be used to cancel it later
     */
    public @NonNull String execute() {
        CancelableTask<RequestResult> requestTask = new CancelableTask<RequestResult>() {
            @Override
            public RequestResult call() throws Exception {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                HttpURLConnection connection = null;
                int responseCode = Integer.MAX_VALUE;
                String responseMessage = "";

                try {
                    URL serviceUrl = new URL(mRequestUrl);

                    if (mUseSsl) {
                        connection = (HttpsURLConnection) serviceUrl.openConnection();
                    } else {
                        connection = (HttpURLConnection) serviceUrl.openConnection();
                    }
                    connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
                    connection.setReadTimeout(Constants.READ_TIMEOUT);
                    connection.setRequestMethod("GET");

                    Closer closer;
                    responseCode = connection.getResponseCode();

                    if (responseCode < 400) {
                        closer = Closer.create();
                        try {
                            InputStream is = closer.register(connection.getInputStream());
                            responseMessage = NetUtils.getRequestResponse(is);
                            is.close();
                        } catch (Throwable e) {
                            //noinspection ThrowableResultOfMethodCallIgnored
                            closer.rethrow(e);
                        } finally {
                            closer.close();
                        }
                    } else {
                        closer = Closer.create();
                        try {
                            InputStream is = connection.getErrorStream();
                            responseMessage = NetUtils.getRequestResponse(is);
                            is.close();
                        } catch (Throwable e) {
                            //noinspection ThrowableResultOfMethodCallIgnored
                            closer.rethrow(e);
                        } finally {
                            closer.close();
                        }
                    }
                }
                finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }

                if (mExecutionListener != null && mParser != null) {
                    try {
                        final int code = responseCode;
                        final T response = mParser.parse(responseMessage);

                        if (response == null) {
                            throw new Exception("cannot parse the response");
                        }

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mExecutionListener.onRequestFinished(code, response, false);
                                    }
                                }
                        );
                    } catch (Exception e) {
                        System.out.println("EXCEPTION: " + e.getMessage());
                    }
                }

                return new RequestResult(mRequestUrl, responseMessage, mRequestUrl, responseCode);
            }

            @Override
            public void cancel() {
            }
        };

        return RequestExecutor.execute(requestTask, mRequestUrl);
    }
}