package com.ae.benchmark.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.ae.benchmark.R;
import com.ae.benchmark.models.Message;
import com.ae.benchmark.utils.RoundedImageView;
import com.ae.benchmark.utils.UrlBuilder;

import java.util.ArrayList;

/**
 * Created by Rakshit on 03-Jan-17.
 */
public class MessageBadgeAdapter extends BaseAdapter implements Filterable {

    private ArrayList<Message> messages;
    private ArrayList<Message> messagesSearch;
    Context context;
    ItemFilter mFilter = new ItemFilter();

    public MessageBadgeAdapter(Context context, ArrayList<Message> messages) {
        this.messagesSearch=messages;
        this.messages = messages;
        this.context = context;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.message_list_adapter, parent, false);
            // get all UI view
            holder = new ViewHolder(convertView);
            // set tag for holder
            convertView.setTag(holder);
        } else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }

        Message message = messages.get(position);

        //  holder.tv_message_title.setText(UrlBuilder.decodeString(message.getStructure()).substring(0,1).toUpperCase() + UrlBuilder.decodeString(message.getStructure()).substring(1).toLowerCase());
        holder.iv_round = (RoundedImageView) convertView.findViewById(R.id.roundedImageView);
        holder.tv_message.setText(UrlBuilder.decodeString(message.getMessage()).substring(0, 1).toUpperCase() + UrlBuilder.decodeString(message.getMessage()).substring(1).toLowerCase());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }


    public class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            constraint = constraint.toString().toLowerCase();
            Log.v("DataAdapter", "constratinst : " + constraint);
            Log.v("DataAdapter", "constratinst : " + messages.size());
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<Message> filteredItems = new ArrayList<>();
                for (int i = 0, l = messages.size(); i < l; i++) {
                    String p = messages.get(i).getMessage().toLowerCase();
                    if (p.contains(constraint)) {
                        filteredItems.add(messages.get(i));
                        Log.v("DataAdapter", p + " -- " + messages.get(i));
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.count = messagesSearch.size();
                    result.values = messagesSearch;
                }
            }
            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            messages = (ArrayList<Message>) results.values;
            Log.v("datalist", messages.size() + "--");
            notifyDataSetChanged();
            notifyDataSetInvalidated();
        }
    }

    private class ViewHolder {
        RoundedImageView iv_round;
        TextView tv_message;
        //  TextView tv_message_title;

        public ViewHolder(View v) {

            tv_message = (TextView) v.findViewById(R.id.tv_message);
            iv_round = (RoundedImageView) v.findViewById(R.id.roundedImageView);
            //   tv_message_title = (TextView) v.findViewById(R.id.tv_message_title);
        }
    }
}
