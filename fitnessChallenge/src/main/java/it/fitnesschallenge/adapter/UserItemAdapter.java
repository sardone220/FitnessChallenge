package it.fitnesschallenge.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import it.fitnesschallenge.R;
import it.fitnesschallenge.UserDetailActivity;
import it.fitnesschallenge.UserDetailFragment;
import it.fitnesschallenge.UserListActivity;
import it.fitnesschallenge.model.User;

public class UserItemAdapter extends RecyclerView.Adapter<UserItemAdapter.ViewHolder> {

    private static final String TAG = "UserItemAdapter";
    private static final String ARG_ITEM_ID = "itemId";

    private final UserListActivity mParentActivity;
    private final List<String> mValues;
    private final boolean mTwoPane;

    public UserItemAdapter(UserListActivity parent,
                           List<String> items,
                           boolean twoPane) {
        mValues = items;
        mParentActivity = parent;
        mTwoPane = twoPane;
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String item = (String) view.getTag();
            Log.d(TAG, "mTowPane: " + mTwoPane);

            if (mTwoPane) {
                UserDetailFragment userDetailFragment = UserDetailFragment.newInstance(item);
                mParentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.user_detail_container, userDetailFragment)
                        .commit();
            } else {
                Context context = view.getContext();
                Intent intent = new Intent(context, UserDetailActivity.class);
                intent.putExtra(ARG_ITEM_ID, item);
                context.startActivity(intent);
            }
        }
    };

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mContentView.setText(mValues.get(position));

        holder.itemView.setTag(mValues.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mContentView;

        ViewHolder(View view) {
            super(view);
            mContentView = view.findViewById(R.id.content);
        }
    }
}