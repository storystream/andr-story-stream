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

package com.logtomobile.readerapp.net.util;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Marcin Przepiórkowski
 *
 * This class contains utility methods that are usful when constructing http request or reading
 * request response.
 */
public final class NetUtils {
    private NetUtils() {
    }

    /**
     * Returns string representation of the connectionStream output.
     *
     * @param connectionStream  the connection stream
     * @return                  string representation of the stream
     * @throws java.io.IOException      when connection stream output cannot be read
     */
    public static @NonNull String getRequestResponse(@NonNull InputStream connectionStream) throws IOException {
        checkNotNull(connectionStream, "connection stream cannot be null");

        BufferedInputStream in = new BufferedInputStream(connectionStream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line;
        StringBuilder responseBuilder = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }
        reader.close();

        return responseBuilder.toString();
    }

    /**
     * Transforms post params from ContentValues object to a String representation. If in the params
     * collections there are:<br/>
     *
     * <ul>
     *      <li>[Key] "appIdString"</li>
     *      <li>[Value] "iCare: Eyes"</li>
     *      <li>[Key] "userName"</li>
     *      <li>[Value] "marcin"</li>
     * </ul>
     *
     * then the method transforms given dictionary to: <b>"appIdString=iCare: Eyes&userName=marcin"</b>.
     *
     * @param params    the dictionary that contains post request parameters
     * @return          post parameters transformed to a String representation
     */
    public static @NonNull String buildPostParamsString(@NonNull ContentValues params) {
        checkNotNull(params, "params cannot be null");

        StringBuilder builder = new StringBuilder();

        boolean withAmpersand = false;
        for (String key : params.keySet()) {
            if (withAmpersand) {
                builder.append("&");
            }

            builder.append(key);
            builder.append("=");
            builder.append(params.get(key).toString());

            withAmpersand = true;
        }

        return builder.toString();
    }

    /**
     * Checks whether the device is online.
     *
     * @param context   the application context
     * @return          true if the device is online, false otherwise
     */
    public static boolean isOnline(@NonNull Context context) {
        checkNotNull(context, "context cannot be null");

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
    }
}