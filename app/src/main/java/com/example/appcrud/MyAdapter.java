package com.example.appcrud;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context context;
    private List<DataClass> dataList;

    public MyAdapter(Context context, List<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //Binding từng phần tử trong mảng
        DataClass data = dataList.get(position);
        if(data==null){
            return;
        }

        //Thư viện glide để tải hình ảnh vào view
        Glide.with(context).load(data.getDataImage()).into(holder.recImage);
        holder.recTitle.setText(data.getDataTitle());
        holder.recDesc.setText(data.getDataDesc());
        holder.recLang.setText(data.getDataLang());

        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailActivity.class);
                //lấy ra chính xác vị trí phần từ nào được click
                intent.putExtra("Image", dataList.get(holder.getAdapterPosition()).getDataImage());
                intent.putExtra("Title", dataList.get(holder.getAdapterPosition()).getDataTitle());
                intent.putExtra("Desc", dataList.get(holder.getAdapterPosition()).getDataDesc());
                intent.putExtra("Lang", dataList.get(holder.getAdapterPosition()).getDataLang());
                intent.putExtra("Key", dataList.get(holder.getAdapterPosition()).getKey());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    //Tìm ra mảng search và gán lại vào dataList để cập nhập lại dữ liệu hiển thị
    public void searchDataList(ArrayList<DataClass> searchList){
        dataList = searchList;
        notifyDataSetChanged();
    }
}
class MyViewHolder extends RecyclerView.ViewHolder{
    ImageView recImage;
    TextView recTitle, recDesc, recLang;
    CardView recCard;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        //Ánh xạ view
        recImage = itemView.findViewById(R.id.recImage);
        recCard = itemView.findViewById(R.id.recCard);
        recTitle = itemView.findViewById(R.id.recTitle);
        recDesc = itemView.findViewById(R.id.recDesc);
        recLang = itemView.findViewById(R.id.recLang);
    }
}
//Glide.with(context)tạo một Glide thể hiện với ngữ cảnh hiện tại.
//load(dataList.get(position).getDataImage())tải hình ảnh tại URL dataList.get(position).getDataImage().
//into(holder.recImage)tải hình ảnh vào ImageView holder.recImage.
