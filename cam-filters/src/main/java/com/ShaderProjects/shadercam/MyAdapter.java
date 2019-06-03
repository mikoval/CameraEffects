package com.ShaderProjects.shadercam;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<ShaderHandle> mDataset;
    private Context context;
    private RecyclerView.LayoutManager mManager;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ShaderWidget view;
        public MyViewHolder(ShaderWidget v) {
            super(v);
            view = v;

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context context, ArrayList<ShaderHandle> myDataset, RecyclerView.LayoutManager manager) {
        this.context = context;
        mDataset = myDataset;
        mManager = manager;

        int half = Integer.MAX_VALUE / 2;
        manager.scrollToPosition(half - half % myDataset.size());


    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        ShaderWidget v = new ShaderWidget(context);
        MyViewHolder vh = new MyViewHolder(v);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.view.setText(mDataset.get(position % mDataset.size()).text);
        holder.view.setShader(mDataset.get(position % mDataset.size()).shader);
        holder.view.setImage(mDataset.get(position % mDataset.size()).image);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }


}