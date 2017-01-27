package com.idea.todo.adapter.recycler;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.idea.todo.R;
import com.idea.todo.frag.dialog.ToDoOptionsDialog;
import com.idea.todo.frag.status.StatusBase;
import com.idea.todo.model.ToDoInfo;
import com.idea.todo.wrapper.DateWrapper;

import java.util.ArrayList;

public class ToDosAdapter extends BaseRecyclerAdapter<ToDosAdapter.ViewHolder>{
    private StatusBase statusBase;
    private int toDoStatus;
    private ArrayList<ToDoInfo> toDos;

    public ToDosAdapter(StatusBase statusBase, ArrayList<ToDoInfo> toDos, int toDoStatus) {
        this.toDos = toDos;
        this.statusBase = statusBase;
        this.toDoStatus = toDoStatus;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        View
                v,
                containerTxtViews;
        TextView
                tvToDoTitle,
                tvDateSummary;
        Button btnOptions;
        ViewHolder(View v) {
            super(v);
            this.v = v;

            containerTxtViews = v.findViewById(R.id.containerTxtViews);
            containerTxtViews.setOnClickListener(this);

            tvToDoTitle = (TextView)  v.findViewById(R.id.tvToDoTitle);
            tvDateSummary = (TextView)  v.findViewById(R.id.tvDateSummary);

            btnOptions = (Button) v.findViewById(R.id.btnOptions);
            btnOptions.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view == containerTxtViews){
                statusBase.editToDo(toDos.get(getLayoutPosition()).getId(), toDoStatus);
            }
           else  if (view == btnOptions){
                showOptionsDialog(getLayoutPosition());
            }
        }
    }

    private void showOptionsDialog(int position) {
        DialogFragment f = ToDoOptionsDialog.newInstance(
                toDoStatus,
                toDos.get(position),
                statusBase);
        f.show(base().getSupportFragmentManager(), "ToDoOptionsDialog");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_todo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        setData(holder, position);
        startScrollAnim(position, holder.v);
    }

    private void setData(ViewHolder holder, int position) {
        ToDoInfo toDo = toDos.get(position);

        Log.e("setData", "toDo.getDetail() = " + toDo.getDetail());

        holder.tvToDoTitle.setMaxLines(3);
        String title = TextUtils.isEmpty(toDo.getTitle()) ? toDo.getDetail() : toDo.getTitle();
        holder.tvToDoTitle.setText(title);

        setDateDetails(holder, toDo);
    }

    private void setDateDetails(ViewHolder holder, ToDoInfo toDo) {
        setDateSummary(holder.tvDateSummary, toDo.getDate());
    }

    private void setDateSummary(TextView view, long date) {
        if (date == 0) {
            view.setVisibility(View.GONE);
            return;
        }
        view.setVisibility(View.VISIBLE);
        String dateTxt = base().titledValue(
                R.string.date,
                DateWrapper.dateSummary(getContext(), date));
        view.setText(dateTxt);
        view.setTextColor(DateWrapper.getDateColor(date, getContext()));
    }

    @Override
    public long getItemId(int arg0) {
        return (long) arg0;
    }

    @Override
    public int getItemCount() {
        return  toDos != null ? toDos.size() : 0;
    }

    protected Context getContext() {
        return statusBase.getActivity();
    }

    @Override
    public ArrayList getList() {
        return toDos;
    }

}