/*
 * Aurora Store
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * Yalp Store
 * Copyright (C) 2018 Sergey Yeriomin <yeriomin@gmail.com>
 *
 * Aurora Store is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Aurora Store is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora Store.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.dragons.aurora.api;

import android.util.Log;

import com.dragons.aurora.model.App;
import com.dragons.aurora.model.AppBuilder;
import com.dragons.aurora.model.Filter;
import com.dragons.aurora.playstoreapiv2.DocV2;
import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CustomAppListIterator implements Iterator {

    protected boolean enableFilter = false;
    protected Filter filter = new Filter();
    protected AppListIteratorV2 iterator;
    private List<String> relatedTags = new ArrayList<>();

    public CustomAppListIterator(AppListIteratorV2 iterator) {
        this.iterator = iterator;
    }

    public List<String> getRelatedTags() {
        Set<String> set = new HashSet<>(relatedTags);
        relatedTags.clear();
        relatedTags.addAll(set);
        return relatedTags;
    }

    public void setEnableFilter(boolean enableFilter) {
        this.enableFilter = enableFilter;
    }

    public void setGooglePlayApi(GooglePlayAPI googlePlayApi) {
        iterator.setGooglePlayApi(googlePlayApi);
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public List<App> next() {
        List<App> apps = new ArrayList<>();
        for (DocV2 docV2 : iterator.next()) {
            if (docV2.getDocType() == 53)
                relatedTags.add(docV2.getTitle());
            else if (docV2.getDocType() == 1) {
                addApp(apps, AppBuilder.build(docV2));
            } else {
                /*
                 * This one is either a movie, music or book, will exploit it at some point of time
                 * Movies = 6, Music = 2, Audio Books = 64
                 */
                continue;
            }
        }
        return apps;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    private boolean shouldSkip(App app) {
        return (!filter.isPaidApps() && !app.isFree())
                || (!filter.isAppsWithAds() && app.containsAds())
                || (!filter.isGsfDependentApps() && !app.getDependencies().isEmpty())
                || (filter.getRating() > 0 && app.getRating().getAverage() < filter.getRating())
                || (filter.getDownloads() > 0 && app.getInstalls() < filter.getDownloads());
    }

    private void addApp(List<App> apps, App app) {
        if (enableFilter && shouldSkip(app)) {
            Log.i("Apps&Tools", "Filtering out " + app.getPackageName());
        } else {
            apps.add(app);
        }
    }
}
