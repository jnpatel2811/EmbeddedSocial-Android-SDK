/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 *
 */

package com.microsoft.socialplus.server.model.build;

import com.microsoft.rest.ServiceException;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.socialplus.autorest.BuildsOperations;
import com.microsoft.socialplus.autorest.BuildsOperationsImpl;
import com.microsoft.socialplus.autorest.models.BuildsCurrentResponse;
import com.microsoft.socialplus.server.exception.NetworkRequestException;
import com.microsoft.socialplus.server.model.UserRequest;

import java.io.IOException;

public class GetBuildInfoRequest extends UserRequest {

    private static final BuildsOperations BUILDS_OPERATIONS = new BuildsOperationsImpl(RETROFIT, CLIENT);

    @Override
    public BuildsCurrentResponse send() throws NetworkRequestException {
        ServiceResponse<BuildsCurrentResponse> serviceResponse;
        try {
            serviceResponse = BUILDS_OPERATIONS.getBuildsCurrent(appKey, bearerToken);
        } catch (ServiceException |IOException e) {
            throw new NetworkRequestException(e.getMessage());
        }
        checkResponseCode(serviceResponse);

        return serviceResponse.getBody();
    }
}