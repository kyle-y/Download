package com.example.yxb.downloaddemo.activity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.yxb.downloaddemo.R;
import com.example.yxb.downloaddemo.javaBean.FileInfo;
import com.example.yxb.downloaddemo.service.DownloadService;

import java.util.ArrayList;

/**
 * PACKAGE_NAME:com.example.yxb.downloaddemo.activity
 * FUNCTIONAL_DESCRIPTION
 * CREATE_BY:xiaobo
 * CREATE_TIME:2016/8/3
 * MODIFY_BY:
 */
public class MyAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<FileInfo> list;

    public MyAdapter(Context context, ArrayList<FileInfo> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public FileInfo getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.textView);
            viewHolder.button_download = (Button) convertView.findViewById(R.id.button_download);
            viewHolder.button_pause = (Button) convertView.findViewById(R.id.button_pause);
            viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textView.setText(list.get(position).getFileName());
        viewHolder.button_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileInfo", list.get(position));
                context.startService(intent);
            }
        });
        viewHolder.button_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DownloadService.class);
                intent.setAction(DownloadService.ACTION_PAUSE);
                intent.putExtra("fileInfo", list.get(position));
                context.startService(intent);
            }
        });
        viewHolder.progressBar.setMax(100);
        viewHolder.progressBar.setProgress(list.get(position).getFinished());
        return convertView;
    }

    //更新进度条
    public void updateProgress(int id, int progress){
        FileInfo fileInfo = list.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }

    class ViewHolder{
        TextView textView;
        Button button_download,button_pause;
        ProgressBar progressBar;
    }
}
