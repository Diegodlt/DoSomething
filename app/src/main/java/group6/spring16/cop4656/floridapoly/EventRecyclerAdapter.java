package group6.spring16.cop4656.floridapoly;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import group6.spring16.cop4656.floridapoly.event.Event;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.ViewHolder> {

    private List<Event> events;

    public EventRecyclerAdapter(List<Event> events) {
        this.events = events;
    }

    @Override
    @NonNull
    public EventRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_recycler_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Log.d("Adapter", "Bind (size: " + getItemCount() + ")");
        final Event event = events.get(position);

        holder.titleView.setText(event.title);

        final SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        final SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a", Locale.getDefault());

        holder.dateView.setText(dateFmt.format(event.date));
        holder.timeView.setText(timeFmt.format(event.date));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleView;
        private TextView dateView;
        private TextView timeView;

        public ViewHolder(View v) {
            super(v);
            titleView = v.findViewById(R.id.event_recycler_row_title);
            dateView  = v.findViewById(R.id.event_recycler_row_date);
            timeView  = v.findViewById(R.id.event_recycler_row_time);
        }
    }
}
