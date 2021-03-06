/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco Activiti Mobile for Android.
 *
 * Alfresco Activiti Mobile for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco Activiti Mobile for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.activiti.android.ui.fragments.form.picker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.activiti.android.app.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author jpascal
 */
public class DatePickerFragment extends DialogFragment implements OnDateSetListener {
    public static final String TAG = DatePickerFragment.class.getName();

    private static final String ARGUMENT_FRAGMENT_TAG = "fragmentTag";

    private static final String ARGUMENT_DATE_ID = "dateId";

    private static final String ARGUMENT_TIME_PICKER = "timePicker";

    private static final String ARGUMENT_START_DATE = "startDate";

    private static final String ARGUMENT_MIN_DATE = "minDate";

    private static final String ARGUMENT_MAX_DATE = "maxDate";

    private OnDateSetListener mListener;

    private boolean showTime, isCancelled = false, clearValue = false;

    private Long minDate = null, maxDate = null, startDate = null;

    private DatePickerDialog picker;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // //////////////////////////////////////////////////////////////////////
    public static DatePickerFragment newInstance(String dateId, String fragmentTag) {
        DatePickerFragment bf = new DatePickerFragment();
        Bundle b = new Bundle();
        b.putString(ARGUMENT_DATE_ID, dateId);
        b.putString(ARGUMENT_FRAGMENT_TAG, fragmentTag);
        bf.setArguments(b);
        return bf;
    }

    public static DatePickerFragment newInstance(String dateId, String fragmentTag, boolean timePicker) {
        DatePickerFragment bf = newInstance(dateId, fragmentTag);
        Bundle b = bf.getArguments();
        b.putBoolean(ARGUMENT_TIME_PICKER, timePicker);
        bf.setArguments(b);
        return bf;
    }

    public static DatePickerFragment newInstance(String dateId, String fragmentTag, Long date, Long minDate,
                                                 Long maxDate, boolean timePicker) {
        DatePickerFragment bf = newInstance(dateId, fragmentTag, timePicker);
        Bundle b = bf.getArguments();
        if (date != null) {
            b.putLong(ARGUMENT_START_DATE, date);
        }
        if (minDate != null) {
            b.putLong(ARGUMENT_MIN_DATE, minDate);
        }
        if (maxDate != null) {
            b.putLong(ARGUMENT_MAX_DATE, maxDate);
        }
        bf.setArguments(b);
        return bf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mListener = this;
    }

    @Override
    public void onDetach() {
        this.mListener = null;
        super.onDetach();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            if (getArguments().containsKey(ARGUMENT_TIME_PICKER)) {
                showTime = getArguments().getBoolean(ARGUMENT_TIME_PICKER);
            }
            if (getArguments().containsKey(ARGUMENT_MIN_DATE)) {
                minDate = getArguments().getLong(ARGUMENT_MIN_DATE);
            }
            if (getArguments().containsKey(ARGUMENT_MAX_DATE)) {
                maxDate = getArguments().getLong(ARGUMENT_MAX_DATE);
            }
            if (getArguments().containsKey(ARGUMENT_START_DATE)) {
                startDate = getArguments().getLong(ARGUMENT_START_DATE);
            }
        }

        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        if (startDate != null) {
            c.setTime(new Date(startDate));
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        picker = new DatePickerDialog(getActivity(), mListener, year, month, day);

        if (maxDate != null) {
            picker.getDatePicker().setMaxDate(maxDate);
        }

        if (minDate != null) {
            picker.getDatePicker().setMinDate(minDate);
        }

        picker.setButton(DialogInterface.BUTTON_POSITIVE, getActivity().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        picker.onClick(dialog, which);
                        dismiss();
                    }
                });
        picker.setButton(DialogInterface.BUTTON_NEUTRAL, getActivity().getString(R.string.clear),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearValue = true;
                        onDateSet(picker.getDatePicker(), picker.getDatePicker().getYear(), picker.getDatePicker()
                                .getMonth(), picker.getDatePicker().getDayOfMonth());
                        dismiss();
                    }
                });
        picker.setButton(DialogInterface.BUTTON_NEGATIVE, getActivity().getString(R.string.general_action_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isCancelled = true;
                        onDateSet(picker.getDatePicker(), picker.getDatePicker().getYear(), picker.getDatePicker()
                                .getMonth(), picker.getDatePicker().getDayOfMonth());
                        dismiss();
                    }
                });
        return picker;

    }

    // //////////////////////////////////////////////////////////////////////
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////
    public void onDateSet(DatePicker picker, int year, int month, int day) {
        if (getArguments() == null || !getArguments().containsKey(ARGUMENT_FRAGMENT_TAG)) {
            return;
        }

        String pickFragmentTag = getArguments().getString(ARGUMENT_FRAGMENT_TAG);
        String dateId = getArguments().getString(ARGUMENT_DATE_ID);
        OnPickDateFragment fragmentPick = ((OnPickDateFragment) getFragmentManager().findFragmentByTag(pickFragmentTag));
        if (fragmentPick == null || isCancelled) {
            return;
        }

        if (clearValue) {
            fragmentPick.onDateClear(dateId);
        } else {
            if (showTime) {
                TimePickerFragment.newInstance(dateId, pickFragmentTag,
                        new GregorianCalendar(year, month, day, 0, 0, 0)).show(getFragmentManager(),
                        TimePickerFragment.TAG);
            } else {
                fragmentPick.onDatePicked(dateId, new GregorianCalendar(year, month, day, 0, 0, 0));
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERFACE
    // //////////////////////////////////////////////////////////////////////
    public interface OnPickDateFragment {
        void onDatePicked(String dateId, Calendar calendar);

        void onDateClear(String dateId);
    }
}