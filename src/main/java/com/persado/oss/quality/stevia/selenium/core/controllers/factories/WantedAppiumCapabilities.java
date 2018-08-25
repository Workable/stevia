package com.persado.oss.quality.stevia.selenium.core.controllers.factories;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.appium.java_client.remote.AndroidMobileCapabilityType.*;
import static io.appium.java_client.remote.IOSMobileCapabilityType.*;
import static io.appium.java_client.remote.MobileCapabilityType.*;

/**
 * Created by dimitris giannakos on 25/08/2018.
 */
class WantedAppiumCapabilities {

    static final List<String> IOS_DEFAULT_CAPABILITIES = Stream.of(
            USE_PREBUILT_WDA,
            WDA_LOCAL_PORT,
            WDA_CONNECTION_TIMEOUT,
            USE_NEW_WDA,
            XCODE_ORG_ID,
            XCODE_SIGNING_ID,
            SHOW_IOS_LOG,
            AUTO_ACCEPT_ALERTS,
            AUTO_DISMISS_ALERTS,
            WDA_LAUNCH_TIMEOUT,
            WDA_STARTUP_RETRIES,
            "useJSONSource"
    ).collect(Collectors.toList());

    static final List<String> COMMON_CAPABILITIES = Stream.of(
            DEVICE_NAME,
            UDID,
            PLATFORM_NAME,
            PLATFORM_VERSION,
            APP,
            AUTO_WEBVIEW,
            AUTOMATION_NAME,
            NO_RESET,
            CLEAR_SYSTEM_FILES
    ).collect(Collectors.toList());

    static final List<String> ANDROID_DEFAULT_CAPABILITIES = Stream.of(
            APP_PACKAGE,
            APP_WAIT_PACKAGE,
            APP_ACTIVITY,
            APP_WAIT_ACTIVITY,
            ADB_PORT,
            SYSTEM_PORT,
            NO_SIGN,
            USE_KEYSTORE,
            KEYSTORE_PATH,
            KEYSTORE_PASSWORD,
            KEY_ALIAS,
            KEY_PASSWORD,
            ANDROID_COVERAGE,
            "skipUnlock"
    ).collect(Collectors.toList());

    static final List<String> TEST_DROID_CAPABILITIES = Stream.of(
            "testdroid_username",
            "testdroid_password",
            "testdroid_password",
            "testdroid_apiKey",
            "testdroid_target",
            "testdroid_project",
            "testdroid_testrun",
            "testdroid_device",
            "testdroid_app"
    ).collect(Collectors.toList());

    static final List<String> SAUCE_LABS_CAPABILITIES = Stream.of(
            "username",
            "access-key",
            "deviceType",
            "appiumVersion"
    ).collect(Collectors.toList());
}