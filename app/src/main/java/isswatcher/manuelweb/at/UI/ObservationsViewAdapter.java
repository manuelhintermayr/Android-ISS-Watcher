package isswatcher.manuelweb.at.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import isswatcher.manuelweb.at.R;
import isswatcher.manuelweb.at.Services.Infrastructure.DateManipulation;
import isswatcher.manuelweb.at.Services.Models.Entities.Observation;

public class ObservationsViewAdapter extends RecyclerView.Adapter<ObservationsViewAdapter.ObservationsViewHolder> {
    private List<Observation> observationList;

    public ObservationsViewAdapter(List<Observation> observationList)
    {
        this.observationList = observationList;
    }

    @NonNull
    @Override
    public ObservationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.observation_element, parent, false);
        ObservationsViewHolder ovh = new ObservationsViewHolder(v);
        return ovh;
    }

    @Override
    public void onBindViewHolder(@NonNull ObservationsViewHolder holder, int position) {
        Observation item = observationList.get(position);

        holder.headline.setText(DateManipulation.getDateByUnixTimestamp(item.timestamp, "DD.MM.YYYY - hh:mm:ss"));
        holder.description.setText(item.notes);
    }

    @Override
    public int getItemCount() {
        return observationList.size();
    }

    public static class ObservationsViewHolder extends RecyclerView.ViewHolder{
        public TextView headline;
        public TextView description;

        public ObservationsViewHolder(@NonNull View itemView) {
            super(itemView);

            headline = itemView.findViewById(R.id.observationTime);
            description = itemView.findViewById(R.id.observationNotes);
        }
    }

}
