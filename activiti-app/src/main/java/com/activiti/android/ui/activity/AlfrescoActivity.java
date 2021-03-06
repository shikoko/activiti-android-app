/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Activiti Mobile for Android.
 *
 *  Alfresco Activiti Mobile for Android is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Alfresco Activiti Mobile for Android is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package com.activiti.android.ui.activity;

import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.activiti.android.app.R;
import com.activiti.android.app.fragments.app.AppInstancesFragment;
import com.activiti.android.platform.EventBusManager;
import com.activiti.android.platform.account.AccountsPreferences;
import com.activiti.android.platform.account.ActivitiAccount;
import com.activiti.android.platform.account.ActivitiAccountManager;
import com.activiti.android.platform.integration.analytics.AnalyticsHelper;
import com.activiti.android.platform.integration.hockeyapp.HockeyAppManager;
import com.activiti.android.sdk.ActivitiSession;
import com.activiti.android.sdk.services.ServiceRegistry;
import com.mattprecious.telescope.EmailDeviceInfoLens;
import com.mattprecious.telescope.TelescopeLayout;

/**
 * Base class for all activities.
 *
 * @author Jean Marie Pascal
 */
public abstract class AlfrescoActivity extends AppCompatActivity
{
    protected ActivitiSession session;

    protected ActivitiAccount account;

    protected int telescopeId = R.id.telescope;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // HockeyApp
        if (HockeyAppManager.getInstance(this) != null)
        {
            HockeyAppManager.getInstance(this).checkForUpdates(this);
        }
    }

    @Override
    protected void onStart()
    {
        EventBusManager.getInstance().register(this);
        initBugReport();
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // HockeyApp
        if (HockeyAppManager.getInstance(this) != null)
        {
            HockeyAppManager.getInstance(this).checkForCrashes(this);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        try
        {
            EventBusManager.getInstance().unregister(this);
        }
        catch (Exception e)
        {
            // DO Nothing
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public Fragment getFragment(String tag)
    {
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    protected boolean isVisible(String tag)
    {
        return getSupportFragmentManager().findFragmentByTag(tag) != null
                && getSupportFragmentManager().findFragmentByTag(tag).isAdded();
    }

    protected ServiceRegistry getAPI()
    {
        return session.getServiceRegistry();
    }

    public void connect(Long accountId)
    {
        if (accountId == null)
        {
            account = ActivitiAccountManager.getInstance(this).getCurrentAccount();
        }
        else
        {
            AccountsPreferences.setDefaultAccount(this, accountId);
            account = ActivitiAccountManager.getInstance(this).getByAccountId(accountId);
        }

        if (account == null) { return; }
        session = new ActivitiSession.Builder()
                .connect(account.getServerUrl(), account.getUsername(), account.getPassword())
                .httpLogging(HttpLoggingInterceptor.Level.HEADERS).build();

        // Analytics
        AnalyticsHelper.analyzeAccount(this, account);

        // Refresh Adapter
        AppInstancesFragment.syncAdapters(this);

        // Do it for retrieving the account cached version
        account = ActivitiAccountManager.getInstance(this).getCurrentAccount();
        checkIsAdmin();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // USER
    // ///////////////////////////////////////////////////////////////////////////
    public void checkIsAdmin()
    {
        getAPI().getUserGroupService().isAdmin(new Callback<ResponseBody>()
        {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
            {
                account.setIsAdmin(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t)
            {
                account.setIsAdmin(false);
            }
        });

    }

    // ////////////////////////////////////////////////////////
    // BUG REPORTING
    // ///////////////////////////////////////////////////////
    public void initBugReport()
    {
        try
        {
            TelescopeLayout telescopeView = (TelescopeLayout) findViewById(telescopeId);
            telescopeView.setLens(new EmailDeviceInfoLens(this, getString(R.string.bug_report_title),
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionName,
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionCode,
                    getResources().getStringArray(R.array.bugreport_email)));
        }
        catch (Exception e)
        {
            Log.w("Test", Log.getStackTraceString(e));
        }
    }
}
