package ie.gov.tracing;

import android.app.Activity;
import android.os.Build;
import android.content.pm.PackageInfo;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.common.GoogleApiAvailability;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.utils.HMSPackageManager;

import org.jetbrains.annotations.NotNull;

import ie.gov.tracing.common.Events;

import static ie.gov.tracing.hms.ApiAvailabilityCheckUtils.isGMS;
import static ie.gov.tracing.hms.ApiAvailabilityCheckUtils.isHMS;

public class ExposureNotificationModule extends ReactContextBaseJavaModule {
    private static int gmsServicesVersion = 0;
    private static int hmsServicesVersion = 0;
    private static int apiError = 0;

    public boolean nearbyNotSupported(){
        return false;
    }

    // after update performed this should be called
    public void setApiError(int errorCode) {
        apiError = errorCode;
    }

    // after update performed this should be called
    public void setGmsServicesVersion(int version) {
        gmsServicesVersion = version;
    }
    public void setHmsServicesVersion(int version) {
        hmsServicesVersion = version;
    }
    public int getGmsServicesVersion() { return gmsServicesVersion; }
    public int getHmsServicesVersion() { return hmsServicesVersion; }

    //@SuppressWarnings("WeakerAccess")
    ExposureNotificationModule(ReactApplicationContext reactContext) {
        super(reactContext);

        try {
            if(isGMS(reactContext.getApplicationContext())){
                GoogleApiAvailability gps = GoogleApiAvailability.getInstance();
                gmsServicesVersion = gps.getApkVersion(reactContext.getApplicationContext());
                Events.raiseEvent(Events.INFO,
                        "Play Services Version: " + gmsServicesVersion
                                + ", Android version: " +  Build.VERSION.SDK_INT
                );
            }else if(isHMS(reactContext.getApplicationContext())){
                hmsServicesVersion = HMSPackageManager.getInstance(reactContext.getApplicationContext()).getHmsVersionCode();
                Events.raiseEvent(Events.INFO,
                        "Huawei Services Version: " + hmsServicesVersion
                                + ", Android version: " +  Build.VERSION.SDK_INT
                );
            }

        } catch(Exception ex) {
            Events.raiseError("ExposureNotification - Unable to get play services version", ex);
        }
        Tracing.init(reactContext, this);
    }

    public Activity getActivity() {
        return getCurrentActivity();
    }

    @NotNull
    @Override
    public String getName() {
        return "ExposureNotificationModule";
    }

    @ReactMethod
    public void configure(ReadableMap params) {
        if(nearbyNotSupported()) return;
        Tracing.configure(params);
    }

    @ReactMethod
    public void start(Promise promise) {
        if(nearbyNotSupported()){
            promise.resolve(false);
            return;
        }
        Tracing.start(promise);
    }

    @ReactMethod
    public void pause(Promise promise) {
        if(nearbyNotSupported()){
            promise.resolve(false);
            return;
        }
        Tracing.pause(promise);
    }

    @ReactMethod
    public void stop() {
        if(nearbyNotSupported()) return;
        Tracing.stop();
    }

    @ReactMethod
    public void isSupported(Promise promise) {
        Tracing.isSupported(promise);
    }

    @ReactMethod
    public void exposureEnabled(Promise promise) {
        if(nearbyNotSupported()){
            promise.resolve(false);
            return;
        }
        Tracing.exposureEnabled(promise);
    }

    @ReactMethod
    public void tryEnable(Promise promise) {
        Tracing.exposureEnabled(promise);
    }

    @ReactMethod
    public void checkExposure(Boolean readExposureDetails, Boolean skipTimeCheck) {
        if(nearbyNotSupported()) return;
        Tracing.checkExposure(readExposureDetails);
    }

    @ReactMethod
    public void simulateExposure(Integer timeDelay) {
        if(nearbyNotSupported()) return;
        Tracing.simulateExposure(timeDelay.longValue());
    }

    @ReactMethod
    public void authoriseExposure(Promise promise) {
        if(nearbyNotSupported()){
            promise.resolve(false);
            return;
        }
        Tracing.authoriseExposure(promise);
    }

    @ReactMethod
    public void isAuthorised(Promise promise) {
        if(nearbyNotSupported()){
            promise.resolve("unavailable");
            return;
        }
        Tracing.isAuthorised(promise);
    }

    @ReactMethod
    public void deleteAllData(Promise promise) {
        if(nearbyNotSupported()){
            promise.resolve(true);
            return;
        }
        Tracing.deleteData(promise);
    }

    @ReactMethod
    public void deleteExposureData(Promise promise) {
        if(nearbyNotSupported()){
            promise.resolve(true);
            return;
        }
        Tracing.deleteExposureData(promise);
    }

    @ReactMethod
    public void getDiagnosisKeys(Promise promise) {
        if(nearbyNotSupported()){
            promise.resolve(Arguments.createArray());
            return;
        }
        Tracing.getDiagnosisKeys(promise);
    }

    @ReactMethod
    public void getCloseContacts(Promise promise) {
        if(nearbyNotSupported()){
            promise.resolve(Arguments.createArray());
            return;
        }

        Tracing.getCloseContacts(promise);
    }

    @ReactMethod
    public void getLogData(Promise promise) {
        Tracing.getLogData(promise);
    }

    @ReactMethod
    public void status(Promise promise) {
        if(nearbyNotSupported()) {
            Tracing.setExposureStatus("unavailable", "apiError: " + apiError);
        }
        Tracing.getExposureStatus(promise);
    }

    @ReactMethod
    public void triggerUpdate(Promise promise) {
        Tracing.triggerUpdate(promise);
    }

    @ReactMethod
    public void canSupport(Promise promise) {
        promise.resolve(true);
    }

    @ReactMethod
    public void version(Promise promise) {
        WritableMap version = Tracing.version();
        promise.resolve(version);
    }

    @ReactMethod
    public void bundleId(Promise promise) {
            promise.resolve(Tracing.reactContext.getApplicationContext().getPackageName());
    }

    private PackageInfo getPackageInfo() throws Exception {
        return Tracing.reactContext.getApplicationContext().getPackageManager().getPackageInfo(Tracing.reactContext.getApplicationContext().getPackageName(), 0);
    }
}
