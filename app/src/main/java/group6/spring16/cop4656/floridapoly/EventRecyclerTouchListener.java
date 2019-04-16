package group6.spring16.cop4656.floridapoly;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class EventRecyclerTouchListener implements RecyclerView.OnItemTouchListener {

    private GestureDetector gestureDetector;

    private ClickListener     clickListener;
    private LongClickListener longClickListener;

    public interface ClickListener {
        void onClick(View view, int position);
    }

    public interface LongClickListener {
        void onLongClick(View view, int position);
    }

    public EventRecyclerTouchListener(@NonNull Context context, @NonNull final RecyclerView recyclerView) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                if (clickListener != null && child != null) {
                    clickListener.onClick(child, recyclerView.getChildAdapterPosition(child));
                    return true;
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if (longClickListener != null && child != null) {
                    longClickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setLongClickListener(LongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }
}
