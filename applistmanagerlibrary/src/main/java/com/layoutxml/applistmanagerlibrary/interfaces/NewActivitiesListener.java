package com.layoutxml.applistmanagerlibrary.interfaces;

import android.content.Intent;

import com.layoutxml.applistmanagerlibrary.objects.AppData;

import java.util.List;

public interface NewActivitiesListener {
    void newActivitiesListener(List<AppData> appDataList, Intent intent, Integer filterFlags, Boolean match, Boolean fromReceiver, Integer uniqueIdentifier);
}