/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.contacts.detail;

import com.android.contacts.ContactLoader;
import com.android.contacts.R;
import com.android.contacts.activities.ContactDetailActivity.FragmentKeyListener;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.util.StreamItemEntry;

import android.app.ListFragment;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.StreamItems;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;

public class ContactDetailUpdatesFragment extends ListFragment
        implements FragmentKeyListener, ViewOverlay {

    private static final String TAG = "ContactDetailUpdatesFragment";

    private ContactLoader.Result mContactData;
    private Uri mLookupUri;

    private LayoutInflater mInflater;
    private StreamItemAdapter mStreamItemAdapter;

    /**
     * This optional view adds an alpha layer over the entire fragment.
     */
    private View mAlphaLayer;

    /**
     * This optional view adds a layer over the entire fragment so that when visible, it intercepts
     * all touch events on the fragment.
     */
    private View mTouchInterceptLayer;

    private OnScrollListener mVerticalScrollListener;

    /**
     * Listener on clicks on a stream item.
     * <p>
     * It assumes the view has a tag of type {@link StreamItemEntry} associated with it.
     */
    private View.OnClickListener mStreamItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            StreamItemEntry streamItemEntry = (StreamItemEntry) view.getTag();
            if (streamItemEntry == null) {
                // Ignore if this item does not have a stream item associated with it.
                return;
            }
            final AccountTypeManager manager = AccountTypeManager.getInstance(getActivity());
            final AccountType accountType = manager.getAccountType(
                    streamItemEntry.getAccountType(), streamItemEntry.getDataSet());

            final Uri uri = ContentUris.withAppendedId(StreamItems.CONTENT_URI,
                    streamItemEntry.getId());
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setClassName(accountType.resPackageName,
                    accountType.getViewStreamItemActivity());
            startActivity(intent);
        }
    };

    public ContactDetailUpdatesFragment() {
        // Explicit constructor for inflation
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        mInflater = inflater;
        View rootView = mInflater.inflate(R.layout.contact_detail_updates_fragment, container,
                false);

        mAlphaLayer = rootView.findViewById(R.id.alpha_overlay);
        mTouchInterceptLayer = rootView.findViewById(R.id.touch_intercept_overlay);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStreamItemAdapter = new StreamItemAdapter(getActivity(), mStreamItemClickListener);
        setListAdapter(mStreamItemAdapter);
        getListView().setOnScrollListener(mVerticalScrollListener);

        // It is possible that the contact data was set to the fragment when it was first attached
        // to the activity, but before this method was called because the fragment was not
        // visible on screen yet (i.e. using a {@link ViewPager}), so display the data if we already
        // have it.
        if (mContactData != null) {
            mStreamItemAdapter.setStreamItems(mContactData.getStreamItems());
        }
    }

    public void setData(Uri lookupUri, ContactLoader.Result result) {
        if (result == null) {
            return;
        }
        mLookupUri = lookupUri;
        mContactData = result;

        // If the adapter has been created already, then try to set stream items. Otherwise,
        // wait for the adapter to get initialized, after which we will try to set the stream items
        // again.
        if (mStreamItemAdapter != null) {
            mStreamItemAdapter.setStreamItems(mContactData.getStreamItems());
        }
    }

    @Override
    public void setAlphaLayerValue(float alpha) {
        ContactDetailDisplayUtils.setAlphaOnViewBackground(mAlphaLayer, alpha);
    }

    @Override
    public void enableAlphaLayer() {
        if (mAlphaLayer != null) {
            mAlphaLayer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void enableTouchInterceptor(OnClickListener clickListener) {
        if (mTouchInterceptLayer != null) {
            mTouchInterceptLayer.setVisibility(View.VISIBLE);
            mTouchInterceptLayer.setOnClickListener(clickListener);
        }
    }

    @Override
    public void disableTouchInterceptor() {
        if (mTouchInterceptLayer != null) {
            mTouchInterceptLayer.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean handleKeyDown(int keyCode) {
        return false;
    }

    public void setVerticalScrollListener(OnScrollListener listener) {
        mVerticalScrollListener = listener;
    }

}
