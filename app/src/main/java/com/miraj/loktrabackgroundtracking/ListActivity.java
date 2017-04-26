package com.miraj.loktrabackgroundtracking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.miraj.loktrabackgroundtracking.adapter.ShiftRVAdapter;
import com.miraj.loktrabackgroundtracking.data.SQLiteDBHelper;

public class ListActivity extends AppCompatActivity {

    private RecyclerView shiftList;
    private SQLiteDBHelper sqLiteDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        shiftList = (RecyclerView)findViewById(R.id.shiftList);
        sqLiteDBHelper = new SQLiteDBHelper(this);
        sqLiteDBHelper.open();

        shiftList.setLayoutManager(new LinearLayoutManager(this));
        shiftList.setAdapter(new ShiftRVAdapter(sqLiteDBHelper.getAllShifts(),this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(sqLiteDBHelper!=null)
            sqLiteDBHelper.close();
    }
}
