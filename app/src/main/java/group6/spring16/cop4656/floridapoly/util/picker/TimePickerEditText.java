package group6.spring16.cop4656.floridapoly.util.picker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimePickerEditText extends TimePickerFragment {
    private EditText text;
    private String dateFormat = "h:mm a";

    private OnContentChangedListener contentChangedListener;

    public interface OnContentChangedListener {
        void onTimeChanged(@NonNull Date date, @Nullable EditText editText);
    }

    public void setEditText(final FragmentManager fragmentManager, EditText t) {
        text = t;
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragmentManager != null) {
                    TimePickerEditText.super.show(fragmentManager, "timePicker");
                }
            }
        });
    }

    public void setOnContentChangedListener(OnContentChangedListener listener) {
        contentChangedListener = listener;
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
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        getCalendar().set(Calendar.HOUR_OF_DAY, hourOfDay);
        getCalendar().set(Calendar.MINUTE, minute);
        updateText();

        if (contentChangedListener != null) {
            contentChangedListener.onTimeChanged(super.getTime(), text);
        }
    }

    public void updateText() {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
        final String date = formatter.format(getCalendar().getTime());
        if (text != null) {
            text.setText(date);
        }
    }
}
