/*
 * Aurora Store
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
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

package com.dragons.aurora.task.playstore;

import android.content.Context;

import androidx.annotation.NonNull;

import com.dragons.aurora.CategoryManager;
import com.dragons.aurora.Filter;
import com.dragons.aurora.Util;
import com.dragons.aurora.api.CustomAppListIterator;
import com.dragons.aurora.model.App;

import java.util.ArrayList;
import java.util.List;

public class SearchTaskV2 extends BaseTask {

    public SearchTaskV2(Context context) {
        super(context);
    }

    public List<App> getSearchResults(@NonNull CustomAppListIterator iterator) {
        if (!iterator.hasNext()) {
            return new ArrayList<>();
        }
        List<App> apps = new ArrayList<>();
        while (iterator.hasNext() && apps.isEmpty()) {
            apps.addAll(getNextBatch(iterator));
        }
        return apps;
    }

    public List<App> getNextBatch(CustomAppListIterator iterator) {
        CategoryManager categoryManager = new CategoryManager(getContext());
        com.dragons.aurora.model.Filter filter = new Filter(getContext()).getFilterPreferences();
        List<App> apps = new ArrayList<>();
        for (App app : iterator.next()) {
            if (categoryManager.fits(app.getCategoryId(), filter.getCategory())) {
                apps.add(app);
            }
        }
        if (Util.filterGoogleAppsEnabled(context))
            return filterGoogleApps(apps);
        else
            return apps;
    }
}
