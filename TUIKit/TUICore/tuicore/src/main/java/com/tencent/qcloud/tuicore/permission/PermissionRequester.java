package com.tencent.qcloud.tuicore.permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;

import com.tencent.qcloud.tuicore.TUIConfig;
import com.tencent.qcloud.tuicore.TUICore;
import com.tencent.qcloud.tuicore.interfaces.ITUINotification;
import com.tencent.qcloud.tuicore.util.TUIBuild;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PermissionRequester {
    private static final String TAG = "PermissionRequester";

    public static final String PERMISSION_NOTIFY_EVENT_KEY = "PERMISSION_NOTIFY_EVENT_KEY";
    public static final String PERMISSION_NOTIFY_EVENT_SUB_KEY = "PERMISSION_NOTIFY_EVENT_SUB_KEY";
    public static final String PERMISSION_RESULT = "PERMISSION_RESULT";
    public static final String PERMISSION_REQUEST_KEY = "PERMISSION_REQUEST_KEY";
    private static final Object sLock = new Object();

    private static AtomicBoolean sIsRequesting = new AtomicBoolean(false);

    private PermissionCallback mPermissionCallback;
    private String[] mPermissions;
    private String mTitle;
    private String mDescription;
    private String mSettingsTip;
    private ITUINotification mPermissionNotification;

    private PermissionRequester(String... permissions) {
        mPermissions = permissions;
        mPermissionNotification = (key, subKey, param) -> {
            if (param == null) {
                return;
            }
            final Object result = param.get(PERMISSION_RESULT);
            if (result == null || !(result instanceof Boolean)) {
                return;
            }
            if ((boolean) result) {
                afterPermissionGranted();
            } else {
                afterPermissionDenied();
            }
        };
    }

    /**
     * ?????? PermissionRequester ?????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param permissions ??????????????????????????????
     * @return PermissionRequester ????????????
     *
     * Generate an instance of PermissionRequester, where the parameters are the specific permissions that need to be
     * applied for, and one or more permissions can be passed in.
     *
     * @param permissions The name of the permission that needs to be applied for.
     * @return An instance of PermissionRequester.
     */
    public static PermissionRequester newInstance(@NonNull @Size(min = 1) String... permissions) {
        return new PermissionRequester(permissions);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param title ??????????????????
     * @return PermissionRequester ????????????
     *
     * The title of the reason: security compliance requirements, when requesting permission, you
     * must explain to the user why you need to apply for the permission;
     *
     * @param title The title of the reason;
     * @return An instance of PermissionRequester.
     */
    public PermissionRequester title(@NonNull String title) {
        mTitle = title;
        return this;
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param description ??????????????????
     * @return PermissionRequester ????????????
     *
     * The text of the reason: security compliance requirements, when requesting permission,
     * explain to the user why the permission is required;
     *
     * @param description The text of the reason;
     * @return An instance of PermissionRequester.
     */
    public PermissionRequester description(@NonNull String description) {
        mDescription = description;
        return this;
    }

    /**
     * ?????????????????? Settings ???????????????????????????????????????????????????????????????
     *
     * @param settingsTip ????????????????????????????????????
     * @return PermissionRequester ????????????
     *
     * Explain to the user what permissions need to be opened after entering the Settings to
     * ensure the normal operation of the function;
     *
     * @param settingsTip Prompt user what to do in settings;
     * @return An instance of PermissionRequester.
     */
    public PermissionRequester settingsTip(@NonNull String settingsTip) {
        mSettingsTip = settingsTip;
        return this;
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param callback ????????????????????????
     * @return PermissionRequester ????????????
     *
     * Set the callback used to get the permission application result.
     *
     * @param callback callback for permission application;
     * @return An instance of PermissionRequester.
     */
    public PermissionRequester callback(@NonNull PermissionCallback callback) {
        mPermissionCallback = callback;
        return this;
    }

    /**
     * ?????????????????????
     *
     * Start requesting permission.
     */
    @SuppressLint("NewApi")
    public void request() {
        synchronized (sLock) {
            if (sIsRequesting.get()) {
                Log.e(TAG, "can not request during requesting");
                mPermissionCallback.onDenied();
                return;
            }
            sIsRequesting.set(true);
        }

        TUICore.registerEvent(PERMISSION_NOTIFY_EVENT_KEY, PERMISSION_NOTIFY_EVENT_SUB_KEY, mPermissionNotification);
        if (TUIBuild.getVersionInt() < Build.VERSION_CODES.M) {
            Log.i(TAG, "current version is lower than 23");
            afterPermissionGranted();
            return;
        }
        String[] unauthorizedPermissions = findUnauthorizedPermissions(mPermissions);
        if (unauthorizedPermissions.length <= 0) {
            afterPermissionGranted();
            return;
        }
        RequestData realRequest =
                new RequestData(mTitle, mDescription, mSettingsTip, unauthorizedPermissions);
        startPermissionActivity(realRequest);
    }

    private void afterPermissionGranted() {
        Log.i(TAG, "afterPermissionGranted");
        if (mPermissionCallback != null) {
            mPermissionCallback.onGranted();
            mPermissionCallback = null;
        }
        TUICore.unRegisterEvent(PERMISSION_NOTIFY_EVENT_KEY, PERMISSION_NOTIFY_EVENT_SUB_KEY, mPermissionNotification);
        sIsRequesting.set(false);
    }

    private void afterPermissionDenied() {
        Log.i(TAG, "afterPermissionDenied");
        if (mPermissionCallback != null) {
            mPermissionCallback.onDenied();
            mPermissionCallback = null;
        }
        TUICore.unRegisterEvent(PERMISSION_NOTIFY_EVENT_KEY, PERMISSION_NOTIFY_EVENT_SUB_KEY, mPermissionNotification);
        sIsRequesting.set(false);
    }

    private String[] findUnauthorizedPermissions(String[] permissions) {
        Context appContext = TUIConfig.getAppContext();
        if (appContext == null) {
            Log.e(TAG, "findUnauthorizedPermissions appContext is null");
            return permissions;
        }
        List<String> unauthorizedList = new LinkedList<>();
        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(appContext, permission)) {
                unauthorizedList.add(permission);
            }
        }
        return unauthorizedList.toArray(new String[0]);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startPermissionActivity(RequestData request) {
        Context context = TUIConfig.getAppContext();
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, PermissionActivity.class);
        intent.putExtra(PERMISSION_REQUEST_KEY, request);
        context.startActivity(intent);
    }

    static class RequestData implements Parcelable {
        private final String[] mPermissions;
        private final String mTitle;
        private final String mDescription;
        private final String mSettingsTip;
        private int mPermissionIconId;

        public RequestData(@NonNull String title, @NonNull String description,
                           @NonNull String settingsTip, @NonNull String... perms) {
            mTitle = title;
            mDescription = description;
            mSettingsTip = settingsTip;
            mPermissions = perms.clone();
        }

        protected RequestData(Parcel in) {
            mPermissions = in.createStringArray();
            mTitle = in.readString();
            mDescription = in.readString();
            mSettingsTip = in.readString();
            mPermissionIconId = in.readInt();
        }

        public static final Creator<RequestData> CREATOR = new Creator<RequestData>() {
            @Override
            public RequestData createFromParcel(Parcel in) {
                return new RequestData(in);
            }

            @Override
            public RequestData[] newArray(int size) {
                return new RequestData[size];
            }
        };

        public boolean isPermissionsExistEmpty() {
            if (mPermissions == null || mPermissions.length <= 0) {
                return true;
            }
            for (String permission : mPermissions) {
                if (TextUtils.isEmpty(permission)) {
                    return true;
                }
            }
            return false;
        }

        public String[] getPermissions() {
            return mPermissions.clone();
        }

        public String getTitle() {
            return mTitle;
        }

        public String getDescription() {
            return mDescription;
        }

        public String getSettingsTip() {
            return mSettingsTip;
        }

        public int getPermissionIconId() {
            return mPermissionIconId;
        }

        public void setPermissionIconId(int permissionIconId) {
            mPermissionIconId = permissionIconId;
        }

        @Override
        public String toString() {
            return "PermissionRequest{" +
                    "mPermissions=" + Arrays.toString(mPermissions) +
                    ", mTitle=" + mTitle +
                    ", mDescription='" + mDescription +
                    ", mSettingsTip='" + mSettingsTip +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStringArray(mPermissions);
            dest.writeString(mTitle);
            dest.writeString(mDescription);
            dest.writeString(mSettingsTip);
            dest.writeInt(mPermissionIconId);
        }
    }
}
