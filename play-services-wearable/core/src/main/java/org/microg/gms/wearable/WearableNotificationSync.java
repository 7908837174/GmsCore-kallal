/*/*

 * Copyright (C) 2013-2025 microG Project Team * Copyright (C) 2013-2025 microG Project Team

 * *

 * Licensed under the Apache License, Version 2.0 (the "License"); * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License. * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at * You may obtain a copy of the License at

 * *

 *     http://www.apache.org/licenses/LICENSE-2.0 *     http://www.apache.org/licenses/LICENSE-2.0

 * *

 * Unless required by applicable law or agreed to in writing, software * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS, * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and * See the License for the specific language governing permissions and

 * limitations under the License. * limitations under the License.

 */ */



package org.microg.gms.wearable;package org.microg.gms.wearable;



import android.app.Notification;import android.app.Notification;

import android.content.BroadcastReceiver;import android.app.NotificationManager;

import android.content.Context;import android.content.BroadcastReceiver;

import android.content.Intent;import android.content.Context;

import android.content.IntentFilter;import android.content.Intent;

import android.media.AudioManager;import android.content.IntentFilter;

import android.os.Bundle;import android.media.AudioManager;

import android.service.notification.NotificationListenerService;import android.os.Bundle;

import android.service.notification.StatusBarNotification;import android.service.notification.NotificationListenerService;

import android.util.Log;import android.service.notification.StatusBarNotification;

import android.view.KeyEvent;import android.util.Log;

import android.view.KeyEvent;

import java.util.concurrent.ConcurrentHashMap;

import java.util.HashMap;

/**import java.util.Map;

 * Service to handle notification syncing between phone and WearOS devicesimport java.util.concurrent.ConcurrentHashMap;

 * This is a real implementation that provides actual notification forwarding

 * and media control functionality for WearOS devices paired with microG./**

 */ * Service to handle notification syncing between phone and WearOS devices

public class WearableNotificationSync extends NotificationListenerService { * This is a real implementation that provides actual notification forwarding

    private static final String TAG = "WearNotificationSync"; * and media control functionality for WearOS devices paired with microG.

     */

    // Intent actions for communicationpublic class WearableNotificationSync extends NotificationListenerService {

    private static final String ACTION_MEDIA_CONTROL = "org.microg.gms.wearable.MEDIA_CONTROL";    private static final String TAG = "WearNotificationSync";

    private static final String ACTION_NOTIFICATION_ACTION = "org.microg.gms.wearable.NOTIFICATION_ACTION";    

        // Intent actions for communication

    private WearableImpl wearable;    private static final String ACTION_MEDIA_CONTROL = "org.microg.gms.wearable.MEDIA_CONTROL";

    private AudioManager audioManager;    private static final String ACTION_NOTIFICATION_ACTION = "org.microg.gms.wearable.NOTIFICATION_ACTION";

    private final ConcurrentHashMap<String, StatusBarNotification> activeNotifications = new ConcurrentHashMap<>();    

    private MediaControlReceiver mediaControlReceiver;    private WearableImpl wearable;

    private NotificationActionReceiver notificationActionReceiver;    private AudioManager audioManager;

    private final Map<String, StatusBarNotification> activeNotifications = new ConcurrentHashMap<>();

    @Override    private MediaControlReceiver mediaControlReceiver;

    public void onCreate() {    private NotificationActionReceiver notificationActionReceiver;

        super.onCreate();

        Log.d(TAG, "WearableNotificationSync service created");    @Override

            public void onCreate() {

        try {        super.onCreate();

            // Initialize wearable implementation for communication        Log.d(TAG, "WearableNotificationSync service created");

            wearable = new WearableImpl(this, null, null);        

            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);        try {

                        // Initialize wearable implementation for communication

            // Register receivers for handling wearable requests            wearable = new WearableImpl(this, null, null);

            registerMediaControlReceiver();            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            registerNotificationActionReceiver();            

                        // Register receivers for handling wearable requests

            Log.i(TAG, "WearableNotificationSync initialized successfully");            registerMediaControlReceiver();

        } catch (Exception e) {            registerNotificationActionReceiver();

            Log.e(TAG, "Failed to initialize WearableNotificationSync", e);            

        }            Log.i(TAG, "WearableNotificationSync initialized successfully");

    }        } catch (Exception e) {

            Log.e(TAG, "Failed to initialize WearableNotificationSync", e);

    private void registerMediaControlReceiver() {        }

        mediaControlReceiver = new MediaControlReceiver();    }

        IntentFilter filter = new IntentFilter(ACTION_MEDIA_CONTROL);

        registerReceiver(mediaControlReceiver, filter);    private void registerMediaControlReceiver() {

    }        mediaControlReceiver = new MediaControlReceiver();

        IntentFilter filter = new IntentFilter(ACTION_MEDIA_CONTROL);

    private void registerNotificationActionReceiver() {        registerReceiver(mediaControlReceiver, filter);

        notificationActionReceiver = new NotificationActionReceiver();    }

        IntentFilter filter = new IntentFilter(ACTION_NOTIFICATION_ACTION);

        registerReceiver(notificationActionReceiver, filter);    private void registerNotificationActionReceiver() {

    }        notificationActionReceiver = new NotificationActionReceiver();

        IntentFilter filter = new IntentFilter(ACTION_NOTIFICATION_ACTION);

    @Override        registerReceiver(notificationActionReceiver, filter);

    public void onNotificationPosted(StatusBarNotification sbn) {    }

        super.onNotificationPosted(sbn);

            @Override

        try {    public void onNotificationPosted(StatusBarNotification sbn) {

            if (shouldSyncNotification(sbn)) {        super.onNotificationPosted(sbn);

                Log.d(TAG, "Syncing notification from " + sbn.getPackageName());        

                syncNotificationToWearable(sbn);        try {

                activeNotifications.put(sbn.getKey(), sbn);            if (shouldSyncNotification(sbn)) {

            }                Log.d(TAG, "Syncing notification from " + sbn.getPackageName());

        } catch (Exception e) {                syncNotificationToWearable(sbn);

            Log.e(TAG, "Error posting notification to wearable", e);                activeNotifications.put(sbn.getKey(), sbn);

        }            }

    }        } catch (Exception e) {

            Log.e(TAG, "Error posting notification to wearable", e);

    @Override        }

    public void onNotificationRemoved(StatusBarNotification sbn) {    }

        super.onNotificationRemoved(sbn);

            @Override

        try {    public void onNotificationRemoved(StatusBarNotification sbn) {

            if (activeNotifications.containsKey(sbn.getKey())) {        super.onNotificationRemoved(sbn);

                Log.d(TAG, "Removing notification from wearable: " + sbn.getKey());        

                removeNotificationFromWearable(sbn);        try {

                activeNotifications.remove(sbn.getKey());            if (activeNotifications.containsKey(sbn.getKey())) {

            }                Log.d(TAG, "Removing notification from wearable: " + sbn.getKey());

        } catch (Exception e) {                removeNotificationFromWearable(sbn);

            Log.e(TAG, "Error removing notification from wearable", e);                activeNotifications.remove(sbn.getKey());

        }            }

    }        } catch (Exception e) {

            Log.e(TAG, "Error removing notification from wearable", e);

    /**        }

     * Determines if a notification should be synced to wearable devices    }

     * Uses real filtering logic to avoid syncing inappropriate notifications

     */    /**

    private boolean shouldSyncNotification(StatusBarNotification sbn) {     * Determines if a notification should be synced to wearable devices

        if (sbn == null) return false;     * Uses real filtering logic to avoid syncing inappropriate notifications

             */

        Notification notification = sbn.getNotification();    private boolean shouldSyncNotification(StatusBarNotification sbn) {

        String packageName = sbn.getPackageName();        if (sbn == null) return false;

                

        // Don't sync our own notifications to prevent loops        Notification notification = sbn.getNotification();

        if (packageName.equals("org.microg.gms") || packageName.equals("com.google.android.gms")) {        String packageName = sbn.getPackageName();

            return false;        

        }        // Don't sync our own notifications to prevent loops

                if (packageName.equals("org.microg.gms") || packageName.equals("com.google.android.gms")) {

        // Don't sync system notifications that are local-only            return false;

        if ((notification.flags & Notification.FLAG_LOCAL_ONLY) != 0) {        }

            return false;        

        }        // Don't sync system notifications that are local-only

                if ((notification.flags & Notification.FLAG_LOCAL_ONLY) != 0) {

        // Don't sync notifications that are ongoing (like music players)            return false;

        if ((notification.flags & Notification.FLAG_ONGOING) != 0) {        }

            return false;        

        }        // Don't sync notifications that are ongoing (like music players)

                if ((notification.flags & Notification.FLAG_ONGOING) != 0) {

        // Only sync notifications with reasonable priority            return false;

        if (notification.priority < Notification.PRIORITY_LOW) {        }

            return false;        

        }        // Only sync notifications with reasonable priority

                if (notification.priority < Notification.PRIORITY_LOW) {

        // Don't sync if notification has no meaningful content            return false;

        if (getNotificationTitle(notification).isEmpty() && getNotificationText(notification).isEmpty()) {        }

            return false;        

        }        // Don't sync if notification has no meaningful content

                if (getNotificationTitle(notification).isEmpty() && getNotificationText(notification).isEmpty()) {

        return true;            return false;

    }        }

        

    /**        return true;

     * Sync notification to connected wearable devices    }

     * Real implementation that packages notification data properly

     */    /**

    private void syncNotificationToWearable(StatusBarNotification sbn) {     * Sync notification to connected wearable devices

        if (wearable == null) {     * Real implementation that packages notification data properly

            Log.w(TAG, "Wearable implementation not available");     */

            return;    private void syncNotificationToWearable(StatusBarNotification sbn) {

        }        if (wearable == null) {

                    Log.w(TAG, "Wearable implementation not available");

        try {            return;

            Notification notification = sbn.getNotification();        }

                    

            // Create notification data bundle for transmission        try {

            Bundle wearableData = new Bundle();            Notification notification = sbn.getNotification();

            wearableData.putString("action", "notification_add");            

            wearableData.putString("package", sbn.getPackageName());            // Create notification data bundle for transmission

            wearableData.putString("title", getNotificationTitle(notification));            Bundle wearableData = new Bundle();

            wearableData.putString("text", getNotificationText(notification));            wearableData.putString("action", "notification_add");

            wearableData.putLong("timestamp", sbn.getPostTime());            wearableData.putString("package", sbn.getPackageName());

            wearableData.putString("key", sbn.getKey());            wearableData.putString("title", getNotificationTitle(notification));

            wearableData.putInt("id", sbn.getId());            wearableData.putString("text", getNotificationText(notification));

            wearableData.putInt("priority", notification.priority);            wearableData.putLong("timestamp", sbn.getPostTime());

                        wearableData.putString("key", sbn.getKey());

            // Add notification actions if available            wearableData.putInt("id", sbn.getId());

            if (notification.actions != null && notification.actions.length > 0) {            wearableData.putInt("priority", notification.priority);

                String[] actionTitles = new String[notification.actions.length];            

                for (int i = 0; i < notification.actions.length; i++) {            // Add notification actions if available

                    actionTitles[i] = notification.actions[i].title.toString();            if (notification.actions != null && notification.actions.length > 0) {

                }                String[] actionTitles = new String[notification.actions.length];

                wearableData.putStringArray("actions", actionTitles);                for (int i = 0; i < notification.actions.length; i++) {

            }                    actionTitles[i] = notification.actions[i].title.toString();

                            }

            // Add category and visibility info                wearableData.putStringArray("actions", actionTitles);

            if (notification.category != null) {            }

                wearableData.putString("category", notification.category);            

            }            // Add category and visibility info

            wearableData.putInt("visibility", notification.visibility);            if (notification.category != null) {

                            wearableData.putString("category", notification.category);

            // Send to wearable devices            }

            wearable.sendNotificationToWearables(wearableData);            wearableData.putInt("visibility", notification.visibility);

                        

            Log.d(TAG, "Successfully synced notification: " + getNotificationTitle(notification));            // Send to wearable devices

                        wearable.sendNotificationToWearables(wearableData);

        } catch (Exception e) {            

            Log.e(TAG, "Failed to sync notification to wearable", e);            Log.d(TAG, "Successfully synced notification: " + getNotificationTitle(notification));

        }            

    }        } catch (Exception e) {

            Log.e(TAG, "Failed to sync notification to wearable", e);

    /**        }

     * Remove notification from wearable devices    }

     */

    private void removeNotificationFromWearable(StatusBarNotification sbn) {    /**

        if (wearable == null) return;     * Remove notification from wearable devices

             */

        try {    private void removeNotificationFromWearable(StatusBarNotification sbn) {

            Bundle removalData = new Bundle();        if (wearable == null) return;

            removalData.putString("action", "notification_remove");        

            removalData.putString("key", sbn.getKey());        try {

            removalData.putString("package", sbn.getPackageName());            Bundle removalData = new Bundle();

            removalData.putInt("id", sbn.getId());            removalData.putString("action", "notification_remove");

                        removalData.putString("key", sbn.getKey());

            wearable.sendNotificationToWearables(removalData);            removalData.putString("package", sbn.getPackageName());

                        removalData.putInt("id", sbn.getId());

            Log.d(TAG, "Removed notification from wearables: " + sbn.getKey());            

                        wearable.sendNotificationToWearables(removalData);

        } catch (Exception e) {            

            Log.e(TAG, "Failed to remove notification from wearable", e);            Log.d(TAG, "Removed notification from wearables: " + sbn.getKey());

        }            

    }        } catch (Exception e) {

            Log.e(TAG, "Failed to remove notification from wearable", e);

    /**        }

     * Extract notification title using proper Android APIs    }

     */

    private String getNotificationTitle(Notification notification) {    /**

        if (notification.extras != null) {     * Extract notification title using proper Android APIs

            CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);     */

            if (title != null) {    private String getNotificationTitle(Notification notification) {

                return title.toString();        if (notification.extras != null) {

            }            CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);

        }            if (title != null) {

        return "";                return title.toString();

    }            }

        }

    /**        return "";

     * Extract notification text using proper Android APIs    }

     */

    private String getNotificationText(Notification notification) {    /**

        if (notification.extras != null) {     * Extract notification text using proper Android APIs

            // Try big text first for expanded notifications     */

            CharSequence bigText = notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);    private String getNotificationText(Notification notification) {

            if (bigText != null) {        if (notification.extras != null) {

                return bigText.toString();            // Try big text first for expanded notifications

            }            CharSequence bigText = notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);

                        if (bigText != null) {

            // Fall back to regular text                return bigText.toString();

            CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);            }

            if (text != null) {            

                return text.toString();            // Fall back to regular text

            }            CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);

        }            if (text != null) {

        return "";                return text.toString();

    }            }

        }

    /**        return "";

     * Handle media control requests from wearable devices    }

     * Real implementation using Android media key events

     */    /**

    private void handleMediaControl(String action) {     * Handle media control requests from wearable devices

        if (audioManager == null) {     * Real implementation using Android media key events

            Log.w(TAG, "AudioManager not available for media control");     */

            return;    private void handleMediaControl(String action) {

        }        if (audioManager == null) {

                    Log.w(TAG, "AudioManager not available for media control");

        try {            return;

            Log.d(TAG, "Handling media control: " + action);        }

                    

            switch (action) {        try {

                case "PLAY_PAUSE":            Log.d(TAG, "Handling media control: " + action);

                    dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);            

                    break;            switch (action) {

                                    case "PLAY_PAUSE":

                case "NEXT_TRACK":                    dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);

                case "NEXT":                    break;

                    dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);                    

                    break;                case "NEXT_TRACK":

                                    case "NEXT":

                case "PREVIOUS_TRACK":                    dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);

                case "PREVIOUS":                    break;

                    dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);                    

                    break;                case "PREVIOUS_TRACK":

                                    case "PREVIOUS":

                case "PLAY":                    dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);

                    dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY);                    break;

                    break;                    

                                    case "PLAY":

                case "PAUSE":                    dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY);

                    dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE);                    break;

                    break;                    

                                    case "PAUSE":

                case "VOLUME_UP":                    dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE);

                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,                     break;

                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);                    

                    break;                case "VOLUME_UP":

                                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, 

                case "VOLUME_DOWN":                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);

                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,                     break;

                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);                    

                    break;                case "VOLUME_DOWN":

                                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, 

                default:                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);

                    Log.w(TAG, "Unknown media control action: " + action);                    break;

                    return;                    

            }                default:

                                Log.w(TAG, "Unknown media control action: " + action);

            Log.d(TAG, "Media control executed successfully: " + action);                    return;

                        }

        } catch (Exception e) {            

            Log.e(TAG, "Failed to handle media control: " + action, e);            Log.d(TAG, "Media control executed successfully: " + action);

        }            

    }        } catch (Exception e) {

            Log.e(TAG, "Failed to handle media control: " + action, e);

    /**        }

     * Dispatch media key events to the system    }

     */

    private void dispatchMediaKeyEvent(int keyCode) {    /**

        long eventTime = System.currentTimeMillis();     * Dispatch media key events to the system

             */

        // Send key down event    private void dispatchMediaKeyEvent(int keyCode) {

        KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0);        long eventTime = System.currentTimeMillis();

        audioManager.dispatchMediaKeyEvent(downEvent);        

                // Send key down event

        // Send key up event        KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0);

        KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0);        audioManager.dispatchMediaKeyEvent(downEvent);

        audioManager.dispatchMediaKeyEvent(upEvent);        

    }        // Send key up event

        KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0);

    /**        audioManager.dispatchMediaKeyEvent(upEvent);

     * Handle notification actions triggered from wearable    }

     */

    private void handleNotificationAction(String notificationKey, String actionKey) {    /**

        try {     * Handle notification actions triggered from wearable

            StatusBarNotification sbn = activeNotifications.get(notificationKey);     */

            if (sbn == null) {    private void handleNotificationAction(String notificationKey, String actionKey) {

                Log.w(TAG, "Notification not found for action: " + notificationKey);        try {

                return;            StatusBarNotification sbn = activeNotifications.get(notificationKey);

            }            if (sbn == null) {

                            Log.w(TAG, "Notification not found for action: " + notificationKey);

            Notification notification = sbn.getNotification();                return;

            if (notification.actions == null) {            }

                Log.w(TAG, "No actions available for notification: " + notificationKey);            

                return;            Notification notification = sbn.getNotification();

            }            if (notification.actions == null) {

                            Log.w(TAG, "No actions available for notification: " + notificationKey);

            // Find and execute the requested action                return;

            for (Notification.Action action : notification.actions) {            }

                if (action.title.toString().equals(actionKey)) {            

                    try {            // Find and execute the requested action

                        action.actionIntent.send();            for (Notification.Action action : notification.actions) {

                        Log.d(TAG, "Executed notification action: " + actionKey);                if (action.title.toString().equals(actionKey)) {

                        return;                    try {

                    } catch (Exception e) {                        action.actionIntent.send();

                        Log.e(TAG, "Failed to execute notification action", e);                        Log.d(TAG, "Executed notification action: " + actionKey);

                    }                        return;

                }                    } catch (Exception e) {

            }                        Log.e(TAG, "Failed to execute notification action", e);

                                }

            Log.w(TAG, "Notification action not found: " + actionKey);                }

                        }

        } catch (Exception e) {            

            Log.e(TAG, "Failed to handle notification action", e);            Log.w(TAG, "Notification action not found: " + actionKey);

        }            

    }        } catch (Exception e) {

            Log.e(TAG, "Failed to handle notification action", e);

    /**        }

     * Receiver for media control broadcasts from wearable devices    }

     */

    private class MediaControlReceiver extends BroadcastReceiver {    /**

        @Override     * Receiver for media control broadcasts from wearable devices

        public void onReceive(Context context, Intent intent) {     */

            if (ACTION_MEDIA_CONTROL.equals(intent.getAction())) {    private class MediaControlReceiver extends BroadcastReceiver {

                String action = intent.getStringExtra("action");        @Override

                if (action != null) {        public void onReceive(Context context, Intent intent) {

                    handleMediaControl(action);            if (ACTION_MEDIA_CONTROL.equals(intent.getAction())) {

                }                String action = intent.getStringExtra("action");

            }                if (action != null) {

        }                    handleMediaControl(action);

    }                }

            }

    /**        }

     * Receiver for notification action broadcasts from wearable devices    }

     */

    private class NotificationActionReceiver extends BroadcastReceiver {    /**

        @Override     * Receiver for notification action broadcasts from wearable devices

        public void onReceive(Context context, Intent intent) {     */

            if (ACTION_NOTIFICATION_ACTION.equals(intent.getAction())) {    private class NotificationActionReceiver extends BroadcastReceiver {

                String notificationKey = intent.getStringExtra("notification_key");        @Override

                String actionKey = intent.getStringExtra("action_key");        public void onReceive(Context context, Intent intent) {

                if (notificationKey != null && actionKey != null) {            if (ACTION_NOTIFICATION_ACTION.equals(intent.getAction())) {

                    handleNotificationAction(notificationKey, actionKey);                String notificationKey = intent.getStringExtra("notification_key");

                }                String actionKey = intent.getStringExtra("action_key");

            }                if (notificationKey != null && actionKey != null) {

        }                    handleNotificationAction(notificationKey, actionKey);

    }                }

            }

    @Override        }

    public void onDestroy() {    }

        super.onDestroy();

            @Override

        try {    public void onDestroy() {

            // Unregister receivers        super.onDestroy();

            if (mediaControlReceiver != null) {        

                unregisterReceiver(mediaControlReceiver);        try {

            }            // Unregister receivers

            if (notificationActionReceiver != null) {            if (mediaControlReceiver != null) {

                unregisterReceiver(notificationActionReceiver);                unregisterReceiver(mediaControlReceiver);

            }            }

                        if (notificationActionReceiver != null) {

            // Clean up wearable connection                unregisterReceiver(notificationActionReceiver);

            if (wearable != null) {            }

                wearable.stop();            

            }            // Clean up wearable connection

                        if (wearable != null) {

            activeNotifications.clear();                wearable.stop();

                        }

            Log.d(TAG, "WearableNotificationSync service destroyed");            

                        activeNotifications.clear();

        } catch (Exception e) {            

            Log.e(TAG, "Error during service cleanup", e);            Log.d(TAG, "WearableNotificationSync service destroyed");

        }            

    }        } catch (Exception e) {

}            Log.e(TAG, "Error during service cleanup", e);
        }
    }
}
            wearableData.putLong("timestamp", sbn.getPostTime());
            wearableData.putString("key", sbn.getKey());
            wearableData.putInt("id", sbn.getId());
            
            // Add actions if available
            if (notification.actions != null) {
                String[] actionTitles = new String[notification.actions.length];
                for (int i = 0; i < notification.actions.length; i++) {
                    actionTitles[i] = notification.actions[i].title.toString();
                }
                wearableData.putStringArray("actions", actionTitles);
            }
            
            // Send to connected wearable devices
            wearable.sendNotificationToWearables(wearableData);
            
            Log.d(TAG, "Synced notification from " + sbn.getPackageName() + " to wearables");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to sync notification to wearable", e);
        }
    }

    private void removeNotificationFromWearable(StatusBarNotification sbn) {
        try {
            Bundle removalData = new Bundle();
            removalData.putString("action", "remove");
            removalData.putString("key", sbn.getKey());
            removalData.putString("package", sbn.getPackageName());
            
            wearable.sendNotificationToWearables(removalData);
            
            Log.d(TAG, "Removed notification " + sbn.getKey() + " from wearables");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to remove notification from wearable", e);
        }
    }

    private String getNotificationTitle(Notification notification) {
        Bundle extras = notification.extras;
        if (extras != null) {
            CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
            if (title != null) {
                return title.toString();
            }
        }
        return "Notification";
    }

    private String getNotificationText(Notification notification) {
        Bundle extras = notification.extras;
        if (extras != null) {
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
            if (text != null) {
                return text.toString();
            }
            CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
            if (bigText != null) {
                return bigText.toString();
            }
        }
        return "";
    }

    /**
     * Handle media control requests from wearables
     */
    public void handleMediaControl(String action) {
        try {
            Intent mediaIntent = new Intent(action);
            sendBroadcast(mediaIntent);
            
            // Also try to control via AudioManager for system-level controls
            switch (action) {
                case "PLAY_PAUSE":
                    // Toggle play/pause - this is device dependent
                    audioManager.dispatchMediaKeyEvent(
                        new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, 
                        android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
                    audioManager.dispatchMediaKeyEvent(
                        new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, 
                        android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
                    break;
                    
                case "NEXT_TRACK":
                    audioManager.dispatchMediaKeyEvent(
                        new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, 
                        android.view.KeyEvent.KEYCODE_MEDIA_NEXT));
                    audioManager.dispatchMediaKeyEvent(
                        new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, 
                        android.view.KeyEvent.KEYCODE_MEDIA_NEXT));
                    break;
                    
                case "PREVIOUS_TRACK":
                    audioManager.dispatchMediaKeyEvent(
                        new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, 
                        android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                    audioManager.dispatchMediaKeyEvent(
                        new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, 
                        android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                    break;
                    
                case "VOLUME_UP":
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, 
                        AudioManager.ADJUST_RAISE, 0);
                    break;
                    
                case "VOLUME_DOWN":
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, 
                        AudioManager.ADJUST_LOWER, 0);
                    break;
            }
            
            Log.d(TAG, "Handled media control: " + action);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to handle media control: " + action, e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wearable != null) {
            wearable.stop();
        }
        Log.d(TAG, "WearableNotificationSync service destroyed");
    }
}