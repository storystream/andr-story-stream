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

import android.support.annotation.Nullable;

/**
 * @author Marcin Przepiórkowski
 *
 * The listener interface that is used by the PostRequest to indicate its execution finish.
 */
public interface OnRequestFinishedListener<T> {
    /**
     * Invoked when the request finishes its execution.
     *
     * @param responseCode  http request response code
     * @param response      http request response
     */
    void onRequestFinished(int responseCode, @Nullable T response, boolean canceled);
}