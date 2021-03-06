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

package com.activiti.android.ui.fragments.task.checklist;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.BaseAdapter;

import com.activiti.android.app.R;
import com.activiti.android.platform.utils.BundleUtils;
import com.activiti.android.ui.fragments.base.BasePagingGridFragment;
import com.activiti.android.ui.fragments.task.TaskAdapter;
import com.activiti.client.api.constant.RequestConstant;
import com.activiti.client.api.model.common.ResultList;
import com.activiti.client.api.model.runtime.TaskRepresentation;

public class TaskCheklistFoundationFragment extends BasePagingGridFragment implements RequestConstant
{
    public static final String TAG = TaskCheklistFoundationFragment.class.getName();

    protected String taskId;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public TaskCheklistFoundationFragment()
    {
        emptyListMessageId = R.string.task_message_no_task_help;
        retrieveDataOnCreation = true;
        layoutId = R.layout.grid;
        setRetainInstance(true);
    }

    protected void onRetrieveParameters(Bundle bundle)
    {
        taskId = BundleUtils.getString(bundle, ARGUMENT_TASK_ID);
    }

    @Override
    protected void performRequest()
    {
        getAPI().getTaskService().getChecklist(taskId, callBack);
    }

    @Override
    protected BaseAdapter onAdapterCreation()
    {
        return new TaskAdapter(getActivity(), R.layout.row_three_lines_caption, new ArrayList<TaskRepresentation>(0),
                null);
    }

    protected Callback<ResultList<TaskRepresentation>> callBack = new Callback<ResultList<TaskRepresentation>>()
    {
        @Override
        public void onResponse(Call<ResultList<TaskRepresentation>> call,
                Response<ResultList<TaskRepresentation>> response)
        {
            if (!response.isSuccessful())
            {
                onFailure(call, new Exception(response.message()));
                return;
            }
            displayData(response.body());
        }

        @Override
        public void onFailure(Call<ResultList<TaskRepresentation>> call, Throwable error)
        {
            displayError(error);
        }
    };

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
    }
}
