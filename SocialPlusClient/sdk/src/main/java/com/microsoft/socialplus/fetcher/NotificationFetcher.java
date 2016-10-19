/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */

package com.microsoft.socialplus.fetcher;

import com.microsoft.socialplus.base.function.Predicate;
import com.microsoft.socialplus.base.utils.debug.DebugLog;
import com.microsoft.socialplus.fetcher.base.DataState;
import com.microsoft.socialplus.fetcher.base.Fetcher;
import com.microsoft.socialplus.fetcher.base.RequestType;
import com.microsoft.socialplus.server.ServerMethod;
import com.microsoft.socialplus.server.model.ListResponse;
import com.microsoft.socialplus.server.model.notification.GetNotificationFeedRequest;
import com.microsoft.socialplus.server.model.view.ActivityView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fetches data for the recent activity page. It filters items the app is not ready for (incorrect data can crash the app or corrupt UI).
 */
class NotificationFetcher extends Fetcher<ActivityView> {

    private final DataRequestExecutor<ActivityView, GetNotificationFeedRequest> activityRequestExecutor;
    private final Predicate<ActivityView> dataFilter;

    NotificationFetcher(ServerMethod<GetNotificationFeedRequest, ListResponse<ActivityView>> serverMethod, Predicate<ActivityView> dataFilter) {
        activityRequestExecutor = new BatchDataRequestExecutor<>(serverMethod, GetNotificationFeedRequest::new);
        this.dataFilter = dataFilter;
    }

    @Override
    protected List<ActivityView> fetchDataPage(DataState dataState, RequestType requestType, int pageSize) throws Exception {
        List<ActivityView> result = new ArrayList<>(pageSize);
        List<ActivityView> events = activityRequestExecutor.fetchData(dataState, requestType, pageSize);
        for (ActivityView event : events) {
            if (dataFilter.test(event)) {
                result.add(event);
            } else {
                DebugLog.e("invalid activity event");
                DebugLog.logObject(event);
            }
        }
        return result;
    }

}
