package isswatcher.manuelweb.at.UI;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import isswatcher.manuelweb.at.R;
import isswatcher.manuelweb.at.Services.Infrastructure.DateManipulation;
import isswatcher.manuelweb.at.Services.Models.Entities.Observation;
import isswatcher.manuelweb.at.Services.ObservationsDatabase;
import isswatcher.manuelweb.at.UI.Activities.AddEditObservationActivity;
import isswatcher.manuelweb.at.UI.Activities.ObservationsActivity;

public class ObservationsViewAdapter extends RecyclerView.Adapter<ObservationsViewAdapter.ObservationsViewHolder> {
    public ObservationsActivity observationsActivity;
    private List<Observation> observationList;
    private OnItemClickListener clickListener;
    Observation recentlyDeletedItem;
    int recentlyDeletedItemPosition;

    public ObservationsViewAdapter(ObservationsActivity observationsActivity) {
        this.observationsActivity = observationsActivity;
        updateList();
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        clickListener = listener;
    }

    public void updateList()
    {
        List<Observation> allEntries = ObservationsDatabase
                .getDatabase(observationsActivity)
                .observationsDao()
                .getAllEntries();
        observationList = allEntries;
    }

    @NonNull
    @Override
    public ObservationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.observation_element, parent, false);
        ObservationsViewHolder ovh = new ObservationsViewHolder(v, clickListener);
        return ovh;
    }

    @Override
    public void onBindViewHolder(@NonNull ObservationsViewHolder holder, final int position) {
        updateList();
        final Observation item = observationList.get(position);

        //image manipulation
        holder.image.setMaxHeight(64);
        holder.image.setMaxWidth(64);
            //todo: call to check if images are aviable for this entry

        //location manipulation
        holder.location.setText(item.lat + " / " + item.lng);

        //time manipulation
        holder.headline.setText(DateManipulation.getDateByUnixTimestamp(item.timestamp, "dd.MM.yyyy - HH:mm:ss"));

        //notes manipulation
        if(item.notes.equals(""))
        {
            holder.description.setVisibility(View.GONE);
        }
        else{
            String notesContent = item.notes;
            notesContent = notesContent.replace('\n',' ');
            notesContent = notesContent.length()>30 ? notesContent.substring(0,30)+"..." : notesContent;
            holder.description.setText(notesContent);
        }

        //edit button manipulation
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                observationsActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openEditOption(position);
                    }
                });
            }
        });

        //remove button manipulation
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(position);
            }
        });

    }

    public void removeItem(final int position) {
        final Observation item = observationList.get(position);
        recentlyDeletedItem = item;
        recentlyDeletedItemPosition = position;

        ObservationsDatabase.getDatabase(observationsActivity)
                .observationsDao()
                .delete(item);
        updateList();

        observationsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                observationsActivity.recyclerViewAdapter.notifyItemRemoved(position);
                observationsActivity.recyclerViewAdapter.notifyDataSetChanged();
            }
        });

        showUndoSnackbar();
    }

    private void showUndoSnackbar() {
        observationsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view = observationsActivity.findViewById(R.id.totalWrap);
                Snackbar snackbar = Snackbar.make(view, "Entry removed",
                        Snackbar.LENGTH_LONG);
                snackbar.setAction("Undo", v -> undoDelete());
                snackbar.show();
            }
        });
    }

    private void undoDelete()
    {
        ObservationsDatabase
                .getDatabase(observationsActivity)
                .observationsDao()
                .insert(recentlyDeletedItem);
        updateList();
        notifyItemInserted(recentlyDeletedItemPosition);
    }

    public void openEditOption(int position)
    {
        final Observation item = observationList.get(position);
        Intent intent = new Intent(observationsActivity, AddEditObservationActivity.class);
        intent.putExtra("updateEntryId",Integer.toString(item.id));
        observationsActivity.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return observationList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class ObservationsViewHolder extends RecyclerView.ViewHolder{
        public CardView cardView;
        public ImageView image;

        public TextView location;
        public TextView headline;
        public TextView description;

        public Button editButton;
        public Button removeButton;

        public ObservationsViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            image = itemView.findViewById(R.id.issImageView);
            location = itemView.findViewById(R.id.observationLocation);
            headline = itemView.findViewById(R.id.observationTime);
            description = itemView.findViewById(R.id.observationNotes);
            editButton = itemView.findViewById(R.id.editButton);
            removeButton = itemView.findViewById(R.id.removeButton);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

}
