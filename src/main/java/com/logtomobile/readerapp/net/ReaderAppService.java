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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.logtomobile.readerapp.StoryStreamApplication;
import com.logtomobile.readerapp.model.Article;
import com.logtomobile.readerapp.model.ArticleHeader;
import com.logtomobile.readerapp.net.event.ArticleDetailFetchedEvent;
import com.logtomobile.readerapp.net.event.ArticlesHeadersFetchedEvent;
import com.logtomobile.readerapp.net.event.DeviceOfflineEvent;
import com.logtomobile.readerapp.net.request.GetArticleDetail;
import com.logtomobile.readerapp.net.request.GetArticles;
import com.logtomobile.readerapp.net.request.OnRequestFinishedListener;
import com.logtomobile.readerapp.net.util.NetUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Marcin Przepiórkowski
 */
@Singleton
public class ReaderAppService {
    @Inject
    private GetArticles mGetArticles;

    @Inject
    private GetArticleDetail mGetArticleDetail;

    private final List<ArticleHeader> mCachedHeaders = new ArrayList<>();
    private final Map<String, Article> mCachedArticles = new HashMap<>();

    private void reportInternetConnectionProblem() {
        EventBus eventBus = StoryStreamApplication.getAppEventBus();
        eventBus.post(new DeviceOfflineEvent());
    }

    @Inject
    public ReaderAppService() {
    }

    public void fetchArticlesHeaders() {
        if (!NetUtils.isOnline(StoryStreamApplication.getAppContext())) {
            reportInternetConnectionProblem();
        } else {
            mGetArticles.execute(
                    new OnRequestFinishedListener<List<ArticleHeader>>() {
                        @Override
                        public void onRequestFinished(int responseCode, @Nullable List<ArticleHeader> response, boolean canceled) {
                            if (responseCode == 200 && !canceled && response != null) {
                                mCachedHeaders.clear();
                                mCachedHeaders.addAll(response);
                            }

                            EventBus eventBus = StoryStreamApplication.getAppEventBus();
                            eventBus.post(new ArticlesHeadersFetchedEvent(responseCode == 200 && !canceled,
                                    response));
                        }
                    }
            );
        }
    }

    public void fetchArticleDetails(@NonNull ArticleHeader header) {
        checkNotNull(header, "article cannot be null");

        if (!NetUtils.isOnline(StoryStreamApplication.getAppContext())) {
            reportInternetConnectionProblem();
        } else {
            mGetArticleDetail.execute(
                    header.getId(),
                    new OnRequestFinishedListener<Article>() {
                        @Override
                        public void onRequestFinished(int responseCode, @Nullable Article response, boolean canceled) {
                            if (responseCode == 200 && !canceled && response != null) {
                                mCachedArticles.put(response.getId(), response);
                            }

                            EventBus eventBus = StoryStreamApplication.getAppEventBus();
                            eventBus.post(new ArticleDetailFetchedEvent(responseCode == 200 && !canceled,
                                    response));
                        }
                    }
            );
        }
    }

    public @NonNull List<ArticleHeader> getCachedHeaders() {
        return new ArrayList<>(mCachedHeaders);
    }

    public @NonNull Optional<Article> getArticleDetail(@NonNull ArticleHeader header) {
        return Optional.fromNullable(mCachedArticles.get(header.getId()));
    }
}