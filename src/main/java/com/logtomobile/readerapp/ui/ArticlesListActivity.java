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
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.logtomobile.readerapp.R;
import com.logtomobile.readerapp.model.Article;
import com.logtomobile.readerapp.model.ArticleHeader;
import com.logtomobile.readerapp.net.ReaderAppService;
import com.logtomobile.readerapp.net.event.ArticleDetailFetchedEvent;
import com.logtomobile.readerapp.net.event.ArticlesHeadersFetchedEvent;
import com.logtomobile.readerapp.net.event.NetErrorEvent;
import com.logtomobile.readerapp.ui.dialog.IncompleteStoryDialogFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Marcin Przepiórkowski
 */
@ContentView(R.layout.activity_articles_list)
public class ArticlesListActivity extends BaseActivity {
    public class ArticleAdapter extends ArrayAdapter<ArticleHeader> {
        class ViewHolder {
            public ImageView mImgvRightArrow;
            public TextView mTxtvArticleName;
            public TextView mTxtvAuthor;
            public TextView mTxtvDate;
            public ProgressBar mPbDownload;
        }

        private final List<ArticleHeader> mArticles;
        private final LayoutInflater mInflater;

        public ArticleAdapter(@NonNull List<ArticleHeader> articles) {
            super(ArticlesListActivity.this, R.layout.lvitem_article, articles);

            mArticles = checkNotNull(articles);
            mInflater = LayoutInflater.from(ArticlesListActivity.this);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.lvitem_article, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.mImgvRightArrow = (ImageView) convertView.findViewById(R.id.imgvRightArrow);
                viewHolder.mTxtvArticleName = (TextView) convertView.findViewById(R.id.txtvArticleName);
                viewHolder.mTxtvAuthor = (TextView) convertView.findViewById(R.id.txtvAuthor);
                viewHolder.mTxtvDate = (TextView) convertView.findViewById(R.id.txtvDate);
                viewHolder.mPbDownload = (ProgressBar) convertView.findViewById(R.id.pbDownloadProgress);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.mTxtvAuthor.setText(mArticles.get(position).getAuthor());

            try {
                DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.US);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                viewHolder.mTxtvDate.setText(df.format(format.parse(
                        mArticles.get(position).getCreateDate())));
            } catch (Exception e) {
                viewHolder.mTxtvDate.setText(mArticles.get(position).getCreateDate());
            }

            viewHolder.mImgvRightArrow.setColorFilter(getResources().getColor(R.color.app_theme_blue));
            viewHolder.mTxtvArticleName.setText(mArticles.get(position).getName());
            viewHolder.mPbDownload.setTag(mArticles.get(position).getId());

            if (viewHolder.mPbDownload.getTag().equals(mDownloadingArticleId)) {
                viewHolder.mPbDownload.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mPbDownload.setVisibility(View.INVISIBLE);
            }

            convertView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDownloadInProgress) {
                            return;
                        }

                        if (mCachingEnabled) {
                            Intent intent = new Intent(ArticlesListActivity.this, ArticleActivity.class);
                            Optional<Article> optArticle = mAppService.getArticleDetail(mArticlesList.get(position));

                            if (optArticle.isPresent()) {
                                intent.putExtra(ArticleActivity.EXTRA_ARTICLE, optArticle.get());
                                startActivity(intent);
                            } else {
                                mDownloadInProgress = true;
                                mDownloadingArticleId = mArticlesList.get(position).getId();
                                viewHolder.mPbDownload.setVisibility(View.VISIBLE);

                                mAppService.fetchArticleDetails(mArticlesList.get(position));
                            }
                        } else {
                            mDownloadInProgress = true;
                            mDownloadingArticleId = mArticlesList.get(position).getId();
                            viewHolder.mPbDownload.setVisibility(View.VISIBLE);

                            mAppService.fetchArticleDetails(mArticlesList.get(position));
                        }
                    }
                }
            );

            return convertView;
        }
    }

    @Inject
    private ReaderAppService mAppService;

    @InjectView(R.id.lvArticles)
    private ListView mLvArticles;

    @InjectView(R.id.swipeRefreshContainer)
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private List<ArticleHeader> mArticlesList = new ArrayList<>();
    private ArticleAdapter mListAdapter;

    private boolean mDownloadInProgress;
    private String mDownloadingArticleId;

    private boolean mCachingEnabled = false;

    private void initializeListView() {
        mListAdapter = new ArticleAdapter(mArticlesList);
        mAppService.fetchArticlesHeaders();
        mLvArticles.setAdapter(mListAdapter);
    }

    private void initializePullToRefresh() {
        mSwipeRefreshLayout.setColorScheme(
                android.R.color.holo_green_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_light
        );
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mSwipeRefreshLayout.setRefreshing(true);
                        mAppService.fetchArticlesHeaders();
                    }
                }
        );
    }

    private boolean isArticleValid(@NonNull Article article) {
        return !article.getLegends().isEmpty() && !article.getStories().isEmpty()
                && !article.getLines().isEmpty();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeListView();
        initializePullToRefresh();
    }

    @Override
    protected void onStart() {
        mArticlesList.clear();
        mArticlesList.addAll(mAppService.getCachedHeaders());

        mListAdapter.notifyDataSetChanged();

        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.articles_list, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            mSwipeRefreshLayout.setRefreshing(true);
            mAppService.fetchArticlesHeaders();
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onArticlesHeadersFetched(@NonNull ArticlesHeadersFetchedEvent event) {
        mSwipeRefreshLayout.setRefreshing(false);

        if (event.success()) {
            mArticlesList.clear();
            mArticlesList.addAll(event.getArticleHeaders());

            mListAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    public void onArticleDetailsFetched(@NonNull ArticleDetailFetchedEvent event) {
        mDownloadInProgress = false;
        mDownloadingArticleId = null;

        mListAdapter.notifyDataSetChanged();

        Intent intent = new Intent(ArticlesListActivity.this, ArticleActivity.class);
        if (event.success() && event.getArticle() != null) {
            if (isArticleValid(event.getArticle())) {
                intent.putExtra(ArticleActivity.EXTRA_ARTICLE, event.getArticle());
                startActivity(intent);
            } else {
                IncompleteStoryDialogFragment dialog = new IncompleteStoryDialogFragment();
                dialog.show(getSupportFragmentManager(), "incomplete story dialog");
            }
        }
    }

    @Override
    public void onNetError(@NonNull NetErrorEvent event) {
        mDownloadInProgress = false;
        mDownloadingArticleId = null;

        super.onNetError(event);
    }
}