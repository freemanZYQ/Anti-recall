/*
 * Copyright © 2016 - 2018 by GitHub.com/JasonQS
 * anti-recall.qsboy.com
 * All Rights Reserved
 */

package com.qsboy.antirecall.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
import com.chad.library.adapter.base.listener.OnItemSwipeListener;
import com.qsboy.antirecall.R;
import com.qsboy.antirecall.db.Dao;
import com.qsboy.antirecall.db.Messages;
import com.qsboy.antirecall.ui.FoldingCellAdapter;
import com.qsboy.utils.CheckAuthority;
import com.qsboy.utils.LogcatHelper;
import com.qsboy.utils.UpdateHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final String TAG = "Main Activity";

    RecyclerView recyclerView;
    FoldingCellAdapter adapter;

    // TODO: 09/04/2018 加一个看聊天记录的
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogcatHelper.getInstance().start();
        Date in = new Date();
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                .detectLeakedSqlLiteObjects()
//                .detectLeakedClosableObjects()
//                .penaltyLog()
//                .penaltyDeath()
//                .build());


//        BottomNavigationView navigation = findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);


        //test
//        prepareDataForTest();
//        Dao dao = new Dao(this);
//        dao.temp();

        adapter = new FoldingCellAdapter(null, this);
        List<Messages> messages = adapter.prepareData();
        if (messages != null && messages.size() != 0)
            adapter.addData(messages);
        recyclerView = findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ItemDragAndSwipeCallback itemDragAndSwipeCallback = new ItemDragAndSwipeCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemDragAndSwipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // 开启滑动删除
        adapter.enableSwipeItem();
        adapter.setOnItemSwipeListener(onItemSwipeListener);

        checkUpdate();

        Date out = new Date();
        Log.d(TAG, "onCreate: time: " + (out.getTime() - in.getTime()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_service) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        if (!new CheckAuthority(this).checkAlertWindowPermission()) {
            Log.i(TAG, "authorized: show warning");
            Toast.makeText(this, "请授予悬浮窗权限\n为了能正常显示撤回的消息 谢谢", Toast.LENGTH_LONG).show();
        }
        List<Messages> messages = adapter.prepareData();
        if (messages != null && messages.size() != 0)
            if (adapter.getData().size() != messages.size()) {
                adapter.getData().clear();
                adapter.addData(messages);
            }
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                return true;
            case R.id.navigation_monitor:
                return true;
            case R.id.navigation_setting:
                return true;
            default:
                return false;
        }
    };

    private OnItemSwipeListener onItemSwipeListener = new OnItemSwipeListener() {

        @Override
        public void onItemSwipeStart(RecyclerView.ViewHolder viewHolder, int pos) {
        }

        @Override
        public void clearView(RecyclerView.ViewHolder viewHolder, int pos) {
        }

        @Override
        public void onItemSwiped(RecyclerView.ViewHolder viewHolder, int pos) {
            Log.i(TAG, "onItemSwiped: pos: " + pos);
            Dao dao = Dao.getInstance(getApplicationContext());
            dao.deleteRecall(adapter.getData().get(pos).getRecalledID());
            Log.i(TAG, "clearView: " + adapter.getData());
        }

        @Override
        public void onItemSwipeMoving(Canvas canvas, RecyclerView.ViewHolder viewHolder, float dX, float dY, boolean isCurrentlyActive) {

        }
    };

    private void checkUpdate() {
        //wifi环境下检查更新
        UpdateHelper helper = new UpdateHelper(this);
//        if (helper.isWifi())
            helper.checkUpdate();
    }

    // for test
    public void prepareDataForTest() {
        Date in = new Date();
        Dao dao = Dao.getInstance(this);
//        dao.deleteAll();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -9);
        for (int i = 1; i < 200; ) {
            for (int j = 1; j < 21; j++, i++) {
                dao.addMessage("Jason", "qs", false, String.valueOf(i), calendar.getTime().getTime());
                calendar.add(Calendar.MINUTE, 3);
                calendar.add(Calendar.SECOND, 3);
            }
            dao.addRecall(i, "Jason", "qs", String.valueOf(i - 1), false, calendar.getTime().getTime(), null, null, null, null, null);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        Date out = new Date();
        Log.i(TAG, "prepareDataForTest: time: " + (out.getTime() - in.getTime()));
    }

}