package de.vion.normmaker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * @author André Pomp
 * 
 *         The MainActivity that is shown when the application is launched
 *
 */
public class MainActivity extends Activity implements OnClickListener {

	private EditText editText;
	private Spinner spinner;
	private Spinner spinnerDistance;
	private Spinner spinnerLight;
	private Button button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Init the GUI elemente
		this.editText = (EditText) this.findViewById(R.id.activity_main_et);
		this.spinner = (Spinner) this.findViewById(R.id.activity_main_sp);
		this.spinnerDistance = (Spinner) this
				.findViewById(R.id.activity_main_sp_face);
		this.spinnerLight = (Spinner) this
				.findViewById(R.id.activity_main_sp_light);
		this.button = (Button) this.findViewById(R.id.activity_main_btn);

		// Add objects to the spinner
		ArrayAdapter<SpinnerObject> spinnerArrayAdapter = new ArrayAdapter<SpinnerObject>(
				this, android.R.layout.simple_spinner_item,
				new SpinnerObject[] { new SpinnerObject(3, 3),
						new SpinnerObject(4, 4), new SpinnerObject(5, 5) });
		this.spinner.setAdapter(spinnerArrayAdapter);
		this.spinner.setSelection(0);

		// Add objects to the spinner
		ArrayAdapter<String> spinnerDistanceArrayAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, new String[] {
						"Small Distance (~20cm)", "Normal Distance (~30cm)" });
		this.spinnerDistance.setAdapter(spinnerDistanceArrayAdapter);
		this.spinnerDistance.setSelection(0);

		ArrayAdapter<String> spinnerLightArrayAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, new String[] {
						"Light Ceiling", "Light Wall", "Both Lights" });
		this.spinnerLight.setAdapter(spinnerLightArrayAdapter);
		this.spinnerLight.setSelection(0);

		// Add the button listener
		this.button.setOnClickListener(this);

		// Create the main directory
		FileManager.createDirectoryStructure();
	}

	@Override
	public void onClick(View v) {
		String abbreviation = this.editText.getText().toString();
		if (abbreviation == null || abbreviation.isEmpty()) {
			Toast.makeText(this, "Invalid Abbreviation", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		SpinnerObject selectedItem = (SpinnerObject) this.spinner
				.getSelectedItem();

		Intent intent = new Intent(this, CameraActivity.class);
		intent.putExtras(CameraActivity.createBundle(selectedItem.getRows(),
				selectedItem.getCols(), abbreviation,
				(String) this.spinnerDistance.getSelectedItem(),
				(String) this.spinnerLight.getSelectedItem()));
		startActivity(intent);
	}
}