/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */

package com.microsoft.socialplus.server.model.account;

import com.microsoft.socialplus.autorest.models.LinkedAccountView;
import com.microsoft.socialplus.autorest.models.UserProfileView;
import com.microsoft.rest.ServiceException;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.socialplus.server.exception.NetworkRequestException;
import com.microsoft.socialplus.server.model.UserRequest;
import com.microsoft.socialplus.server.model.view.UserAccountView;

import java.io.IOException;
import java.util.List;

public class GetUserAccountRequest extends UserRequest {

    public GetUserAccountRequest() {}

    public GetUserAccountRequest(String authorization) {
        this.authorization = authorization;
    }

    @Override
    public GetUserAccountResponse send() throws NetworkRequestException {
        ServiceResponse<UserProfileView> myProfileResponse;
        ServiceResponse<List<LinkedAccountView>> myLinkedAccountsResponse;
        try {
            myProfileResponse = USERS.getMyProfile(authorization);
            myLinkedAccountsResponse = LINKED_ACCOUNTS.getLinkedAccounts(authorization);
        } catch (ServiceException|IOException e) {
            throw new NetworkRequestException(e.getMessage());
        }
        checkResponseCode(myProfileResponse);
        checkResponseCode(myLinkedAccountsResponse);

        return new GetUserAccountResponse(
                new UserAccountView(myProfileResponse.getBody(), myLinkedAccountsResponse.getBody()));
    }
}
