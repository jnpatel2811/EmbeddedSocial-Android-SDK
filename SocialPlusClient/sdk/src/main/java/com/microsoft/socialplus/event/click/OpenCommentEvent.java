/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */

package com.microsoft.socialplus.event.click;

import com.microsoft.socialplus.base.event.AbstractEvent;
import com.microsoft.socialplus.server.model.view.CommentView;

public class OpenCommentEvent extends AbstractEvent {

	private final CommentView comment;
	private final boolean jumpToEdit;

	public OpenCommentEvent(CommentView comment) {
		this(comment, false);
	}

	public OpenCommentEvent(CommentView comment, boolean jumpToEdit) {
		this.comment = comment;
		this.jumpToEdit = jumpToEdit;
	}

	public CommentView getComment() {
		return comment;
	}

	public boolean jumpToEdit() {
		return jumpToEdit;
	}
}
