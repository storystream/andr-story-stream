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

package com.logtomobile.readerapp.net.event;

import android.support.annotation.Nullable;

/**
 * @author Marcin Przepiórkowski
 *
 * This class should be used to indicate that an error occurred in a network communication process.
 * It contains all informations about the error: Throwable that was thrown during the execution
 * of releated network request and the error message.
 */
public class NetErrorEvent {
    private final String mErrorMessage;
    private final Throwable mCause;

    /**
     * Default NetErrorEvent constructor.
     *
     * @param errorMessage  the error message
     * @param cause         the cause of this error
     */
    public NetErrorEvent(@Nullable String errorMessage, @Nullable Throwable cause) {
        mErrorMessage = errorMessage;
        mCause = cause;
    }

    /**
     * Returns the error message.
     *
     * @return  the error message
     */
    @Nullable
    public String getErrorMessage() {
        return mErrorMessage;
    }

    /**
     * Returns the cause of the error.
     *
     * @return  the cause of the error
     */
    @Nullable
    public Throwable getCause() {
        return mCause;
    }
}