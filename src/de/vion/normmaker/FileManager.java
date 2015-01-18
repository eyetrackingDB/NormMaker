package de.vion.normmaker;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Environment;

/**
 * @author André Pomp
 * 
 *         A helper class for creating the folders and file access for the
 *         NormMaker application
 *
 */
public class FileManager {

	// Main Path
	private static String DIR_MAIN_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/NormMaker/";

	// Logging files
	private static String FILE_NAME_SENSOR_LIGHT = "logging.log";

	public static void createDirectoryStructure() {
		// Create the directory if it does not exist
		makeDir(DIR_MAIN_PATH);
	}

	private static void makeDir(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	@SuppressLint("SimpleDateFormat")
	public static String createTestDirectory(String abbreviation,
			String distance, String light) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date currentTime = Calendar.getInstance().getTime();
		String directoryName = df.format(currentTime) + "_" + abbreviation
				+ "_" + distance + "_" + light + "/";

		String filePath = DIR_MAIN_PATH + directoryName;
		makeDir(filePath);

		return filePath;
	}

	public static String generateSensorLightFilenPath(String subdir) {
		return subdir + FILE_NAME_SENSOR_LIGHT;
	}

	public static String generatePictureFilePath(String subdir,
			int currentPicture) {
		return subdir + "image_" + currentPicture + ".jpg";
	}
}