package project.julie.usafe_trial2.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.core.app.NotificationCompat;
import project.julie.usafe_trial2.MapsFollowersActivity;
import project.julie.usafe_trial2.R;

public class PushNotificationService extends FirebaseMessagingService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d("", "Refreshed token: " + token);
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("", "Message data payload: " + remoteMessage.getData());
//            Toast.makeText(this, remoteMessage.getData().get("message"), Toast.LENGTH_LONG).show();
            NotificationCompat.Builder notification_builder;
            NotificationManager notification_manager = (NotificationManager) this
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String chanel_id = "3000";
                CharSequence name = "Channel Name";
                String description = "Chanel Description";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(chanel_id, name, importance);
                mChannel.setDescription(description);
                mChannel.enableLights(true);
                mChannel.setLightColor(Color.BLUE);
                notification_manager.createNotificationChannel(mChannel);
                notification_builder = new NotificationCompat.Builder(this, chanel_id);
            } else {
                notification_builder = new NotificationCompat.Builder(this);
            }


            Intent i = null;
            switch (remoteMessage.getData().get("type")) {
                case "followUser":
                    i = new Intent(this, MapsFollowersActivity.class);
                    i.putExtra("userId", remoteMessage.getData().get("userId"));
                    break;
                case "userRunning":
                case "userNotFollowingRoute":
                    i = new Intent(Intent.ACTION_DIAL);
                    String phone1 = remoteMessage.getData().get("phoneNo");
                    i.setData(Uri.parse("tel:" + phone1));
                    break;
                case "journeyFinished":
                    break;
                default:
                    break;
            }

            PendingIntent pending_intent;
                    if (i != null) {
                       pending_intent = PendingIntent
                                .getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
                    } else {
                        pending_intent = PendingIntent.getActivity(
                                getApplicationContext(),
                                0,
                                new Intent(), // add this
                                PendingIntent.FLAG_UPDATE_CURRENT);
                    }
            notification_builder.setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                    .setContentTitle("From Usafe")
                    .setContentText(remoteMessage.getData().get("message"))
                    .setAutoCancel(true)
                    .setContentIntent(pending_intent);

            notification_manager.notify(0, notification_builder.build());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }


}
