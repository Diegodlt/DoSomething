package group6.spring16.cop4656.floridapoly.picker;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.app.TimePickerDialog;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import group6.spring16.cop4656.floridapoly.R;

abstract public class TimePickerFragment extends AppCompatDialogFragment implements TimePickerDialog.OnTimeSetListener {
    private Calendar c;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default time in the picker
        c = Calendar.getInstance();
        int hour   = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Call the child class onCreatePicker method
        onCreatePicker(savedInstanceState);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), R.style.PickerTheme, this, hour, minute, false);
    }

    protected Calendar getCalendar() {
        return c;
    }

    public Date getTime() {
        return c.getTime();
    }

    abstract protected void onCreatePicker(Bundle savedInstanceState);

    abstract public void onTimeSet(TimePicker view, int hourOfDay, int minute);
}
