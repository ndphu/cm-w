package ndphu.app.android.cw.task;

public interface CommonTaskListener<T> {
	public void onBegin();

	public void onCompleted(T result);

	public void onErrorOccurred(Exception cause);
}
