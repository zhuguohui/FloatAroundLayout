package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    FloatingAroundLayout floatingAroundLayout;
    String[] titls=new String[]{"百姓热点","十九大","环保督察"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        floatingAroundLayout=findViewById(R.id.layout);
        findViewById(R.id.btn_ssz).setOnClickListener(v->{
            floatingAroundLayout.setDirection(FloatingAroundLayout.Direction.Clockwise);
        });
        findViewById(R.id.btn_nsz).setOnClickListener(v->{
            floatingAroundLayout.setDirection(FloatingAroundLayout.Direction.Anti_clockwise);
        });
        floatingAroundLayout.setAdapter(new FloatingAroundLayout.Adapter() {
            @Override
            public int getPointSize() {
                return 5;
            }

            @Override
            public int getViewCount() {
                return 3;
            }

            @Override
            public View getView(int position, Context context, FloatingAroundLayout floatingAroundLayout) {
                TextView view = (TextView) LayoutInflater.from(context).inflate(R.layout.layout_item, floatingAroundLayout, false);
                view.setText(titls[position]);
                return view;
            }
        });
    }
}
