package com.persado.oss.quality.stevia.selenium.core.controllers.commonapi;

/*
 * #%L
 * Stevia QA Framework - Core
 * %%
 * Copyright (C) 2013 - 2018 Persado Intellectual Property Limited
 * %%
 * Copyright (c) Persado Intellectual Property Limited. All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *  
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  
 * * Neither the name of the Persado Intellectual Property Limited nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.appium.java_client.remote.AndroidMobileCapabilityType.*;
import static io.appium.java_client.remote.IOSMobileCapabilityType.*;
import static io.appium.java_client.remote.MobileCapabilityType.*;

/**
 * Created by dimitris giannakos on 25/08/2018.
 */
public class WantedAppiumCapabilities {

    public static final List<String> IOS_DEFAULT_CAPABILITIES = Stream.of(
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

    public static final List<String> COMMON_CAPABILITIES = Stream.of(
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

    public static final List<String> ANDROID_DEFAULT_CAPABILITIES = Stream.of(
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

    public static final List<String> TEST_DROID_CAPABILITIES = Stream.of(
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

    public static final List<String> SAUCE_LABS_CAPABILITIES = Stream.of(
            "username",
            "access-key",
            "deviceType",
            "appiumVersion"
    ).collect(Collectors.toList());

    public static final List<String> SELENIUM_GRID_CAPABILITIES = Stream.of(
            "deviceName",
            "deviceType"
    ).collect(Collectors.toList());
}