package com.example.y3033006.kadai3;
//参考：https://www.shookuro.com/entry/android-recycler-view
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

//RecyclerView.Adapterを継承したRecyclerViewのAdapterクラス
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    //表示する文字列のリスト
    private final List<String> listFileName;
    //チェックボックスにチェックが入ってるものを記憶するリスト、positionを記憶
    public List<Integer> selectedFile;

    //スライドするやつの本体
    static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView textView;
        private final CheckBox checkBox;

        //layoutのlist.xmlからlayout情報取得
        public ViewHolder(View view){
            super(view);
            textView = view.findViewById(R.id.musicTitle);
            textView.setTextSize(30);
            checkBox = view.findViewById(R.id.musicCheckBox);
        }

        //CheckBoxで使うやつ
        void bind(List<Integer> selectedFile){
            //チェックが切り換えられると呼び出される
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                //そのチェックボックスのposition番号が記憶されてるか確認、あったらチェックを消した動作
                if(selectedFile.contains(getLayoutPosition())){
                    //番号を削除
                    selectedFile.remove((Integer) getLayoutPosition());
                //position番号がなかったらチェックを入れる動作
                }else{
                    //番号を追加
                    selectedFile.add(getLayoutPosition());
                }
            });
        }

    }

    //コンストラクタ
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
        //絶対パス化の簡易的な判別で絶対パスと判断されたら
        if(listFileName.get(position).contains("/")){
            //絶対パスからそのファイルの名前だけに切り取り表示させる
            holder.textView.setText(listFileName.get(position).substring(listFileName.get(position).lastIndexOf("/")+1));
        //絶対パスではなかったら
        }else {
            //そのままを表示させる
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
