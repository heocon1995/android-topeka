/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.topeka.fragment;

import android.app.Activity;
import android.app.ActivityOptions;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toolbar;

import com.google.samples.apps.topeka.activity.CategorySelectionActivity;
import com.google.samples.apps.topeka.helper.PreferencesHelper;
import com.google.samples.apps.topeka.R;
import com.google.samples.apps.topeka.adapter.AvatarAdapter;
import com.google.samples.apps.topeka.model.Avatar;
import com.google.samples.apps.topeka.model.Player;
import com.google.samples.apps.topeka.widget.DoneFab;

/**
 * Enables selection of an {@link Avatar} and user name.
 */
public class SignInFragment extends Fragment implements View.OnClickListener,
        AdapterView.OnItemClickListener, View.OnLayoutChangeListener, TextWatcher {

    private static final String ARG_EDIT = "EDIT";
    private Player mPlayer;
    private EditText mFirstName;
    private EditText mLastInitial;
    private Avatar mSelectedAvatar = Avatar.ONE;
    private GridView mGridView;
    private DoneFab mDoneFab;
    private boolean edit;

    private static final String KEY_FIRST_NAME = "firstName";
    private static final String KEY_LAST_INITIAL = "lastInitial";
    private static final String KEY_AVATAR_ID = "avatarId";

    public static SignInFragment newInstance(boolean edit) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_EDIT, edit);
        SignInFragment fragment = new SignInFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final View contentView = inflater.inflate(R.layout.fragment_sign_in, container, false);
        contentView.addOnLayoutChangeListener(this);
        return contentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_FIRST_NAME, mFirstName.getText().toString());
        outState.putString(KEY_LAST_INITIAL, mLastInitial.getText().toString());
        outState.putInt(KEY_AVATAR_ID, mGridView.getSelectedItemPosition());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        assurePlayerInit();
        checkIsInEditMode();

        if (null == mPlayer || edit) {
            view.findViewById(R.id.empty).setVisibility(View.GONE);
            view.findViewById(R.id.content).setVisibility(View.VISIBLE);
            initContentViews(view);
            initContents();
        } else {
            CategorySelectionActivity.start(getActivity(), mPlayer);
            getActivity().finish();
        }
        super.onViewCreated(view, savedInstanceState);
    }

    private void checkIsInEditMode() {
        final Bundle arguments = getArguments();
        if (null != getArguments()) {
            edit = arguments.getBoolean(ARG_EDIT, false);
        } else {
            edit = false;
        }
    }

    private void initContentViews(View view) {
        ((Toolbar) view.findViewById(R.id.toolbar_sign_in)).setTitle(R.string.sign_in);
        ((Toolbar) view.findViewById(R.id.toolbar_choose_avatar))
                .setTitle(R.string.choose_avatar);
        mFirstName = (EditText) view.findViewById(R.id.first_name);
        mFirstName.addTextChangedListener(this);
        mLastInitial = (EditText) view.findViewById(R.id.last_initial);
        mLastInitial.addTextChangedListener(this);
        mDoneFab = (DoneFab) view.findViewById(R.id.check);
        mDoneFab.setOnClickListener(this);
    }

    private void setUpGridView(View container) {
        mGridView = (GridView) container.findViewById(R.id.avatars);
        mGridView.setAdapter(new AvatarAdapter(getActivity()));
        mGridView.setOnItemClickListener(this);
        mGridView.setNumColumns(calculateSpanCount());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check:
                savePlayer(getActivity());
                performSignInWithTransition(v);
                break;
            default:
                throw new UnsupportedOperationException(
                        "The onClick method has not been implemented for " + getResources()
                                .getResourceEntryName(v.getId()));
        }
    }

    private void performSignInWithTransition(View v) {
        FragmentActivity activity = getActivity();
        ActivityOptions activityOptions = ActivityOptions
                .makeSceneTransitionAnimation(activity, v,
                        activity.getString(R.string.transition_avatar));
        CategorySelectionActivity.start(activity, mPlayer, activityOptions);
        activity.supportFinishAfterTransition();
    }

    private void initContents() {
        assurePlayerInit();
        if (null != mPlayer) {
            mFirstName.setText(mPlayer.getFirstName());
            mLastInitial.setText(mPlayer.getLastInitial());
            mSelectedAvatar = mPlayer.getAvatar();
            //TODO: 10/28/14 keep avatar selected on GridView
        }
    }

    private void assurePlayerInit() {
        if (null == mPlayer) {
            mPlayer = PreferencesHelper.getPlayer(getActivity());
        }
    }

    private void savePlayer(Activity activity) {
        mPlayer = new Player(mFirstName.getText().toString(), mLastInitial.getText().toString(),
                mSelectedAvatar);
        PreferencesHelper.writeToPreferences(activity, mPlayer);
    }

    /**
     * Calculates spans for avatars dynamically.
     *
     * @return The recommended amount of columns.
     */
    private int calculateSpanCount() {
        int avatarSize = getResources().getDimensionPixelSize(R.dimen.avatar_size);
        int defaultPadding = getResources().getDimensionPixelSize(R.dimen.padding_default);
        return mGridView.getWidth() / (avatarSize + defaultPadding * 2);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mSelectedAvatar = Avatar.values()[position];
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
            int oldTop, int oldRight, int oldBottom) {
        v.removeOnLayoutChangeListener(this);
        setUpGridView(getView());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        /* no-op */
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // showing the floating action button if text is entered
        // TODO: 12/15/14 make sure that both edittexts have input before showing the fab 
        if (count > 0) {
            mDoneFab.setVisibility(View.VISIBLE);
        } else {
            mDoneFab.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        /* no-op */
    }
}
