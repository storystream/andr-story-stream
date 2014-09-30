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

package com.logtomobile.readerapp.model;

import android.support.annotation.NonNull;

import com.logtomobile.readerapp.ui.fragment.TimeLineFragment;

import java.io.Serializable;
import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Marcin Przepiórkowski
 */
public class TimeLine implements Serializable {
    private final String mName;
    private final int mForegroundColor;
    private final int mBackgroundColor;
    private final int mOffset;
    private final ArrayList<TimeLineFragment.CircleType> mCircles;

    public TimeLine(@NonNull String name, int fgColor, int bgColor, int offset,
                    @NonNull ArrayList<TimeLineFragment.CircleType> circles) {
        mName = checkNotNull(name, "name cannot be null");
        mForegroundColor = fgColor;
        mBackgroundColor = bgColor;
        mOffset = offset;
        mCircles = checkNotNull(circles, "circles cannot be null");
    }

    public @NonNull String getName() {
        return mName;
    }

    public int getForegroundColor() {
        return mForegroundColor;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public int getOffset() {
        return mOffset;
    }

    public @NonNull ArrayList<TimeLineFragment.CircleType> getCircles() {
        return mCircles;
    }
}