package com.github.stevietodd.stickytodo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

	private List<String> mData;
	private LayoutInflater mInflater;
	private ItemClickListener mClickListener;

	// data is passed into the constructor
	MyRecyclerViewAdapter(Context context, List<String> data) {
		this.mInflater = LayoutInflater.from(context);
		this.mData = data;
	}

	// inflates the row layout from xml when needed
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
		return new ViewHolder(view);
	}

	// binds the data to the TextView in each row
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		String task = mData.get(position);
		holder.chkTask.setText(task);
	}

	// total number of rows
	@Override
	public int getItemCount() {
		return mData.size();
	}


	// stores and recycles views as they are scrolled off screen
	public class ViewHolder extends RecyclerView.ViewHolder {
		CheckBox chkTask;
		ImageButton imgBtnDelete;

		ViewHolder(View itemView) {
			super(itemView);
			chkTask = itemView.findViewById(R.id.chkTask);
			imgBtnDelete = itemView.findViewById(R.id.imgBtnDelete);
			imgBtnDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = getAdapterPosition();
					String task = mData.get(pos);
					mData.remove(pos);
					notifyItemRemoved(pos);
					mClickListener.onItemDeleteButtonClick(pos, task);
				}
			});
		}
	}

	// convenience method for getting data at click position
	String getItem(int id) {
		return mData.get(id);
	}

	// allows clicks events to be caught
	void setClickListener(ItemClickListener itemClickListener) {
		this.mClickListener = itemClickListener;
	}

	// parent activity will implement this method to respond to click events
	public interface ItemClickListener {
		void onItemDeleteButtonClick(int position, String text);
	}
}