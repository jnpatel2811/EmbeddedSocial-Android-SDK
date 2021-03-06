/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */

package com.microsoft.embeddedsocial.service.handler;

import com.microsoft.embeddedsocial.base.service.IServiceIntentHandler;
import com.microsoft.embeddedsocial.service.ServiceAction;
import com.microsoft.embeddedsocial.service.WorkerService;

import android.content.Context;
import android.content.Intent;

public class BackgroundInitializationHandler implements IServiceIntentHandler<ServiceAction> {

    private final Context context;

    public BackgroundInitializationHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handleIntent(ServiceAction action, Intent intent) {
        WorkerService.getLauncher(context).launchService(ServiceAction.FCM_REGISTER);
    }

    @Override
    public void dispose() {  }
}
