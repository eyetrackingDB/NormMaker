package de.vion.normmaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * @author André Pomp
 * 
 *         Class for showing the camera view and draw view and handling the
 *         actions of taking a picture and saving a picture
 *
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends Activity implements OnTouchListener,
		PictureCallback, ShutterCallback, SensorEventListener {

	private static final String INTENT_VALUE_ROWS = "INTENT_VALUE_ROWS";
	private static final String INTENT_VALUE_COLS = "INTENT_VALUE_COLS";
	private static final String INTENT_VALUE_ABB = "INTENT_VALUE_ABB";
	private static final String INTENT_VALUE_DISTANCE = "INTENT_VALUE_DISTANCE";
	private static final String INTENT_VALUE_LIGHT = "INTENT_VALUE_LIGHT";

	// The camera
	private Camera camera;

	// The current layouts and views
	private FrameLayout layout;
	private CameraView cameraView;
	private DrawView drawView;

	// The current subdir and files
	private String subdir;
	private FileWriter fileWriter;
	private float currentSensorValue;
	private SensorManager sensorManager;
	private Sensor sensor;

	private int rows;
	private int cols;

	public static Bundle createBundle(int rows, int cols, String abbreviation,
			String face, String light) {
		Bundle bundle = new Bundle();
		bundle.putInt(INTENT_VALUE_ROWS, rows);
		bundle.putInt(INTENT_VALUE_COLS, cols);
		bundle.putString(INTENT_VALUE_ABB, abbreviation);
		bundle.putString(INTENT_VALUE_DISTANCE, face);
		bundle.putString(INTENT_VALUE_LIGHT, light);
		return bundle;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		// Get all bundle values
		Bundle args = getIntent().getExtras();
		this.rows = args.getInt(INTENT_VALUE_ROWS);
		this.cols = args.getInt(INTENT_VALUE_COLS);
		String abbreviation = args.getString(INTENT_VALUE_ABB);

		String distanceType = args.getString(INTENT_VALUE_DISTANCE);
		String distanceAbb = null;
		if (distanceType.equals("Small Distance (~20cm)")) {
			distanceAbb = "small";
		} else {
			distanceAbb = "normal";
		}

		String light = args.getString(INTENT_VALUE_LIGHT);
		String lightAbb = null;
		if (light.equals("Light Ceiling")) {
			lightAbb = "lc";
		} else if (light.equals("Light Wall")) {
			lightAbb = "lw";
		} else {
			lightAbb = "lb";
		}

		// Create the sub directory
		this.subdir = FileManager.createTestDirectory(abbreviation,
				distanceAbb, lightAbb);

		// Init the layouts
		this.layout = (FrameLayout) findViewById(R.id.ff);
		this.layout.setOnTouchListener(this);

		this.cameraView = new CameraView(this);
		this.layout.addView(this.cameraView);

		FrameLayout.LayoutParams face = null;
		if (distanceType.equals("Small Distance (~20cm)")) {
			// big face for small distance
			face = new FrameLayout.LayoutParams(1200, 1920);
		} else {
			// small face for high distance
			face = new FrameLayout.LayoutParams(900, 1440);
		}
		face.gravity = Gravity.CENTER;
		ImageView faceView = new ImageView(this);
		faceView.setImageDrawable(getResources().getDrawable(R.drawable.face2));
		faceView.setLayoutParams(face);
		this.layout.addView(faceView);

		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				1200, 1920);
		this.drawView = new DrawView(this, this.rows, this.cols);
		this.drawView.setLayoutParams(layoutParams);
		this.layout.addView(this.drawView);

		// Init the sensorManager
		this.sensorManager = (SensorManager) this
				.getSystemService(Context.SENSOR_SERVICE);
		this.sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (this.cameraView != null) {
			if (startCamera()) {
				try {
					this.fileWriter = new FileWriter(
							FileManager
									.generateSensorLightFilenPath(this.subdir));
					this.cameraView.setCamera(this.camera);
					this.sensorManager.registerListener(this, this.sensor,
							SensorManager.SENSOR_DELAY_NORMAL);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (this.cameraView != null) {
			try {
				stopCamera();
				this.sensorManager.unregisterListener(this, this.sensor);
				this.fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (this.cameraView.isSafeToTakePicture()) {
			this.camera.takePicture(this, null, this);
			this.cameraView.setSafeToTakePicture(false);
		}
		return true;
	}

	private boolean startCamera() {
		try {
			stopCamera();
			this.camera = Camera.open(1);
			if (this.camera != null) {
				Camera.Parameters params = this.camera.getParameters();

				// Check what resolutions are supported by your camera
				List<Size> sizes = params.getSupportedPictureSizes();

				this.camera.setDisplayOrientation(270);

				// Iterate through all available resolutions and choose one.
				// The chosen resolution will be stored in mSize.
				Size mSize = null;
				for (Size size : sizes) {
					if (size.width == 1280 && size.height == 720) {
						mSize = size;
					}
				}
				params.setPictureSize(mSize.width, mSize.height);
				this.camera.setParameters(params);

				return true;
			}
			return false;
		} catch (Exception e) {
			Log.e("NORM MAKER", "Failed to open camera");
			e.printStackTrace();
			return false;
		}
	}

	private void stopCamera() {
		this.cameraView.setCamera(null);
		if (this.camera != null) {
			this.camera.release();
			this.camera = null;
		}
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		File pictureFile = new File(FileManager.generatePictureFilePath(
				this.subdir,
				(this.drawView.getCurrentPosition().picturePosition + 1)));
		this.camera.startPreview();

		try {
			// Log the sensor data
			this.fileWriter.append((this.drawView.getCurrentPointNumber() + 1)
					+ ";"
					+ (this.drawView.getCurrentPosition().picturePosition + 1)
					+ ";" + this.drawView.getCurrentPosition().point.x + ";"
					+ this.drawView.getCurrentPosition().point.y + ";"
					+ this.currentSensorValue + "\n");

			// Store the image
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (this.drawView.getCurrentPointNumber() >= ((this.rows * this.cols) - 1)) {
			finish();
		}

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				CameraActivity.this.drawView.updatePoint();
				CameraActivity.this.cameraView.setSafeToTakePicture(true);
			}
		}, 1000);
	}

	@Override
	public void onShutter() {
		// do nothing
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// do nothing
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			this.currentSensorValue = event.values[0];
		}
	}
}