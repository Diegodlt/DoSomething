package group6.spring16.cop4656.floridapoly.util.picker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatePickerEditText extends DatePickerFragment {
    private EditText text;
    private String   dateFormat = "EEEE, MMMM d, yyyy";

    private OnContentChangedListener contentChangedListener;

    public interface OnContentChangedListener {
        void onDateChanged(@NonNull Date date, @Nullable EditText editText);
    }

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
    public void onDateSet(DatePicker view, int year, int month, int day) {
        getCalendar().set(Calendar.YEAR, year);
        getCalendar().set(Calendar.MONTH, month);
        getCalendar().set(Calendar.DAY_OF_MONTH, day);
        updateText();

        if (contentChangedListener != null) {
            contentChangedListener.onDateChanged(super.getDate(), text);
        }
    }

    private void updateText() {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
        final String date = formatter.format(getCalendar().getTime());
        if (text != null) {
            text.setText(date);
        }
    }
}
