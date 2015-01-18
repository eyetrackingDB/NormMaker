package de.vion.normmaker;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author André Pomp
 * 
 *         The view that shows the camera picture
 *
 */
@SuppressWarnings("deprecation")
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder holder;

	private Camera camera;
	private boolean safeToTakePicture = false;

	public CameraView(Context context) {
		super(context);
		this.holder = getHolder();
		this.holder.addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// do nothing
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		try {
			this.camera.stopPreview();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// start preview with new settings
		try {
			this.camera.setPreviewDisplay(holder);
			this.camera.startPreview();

			// Delay the starting of pictures for about 1 second
			CameraView.this.safeToTakePicture = true;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (this.camera != null) {
			this.camera.stopPreview();
			this.safeToTakePicture = false;
		}
	}

	public void setCamera(Camera camera) {
		if (this.camera == camera) {
			return;
		}

		stopPreviewAndFreeCamera();

		this.camera = camera;

		if (this.camera != null) {
			try {
				this.camera.setPreviewDisplay(this.holder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.camera.startPreview();
		}
	}

	private void stopPreviewAndFreeCamera() {
		if (this.camera != null) {
			this.camera.stopPreview();
			this.camera.release();
			this.camera = null;
		}
	}

	public boolean isSafeToTakePicture() {
		return this.safeToTakePicture;
	}

	public void setSafeToTakePicture(boolean safeToTakePicture) {
		this.safeToTakePicture = safeToTakePicture;
	}
}