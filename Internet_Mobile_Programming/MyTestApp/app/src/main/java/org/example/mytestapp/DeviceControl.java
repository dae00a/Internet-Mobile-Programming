package org.example.mytestapp;

public class DeviceControl {
    // Used to load the 'mytestapp' library on application startup.
    static {
        System.loadLibrary("mytestapp");
    }
    
    public DeviceControl() {
    }
    
    /**
     * A native method that is implemented by the 'mytestapp' native library,
     * which is packaged with this application.
     */
    public native void segmentWrite(int num);
    public native void segmentIOctl(int num);
    public native void ledWrite();
    public native void dotMatrixWrite(String str);
    public native void textWrite(boolean isClear);
    public native void textClear();
}
