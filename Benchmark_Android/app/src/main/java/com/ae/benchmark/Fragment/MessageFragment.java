package com.ae.benchmark.Fragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.activities.BeginTripActivity;
import com.ae.benchmark.adapters.MessageBadgeAdapter;
import com.ae.benchmark.data.Messages;
import com.ae.benchmark.models.Message;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.RoundedImageView;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;
/**
 * Created by eheuristic on 12/2/2016.
 */
public class MessageFragment extends Fragment {
    ListView lv_message;
    RoundedImageView iv_round;
    //public static MessageListAdapter adapter;
    public static MessageBadgeAdapter adapter;
    View view;
    LoadingSpinner loadingSpinner;
    DatabaseHandler db;
    String arr[] = {"silent meeting", "silent meeting"};
    ArrayList<Message> arrayList;
    SwipeRefreshLayout refreshLayout;

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            ((BeginTripActivity)getActivity()).hello = true;

        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.message_fragment, container, false);
        loadingSpinner = new LoadingSpinner(getActivity());
        db = new DatabaseHandler(getActivity());
        lv_message = (ListView) view.findViewById(R.id.lv_messages);
        iv_round = (RoundedImageView) view.findViewById(R.id.roundedImageView);
        arrayList = new ArrayList<>();
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (refreshLayout.isRefreshing()) {
                    dispatchRefresh();
                    refreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();
                }
                /*Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout.isRefreshing()) {
                            dispatchRefresh();
                            refreshLayout.setRefreshing(false);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }, 2000);*/
            }
        });

        adapter = new MessageBadgeAdapter(getActivity(),arrayList);

       // adapter = new MessageListAdapter(getActivity().getBaseContext(), arr);
        lv_message.setAdapter(adapter);
        bindData();
        return view;
    }

    public void bindData()
    {
        new loadMessages(Settings.getString(App.DRIVER));
    }

    public class loadMessages extends AsyncTask<Void,Void,Void> {
        String from = "";
        String filter = "";
        private loadMessages(String from) {
            this.from = from;
            execute();
        }

        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String,String> map = new HashMap<>();
            map.put(db.KEY_USERNAME,"");
            map.put(db.KEY_STRUCTURE,"");
            map.put(db.KEY_MESSAGE,"");
            map.put(db.KEY_DRIVER, "");

            if(from != null) {
                if (from.equals("dash")) {
                    filter = Settings.getString(App.DRIVER);
                } else {
                    filter = from;
                }
            }else{
                filter = "";
            }

            HashMap<String,String> filterMap = new HashMap<>();
            filterMap.put(db.KEY_USERNAME,filter);
            filterMap.put(db.KEY_STRUCTURE,App.DRIVER_MESSAGE_KEY);

            Cursor cursor = db.getData(db.MESSAGES,map,filterMap);
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                setMessages(cursor);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void setMessages(Cursor cursor){

        do{
            Message message = new Message();
            message.setId(cursor.getString(cursor.getColumnIndex(db.KEY_USERNAME)));
            message.setDriver(cursor.getString(cursor.getColumnIndex(db.KEY_DRIVER)));
            message.setStructure(cursor.getString(cursor.getColumnIndex(db.KEY_STRUCTURE)));
            message.setMessage(cursor.getString(cursor.getColumnIndex(db.KEY_MESSAGE)));
            Log.e("Message","" + UrlBuilder.decodeString(message.getStructure()) +  message.getMessage());
            arrayList.add(message);
        }
        while (cursor.moveToNext());
    }

    public void dispatchRefresh() {
        Log.e("Dispatch Called","Called");
        refreshLayout.setRefreshing(true);
        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
            lv_message.setAdapter(null);
            HashMap<String,String> filter = new HashMap<String, String>();
            filter.put(db.KEY_STRUCTURE, App.DRIVER_MESSAGE_KEY);
            db.deleteData(db.MESSAGES, filter);
            Log.e("MEssage","Bring");
            Messages.load(getActivity(), Settings.getString(App.DRIVER), db);
            bindData();
            // adapter.notifyDataSetChanged();
        }
        /*Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                    adapter.clear();
                    HashMap<String,String> filter = new HashMap<String, String>();
                    filter.put(db.KEY_STRUCTURE, App.DRIVER_MESSAGE_KEY);
                    db.deleteData(db.MESSAGES, filter);
                    Log.e("MEssage","Bring");
                    Messages.load(getActivity(), Settings.getString(App.DRIVER), db);
                    bindData();
                    // adapter.notifyDataSetChanged();
                }
            }
        }, 4000);*/
    }
}
