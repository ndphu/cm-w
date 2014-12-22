package ndphu.app.android.cw.taskmanager;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ndphu.app.android.cw.runable.DownloadFileRunnable;

public class TaskManager implements RejectedExecutionHandler {
	private static final Object LOCK = new Object();

	protected static final String TAG = TaskManager.class.getSimpleName();

	private static TaskManager instance = null;
	private int mCorePoolSize = 8;
	private int mMaximumPoolSize = 24;
	private long mKeepAlive = 10000;
	private ThreadPoolExecutor mExecutor;


	private TaskManager() {
		mExecutor = new ThreadPoolExecutor(mCorePoolSize, mMaximumPoolSize, mKeepAlive, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), this);
	}

	public static TaskManager getInstance() {
		synchronized (LOCK) {
			if (instance == null) {
				instance = new TaskManager();
			}
		}
		return instance;
	}

	public void downloadFile(DownloadFileRunnable dfr) {
		mExecutor.execute(dfr);
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

	}

}
