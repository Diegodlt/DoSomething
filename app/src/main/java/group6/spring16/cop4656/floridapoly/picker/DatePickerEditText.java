package group6.spring16.cop4656.floridapoly.picker;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatePickerEditText extends DatePickerFragment {
    private EditText text;
    private String   dateFormat = "EEEE, MMMM d, yyyy";

    public void setEditText(final FragmentManager fragmentManager, EditText t) {
        text = t;
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragmentManager != null) {
                    DatePickerEditText.super.show(fragmentManager, "datePicker");
                }
            }
        });
    }

    public EditText getEditText() {
        return text;
    }

    public String getDateFormatPattern() {
        return dateFormat;
    }

    public void setDateFormatPattern(String pattern) {
        if (pattern != null) {
            dateFormat = pattern;
        }
    }

    @Override
    protected void onCreatePicker(Bundle savedInstanceState) {
        text.setFocusable(false);
        text.setLongClickable(false);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        getCalendar().set(Calendar.YEAR, year);
        getCalendar().set(Calendar.MONTH, month);
        getCalendar().set(Calendar.DAY_OF_MONTH, day);
        updateText();
    }

    public void updateText() {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        final String date = formatter.format(getCalendar().getTime());
        if (text != null) {
            text.setText(date);
        }
    }
}
