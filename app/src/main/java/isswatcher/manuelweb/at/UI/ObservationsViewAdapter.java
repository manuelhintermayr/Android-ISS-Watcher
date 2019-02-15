package isswatcher.manuelweb.at.UI;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import isswatcher.manuelweb.at.R;
import isswatcher.manuelweb.at.Services.Infrastructure.DateManipulation;
import isswatcher.manuelweb.at.Services.Models.Entities.Observation;
import isswatcher.manuelweb.at.Services.Models.Entities.Picture;
import isswatcher.manuelweb.at.Services.ObservationsDatabase;
import isswatcher.manuelweb.at.UI.Activities.AddEditObservationActivity;
import isswatcher.manuelweb.at.UI.Activities.ObservationsActivity;

public class ObservationsViewAdapter extends RecyclerView.Adapter<ObservationsViewAdapter.ObservationsViewHolder> {
    public ObservationsActivity observationsActivity;
    private List<Observation> observationList;
    private OnItemClickListener clickListener;
    Observation recentlyDeletedItem;
    List<Picture> recentlyDeletedPictureList;
    int recentlyDeletedItemPosition;

    public ObservationsViewAdapter(ObservationsActivity observationsActivity) {
        this.observationsActivity = observationsActivity;
        updateList();
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        clickListener = listener;
    }

    public List<Observation> updateList()
    {
        List<Observation> allEntries = ObservationsDatabase
                .getDatabase(observationsActivity)
                .observationsDao()
                .getAllEntries();
        observationList = allEntries;
        return allEntries;
    }

    @NonNull
    @Override
    public ObservationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.observation_element, parent, false);
        ObservationsViewHolder ovh = new ObservationsViewHolder(v, clickListener, observationsActivity);
        return ovh;
    }

    @Override
    public void onBindViewHolder(@NonNull ObservationsViewHolder holder, final int position) {
        updateList();
        final Observation item = updateList().get(position);

        //image manipulation
        holder.image.setMaxHeight(64);
        holder.image.setMaxWidth(64);

        //location manipulation
        holder.location.setText(item.lat + " / " + item.lng);

        //time manipulation
        holder.headline.setText(DateManipulation.getDateByUnixTimestamp(item.timestamp, "dd.MM.yyyy - HH:mm:ss"));

        //imageslider
        List<Picture> pictureList = getPictureListByObservationsId(item.id);
        if(pictureList.size()==0)
        {
            holder.imageFlipperWrap.setVisibility(View.GONE);
        }
        else
        {
            ImageFlipAdapter adapter = new ImageFlipAdapter(observationsActivity, pictureList);
            holder.imageFlipper.setAdapter(adapter);
        }

        //notes manipulation
        if(item.notes.equals(""))
        {
            holder.notes.setVisibility(View.GONE);
        }
        else{
            String notesContent = item.notes;
            notesContent = notesContent.replace('\n',' ');
            notesContent = notesContent.length()>30 ? notesContent.substring(0,30)+"..." : notesContent;
            holder.notes.setText(notesContent);
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

    private List<Picture> getPictureListByObservationsId(int observationId) {
        return ObservationsDatabase
                .getDatabase(observationsActivity)
                .pictureDao()
                .getEntriesByObservationId(observationId);
    }

    public void removeItem(final int position) {
        final Observation item = observationList.get(position);
        recentlyDeletedItem = item;
        recentlyDeletedItemPosition = position;
        recentlyDeletedPictureList = ObservationsDatabase
                .getDatabase(observationsActivity)
                .pictureDao()
                .getEntriesByObservationId(item.id);

        ObservationsDatabase.getDatabase(observationsActivity)
                .observationsDao()
                .delete(item);

        for(int i = 0;i<recentlyDeletedPictureList.size(); i++)
        {
            ObservationsDatabase
                    .getDatabase(observationsActivity)
                    .pictureDao()
                    .delete(recentlyDeletedPictureList.get(i));

            // delete of local images will not be done here, due to the fact that deleting can be made "undone",
            // real deleting will occur in check in add/editOBservationsActivity
        }

        updateList();

        observationsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                observationsActivity.recyclerViewAdapter.notifyItemRemoved(position);
            }
        });

        showUndoSnackbar();
    }

    private void showUndoSnackbar() {
        //used tutorial from https://medium.com/@zackcosborn/step-by-step-recyclerview-swipe-to-delete-and-undo-7bbae1fce27e 

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

        for(int i = 0;i<recentlyDeletedPictureList.size(); i++)
        {
            ObservationsDatabase
                    .getDatabase(observationsActivity)
                    .pictureDao()
                    .insert(recentlyDeletedPictureList.get(i));
        }

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
        return updateList().size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class ObservationsViewHolder extends RecyclerView.ViewHolder{
        public CardView cardView;
        public ImageView image;

        public TextView location;
        public TextView headline;
        public TextView notes;
        public LinearLayout imageFlipperWrap;
        public ViewPager imageFlipper;

        public Button editButton;
        public Button removeButton;

        public ObservationsViewHolder(@NonNull View itemView, final OnItemClickListener listener, Context context) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            image = itemView.findViewById(R.id.issImageView);
            location = itemView.findViewById(R.id.observationLocation);
            headline = itemView.findViewById(R.id.observationTime);
            notes = itemView.findViewById(R.id.observationNotes);
            imageFlipperWrap = itemView.findViewById(R.id.imageFlipperWrap);
            imageFlipper = itemView.findViewById(R.id.imageFlipper);
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
