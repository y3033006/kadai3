package com.example.y3033006.kadai3;
//参考：https://www.shookuro.com/entry/android-recycler-view
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private final String[] listFileName;

    static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView textView;
        public ViewHolder(View view){
            super(view);
            textView = view.findViewById(R.id.musicTitle);
            textView.setTextSize(30);
        }
    }

    public MyAdapter(String[] listFileName){
        this.listFileName=listFileName;
    }

    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View v =LayoutInflater.from(parent.getContext()).inflate(R.layout.list,parent,false);
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
