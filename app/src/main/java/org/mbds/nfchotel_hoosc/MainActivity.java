package org.mbds.nfchotel_hoosc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.mbds.nfchotel_hoosc.databinding.ActivityMainBinding;
import org.mbds.nfchotel_hoosc.read.NFCReaderActivity;
import org.mbds.nfchotel_hoosc.write.NFCWriterActivity;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        handleActions();
    }

    private void handleActions() {
        binding.btnReadTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NFCReaderActivity.class));
            }
        });

        binding.btnWriteTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, WriteNfcTagActivity.class));
            }
        });
    }

}
