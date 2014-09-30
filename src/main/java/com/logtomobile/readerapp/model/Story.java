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

/**
 * @author Marcin Przepiórkowski
 */
public class Story implements Serializable {
    @SerializedName("id")
    private String mId;

    @SerializedName("parent")
    private String mParent;

    @SerializedName("line")
    private String mLine;

    @SerializedName("legend")
    private String mLegend;

    @SerializedName("html")
    private String mHtml;

    @SerializedName("create")
    private String mCreateDate;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getParent() {
        return mParent;
    }

    public void setParent(String parent) {
        this.mParent = parent;
    }

    public String getLine() {
        return mLine;
    }

    public void setLine(String line) {
        this.mLine = line;
    }

    public String getLegend() {
        return mLegend;
    }

    public void setLegend(String legend) {
        this.mLegend = legend;
    }

    public String getHtml() {
        return mHtml;
    }

    public void setHtml(String html) {
        this.mHtml = html;
    }

    public String getCreateDate() {
        return mCreateDate;
    }

    public void setCreateDate(String createDate) {
        this.mCreateDate = createDate;
    }
}