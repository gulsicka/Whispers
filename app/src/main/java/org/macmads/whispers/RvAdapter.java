package org.macmads.whispers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.siyamed.shapeimageview.CircularImageView;

        import java.util.List;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.RowViewHolder> {

    private List<RowModel> list;
    public class RowViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView number;
        public CircularImageView image;
        public RowViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);


        }
    }
    public RvAdapter(List<RowModel> moviesList) {
        this.list = moviesList;
    }
    @Override
    public RowViewHolder onCreateViewHolder(ViewGroup parent, int
            viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row, parent, false);
        return new RowViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(RowViewHolder holder, int
            position) {
        RowModel movie = list.get(position);
        holder.name.setText(movie.getName());
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
}