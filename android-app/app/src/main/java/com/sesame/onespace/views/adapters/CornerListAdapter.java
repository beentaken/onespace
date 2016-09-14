package com.sesame.onespace.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.models.map.Corner;

import java.util.ArrayList;

/**
 * Created by chongos on 11/23/15 AD.
 */
public class CornerListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CORNER = 1;
    private static final int TYPE_EMPTY = -1;

    private Context context;
    private ArrayList<Corner> corners;
    private OnCornerListInteractionListener listener;

    public CornerListAdapter(Context context) {
        this.context = context;
        this.corners = new ArrayList<>();
    }

    public void setOnCornerListInteractionListener(OnCornerListInteractionListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_CORNER:
                return new CornerInfoViewHolder(
                        inflater.inflate(R.layout.row_corner_info, parent, false));
            case TYPE_EMPTY:
            default:
                return new EmptyViewHolder(
                        inflater.inflate(R.layout.row_corner_empty, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        try {
            corners.get(position);
            return TYPE_CORNER;
        } catch (IndexOutOfBoundsException e) {
            return TYPE_EMPTY;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = holder.getItemViewType();
        if (type == TYPE_CORNER) {
            CornerInfoViewHolder cornerInfoViewHolder = (CornerInfoViewHolder) holder;
            final Corner corner = corners.get(position);
            String cornerLocation = corner.getLat() + ", " + corner.getLng();

            cornerInfoViewHolder.cornerName.setText(corner.getName());
            cornerInfoViewHolder.cornerDescription.setText(corner.getDescription());
            cornerInfoViewHolder.cornerLocation.setText(cornerLocation);
            cornerInfoViewHolder.openChatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onOpenChat(corner);
                }
            });
            cornerInfoViewHolder.deleteCornerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onDelete(corner);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int size = corners.size();
        size += size > 0 ? 0 : 1;
        return size;
    }

    public void addCorner(Corner corner) {
        corners.add(corner);
        notifyDataSetChanged();
    }

    public void deleteCorner(Corner corner) {
        corners.remove(corner);
        notifyDataSetChanged();
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {

        public EmptyViewHolder(View itemView) {
            super(itemView);
        }

    }

    public static class CornerInfoViewHolder extends RecyclerView.ViewHolder {
        public TextView cornerName;
        public TextView cornerDescription;
        public TextView cornerLocation;
        public ImageButton openChatButton;
        public ImageButton deleteCornerButton;

        public CornerInfoViewHolder(View view) {
            super(view);
            cornerName = (TextView) view.findViewById(R.id.corner_name);
            cornerDescription = (TextView) view.findViewById(R.id.corner_description);
            cornerLocation = (TextView) view.findViewById(R.id.corner_location);
            openChatButton = (ImageButton) view.findViewById(R.id.corner_open_chat_button);
            deleteCornerButton = (ImageButton) view.findViewById(R.id.corner_delete_button);
        }
    }

    public interface OnCornerListInteractionListener {
        void onOpenChat(Corner corner);
        void onDelete(Corner corner);
    }

}
