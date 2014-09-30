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

package com.logtomobile.readerapp;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.eventbus.EventBus;

/**
 * @author Marcin Przepiórkowski
 */
public class StoryStreamApplication extends Application {
    private static EventBus sAppEventBus;
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sAppEventBus = new EventBus("reader app event bus");
        sContext = getApplicationContext();
    }

    public static @NonNull EventBus getAppEventBus() {
        return sAppEventBus;
    }

    public static @NonNull Context getAppContext() {
        return sContext;
    }
}