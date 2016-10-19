/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */

package com.microsoft.socialplus.data.storage.syncadapter;

import com.microsoft.socialplus.base.GlobalObjectRegistry;
import com.microsoft.socialplus.server.SocialPlusServiceProvider;
import com.microsoft.socialplus.server.exception.BadRequestException;
import com.microsoft.socialplus.server.exception.NetworkRequestException;
import com.microsoft.socialplus.server.sync.ISynchronizable;
import com.microsoft.socialplus.server.sync.exception.OperationRejectedException;
import com.microsoft.socialplus.server.sync.exception.SynchronizationException;

/**
 * Base class for synchronization adapters.
 * @param <T>   sync item type
 */
public abstract class AbstractSyncAdapter<T> implements ISynchronizable {

	private final T item;
	private final SocialPlusServiceProvider serviceProvider;

	/**
	 * Creates an instance.
	 * @param item  the item to synchronize
	 */
	protected AbstractSyncAdapter(T item) {
		this.item = item;
		this.serviceProvider = GlobalObjectRegistry.getObject(SocialPlusServiceProvider.class);
	}

	/**
	 * Gets the item for synchronization.
	 * @return  sync item.
	 */
	protected final T getItem() {
		return item;
	}

	/**
	 * Gets Social Plus API service provider.
	 * @return  {@link SocialPlusServiceProvider} instance.
	 */
	protected final SocialPlusServiceProvider getServiceProvider() {
		return serviceProvider;
	}

	/**
	 * Is called when the item should be synchronized.
	 * @param item  the item to synchronize
	 * @throws NetworkRequestException  if network request fails
	 * @throws SynchronizationException if synchronization fails
	 */
	protected abstract void onSynchronize(T item)
		throws NetworkRequestException, SynchronizationException;

	/**
	 * Is called when synchronization is completed successfully.
	 * @param item  the item that was synchronized.
	 */
	protected abstract void onSynchronizationSuccess(T item);

	@Override
	public final void synchronize() throws SynchronizationException {
		try {
			onSynchronize(item);
		} catch (BadRequestException e) {
			throw new OperationRejectedException(e);
		} catch (NetworkRequestException e) {
			throw new SynchronizationException(e);
		}
	}

	@Override
	public final void onSynchronizationSuccess() {
		onSynchronizationSuccess(item);
	}
}
