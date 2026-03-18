package com.bryanwijaya.spaceshooters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LeaderboardAdapter extends BaseAdapter {
    private String[] data;
    private LayoutInflater inflater;

    public LeaderboardAdapter(Context context, String[] data) {
        this.data = data;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.leaderboard_item, null);
        TextView dataTv = view.findViewById(R.id.dataTv);
        dataTv.setText(data[position]);
        return view;
    }
}
