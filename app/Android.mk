LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := app
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_SRC_FILES := \
	C:\Users\elija\AndroidStudioProjects\1791\app\src\main\jniLibs\arm64-v8a\libopencv_java3.so \
	C:\Users\elija\AndroidStudioProjects\1791\app\src\main\jniLibs\armeabi\libopencv_java3.so \
	C:\Users\elija\AndroidStudioProjects\1791\app\src\main\jniLibs\armeabi-v7a\libopencv_java3.so \
	C:\Users\elija\AndroidStudioProjects\1791\app\src\main\jniLibs\mips\libopencv_java3.so \
	C:\Users\elija\AndroidStudioProjects\1791\app\src\main\jniLibs\mips64\libopencv_java3.so \
	C:\Users\elija\AndroidStudioProjects\1791\app\src\main\jniLibs\x86\libopencv_java3.so \
	C:\Users\elija\AndroidStudioProjects\1791\app\src\main\jniLibs\x86_64\libopencv_java3.so \

LOCAL_C_INCLUDES += C:\Users\elija\AndroidStudioProjects\1791\app\src\main\jniLibs
LOCAL_C_INCLUDES += C:\Users\elija\AndroidStudioProjects\1791\app\src\main\jni
LOCAL_C_INCLUDES += C:\Users\elija\AndroidStudioProjects\1791\app\src\debug\jni

include $(BUILD_SHARED_LIBRARY)
