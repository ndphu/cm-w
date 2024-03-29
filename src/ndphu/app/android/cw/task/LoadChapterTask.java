package ndphu.app.android.cw.task;

import java.io.IOException;
import java.util.List;

import ndphu.app.android.cw.dao.DaoUtils;
import ndphu.app.android.cw.io.processor.BlogTruyenProcessor;
import ndphu.app.android.cw.io.processor.BookProcessor;
import ndphu.app.android.cw.io.processor.IZMangaProcessor;
import ndphu.app.android.cw.io.processor.Manga24hProcessor;
import ndphu.app.android.cw.io.processor.TT8Processor;
import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.model.Page;
import android.os.AsyncTask;

public class LoadChapterTask extends AsyncTask<Void, Void, Object> {
	private static final String TAG = LoadChapterTask.class.getSimpleName();
	private Long mChapterId;
	private Chapter mChapter;
	private boolean mLoadFromDB;

	LoadChapterTaskListener mListener;

	public static interface LoadChapterTaskListener extends CommonTaskListener<Chapter> {

	}

	public LoadChapterTask(Long chapterId, LoadChapterTaskListener listener, boolean loadFromDB) {
		mChapterId = chapterId;
		mChapter = DaoUtils.getChapterById(mChapterId);
		mListener = listener;
		mLoadFromDB = loadFromDB;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mListener != null) {
			mListener.onBegin();
		}
	}

	@Override
	protected Object doInBackground(Void... params) {
		if (mLoadFromDB) {
			DaoUtils.loadPagesToChapter(mChapter);
			if (mChapter.getPages().size() > 0) {
				return mChapter;
			}
		}
		BookProcessor processor = null;
		switch (mChapter.getSource()) {
		case MANGA24H:
			processor = new Manga24hProcessor();
			break;
		case BLOGTRUYEN:
			processor = new BlogTruyenProcessor();
			break;
		case IZMANGA:
			processor = new IZMangaProcessor();
		case TT8:
			processor = new TT8Processor();
			break;
		default:
			break;
		}
		try {
			List<Page> pageList = processor.getPageList(mChapter.getUrl());
			mChapter.setPages(pageList);
			DaoUtils.saveOrUpdate(mChapter);
			return mChapter;
		} catch (IOException e) {
			e.printStackTrace();
			return e;
		}
	}

	@Override
	protected void onPostExecute(Object result) {
		if (mListener != null) {
			if (result == null) {
				mListener.onErrorOccurred(new RuntimeException("Unexpected Error"));
			} else {
				if (result instanceof Exception) {
					mListener.onErrorOccurred((Exception) result);
				} else if (result instanceof Chapter) {
					mListener.onCompleted((Chapter) result);
				}
			}
		}
	}

	;

}