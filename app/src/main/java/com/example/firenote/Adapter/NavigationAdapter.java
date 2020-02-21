package com.example.firenote.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firenote.Activities.MainActivity;
import com.example.firenote.R;
import com.example.firenote.model.UserData;

import java.util.ArrayList;
import java.util.List;

public class NavigationAdapter extends RecyclerView.Adapter<com.example.firenote.Adapter.NavigationAdapter.MyViewHolder>
    {
        private Context context;
        ArrayList<UserData> user;
        List<UserData> userData;
        int count = 0;
        String catcompare="";
        public static int positioncolor = 0;

        public NavigationAdapter(Context contexts, ArrayList<UserData> user, List<UserData> userData)
        {
            this.context = contexts;
            this.user = user;
            this.userData = userData;
        }

        @Override
        public com.example.firenote.Adapter.NavigationAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.categorylayout, parent, false);
            return new com.example.firenote.Adapter.NavigationAdapter.MyViewHolder(itemView);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(final com.example.firenote.Adapter.NavigationAdapter.MyViewHolder holder, final int position)
        {
            catcompare = user.get(position).getCategory();
            for(int i = 0; i<userData.size(); i++)
            {
                if(userData.get(i).getCategory().equals(catcompare))
                {
                    holder.txt_cat_title.setText(user.get(position).getCategory());
                    count++;
                }
            }

            try
            {
                holder.txt_datenav.setText(user.get(position).getDatedata());
            }
            catch (Exception e)
            {

            }

         //   holder.txt_datenav.setText(user.get(position).getDatedata());

                        if(position == positioncolor)
                        {
                            holder.card_view_cat.setCardBackgroundColor(0xFF3D4552);
                        }
                        else
                        {
                            holder.card_view_cat.setCardBackgroundColor(0xFF232931);
                        }

            holder.txt_notecount.setText(""+(count-1));

            holder.card_view_cat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)context).setreccolor(position, user.get(position).getCategory().toString());
                }
            });

            count = 0;
        }

        @Override
        public int getItemCount() {

            return user.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView txt_cat_title, txt_notecount, txt_datenav, model, year, vstatus;
            CardView card_view_cat;

            public MyViewHolder(View view) {
                super(view);

                card_view_cat = view.findViewById(R.id.card_view_cat);
                txt_cat_title = view.findViewById(R.id.txt_cat_title);
                txt_notecount = view.findViewById(R.id.txt_notecount);
                txt_datenav = view.findViewById(R.id.txt_datenav);
            }
        }

        public static void recposition(int position)
        {
            positioncolor = position;
        }
    }
