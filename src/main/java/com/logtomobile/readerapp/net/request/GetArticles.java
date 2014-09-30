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

package com.logtomobile.readerapp.net.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.logtomobile.readerapp.model.Article;
import com.logtomobile.readerapp.model.ArticleHeader;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Marcin Przepiórkowski
 */

@Singleton
public final class GetArticles {
    @Inject
    public GetArticles() {
    }

    public void execute(@Nullable OnRequestFinishedListener<List<ArticleHeader>> listener) {
        HttpGet<List<ArticleHeader>> getRequest = new HttpGet<>(
            false,
            "http://194.169.126.47/history/json_articles.php",
            new HttpGet.ServerResponseParser<List<ArticleHeader>>() {
                @Override
                public List<ArticleHeader> parse(@NonNull String json) {
                    Type type = new TypeToken<List<ArticleHeader>>() {
                    }.getType();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();

                    return gson.fromJson(json, type);
                }
            },
            listener
        );
        getRequest.execute();
    }
}