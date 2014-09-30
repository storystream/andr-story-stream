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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;

import com.logtomobile.readerapp.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

import static com.google.inject.internal.util.$Preconditions.checkArgument;
import static com.google.inject.internal.util.$Preconditions.checkNotNull;

/**
 * @author Marcin Przepiórkowski
 */
public class TimeLineFragment extends RoboFragment {
    public interface TimeLineProgressHandler {
        void onProgressChanged(@NonNull TimeLineFragment sender, int currentStep, int absoluteStep,
            int timeLineColor);
        boolean animationInProgress();
    }

    public enum CircleType {
        Empty,
        Filled,
        None
    }

    private static final String EXTRA_PATTERN = "pattern";
    private static final String EXTRA_FOREGROUND_COLOR_RES = "foreground_color_res";
    private static final String EXTRA_BACKGROUND_COLOR_RES = "background_color_res";
    private static final String EXTRA_OFFSET = "offset";

    @InjectView(R.id.llCirclesContainer)
    private LinearLayout mRlCirclesContainer;

    @InjectView(R.id.viewHorizontalLine)
    private View mViewHorizontalLine;

    private List<View> mCircles = new LinkedList<>();
    private List<CircleType> mPattern;

    private int mFgColor;
    private int mBgColor;
    private int mOffset;

    private boolean mAnimationInProgress;
    private int mSelectionIndex = -1;

    private View mCurrentlySelectedCircle;

    private TimeLineProgressHandler mHandler;

    private void animateClick(final View circle) {
        CircleType ct = (CircleType) circle.getTag(R.id.tag_circle_type);

        View innerCircle = circle.findViewById(R.id.viewInnerCircle);
        innerCircle.animate().alpha(ct == CircleType.Empty ? 0.0f : 1.0f);

        float scale = ct == CircleType.Empty ? 1.4f : 1.0f;

        PropertyValuesHolder scaleXpvh = PropertyValuesHolder.ofFloat("scaleX", scale);
        PropertyValuesHolder scaleYpvh = PropertyValuesHolder.ofFloat("scaleY", scale);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(circle, scaleXpvh,
                scaleYpvh);
        animator.setRepeatCount(1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimationInProgress = false;
                    }
                }
        );
        animator.start();

        circle.setTag(R.id.tag_circle_type, ct == CircleType.Empty ?
                CircleType.Filled : CircleType.Empty);
        mAnimationInProgress = true;
    }

    private void onCircleClickAction(View v) {
        if (mAnimationInProgress) {
            return;
        }

        if (mHandler != null && mHandler.animationInProgress()) {
            return;
        }

        CircleType ct = (CircleType) v.getTag(R.id.tag_circle_type);
        if (ct == CircleType.Empty || ct == CircleType.Filled) {
            mSelectionIndex = (Integer) v.getTag(R.id.tag_circle_index);

            if (mCurrentlySelectedCircle != null) {
                animateClick(mCurrentlySelectedCircle);
            }

            animateClick(mCurrentlySelectedCircle = v);

            if (mHandler != null) {
                int index = (Integer) v.getTag(R.id.tag_circle_index);
                mHandler.onProgressChanged(TimeLineFragment.this, index, index + mOffset, mFgColor);
            }
        }
    }

    private View.OnClickListener mCircleOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            onCircleClickAction(v);
        }
    };

    private void addOffset() {
        Resources res = getResources();
        int circleSize = res.getDimensionPixelSize(R.dimen.time_line_circle_size);
        int circlePadding = res.getDimensionPixelSize(R.dimen.time_line_circle_padding);
        int separatorWidth = res.getDimensionPixelSize(R.dimen.time_line_separator_width);

        for (int i = 1; i <= mOffset; ++i) {
            Space space = new Space(getActivity());
            space.setLayoutParams(new LinearLayout.LayoutParams(circleSize + 2 * circlePadding, 8));
            mRlCirclesContainer.addView(space);
        }

        if (mOffset > 0) {
            Space space = new Space(getActivity());
            space.setLayoutParams(new LinearLayout.LayoutParams(mOffset * separatorWidth, 8));
            mRlCirclesContainer.addView(space);
        }
    }

    private void buildCirclesList() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        for (CircleType ct : mPattern) {
            View c = inflater.inflate(R.layout.circle, mRlCirclesContainer, false);
            c.setTag(R.id.tag_circle_type, ct);
            mCircles.add(c);

            if (ct == CircleType.Filled) {
                mCurrentlySelectedCircle = c;
                mSelectionIndex = mCircles.size() - 1;
            }
        }
    }

    private void changeHorizontalLineColor() {
        mViewHorizontalLine.setBackgroundColor(mFgColor);
    }

    private void adjustHorizontalLinePosition() {
        Resources res = getResources();

        int separatorWidth = res.getDimensionPixelSize(R.dimen.time_line_separator_width);
        int circleSize = res.getDimensionPixelSize(R.dimen.time_line_circle_size);
        int circlePadding = res.getDimensionPixelSize(R.dimen.time_line_circle_padding);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                (mCircles.size() - 1) * (separatorWidth + circleSize + 2 * circlePadding),
                res.getDimensionPixelSize(R.dimen.time_line_height));
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mViewHorizontalLine.setLayoutParams(layoutParams);
        mViewHorizontalLine.setTranslationX(circleSize / 2 + circlePadding + res.getDimensionPixelSize(R.dimen.time_line_padding) +
                mOffset * (separatorWidth + circleSize + 2 * circlePadding));
    }

    private void addEmptySpace() {
        Resources res = getResources();
        int separatorWidth = res.getDimensionPixelSize(R.dimen.time_line_separator_width);

        Space space = new Space(getActivity());
        space.setLayoutParams(new LinearLayout.LayoutParams(separatorWidth, 8));
        mRlCirclesContainer.addView(space);
    }

    private void initializeAndInsertCircle(View c, int circleIndex) {
        int fgColor = mFgColor;
        int bgColor = mBgColor;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        c.setLayoutParams(params);

        View filling = c.findViewById(R.id.viewCircle);
        filling.getBackground().setColorFilter(new PorterDuffColorFilter(fgColor,
                PorterDuff.Mode.SRC_IN));

        View innerCircle = c.findViewById(R.id.viewInnerCircle);
        innerCircle.getBackground().setColorFilter(new PorterDuffColorFilter(
                bgColor, PorterDuff.Mode.SRC_IN));

        CircleType ct = (CircleType) c.getTag(R.id.tag_circle_type);
        if (ct == CircleType.Filled) {
            innerCircle.setAlpha(0.0f);
        } else if (ct == CircleType.None) {
            c.setVisibility(View.INVISIBLE);
        }

        c.setTag(R.id.tag_circle_index, circleIndex);
        c.setOnClickListener(mCircleOnClickListener);

        mRlCirclesContainer.addView(c);
    }

    private void addContainerPadding() {
        Space space = new Space(getActivity());
        space.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.time_line_padding), 8));
        mRlCirclesContainer.addView(space);
    }

    private void addCircles() {
        int circleIndex = 0;

        addContainerPadding();
        addOffset();
        buildCirclesList();
        changeHorizontalLineColor();
        adjustHorizontalLinePosition();

        for (View c : mCircles) {
            initializeAndInsertCircle(c, circleIndex);

            if (circleIndex++ < mCircles.size() - 1) {
                addEmptySpace();
            }
        }

        addContainerPadding();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getArguments();
        //noinspection unchecked
        mPattern = (ArrayList<CircleType>) extras.getSerializable(EXTRA_PATTERN);
        mOffset = extras.getInt(EXTRA_OFFSET);
        mSelectionIndex = 0;
        mFgColor = extras.getInt(EXTRA_FOREGROUND_COLOR_RES);
        mBgColor = extras.getInt(EXTRA_BACKGROUND_COLOR_RES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time_line, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addCircles();
    }

    public static @NonNull TimeLineFragment createInstance(@NonNull ArrayList<CircleType> pattern,
                                                           int offset,
                                                           @ColorRes int bgColorRes,
                                                           @ColorRes int fgColorRes) {
        checkNotNull(pattern, "pattern cannot be null");
        checkArgument(offset >= 0, "offset cannot be less than 0");

        Bundle extras = new Bundle();
        extras.putSerializable(EXTRA_PATTERN, pattern);
        extras.putInt(EXTRA_OFFSET, offset);
        extras.putInt(EXTRA_FOREGROUND_COLOR_RES, fgColorRes);
        extras.putInt(EXTRA_BACKGROUND_COLOR_RES, bgColorRes);

        TimeLineFragment fragment = new TimeLineFragment();
        fragment.setArguments(extras);

        return fragment;
    }

    public boolean animationInProgress() {
        return mAnimationInProgress;
    }

    public void unselect() {
        if (mCurrentlySelectedCircle != null) {
            mCurrentlySelectedCircle.setTag(R.id.tag_circle_type, CircleType.Filled);
            View v = mCurrentlySelectedCircle;
            mCurrentlySelectedCircle = null;

            animateClick(v);
        }

        mSelectionIndex = -1;
    }

    public void setTimeLineProgressHandler(@Nullable TimeLineProgressHandler handler) {
        mHandler = handler;
    }

    public void forward() {
        if (!mCircles.isEmpty()) {
            if (mSelectionIndex <= 0) {
                View v = mCircles.get(mSelectionIndex = 1);

                CircleType ct = (CircleType) v.getTag(R.id.tag_circle_type);
                if (ct == CircleType.None) {
                    forward();
                } else {
                    onCircleClickAction(v);
                }
            } else if (mSelectionIndex < mCircles.size() - 1) {
                View v = mCircles.get(++mSelectionIndex);

                CircleType ct = (CircleType) v.getTag(R.id.tag_circle_type);
                if (ct == CircleType.None) {
                    forward();
                } else {
                    onCircleClickAction(v);
                }
            }
        }
    }

    public void backward() {
        if (!mCircles.isEmpty()) {
            if (mSelectionIndex >= mCircles.size()) {
                View v = mCircles.get(mCircles.size() - 1);
                onCircleClickAction(v);
            } else if (mSelectionIndex >= 0 && mSelectionIndex > 0) {
                View v = mCircles.get(--mSelectionIndex);

                CircleType ct = (CircleType) v.getTag(R.id.tag_circle_type);
                if (ct == CircleType.None){
                    backward();
                } else {
                    onCircleClickAction(v);
                }
            } else {
                ++mSelectionIndex;
            }
        }
    }

    public boolean canMoveForward() {
        boolean canMoveForward = !mCircles.isEmpty() && mSelectionIndex >= 0 &&
                mSelectionIndex < mCircles.size() - 1;

        if (canMoveForward) {
            canMoveForward = false;

            for (int i = mSelectionIndex + 1; i < mCircles.size(); ++i) {
                canMoveForward |= (mCircles.get(i).getTag(R.id.tag_circle_type)) != CircleType.None;
            }
        }

        return canMoveForward;
    }

    public boolean canMoveBackward() {
        return !mCircles.isEmpty() && mSelectionIndex > 0 && mSelectionIndex <= mCircles.size();
    }

    public boolean isSelected() {
        return !mCircles.isEmpty() && mSelectionIndex >= 0 && mSelectionIndex < mCircles.size();
    }

    public void selectFirst() {
        if (!mCircles.isEmpty() && mCurrentlySelectedCircle == null) {
            View v = mCircles.get(0);
            onCircleClickAction(v);
        }
    }

    public int getCirclePosition() {
        if (mCurrentlySelectedCircle != null) {
            return (int) mCurrentlySelectedCircle.getX();
        } else {
            return 0;
        }
    }
}