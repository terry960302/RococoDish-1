package com.example.front_ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class DishWrite extends AppCompatActivity {

    Button buttonToDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dish_write);

        buttonToDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToDetail();
            }
        });
    }

    public void moveToDetail() {
        Intent intent = new Intent(this, DishWriteDetail.class);
        startActivity(intent);
    }
}