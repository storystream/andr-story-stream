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

package com.logtomobile.readerapp.net;

/**
 * @author Marcin Przepiórkowski
 *
 * This class contains some constants used during the execution of http requests.
 */
public final class Constants {
    private Constants() {
    }

    public static final int CONNECTION_TIMEOUT = 15 * 1000; // 15s
    public static final int READ_TIMEOUT = 15 * 1000; // 15s
}