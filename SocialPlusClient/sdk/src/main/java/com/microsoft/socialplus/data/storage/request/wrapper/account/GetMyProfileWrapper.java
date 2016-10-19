/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */

package com.microsoft.socialplus.data.storage.request.wrapper.account;

import com.microsoft.socialplus.data.storage.UserCache;
import com.microsoft.socialplus.data.storage.request.wrapper.AbstractNetworkMethodWrapper;
import com.microsoft.socialplus.server.model.account.GetMyProfileRequest;
import com.microsoft.socialplus.server.model.account.GetUserProfileRequest;
import com.microsoft.socialplus.server.model.account.GetUserProfileResponse;

import java.sql.SQLException;

public class GetMyProfileWrapper extends AbstractNetworkMethodWrapper<GetMyProfileRequest, GetUserProfileResponse> {

    private final UserCache userCache;

    public GetMyProfileWrapper(INetworkMethod<GetMyProfileRequest, GetUserProfileResponse> networkMethod,
                                 UserCache userCache) {

        super(networkMethod);
        this.userCache = userCache;
    }

    @Override
    protected void storeResponse(GetMyProfileRequest request, GetUserProfileResponse response)
            throws SQLException {

        userCache.storeUserProfile(response.getUser());
    }

    @Override
    protected GetUserProfileResponse getCachedResponse(GetMyProfileRequest request)
            throws SQLException {

        return new GetUserProfileResponse(userCache.getUserProfile(request.getUserHandle()));
    }
}
