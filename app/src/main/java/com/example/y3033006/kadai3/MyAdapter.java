package com.example.y3033006.kadai3;
//参考：https://www.shookuro.com/entry/android-recycler-view
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private final List<String> listFileName;
    public List<Integer> selectedFile;

    static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView textView;
        private final CheckBox checkBox;

        public ViewHolder(View view){
            super(view);
            textView = view.findViewById(R.id.musicTitle);
            textView.setTextSize(30);
            checkBox = view.findViewById(R.id.musicCheckBox);
        }

        void bind(List<Integer> selectedFile){
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                System.out.println("SSS"+getLayoutPosition());
                if(selectedFile.contains(getLayoutPosition())){
                    selectedFile.remove((Integer) getLayoutPosition());
                }else{
                    selectedFile.add(getLayoutPosition());
                }
                System.out.println(selectedFile);
            });
        }

    }

    public MyAdapter(List<String > listName,List<Integer> listSelected){
        selectedFile=listSelected;
        listFileName=listName;
    }

    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View v =LayoutInflater.from(parent.getContext()).inflate(R.layout.list,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        if(listFileName.get(position).contains("/")){
            holder.textView.setText(listFileName.get(position).substring(listFileName.get(position).lastIndexOf("/")+1));
        }else {
            holder.textView.setText(listFileName.get(position));
        }
        holder.bind(selectedFile);
    }

    @Override
    public  int getItemCount(){
        return listFileName.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
