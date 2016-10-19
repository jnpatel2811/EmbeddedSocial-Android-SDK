/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */

package com.microsoft.socialplus.server.model.relationship;

import com.microsoft.socialplus.autorest.models.PostBlockedUserRequest;
import com.microsoft.rest.ServiceException;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.socialplus.server.exception.NetworkRequestException;

import java.io.IOException;

import retrofit2.Response;

public class BlockUserRequest extends UserRelationshipRequest {

    public BlockUserRequest(String relationshipUserHandle) {
        super(relationshipUserHandle);
    }

    @Override
    public Response send() throws NetworkRequestException {
        PostBlockedUserRequest request = new PostBlockedUserRequest();
        request.setUserHandle(relationshipUserHandle);
        ServiceResponse<Object> serviceResponse;
        try {
            serviceResponse = BLOCKED.postBlockedUser(request, authorization);
        } catch (ServiceException|IOException e) {
            throw new NetworkRequestException(e.getMessage());
        }
        checkResponseCode(serviceResponse);

        return serviceResponse.getResponse();
    }
}
