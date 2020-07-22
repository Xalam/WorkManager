package com.example.myworkmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnOneTime, btnPeriodic, btnCancel;
    EditText edtCity;
    TextView tvStatus;
    private PeriodicWorkRequest periodicWorkRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOneTime = findViewById(R.id.btnOneTimeTask);
        btnPeriodic = findViewById(R.id.btnPeriodicTask);
        btnCancel = findViewById(R.id.btnCancelTask);
        edtCity = findViewById(R.id.editCity);
        tvStatus = findViewById(R.id.textStatus);

        btnOneTime.setOnClickListener(this);
        btnPeriodic.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    private void startOneTimeTask() {
        tvStatus.setText(getString(R.string.status));

        Data data = new Data.Builder()
                .putString(MyWorker.CITY, edtCity.getText().toString())
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
                .setInputData(data)
                .build();

        WorkManager.getInstance().enqueue(oneTimeWorkRequest);

        WorkManager.getInstance().getWorkInfoByIdLiveData(oneTimeWorkRequest.getId()).observe(MainActivity.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                String status = workInfo.getState().name();
                tvStatus.append("\n" + status);
            }
        });
    }

    private void startPeriodicTask(){
        tvStatus.setText(getString(R.string.status));

        Data data = new Data.Builder()
                .putString(MyWorker.CITY, edtCity.getText().toString())
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        periodicWorkRequest = new PeriodicWorkRequest.Builder(MyWorker.class, 5, TimeUnit.MINUTES)
                .setInputData(data)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance().enqueue(periodicWorkRequest);
        WorkManager.getInstance().getWorkInfoByIdLiveData(periodicWorkRequest.getId()).observe(MainActivity.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                String status = workInfo.getState().name();
                tvStatus.append("\n"+status);
                btnCancel.setEnabled(false);
                if (workInfo.getState() == WorkInfo.State.ENQUEUED){
                    btnCancel.setEnabled(true);
                }
            }
        });
    }

    private void cancelPeriodicTask(){
        WorkManager.getInstance().cancelWorkById(periodicWorkRequest.getId());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnOneTimeTask:
                startOneTimeTask();
                break;
            case R.id.btnPeriodicTask:
                startPeriodicTask();
                break;
            case R.id.btnCancelTask:
                cancelPeriodicTask();
                break;
        }
    }
}
