package com.layoutxml.applistmanagerlibrary.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import com.layoutxml.applistmanagerlibrary.interfaces.ActivityListener;
import com.layoutxml.applistmanagerlibrary.objects.AppData;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ActivityTask extends AsyncTask<Void,Void,List<AppData>> {

    private final WeakReference<ActivityListener> activitiesListenerWeakReference;
    private final WeakReference<Context> contextWeakReference;
    private final Intent intent;
    private final Integer uniqueIdentifier;
    private final Integer flags;
    private final Integer activitiesFlags;
    private final Boolean match;
    private final String[] permissions;
    private final Boolean matchPermissions;

    public ActivityTask(WeakReference<Context> context, Intent intent, Integer activitiesFlags, Integer appFlags, Boolean appMatch, String[] permissions, Boolean matchPermissions, Integer uniqueIdentifier, WeakReference<ActivityListener> activitiesListenerWeakReference) {
        this.contextWeakReference = context;
        this.intent = intent;
        this.uniqueIdentifier = uniqueIdentifier;
        this.activitiesListenerWeakReference = activitiesListenerWeakReference;
        this.flags = appFlags;
        this.match = appMatch;
        this.activitiesFlags = activitiesFlags;
        this.permissions = permissions;
        this.matchPermissions = matchPermissions;
    }

    @Override
    protected List<AppData> doInBackground(Void... voids){
        Context context1 = contextWeakReference.get();
        if (context1!=null) {
            PackageManager packageManager = context1.getPackageManager();
            List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent,activitiesFlags);
            List<AppData> appDataList = new ArrayList<>();
            for (ResolveInfo resolveInfo : resolveInfoList) {
                AppData app = new AppData();
                app.setName(resolveInfo.loadLabel(packageManager).toString());
                app.setPackageName(resolveInfo.activityInfo.packageName);
                app.setIcon(resolveInfo.activityInfo.loadIcon(packageManager));
                app.setActivityName(resolveInfo.activityInfo.name);
                Boolean containsPermission = false;
                try {
                    ApplicationInfo appInfo = packageManager.getApplicationInfo(app.getPackageName(),0);
                    app.setFlags(appInfo.flags);
                    PackageInfo packageInfo = packageManager.getPackageInfo(app.getPackageName(), PackageManager.GET_PERMISSIONS);
                    String[] requestedPermissions = packageInfo.requestedPermissions;
                    if (permissions!=null) {
                        if (requestedPermissions != null) {
                            for (String requestedPermission : requestedPermissions) {
                                for (String permission : permissions) {
                                    if (requestedPermission.equals(permission)) {
                                        containsPermission = true;
                                        break;
                                    }
                                }
                                if (containsPermission)
                                    break;
                            }
                        }
                    } else {
                        containsPermission = matchPermissions;
                    }
                    app.setPermissions(requestedPermissions);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    if (permissions==null)
                        containsPermission=matchPermissions;
                }
                if ((containsPermission && matchPermissions) || (!containsPermission && !matchPermissions)) {
                    if (match) {
                        if ((flags == null) || ((app.getFlags() & flags) != 0))
                            appDataList.add(app);
                    } else {
                        if ((flags == null) || ((app.getFlags() & flags) == 0))
                            appDataList.add(app);
                    }
                }
                if (isCancelled())
                    break;
            }
            return appDataList;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<AppData> appDataList) {
        final ActivityListener listener = activitiesListenerWeakReference.get();
        if (listener!=null) {
            listener.activityListener(appDataList, intent, activitiesFlags, flags, match, permissions, matchPermissions, uniqueIdentifier);
        }
    }
}
