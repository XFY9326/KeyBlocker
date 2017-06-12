package tool.xfy9326.keyblocker.base;

import android.app.Application;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        CrashHandler.get().Catch(this);
        super.onCreate();
    }

}
