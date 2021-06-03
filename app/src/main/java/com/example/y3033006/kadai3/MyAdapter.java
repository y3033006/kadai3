package com.example.y3033006.kadai3;
//参考：https://www.shookuro.com/entry/android-recycler-view
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private final String[] listFileName;

    static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView textView;
        public ViewHolder(TextView view){
            super(view);
            textView = view;
            textView.setTextSize(30);
        }
    }

    public MyAdapter(String[] listFileName){
        this.listFileName=listFileName;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        TextView v = new TextView(parent.getContext());
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        holder.textView.setText(listFileName[position]);
    }

    @Override
    public  int getItemCount(){
        return listFileName.length;
    }
}
