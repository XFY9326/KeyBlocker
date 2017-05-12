package tool.xfy9326.keyblocker.base;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	private Context mContext;

	public static CrashHandler get() {
		return new CrashHandler();
	}

	public void Catch(Context context) {
		this.mContext = context;
		this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, final Throwable ex) {
		if (ex == null && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			new Thread(new Runnable() {
					@Override
					public void run() {
						Looper.prepare();
						Toast.makeText(mContext, ExToString(ex), Toast.LENGTH_LONG).show();
						Looper.loop();
					}
				}).start();
			try {
				thread.sleep(3000);
				thread.stop();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
	}

	private String ExToString(Throwable ex) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String str = writer.toString();
		String[] errs = str.split("\n");
		String result = "";
		for (String err : errs) {
			if (err.contains("at")) {
				if (!err.contains(mContext.getPackageName())) {
					continue;
				}
				else {
					err = err.replace(mContext.getPackageName(), "").trim();
					StringBuilder sb = new StringBuilder(err);
					sb.insert(err.indexOf("(") , "\n");
					err = sb.toString();
				}
			}
			result += err + "\n";
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}

}
