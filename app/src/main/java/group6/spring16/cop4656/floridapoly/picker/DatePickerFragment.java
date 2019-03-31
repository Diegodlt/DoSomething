package group6.spring16.cop4656.floridapoly.picker;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.app.DatePickerDialog;
import android.widget.DatePicker;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import group6.spring16.cop4656.floridapoly.R;

abstract public class DatePickerFragment extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {
    private Calendar c;
    LocalDate date;

    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        c = Calendar.getInstance();
        int year  = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day   = c.get(Calendar.DAY_OF_MONTH);

        // Call the child class onCreatePicker method
        onCreatePicker(savedInstanceState);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), R.style.PickerTheme,this, year, month, day);
    }

    protected Calendar getCalendar() {
        return c;
    }

    public Date getDate() {
        return c.getTime();
    }

    abstract protected void onCreatePicker(Bundle savedInstanceState);

    abstract public void onDateSet(DatePicker view, int year, int month, int day);
}
