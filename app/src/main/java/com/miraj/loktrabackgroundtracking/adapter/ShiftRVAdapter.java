package com.miraj.loktrabackgroundtracking.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miraj.loktrabackgroundtracking.R;
import com.miraj.loktrabackgroundtracking.model.Shift;
import com.miraj.loktrabackgroundtracking.util.Utils;

import java.util.List;



public class ShiftRVAdapter extends RecyclerView.Adapter<ShiftRVAdapter.ViewHolder> {

    private final List<Shift> mValues;
    private final Context mContext;

    public ShiftRVAdapter(List<Shift> items, Context context) {
        mValues = items;
        mContext=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shift_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);

        holder.mTitleTV.setText(String.valueOf(holder.mItem.get_ID()+""));
        holder.mDurationTV.setText(Utils.convertMillistoDuration(
                holder.mItem.getEndTime() - holder.mItem.getStartTime()
        ));

        holder.mStartTimeTV.setText(Utils.convertMillisToDateString(holder.mItem.getStartTime()));
        holder.mEndTimeTV.setText(Utils.convertMillisToDateString(holder.mItem.getEndTime()));

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        private final TextView mTitleTV;
        private final TextView mStartTimeTV;
        private final TextView mDurationTV;
        private final TextView mEndTimeTV;

        Shift mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;

            mTitleTV = (TextView) view.findViewById(R.id.shiftTitle);
            mStartTimeTV = (TextView) view.findViewById(R.id.shiftStartTimeTV);
            mEndTimeTV = (TextView) view.findViewById(R.id.shiftEndTimeTV);
            mDurationTV = (TextView) view.findViewById(R.id.shiftDurationTV);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mItem.toString() + "'";
        }
    }
}
