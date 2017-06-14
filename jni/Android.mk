LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := gpioSet
LOCAL_SRC_FILES := libgpioSet.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := live
LOCAL_SRC_FILES := liblive.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := netcfg
LOCAL_SRC_FILES := libnetcfg.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := openh264
LOCAL_SRC_FILES := libopenh264.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := PhoneCore
LOCAL_SRC_FILES := libPhoneCore.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := pjsua2
LOCAL_SRC_FILES := libpjsua2.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := safe
LOCAL_SRC_FILES := libsafe.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := talkServer
LOCAL_SRC_FILES := libtalkServer.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := YunClient
LOCAL_SRC_FILES := libYunClient.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := yuv
LOCAL_SRC_FILES := libyuv.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := Base64Code
LOCAL_SRC_FILES := Base64Code.c
LOCAL_LDLIBS := -llog -landroid
include $(BUILD_SHARED_LIBRARY)
