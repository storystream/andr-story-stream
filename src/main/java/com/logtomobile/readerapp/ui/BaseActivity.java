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

package com.logtomobile.readerapp.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.logtomobile.readerapp.StoryStreamApplication;
import com.logtomobile.readerapp.net.event.DeviceOfflineEvent;
import com.logtomobile.readerapp.net.event.NetErrorEvent;

import roboguice.activity.RoboFragmentActivity;

/**
 * @author Marcin Przepiórkowski
 */
public class BaseActivity extends RoboFragmentActivity {
    protected EventBus mEventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventBus = StoryStreamApplication.getAppEventBus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mEventBus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mEventBus.unregister(this);
    }

    @Subscribe
    public void onNetError(@NonNull NetErrorEvent event) {
        Toast.makeText(this, "net error occurred, check your internet connection",
                Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onDeviceOffline(@NonNull DeviceOfflineEvent event) {
        Toast.makeText(this, "device is offline", Toast.LENGTH_LONG).show();
    }
}