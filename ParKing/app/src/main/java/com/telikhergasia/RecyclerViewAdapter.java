package com.telikhergasia;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private ArrayList<String> titles, addresses, ids;
    private Context context;

    public RecyclerViewAdapter(ArrayList<String> titles, ArrayList<String> addresses, ArrayList<String> ids, Context context) {
        this.titles = titles;
        this.addresses = addresses;
        this.ids = ids;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.garagetitle.setText(titles.get(position));
        holder.garageaddress.setText(addresses.get(position));
        holder.listitem.setTag(ids.get(position));
        holder.listitem.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent i = new Intent(context, DetailsActivity.class);
                Bundle b = new Bundle();
                b.putString("id", view.getTag().toString());
                i.putExtras(b);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ids.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView garagetitle, garageaddress;
        RelativeLayout listitem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            garagetitle = itemView.findViewById(R.id.garage_title);
            garageaddress = itemView.findViewById(R.id.garage_address);
            listitem = itemView.findViewById(R.id.list_item);
        }
    }
}
