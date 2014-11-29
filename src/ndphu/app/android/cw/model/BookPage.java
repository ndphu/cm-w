package ndphu.app.android.cw.model;

import android.graphics.Bitmap;

public class BookPage {
	private String fileName;
	private byte[] data;
	private Bitmap bitmap;
	

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	
	
}
