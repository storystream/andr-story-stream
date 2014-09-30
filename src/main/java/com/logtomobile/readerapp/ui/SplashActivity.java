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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.inject.Inject;
import com.logtomobile.readerapp.R;
import com.logtomobile.readerapp.net.ReaderAppService;

import roboguice.inject.ContentView;

/**
 * @author Marcin Przepiórkowski
 */
@ContentView(R.layout.activity_splash)
public class SplashActivity extends BaseActivity {
    @Inject
    private ReaderAppService mAppService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppService.fetchArticlesHeaders();

        Handler handler = new Handler();
        handler.postDelayed(
            new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, ArticlesListActivity.class);
                    startActivity(intent);
                }
            }, 1200
        );
    }
}