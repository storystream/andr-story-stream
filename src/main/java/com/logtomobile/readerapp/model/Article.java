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

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author Marcin Przepiórkowski
 */
public class Article implements Serializable {
    @SerializedName("id")
    private String mId;

    @SerializedName("name")
    private String mName;

    @SerializedName("create")
    private String mCreateDate;

    @SerializedName("legends")
    private List<Legend> mLegends;

    @SerializedName("line")
    private List<Line> mLines;

    @SerializedName("article")
    private List<Story> mStories;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getCreateDate() {
        return mCreateDate;
    }

    public void setCreateDate(String createDate) {
        this.mCreateDate = createDate;
    }

    public List<Legend> getLegends() {
        return mLegends;
    }

    public void setLegends(List<Legend> legends) {
        this.mLegends = legends;
    }

    public List<Line> getLines() {
        return mLines;
    }

    public void setLines(List<Line> lines) {
        this.mLines = lines;
    }

    public List<Story> getStories() {
        return mStories;
    }

    public void setStories(List<Story> history) {
        this.mStories = history;
    }
}