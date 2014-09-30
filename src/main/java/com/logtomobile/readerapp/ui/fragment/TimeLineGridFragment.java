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

package com.logtomobile.readerapp.ui.fragment;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;

import com.logtomobile.readerapp.R;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

import static com.google.inject.internal.util.$Preconditions.checkArgument;

/**
 * @author Marcin Przepiórkowski
 */
public class TimeLineGridFragment extends RoboFragment {
    private static final String EXTRA_LINES_COUNT = "lines_count";

    @InjectView(R.id.llGridContainer)
    private LinearLayout mLlGridContainer;

    @InjectView(R.id.viewCircleIndicator)
    private View mViewCircleIndicator;

    private int mLinesCount;
    private int mIndicatorLastColor;

    private void adjustContainerPadding() {
        Resources res = getResources();
        int padding =
                res.getDimensionPixelSize(R.dimen.time_line_circle_size) / 2 +
                res.getDimensionPixelSize(R.dimen.time_line_circle_padding) +
                res.getDimensionPixelSize(R.dimen.time_line_padding);

        mLlGridContainer.setPadding(padding, 0, 0, 0);
    }

    private void adjustCircleIndicatorPosition() {
        Resources res = getResources();
        int padding =
                res.getDimensionPixelSize(R.dimen.time_line_circle_size) / 2 +
                res.getDimensionPixelSize(R.dimen.time_line_circle_padding) +
                res.getDimensionPixelSize(R.dimen.time_line_padding) -
                res.getDimensionPixelSize(R.dimen.grid_circle_indicator_size) / 2;

        mViewCircleIndicator.setTranslationX(padding);
        mViewCircleIndicator.setTranslationY(-res.getDimensionPixelSize(R.dimen.grid_circle_indicator_size) / 2);
    }

    private void setCircleIndicatorColor() {
        Resources res = getResources();
        mViewCircleIndicator.getBackground().setColorFilter(new PorterDuffColorFilter(
                res.getColor(R.color.time_line_orange_selection), PorterDuff.Mode.SRC_IN));
    }

    private void moveCircleIndicator(int position) {
        Resources res = getResources();
        int padding =
                res.getDimensionPixelSize(R.dimen.time_line_circle_size) / 2 +
                res.getDimensionPixelSize(R.dimen.time_line_circle_padding) +
                res.getDimensionPixelSize(R.dimen.time_line_padding) -
                res.getDimensionPixelSize(R.dimen.grid_circle_indicator_size) / 2;

        int emptySpaceWidth =
                res.getDimensionPixelSize(R.dimen.time_line_separator_width) +
                res.getDimensionPixelSize(R.dimen.time_line_circle_size) +
                res.getDimensionPixelSize(R.dimen.time_line_circle_padding) * 2;

        int translation = padding + position * emptySpaceWidth;
        if (mViewCircleIndicator.getTranslationX() != translation) {
            PropertyValuesHolder translationPvh = PropertyValuesHolder.ofFloat("translationX",
                    translation);
            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(mViewCircleIndicator,
                    translationPvh);
            animator.start();
        }
    }

    private void addLines() {
        Resources res = getResources();
        int emptySpaceWidth =
                res.getDimensionPixelSize(R.dimen.time_line_separator_width) +
                res.getDimensionPixelSize(R.dimen.time_line_circle_size) +
                res.getDimensionPixelSize(R.dimen.time_line_circle_padding) * 2 -
                res.getDimensionPixelSize(R.dimen.grid_line_width);

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        for (int i = 0; i < mLinesCount; ++i) {
            View line = inflater.inflate(R.layout.grid_line, mLlGridContainer, false);
            mLlGridContainer.addView(line);

            if (i < mLinesCount - 1) {
                Space space = new Space(getActivity());
                space.setLayoutParams(new LinearLayout.LayoutParams(emptySpaceWidth, 8));
                mLlGridContainer.addView(space);
            }
        }

        Space space = new Space(getActivity());
        space.setLayoutParams(new LinearLayout.LayoutParams(
                res.getDimensionPixelSize(R.dimen.time_line_circle_size) / 2 +
                res.getDimensionPixelSize(R.dimen.time_line_circle_padding) +
                res.getDimensionPixelSize(R.dimen.time_line_padding), 8));
        mLlGridContainer.addView(space);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getArguments();
        mLinesCount = extras.getInt(EXTRA_LINES_COUNT);

        mIndicatorLastColor = getResources().getColor(R.color.time_line_orange_selection);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adjustContainerPadding();
        addLines();
        adjustCircleIndicatorPosition();
        setCircleIndicatorColor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time_line_grid, container, false);
    }

    public static @NonNull TimeLineGridFragment createInstance(int linesCount) {
        checkArgument(linesCount > 0, "lines count has to be greater than 0");

        Bundle extras = new Bundle();
        extras.putInt(EXTRA_LINES_COUNT, linesCount);

        TimeLineGridFragment fragment = new TimeLineGridFragment();
        fragment.setArguments(extras);

        return fragment;
    }

    public void moveIndicator(int absolutePosition) {
        checkArgument(absolutePosition >= 0, "absolutePosition cannot be less than 0");
        checkArgument(absolutePosition < mLinesCount, "absolutePosition cannot be greater than lines count");

        moveCircleIndicator(absolutePosition);
    }

    public void animateIndicatorColor(int color) {
        ValueAnimator animator = ValueAnimator.ofInt(mIndicatorLastColor, color);
        animator.addUpdateListener(
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mViewCircleIndicator.getBackground().setColorFilter(new PorterDuffColorFilter(
                            (Integer) animation.getAnimatedValue(), PorterDuff.Mode.SRC_IN));
                }
            }
        );
        animator.setEvaluator(new ArgbEvaluator());
        animator.start();

        mIndicatorLastColor = color;
    }
}