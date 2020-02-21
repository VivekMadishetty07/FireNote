package com.example.firenote.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firenote.Activities.MainActivity;
import com.example.firenote.Activities.Writenote;
import com.example.firenote.R;
import com.example.firenote.model.UserData;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder>
    {
        private Context context;
        int cattitle;
        ArrayList<UserData> notesarray;

        public HomeAdapter(Context contexts, int cattitle, ArrayList<UserData> notesarray)
        {
            this.context = contexts;
            this.cattitle = cattitle;
            this.notesarray = notesarray;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.homelayout, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position)
        {
            holder.txt_notetitle.setText(notesarray.get(position).getTitle().toString());
            holder.txt_subtitle.setText(notesarray.get(position).getSubtitle().toString());
            holder.txt_datehome.setText(notesarray.get(position).getDatedata().toString());

            holder.card_view_note.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(context, Writenote.class);
                    intent.putExtra("notedata", notesarray.get(position));
                    context.startActivity(intent);
                }
            });


            holder.card_view_note.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ((MainActivity)context).movedialog(notesarray.get(position), false);
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return notesarray.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView txt_subtitle, txt_notetitle, txt_datehome, model, year, vstatus;
            CardView card_view_note;

            public MyViewHolder(View view) {
                super(view);

                txt_datehome = view.findViewById(R.id.txt_datehome);
                txt_notetitle = view.findViewById(R.id.txt_notetitle);
                txt_subtitle = view.findViewById(R.id.txt_subtitle);
                card_view_note = view.findViewById(R.id.card_view_note);
            }
        }
    }

