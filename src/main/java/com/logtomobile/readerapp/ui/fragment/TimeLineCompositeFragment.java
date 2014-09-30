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
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.logtomobile.readerapp.R;
import com.logtomobile.readerapp.model.TimeLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Marcin Przepiórkowski
 */
public class TimeLineCompositeFragment extends RoboFragment implements TimeLineFragment.TimeLineProgressHandler {
    public interface OnTimeLineChangedListener {
        void onTimeLineChanged(int newTimeLineColor, int absolutePosition,
                               boolean canMoveForward, boolean canMoveBackward);
    }

    public static final String EXTRA_TIME_LINES = "extra_time_lines";
    public static final String EXTRA_LABELS = "extra_labels";

    @InjectView(R.id.llLabelsContainer)
    private LinearLayout mLlLabelsContainer;

    @InjectView(R.id.llRoot)
    private RelativeLayout mLlRoot;

    @InjectView(R.id.timeLineScroll)
    private HorizontalScrollView mScrollView;

    @InjectView(R.id.txtvSpace)
    private TextView mTxtvSpace;

    private List<TimeLineFragment> mTimeLines = new ArrayList<>();
    private List<TextView> mLabels = new ArrayList<>();
    private TextView mLastSelectedLabel;

    private Map<Integer, TimeLineFragment> mColorToTimeLineMap = new HashMap<>();

    private int mFragmentCounter;

    private TimeLineGridFragment mGridFragment;
    private OnTimeLineChangedListener mTimeLineListener;

    private void addTimeLine(@NonNull TimeLine timeLine) {
        TimeLineFragment fragment = TimeLineFragment.createInstance(timeLine.getCircles(),
                timeLine.getOffset(), timeLine.getBackgroundColor(), timeLine.getForegroundColor());
        fragment.setTimeLineProgressHandler(this);
        mTimeLines.add(fragment);

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        String tag = String.valueOf(++mFragmentCounter);
        ft.add(R.id.llTimeLineContainer, fragment, tag);
        ft.commit();

        mColorToTimeLineMap.put(timeLine.getForegroundColor(), fragment);
    }

    private void addLabels(@NonNull ArrayList<String> labels) {
        Resources res = getResources();

        int width = res.getDimensionPixelSize(R.dimen.time_line_separator_width) +
                res.getDimensionPixelSize(R.dimen.time_line_circle_size) +
                res.getDimensionPixelSize(R.dimen.time_line_circle_padding) * 2;
        width *= labels.size() - 1;
        width += res.getDimensionPixelSize(R.dimen.time_line_circle_size) +
                res.getDimensionPixelSize(R.dimen.time_line_circle_padding) * 2 +
                res.getDimensionPixelSize(R.dimen.time_line_padding) * 2;

        int firstLabelWidth = res.getDimensionPixelSize(R.dimen.time_line_separator_width);
        firstLabelWidth += res.getDimensionPixelSize(R.dimen.time_line_circle_padding) +
                res.getDimensionPixelSize(R.dimen.time_line_circle_size);
        res.getDimensionPixelSize(R.dimen.time_line_padding);
        int labelWidth = (width - 2 * firstLabelWidth) / (!labels.isEmpty() ? labels.size() - 2 : 1);

        for (int i = 0; i < labels.size(); ++i) {
            TextView txtv = new TextView(getActivity());
            if (i == 0 || i == labels.size() - 1) {
                txtv.setLayoutParams(new LinearLayout.LayoutParams(firstLabelWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                txtv.setLayoutParams(new LinearLayout.LayoutParams(labelWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            txtv.setText(labels.get(i));
            txtv.setGravity(Gravity.CENTER);
            txtv.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.time_line_label_font));
            txtv.setTextColor(getResources().getColor(R.color.time_line_labels_color));

            if (i == 0) {
                txtv.setTextColor(getResources().getColor(R.color.time_line_orange_selection));
                mLastSelectedLabel = txtv;
            }

            mLlLabelsContainer.addView(txtv);
            mLabels.add(txtv);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time_line_composite, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle extras = getArguments();

        if (extras.containsKey(EXTRA_TIME_LINES)) {
            //noinspection unchecked
            ArrayList<TimeLine> timeLines = (ArrayList<TimeLine>) extras.getSerializable(EXTRA_TIME_LINES);
            for (TimeLine tl : timeLines) {
                addTimeLine(tl);
            }
        }

        if (extras.containsKey(EXTRA_LABELS)) {
            //noinspection unchecked
            ArrayList<String> labels = (ArrayList<String>) extras.getSerializable(EXTRA_LABELS);
            addLabels(labels);

            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.flGridContainer, mGridFragment = TimeLineGridFragment.createInstance(labels.size()),
                    "grid fragment");
            ft.commit();
        }

        final ViewTreeObserver observer = mLlLabelsContainer.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (observer.isAlive()) {
                        //noinspection deprecation
                        observer.removeGlobalOnLayoutListener(this);
                    }

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, mLlLabelsContainer.getHeight()
                                + getResources().getDimensionPixelSize(R.dimen.padding_small));
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.timeLineScroll);
                    mTxtvSpace.setLayoutParams(params);
                }
            }
        );
    }

    public static @NonNull TimeLineCompositeFragment createInstance(@NonNull ArrayList<TimeLine> lines, @NonNull ArrayList<String> labels) {
        checkNotNull(lines, "lines cannot be null");
        checkNotNull(labels, "labels cannot be null");
        checkArgument(!lines.isEmpty(), "lines cannot be empty");
        checkArgument(!labels.isEmpty(), "labels cannot be empty");

        Bundle extras = new Bundle();
        extras.putSerializable(EXTRA_TIME_LINES, lines);
        extras.putSerializable(EXTRA_LABELS, labels);

        TimeLineCompositeFragment fragment = new TimeLineCompositeFragment();
        fragment.setArguments(extras);

        return fragment;
    }

    @Override
    public void onProgressChanged(@NonNull TimeLineFragment sender, int currentStep, int absoluteStep,
                                  int timeLineColor) {
        for (TimeLineFragment f : mTimeLines) {
            if (f != sender) {
                f.unselect();
            }
        }

        TextView label = mLabels.get(absoluteStep);

        if (mLastSelectedLabel != null && mLastSelectedLabel != label) {
            ObjectAnimator colorAnim = ObjectAnimator.ofInt(mLastSelectedLabel, "textColor",
                    mLastSelectedLabel.getCurrentTextColor(), getResources().getColor(R.color.time_line_labels_color));
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.start();
        }

        ObjectAnimator colorAnim = ObjectAnimator.ofInt(mLastSelectedLabel = label, "textColor",
                label.getCurrentTextColor(), timeLineColor);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.start();

        mGridFragment.animateIndicatorColor(timeLineColor);
        mGridFragment.moveIndicator(absoluteStep);

        if (mTimeLineListener != null) {
            mTimeLineListener.onTimeLineChanged(timeLineColor, absoluteStep, sender.canMoveForward(),
                    sender.canMoveBackward());
        }
    }

    @Override
    public boolean animationInProgress() {
        boolean result = false;

        for (TimeLineFragment f : mTimeLines) {
            result |= f.animationInProgress();
        }

        return result;
    }

    public int getHeight() {
        return mLlRoot.getHeight();
    }

    public void next() {
        if (!animationInProgress()) {
            for (TimeLineFragment f : mTimeLines) {
                if (f.isSelected() && f.canMoveForward()) {
                    int oldPosition = f.getCirclePosition();

                    f.forward();

                    if (f.getCirclePosition() > mLlRoot.getWidth() / 2) {
                        mScrollView.smoothScrollBy(f.getCirclePosition() - oldPosition, 0);
                    }

                    break;
                }
            }
        }
    }

    public void prev() {
        if (!animationInProgress()) {
            for (TimeLineFragment f : mTimeLines) {
                if (f.isSelected() && f.canMoveBackward()) {
                    int oldPosition = f.getCirclePosition();

                    f.backward();
                    mScrollView.smoothScrollBy(f.getCirclePosition() - oldPosition, 0);

                    break;
                }
            }
        }
    }

    public void setOnTimeLineChangedListener(@Nullable OnTimeLineChangedListener listener) {
        mTimeLineListener = listener;
    }

    public void changeTimeLine(int timeLineColor) {
        TimeLineFragment fragment = mColorToTimeLineMap.get(timeLineColor);
        if (fragment != null) {
            fragment.selectFirst();
        }
    }
}