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

package com.logtomobile.readerapp.net.event;

import android.support.annotation.Nullable;

import com.logtomobile.readerapp.model.ArticleHeader;

import java.util.List;

/**
 * @author Marcin Przepiórkowski
 */
public class ArticlesHeadersFetchedEvent {
    private final boolean mSuccess;
    private final List<ArticleHeader> mArticlesHeaders;

    public ArticlesHeadersFetchedEvent(boolean success, @Nullable List<ArticleHeader> headers) {
        mSuccess = success;
        mArticlesHeaders = headers;
    }

    public boolean success() {
        return mSuccess;
    }

    public List<ArticleHeader> getArticleHeaders() {
        return mArticlesHeaders;
    }
}