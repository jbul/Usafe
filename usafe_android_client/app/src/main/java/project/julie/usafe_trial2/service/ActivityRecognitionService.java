package project.julie.usafe_trial2.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ActivityRecognitionService extends IntentService {

    public static final String ACTION = "com.project.julie.usafe_trial2.MyTestService";


    public ActivityRecognitionService() {
        super("ActivityRecognitionService");
    }

    public ActivityRecognitionService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        handleDetectedActivity(result.getProbableActivities());
    }

    private void handleDetectedActivity(List<DetectedActivity> probableActivities) {
        for (DetectedActivity activity : probableActivities) {
            switch (activity.getType()) {
                case DetectedActivity.IN_VEHICLE: {
                    Log.d("", "handleDetectedActivity: IN VEHICLE " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Log.d("", "handleDetectedActivity: ON BICYCLE " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.WALKING: {
                    Log.d("", "handleDetectedActivity: WALKING " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Log.d("", "handleDetectedActivity: RUNNING " + activity.getConfidence());
                    if (activity.getConfidence() >= 51) {
                        Intent i = new Intent(ACTION);
                        i.putExtra("running", "UserIsRunning");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                    }
                    break;
                }
                case DetectedActivity.STILL: {
                    Log.d("", "handleDetectedActivity: STILL " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Log.d("", "handleDetectedActivity: UNKNOWN" + activity.getConfidence());
                    break;
                }
            }
        }
    }


    @Override
    public void sendBroadcast(Intent intent) {
        super.sendBroadcast(intent);
    }
}



