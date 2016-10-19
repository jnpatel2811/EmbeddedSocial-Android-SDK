/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */

package com.microsoft.socialplus.ui.fragment;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.microsoft.socialplus.account.UserAccount;
import com.microsoft.socialplus.actions.Action;
import com.microsoft.socialplus.actions.ActionTagFilter;
import com.microsoft.socialplus.actions.ActionsLauncher;
import com.microsoft.socialplus.actions.OngoingActions;
import com.microsoft.socialplus.base.utils.BitmapUtils;
import com.microsoft.socialplus.base.utils.ObjectUtils;
import com.microsoft.socialplus.base.utils.ViewUtils;
import com.microsoft.socialplus.data.model.AccountData;
import com.microsoft.socialplus.data.model.AccountDataDifference;
import com.microsoft.socialplus.image.CoverLoader;
import com.microsoft.socialplus.image.ImageLoader;
import com.microsoft.socialplus.image.ImageLocation;
import com.microsoft.socialplus.image.ImageViewContentLoader;
import com.microsoft.socialplus.image.UserPhotoLoader;
import com.microsoft.socialplus.sdk.R;
import com.microsoft.socialplus.ui.activity.base.BaseActivity;
import com.microsoft.socialplus.ui.fragment.base.ActionListener;
import com.microsoft.socialplus.ui.fragment.base.BaseFragmentWithProgress;
import com.microsoft.socialplus.ui.fragment.module.PhotoProviderModule;
import com.microsoft.socialplus.ui.theme.ThemeAttributes;
import com.microsoft.socialplus.ui.util.FieldNotEmptyValidator;
import com.microsoft.socialplus.ui.util.FitWidthSizeSpec;
import com.microsoft.socialplus.ui.view.TextInput;

import java.util.LinkedList;
import java.util.List;

/**
 * Fragment to edit profile.
 */
@SuppressWarnings("FieldCanBeLocal")
public class EditProfileFragment extends BaseFragmentWithProgress {
	private static final String PREF_PHOTO_URI = "photo";
	private static final String PREF_PHOTO_CHANGED = "photoChanged";

	private final PhotoProviderModule photoProvider;

	private TextInput firstNameView;
	private TextInput lastNameView;
	private TextInput bioView;
	private View uploadPhotoView;
	private View photoLayout;
	private ImageView largePhotoView;
	private ImageView profilePhotoView;
	private SwitchCompat privacySwitch;
	private ImageViewContentLoader largePhotoLoader;
	private ImageViewContentLoader profilePhotoLoader;

	private EditImageOnClickListener editPhotoOnClickListener;
	private SelectImageOnClickListener selectPhotoOnClickListener;

	private List<TextInput> inputFields = new LinkedList<>();

	private Uri userPhotoUri;
	private boolean photoChanged = false;

	public EditProfileFragment() {
		photoProvider = new PhotoProviderModule(this, new SelectProfilePhotoConsumer());

		addModule(photoProvider);
		addActionListener(new ActionTagFilter(Action.Tags.UPDATE_ACCOUNT), new ActionListener() {
			@Override
			protected void onActionFailed(Action action, String error) {
				onUpdateFailed();
			}

			@Override
			protected void onActionSucceeded(Action action) {
				onUpdateSucceeded();
			}

			@Override
			protected void onActionsCompletionMissed(List<Action> completedActions, List<Action> succeededActions, List<Action> failedActions) {
				if (!succeededActions.isEmpty()) {
					onUpdateSucceeded();
				} else if (!failedActions.isEmpty()) {
					onUpdateFailed();
				}
			}
		});
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		BaseActivity owner = getOwner();
		owner.showBottomBar();
		owner.setOnDoneClickListener(v -> onDone());
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.sp_fragment_edit_profile;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initViews(view);
		if (savedInstanceState != null) {
			userPhotoUri = savedInstanceState.getParcelable(PREF_PHOTO_URI);
			photoChanged = savedInstanceState.getBoolean(PREF_PHOTO_CHANGED);
			if (photoChanged) {
				updatePhotoLayout(userPhotoUri != null);
				if (userPhotoUri != null) {
					photoProvider.loadBitmap(userPhotoUri);
				}
			} else {
				renderUserPhoto(UserAccount.getInstance().getAccountDetails());
			}
		} else {
			renderAccountData();
		}
	}

	private void onUpdateSucceeded() {
		finishActivity();
	}

	private void onUpdateFailed() {
		setProgressVisible(false);
		showToast(R.string.sp_message_network_error);
	}

	private void onDone() {
		if (checkCorrectness()) {
			hideKeyboard();
			AccountDataDifference difference = collectAccountDataDifference();
			if (difference.isEmpty()) {
				finishActivity();
			} else {
				setProgressVisible(true);
				ActionsLauncher.updateAccount(getContext(), difference);
			}
		}
	}

	public boolean checkCorrectness() {
		boolean result = true;
		TextInput firstViewWithError = null;
		for (TextInput inputView : inputFields) {
			boolean viewInputIsCorrect = inputView.validate();
			result &= viewInputIsCorrect;
			if (!viewInputIsCorrect && firstViewWithError == null) {
				firstViewWithError = inputView;
			}
		}
		if (firstViewWithError != null) {
			firstViewWithError.focusAndShowKeyboard();
			Toast.makeText(getContext(), R.string.sp_message_correct_input, Toast.LENGTH_SHORT).show();
		}
		return result;
	}

	@NonNull
	private AccountDataDifference collectAccountDataDifference() {
		AccountData accountData = UserAccount.getInstance().getAccountDetails();
		AccountDataDifference difference = new AccountDataDifference();
		if (photoChanged) {
			difference.setNewPhoto(userPhotoUri);
		}
		String firstName = firstNameView.getText();
		String lastName = lastNameView.getText();
		String bio = bioView.getText();
		boolean firstNameChanged = !ObjectUtils.equal(firstName, accountData.getFirstName());
		boolean lastNameChanged = !ObjectUtils.equal(lastName, accountData.getLastName());
		boolean bioChanged = !(ObjectUtils.equal(bio, accountData.getBio()) || TextUtils.isEmpty(bio) && TextUtils.isEmpty(accountData.getBio()));
		if (firstNameChanged || lastNameChanged || bioChanged) {
			difference.setNewPublicInfo(firstName, lastName, bio);
		}
		boolean isPrivate = privacySwitch.isChecked();
		if (isPrivate != accountData.isPrivate()) {
			difference.setNewPrivacy(isPrivate);
		}
		return difference;
	}

	protected void renderAccountData() {
		AccountData accountData = UserAccount.getInstance().getAccountDetails();
		firstNameView.setText(accountData.getFirstName());
		lastNameView.setText(accountData.getLastName());
		bioView.setText(accountData.getBio());
		privacySwitch.setChecked(accountData.isPrivate());

		renderUserPhoto(accountData);
	}

	private void renderUserPhoto(AccountData accountData) {
		ImageLocation photoLocation = accountData.getUserPhotoLocation();
		boolean hasPhoto = photoLocation != null;
		updatePhotoLayout(hasPhoto);
		if (hasPhoto) {
			if (largePhotoView != null) {
				largePhotoLoader = new CoverLoader(largePhotoView);
				largePhotoLoader.load(photoLocation, ViewUtils.getDisplayWidth(getOwner()));
			}

			if (isTablet()) {
				profilePhotoLoader = new UserPhotoLoader(profilePhotoView);
				profilePhotoLoader.load(
					photoLocation,
					getResources().getDimensionPixelSize(R.dimen.sp_user_icon_size)
				);
			}
		}
	}

	protected void updatePhotoLayout(boolean hasPhoto) {
		if (photoLayout != null) {
			ViewUtils.setVisible(photoLayout, hasPhoto);
		}
		if (isTablet()) {
			uploadPhotoView.setOnClickListener((hasPhoto) ? editPhotoOnClickListener : selectPhotoOnClickListener);
		} else {
			ViewUtils.setVisible(uploadPhotoView, !hasPhoto);
		}
	}

	protected void initViews(View view) {
		editPhotoOnClickListener = new EditImageOnClickListener(photoProvider);
		selectPhotoOnClickListener = new SelectImageOnClickListener(photoProvider);

		firstNameView = findView(view, R.id.sp_firstNameLayout);
		firstNameView.setValidator(new FieldNotEmptyValidator(getContext()));
		inputFields.add(firstNameView);
		lastNameView = findView(view, R.id.sp_lastNameLayout);
		lastNameView.setValidator(new FieldNotEmptyValidator(getContext()));
		inputFields.add(lastNameView);
		bioView = findView(view, R.id.sp_bioLayout);
		uploadPhotoView = findView(view, R.id.sp_uploadPhotoLayout);
		largePhotoView = findView(view, R.id.sp_largePhoto);
		profilePhotoView = findView(view, R.id.sp_profileImage);
		photoLayout = findView(view, R.id.sp_photoLayout);
		if (findView(view, R.id.sp_editPhoto) != null) {
			setOnClickListener(view, R.id.sp_editPhoto, editPhotoOnClickListener);
		}
		if (!isTablet()) {
			uploadPhotoView.setOnClickListener(selectPhotoOnClickListener);
		}
		privacySwitch = findView(view, R.id.sp_privacySwitch);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (userPhotoUri != null) {
			outState.putParcelable(PREF_PHOTO_URI, userPhotoUri);
		}
		outState.putBoolean(PREF_PHOTO_CHANGED, photoChanged);
	}

	@Override
	public boolean onBackPressed() {
		hideKeyboard();
		if (OngoingActions.hasActionsWithTag(Action.Tags.UPDATE_ACCOUNT)) {
			showToast(R.string.sp_message_wait_until_account_updated);
			return false;
		}
		return super.onBackPressed();
	}

	@Override
	protected void setProgressVisible(boolean progressVisible) {
		super.setProgressVisible(progressVisible);
		if (progressVisible) {
			getOwner().hideBottomBar();
		} else {
			getOwner().showBottomBar();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		photoProvider.dispose();
	}

	private class EditImageOnClickListener implements View.OnClickListener {
		final PhotoProviderModule photoProviderModule;

		public EditImageOnClickListener(PhotoProviderModule photoProviderModule) {
			this.photoProviderModule = photoProviderModule;
		}

		@Override
		public void onClick(View v) {
			photoProviderModule.showEditImageDialog();
		}
	}

	private class SelectImageOnClickListener implements View.OnClickListener {
		final PhotoProviderModule photoProviderModule;

		public SelectImageOnClickListener(PhotoProviderModule photoProviderModule) {
			this.photoProviderModule = photoProviderModule;
		}

		@Override
		public void onClick(View v) {
			photoProviderModule.showSelectImageDialog();
		}
	}

	private class SelectProfilePhotoConsumer implements PhotoProviderModule.Consumer {
		@Override
		public void onPhotoSelected(Uri photoUri) {
			if (largePhotoView != null) {
				ImageLoader.cancel(largePhotoView);
				largePhotoView.setImageBitmap(null);
			}
			if (isTablet()) {
				ImageLoader.cancel(profilePhotoView);
				profilePhotoView.setImageResource(ThemeAttributes.getResourceId(getContext(), R.styleable.sp_AppTheme_sp_userNoPhotoIcon));
			}
			userPhotoUri = photoUri;
			photoChanged = true;
			updatePhotoLayout(photoUri != null);
		}

		@Override
		public void onPhotoLoaded(Uri photoUri, Bitmap thumbnail) {
			if (userPhotoUri.equals(photoUri) && thumbnail != null && isAdded()) {
				if (largePhotoView != null) {
					largePhotoView.setImageBitmap(thumbnail);
				}
				profilePhotoView.setImageBitmap(thumbnail);
			}
		}

		@Override
		public BitmapUtils.SizeSpec getSizeSpec() {
			return new FitWidthSizeSpec(getOwner());
		}
	}
}
