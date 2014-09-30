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

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.logtomobile.readerapp.R;
import com.logtomobile.readerapp.model.Article;
import com.logtomobile.readerapp.model.Legend;
import com.logtomobile.readerapp.model.Line;
import com.logtomobile.readerapp.model.Story;
import com.logtomobile.readerapp.model.TimeLine;
import com.logtomobile.readerapp.ui.fragment.TimeLineCompositeFragment;
import com.logtomobile.readerapp.ui.fragment.TimeLineFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import static com.google.inject.internal.util.$Preconditions.checkNotNull;

/**
 * @author Marcin Przepiórkowski
 */
@ContentView(R.layout.activity_article)
public class ArticleActivity extends BaseActivity {
    public class AuthorAdapter extends ArrayAdapter<Pair<String, Integer>> {
        class ViewHolder {
            public View mViewCircle;
            public TextView mTxtvAuthor;
        }

        private final List<Pair<String, Integer>> mAuthors;

        public AuthorAdapter(@NonNull List<Pair<String, Integer>> authors) {
            super(ArticleActivity.this, R.layout.spinitem_author_drop_down, authors);
            checkNotNull(authors);
            mAuthors = authors;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(ArticleActivity.this);
                convertView = inflater.inflate(R.layout.spinitem_author, parent, false);

                View viewCircle = convertView.findViewById(R.id.viewCircle);
                TextView txtvAuthor = (TextView) convertView.findViewById(R.id.txtvAuthor);

                viewHolder = new ViewHolder();
                viewHolder.mViewCircle = viewCircle;
                viewHolder.mTxtvAuthor = txtvAuthor;

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.mTxtvAuthor.setText(mAuthors.get(position).first);
            viewHolder.mViewCircle.getBackground().setColorFilter(new PorterDuffColorFilter(
                    mAuthors.get(position).second, PorterDuff.Mode.SRC_IN));

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(ArticleActivity.this);
                convertView = inflater.inflate(R.layout.spinitem_author_drop_down, parent, false);

                View viewCircle = convertView.findViewById(R.id.viewCircle);
                TextView txtvAuthor = (TextView) convertView.findViewById(R.id.txtvAuthor);

                viewHolder = new ViewHolder();
                viewHolder.mViewCircle = viewCircle;
                viewHolder.mTxtvAuthor = txtvAuthor;

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.mTxtvAuthor.setText(mAuthors.get(position).first);
            viewHolder.mViewCircle.getBackground().setColorFilter(new PorterDuffColorFilter(
                    mAuthors.get(position).second, PorterDuff.Mode.SRC_IN));

            return convertView;
        }
    }

    public static final String EXTRA_ARTICLE = "extra_article";
    private static final String TAG_TIME_LINE_FRAGMENT = "time_line_fragment";

    @InjectView(R.id.imgvHide)
    private ImageView mImgvHide;

    @InjectView(R.id.llMenuContainer)
    private LinearLayout mLlMenuContainer;

    @InjectView(R.id.llTextContainer)
    private LinearLayout mLlTextContainer;

    @InjectView(R.id.rlMenuBar)
    private RelativeLayout mRlMenuBar;

    @InjectView(R.id.txtvHide)
    private TextView mTxtvHide;

    @InjectView(R.id.spinnerAuthors)
    private Spinner mSpinnerAuthors;

    @InjectView(R.id.imgvPrev)
    private ImageView mImgvPrev;

    @InjectView(R.id.imgvNext)
    private ImageView mImgvNext;

    @InjectView(R.id.txtvArticle)

    private TextView mTxtvArticle;
    private TimeLineCompositeFragment mTimeLine;

    private boolean mMenuHidden;
    private boolean mSpaceAdded;

    private boolean mCanMoveForward = true;
    private boolean mCanMoveBackward;

    private Map<Integer, Integer> mColorToIndexMap = new HashMap<>();
    private Map<Integer, List<Story>> mColorToStoriesMap = new HashMap<>();
    private Map<Line, List<Story>> mLineToStoriesMap = new HashMap<>();
    private AuthorAdapter mAuthorAdapter;

    private void hideMenu() {
        PropertyValuesHolder translationPvh = PropertyValuesHolder.ofFloat("translationY",
                mTimeLine.getHeight());
        PropertyValuesHolder rotationPvh = PropertyValuesHolder.ofFloat("rotation", 180.0f);
        PropertyValuesHolder scaleXpvh = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.4f, 1.0f);
        PropertyValuesHolder scaleYpvh = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.4f, 1.0f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(mImgvHide, rotationPvh,
                scaleXpvh, scaleYpvh);
        animator.start();

        animator = ObjectAnimator.ofPropertyValuesHolder(mLlMenuContainer, translationPvh);
        animator.start();

        mMenuHidden = true;
        mTxtvHide.setText(getString(R.string.txtvShow));

        while (mLlTextContainer.getChildCount() > 1) {
            mLlTextContainer.removeViewAt(mLlTextContainer.getChildCount() - 1);
        }

        Space space = new Space(ArticleActivity.this);
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                mRlMenuBar.getHeight()));
        mLlTextContainer.addView(space, mLlMenuContainer.getChildCount() - 1);
    }

    private void showMenu() {
        PropertyValuesHolder translationPvh = PropertyValuesHolder.ofFloat("translationY", 0);
        PropertyValuesHolder rotationPvh = PropertyValuesHolder.ofFloat("rotation", 0);
        PropertyValuesHolder scaleXpvh = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.4f, 1.0f);
        PropertyValuesHolder scaleYpvh = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.4f, 1.0f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(mImgvHide, rotationPvh,
                scaleXpvh, scaleYpvh);
        animator.start();

        animator = ObjectAnimator.ofPropertyValuesHolder(mLlMenuContainer, translationPvh);
        animator.start();

        mMenuHidden = false;
        mTxtvHide.setText(getString(R.string.txtvHide));

        while (mLlTextContainer.getChildCount() > 1) {
            mLlTextContainer.removeViewAt(mLlTextContainer.getChildCount() - 1);
        }

        Space space = new Space(ArticleActivity.this);
        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                mLlMenuContainer.getHeight()));
        mLlTextContainer.addView(space, mLlMenuContainer.getChildCount() - 1);
    }

    private void setHideAction() {
        mImgvHide.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMenuHidden) {
                        showMenu();
                    } else {
                        hideMenu();
                    }
                }
            }
        );

        mTxtvHide.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mImgvHide.performClick();
                }
            }
        );
    }

    private void addTextBottomPadding() {
        final ViewTreeObserver observer = mLlMenuContainer.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mSpaceAdded) {
                        return;
                    }

                    if (observer.isAlive()) {
                        //noinspection deprecation
                        observer.removeGlobalOnLayoutListener(this);
                    }

                    if (mLlTextContainer.getChildCount() == 1 && mLlMenuContainer.getHeight() > 0) {
                        Space space = new Space(ArticleActivity.this);
                        space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                mLlMenuContainer.getHeight()));
                        mLlTextContainer.addView(space, mLlMenuContainer.getChildCount() - 1);

                        mSpaceAdded = true;
                    }
                }
            }
        );
    }

    private void setPrevAction() {
        mImgvPrev.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCanMoveBackward) {
                        PropertyValuesHolder scaleXpvh = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.4f, 1.0f);
                        PropertyValuesHolder scaleYpvh = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.4f, 1.0f);

                        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(v, scaleXpvh, scaleYpvh);
                        animator.start();

                        mTimeLine.prev();
                    }
                }
            }
        );
    }

    private void setNextAction() {
        mImgvNext.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCanMoveForward) {
                        PropertyValuesHolder scaleXpvh = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.4f, 1.0f);
                        PropertyValuesHolder scaleYpvh = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.4f, 1.0f);

                        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(v, scaleXpvh, scaleYpvh);
                        animator.start();

                        mTimeLine.next();
                    }
                }
            }
        );
    }

    private int getLineColor(String color) {
        try {
            return Color.parseColor(color);
        } catch (IllegalArgumentException exc) {
            return getResources().getColor(R.color.app_theme_blue);
        }
    }

    private void initializeAuthors() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ARTICLE)) {
            Article article = (Article) intent.getSerializableExtra(EXTRA_ARTICLE);

            List<Line> lines = article.getLines();
            final List<Pair<String, Integer>> authors = new ArrayList<>(
                Collections2.transform(
                    lines,
                    new Function<Line, Pair<String, Integer>>() {
                        @Override
                        public Pair<String, Integer> apply(Line input) {
                            String name = input.getName();
                            return new Pair<>(name, getLineColor(input.getColor()));
                        }
                    }
                )
            );

            for (int i = 0; i < authors.size(); ++i) {
                mColorToIndexMap.put(authors.get(i).second, i);
            }

            mSpinnerAuthors.setAdapter(mAuthorAdapter = new AuthorAdapter(authors));
            mSpinnerAuthors.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            mTimeLine.changeTimeLine(authors.get(position).second);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    }
            );
        }
    }

    private void prepareStories() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ARTICLE)) {
            Article article = (Article) intent.getSerializableExtra(EXTRA_ARTICLE);

            for (final Line line : article.getLines()) {
                ArrayList<Story> stories = new ArrayList<>();
                stories.addAll(
                    Collections2.filter(
                        article.getStories(),
                        new Predicate<Story>() {
                            @Override
                            public boolean apply(Story input) {
                                return line.getId().equals(input.getLine());
                            }
                        }
                    )
                );

                Collections.sort(
                    stories,
                    new Comparator<Story>() {
                        @Override
                        public int compare(Story lhs, Story rhs) {
                            int intLhs = Integer.parseInt(lhs.getLegend());
                            int intRhs = Integer.parseInt(rhs.getLegend());

                            if (intLhs < intRhs) {
                                return -1;
                            } else if (intLhs > intRhs) {
                                return 1;
                            }

                            return 0;
                        }
                    }
                );

                mColorToStoriesMap.put(getLineColor(line.getColor()), stories);
                mLineToStoriesMap.put(line, stories);
            }
        }
    }

    private void removeEmptyLines() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ARTICLE)) {
            Article article = (Article) intent.getSerializableExtra(EXTRA_ARTICLE);

            for (int i = article.getLines().size() - 1; i >= 0; --i) {
                final Line line = article.getLines().get(i);

                ArrayList<Story> stories = new ArrayList<>();
                stories.addAll(
                        Collections2.filter(
                                article.getStories(),
                                new Predicate<Story>() {
                                    @Override
                                    public boolean apply(Story input) {
                                        return line.getId().equals(input.getLine());
                                    }
                                }
                        )
                );

                boolean onlyNoneElements = true;
                for (Story s : stories) {
                    onlyNoneElements &= s.getHtml().isEmpty();
                }

                if (onlyNoneElements) {
                    article.getLines().remove(i);
                }
            }
        }
    }

    private void disableView(View v) {
        PropertyValuesHolder scaleXpvh = PropertyValuesHolder.ofFloat("scaleX", 0.75f);
        PropertyValuesHolder scaleYpvh = PropertyValuesHolder.ofFloat("scaleY", 0.75f);
        PropertyValuesHolder alphaPvh = PropertyValuesHolder.ofFloat("alpha", 0.35f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(v, scaleXpvh, scaleYpvh, alphaPvh);
        animator.start();
    }

    private void enableView(View v) {
        PropertyValuesHolder scaleXpvh = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
        PropertyValuesHolder scaleYpvh = PropertyValuesHolder.ofFloat("scaleY", 1.0f);
        PropertyValuesHolder alphaPvh = PropertyValuesHolder.ofFloat("alpha", 1.0f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(v, scaleXpvh, scaleYpvh, alphaPvh);
        animator.start();
    }

    private void createTimeLineFragment() {
        ArrayList<TimeLine> timeLines = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ARTICLE)) {
            Article article = (Article) intent.getSerializableExtra(EXTRA_ARTICLE);
            labels.addAll(
                    Collections2.transform(
                            article.getLegends(),
                            new Function<Legend, String>() {
                                @Override
                                public String apply(Legend input) {
                                    return input.getName();
                                }
                            }
                    )
            );

            for (Line line : article.getLines()) {
                List<Story> stories = mLineToStoriesMap.get(line);

                if (stories != null && line != null) {
                    boolean shouldBeTrimmed = false;
                    for (Story s : stories) {
                        shouldBeTrimmed |= !s.getHtml().isEmpty();
                    }

                    if (shouldBeTrimmed) {
                        int lastStoryIndex = stories.size();
                        while (--lastStoryIndex >= 0 && stories.get(lastStoryIndex).getHtml().isEmpty()) {
                            stories.remove(lastStoryIndex);
                        }
                    }

                    ArrayList<TimeLineFragment.CircleType> pattern = new ArrayList<>();

                    for (Story s : stories) {
                        pattern.add(s.getHtml().isEmpty() ? TimeLineFragment.CircleType.None :
                                TimeLineFragment.CircleType.Empty);
                    }

                    int offset = 0;
                    int initialSize = pattern.size();
                    //noinspection StatementWithEmptyBody
                    while (offset++ < initialSize && pattern.get(0) == TimeLineFragment.CircleType.None) {
                        pattern.remove(0);
                    }
                    --offset;

                    if (offset + pattern.size() > article.getLegends().size()) {
                        if ((offset = article.getLegends().size() - pattern.size()) < 0) {
                            offset = 0;
                        }
                    }

                    timeLines.add(new TimeLine(line.getColor(), getLineColor(line.getColor()),
                            getResources().getColor(android.R.color.white), offset,
                            pattern));
                }
            }
        }

        mTimeLine = TimeLineCompositeFragment.createInstance(timeLines, labels);
        mTimeLine.setOnTimeLineChangedListener(
                new TimeLineCompositeFragment.OnTimeLineChangedListener() {
                    @Override
                    public void onTimeLineChanged(int newTimeLineColor, int absolutePosition,
                                                  boolean canMoveForward, boolean canMoveBackward) {
                        if (canMoveForward != mCanMoveForward) {
                            if (canMoveForward) {
                                enableView(mImgvNext);
                            } else {
                                disableView(mImgvNext);
                            }
                        }

                        if (canMoveBackward != mCanMoveBackward) {
                            if (canMoveBackward) {
                                enableView(mImgvPrev);
                            } else {
                                disableView(mImgvPrev);
                            }
                        }

                        mCanMoveForward = canMoveForward;
                        mCanMoveBackward = canMoveBackward;

                        if (mColorToIndexMap.containsKey(newTimeLineColor)) {
                            if (mAuthorAdapter != null) {
                                mSpinnerAuthors.setSelection(mColorToIndexMap.get(newTimeLineColor));
                            }

                            List<Story> stories = mColorToStoriesMap.get(newTimeLineColor);
                            if (stories != null && stories.size() > absolutePosition) {
                                Story story = stories.get(absolutePosition);
                                mTxtvArticle.setText(story.getHtml());
                            }
                        }
                    }
                }
        );

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        TimeLineCompositeFragment f = (TimeLineCompositeFragment) getSupportFragmentManager().
                findFragmentByTag(TAG_TIME_LINE_FRAGMENT);
        if (f != null) {
            ft.remove(f);
        }

        ft.add(R.id.flTimeLineContainer, mTimeLine, TAG_TIME_LINE_FRAGMENT);
        ft.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHideAction();
        setPrevAction();
        setNextAction();

        addTextBottomPadding();

        removeEmptyLines();
        prepareStories();
        initializeAuthors();

        createTimeLineFragment();

        disableView(mImgvPrev);
    }
}
