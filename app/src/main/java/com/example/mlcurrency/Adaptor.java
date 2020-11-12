package com.example.mlcurrency;

import android.content.Context;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adaptor extends RecyclerView.Adapter<Adaptor.MyViewHolder>  {

    Context context;
    ArrayList<String> CountryCodes;
    ArrayList<Double> rates;
    ArrayList<Double> convertedValues;


    public Adaptor(Context c,ArrayList<String> CC,ArrayList<Double> r , ArrayList<Double> cV)
    {
        this.context = c;
        this.CountryCodes = CC;
        this.rates = r;
        this.convertedValues = cV;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder((LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false)));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        int x = convertedValues.size();

        if(convertedValues.get(position) < convertedValues.get(x-1))
        {
            holder.textCountry.setTextColor(Color.RED);
            holder.textAmount.setTextColor(Color.RED);
            holder.linearLayout.setBackgroundColor(Color.TRANSPARENT);
        }
        else if(convertedValues.get(position) > convertedValues.get(x-1))
        {
            holder.textCountry.setTextColor(Color.BLUE);
            holder.textAmount.setTextColor(Color.BLUE);
            holder.linearLayout.setBackgroundColor(Color.TRANSPARENT);
        }
        else if(convertedValues.get(position).equals(convertedValues.get(x - 1)))
        {
            holder.textCountry.setTextColor(Color.WHITE);
            holder.textAmount.setTextColor(Color.WHITE);
            holder.linearLayout.setBackgroundColor(Color.parseColor("#05445E"));
            holder.linearLayout.setElevation(10f);
            Log.i("<><><><><><","FOUND");
        }
        holder.textCountry.setText(CountryCodes.get(position));
        holder.textAmount.setText(String.format("%.2f",convertedValues.get(position)));
    }

    @Override
    public int getItemCount() {
        return rates.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{


        TextView textCountry,textAmount;
        LinearLayout linearLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textCountry = itemView.findViewById(R.id.CountryCode);
            textAmount = itemView.findViewById(R.id.Amount);
            linearLayout = itemView.findViewById(R.id.countryDetails);
        }
    }
}
