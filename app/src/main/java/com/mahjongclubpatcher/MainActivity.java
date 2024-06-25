package com.mahjongclubpatcher;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mahjongclubpatcher.constant.RequestCode;
import com.mahjongclubpatcher.util.FileTools;
import com.mahjongclubpatcher.util.GameJSON;
import com.mahjongclubpatcher.util.PermissionTools;
import com.mahjongclubpatcher.util.ToastUtils;

import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ProcessCallbackInterface {
	TextView textViewDailyStartDate;
	TextView textViewDailyEndDate;
	Button buttonDailyButtonPatch;
	Button buttonButterflyCollect;
	Button buttonButterflyRestore;
	TextView textViewExperienceLevel;
	Button buttonExperienceBooster;
	TextView progressField;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Check storage permission
		if (!PermissionTools.hasStoragePermission()) {
			showStoragePermissionDialog();
		}
		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initView() {
		setContentView(R.layout.activity_main);
		progressField = findViewById(R.id.field_process);
		textViewDailyStartDate = findViewById(R.id.daily_start_date);
		textViewDailyEndDate = findViewById(R.id.daily_end_date);
		buttonDailyButtonPatch = findViewById(R.id.daily_button_patch);
		buttonButterflyCollect = findViewById(R.id.butterfly_button_patch);
		buttonButterflyRestore = findViewById(R.id.butterfly_button_restore);
		textViewExperienceLevel = findViewById(R.id.experience_level);
		buttonExperienceBooster = findViewById(R.id.experience_booster);
		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH);
		textViewDailyEndDate.setText(
				getString(R.string.daily_levels_patch_dummy_date,
						mDay,
						(mMonth + 1),
						mYear)
		);
		initDatePicker();
		buttonDailyButtonPatch.setOnClickListener(dailyButtonListener);
		if (!FileTools.shouldRequestUriPermission(FileTools.dataPath)) {
			textViewExperienceLevel.setText(GameJSON.currentLevel(this));
		}
	}

	private final View.OnClickListener dailyButtonListener = button -> {
		if (FileTools.shouldRequestUriPermission(FileTools.dataPath)) {
			showRequestUriPermissionDialog();
		} else {
			progressField.setText("Removing existing files in DailyChallengeLevelsStatus");
			FileTools.cleanDailyChallengeLevelsStatus();
			progressField.setText("Generate new files for DailyChallengeLevelsStatus");
			GameJSON.dailyLevelsMaker(
					this,
					textViewDailyStartDate.getText().toString(),
					textViewDailyEndDate.getText().toString());
			progressField.setText("Generating of new files for DailyChallengeLevelsStatus is done!");
		}
//        String sourcePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.gamovation.mahjongclub/files/Requests";
//        Path sourceDirectory = Paths.get(sourcePath);
//        String targetPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MahjongClubBackup/DailyChallengeLevelsStatus";
//        Path targetDirectory = Paths.get(targetPath);
//
//        try {
//            Files.copy(sourceDirectory, targetDirectory);
//            progressField.setText(progressField.getText() + "\nBackup done to " + targetPath);
//        } catch (IOException e) {
//            progressField.setText(progressField.getText() + "\nNo success");
//            Log.e("Copy", e.getMessage());
//        }
	};

	private final View.OnClickListener butterflyCollectListener = button -> {
		if (FileTools.shouldRequestUriPermission(FileTools.dataPath)) {
			showRequestUriPermissionDialog();
		} else {
			progressField.setText("Backup existing butterfly event file");
			FileTools.copyFile(this,
					FileTools.mahjongClubFilesPath + "Data/",
					"butterfly_event_data.gvmc",
					Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath());
			progressField.setText("Saving new butterfly event file");
			GameJSON.butterflyEventFilePatched(this);
			progressField.setText("New butterfly event file ready");
		}
	};

	private void initDatePicker() {
		textViewDailyStartDate.setOnClickListener(v -> {
			int mYear = 2022;
			int mMonth = 1;
			int mDay = 1;

			// Launch Date Picker Dialog
			DatePickerDialog dpd = new DatePickerDialog(MainActivity.this,
					(view, year, monthOfYear, dayOfMonth) -> {
						// Display Selected date in textbox
						view.updateDate(mYear, mMonth, mDay);
						textViewDailyStartDate.setText(
								getString(R.string.daily_levels_patch_dummy_date,
										dayOfMonth,
										(monthOfYear + 1),
										year)
						);
						progressField.setText(textViewDailyStartDate.getText());

					}, mYear, mMonth, mDay);
			dpd.show();

		});
		textViewDailyEndDate.setOnClickListener(v -> {
			final Calendar c = Calendar.getInstance();
			int mYear = c.get(Calendar.YEAR);
			int mMonth = c.get(Calendar.MONTH);
			int mDay = c.get(Calendar.DAY_OF_MONTH);

			// Launch Date Picker Dialog
			DatePickerDialog dpd = new DatePickerDialog(MainActivity.this,
					(view, year, monthOfYear, dayOfMonth) -> {
						// Display Selected date in textbox
						textViewDailyEndDate.setText(
								getString(R.string.daily_levels_patch_dummy_date,
										dayOfMonth,
										(monthOfYear + 1),
										year)
						);

					}, mYear, mMonth, mDay);
			dpd.show();

		});
	}

	// Storage and file access part

	private void showStoragePermissionDialog() {
		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(R.string.dialog_storage_message)
				.setPositiveButton(R.string.dialog_button_request_permission, (dialog, which) -> {
					PermissionTools.requestStoragePermission(this);
				})
				.setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
					finish();
				}).create().show();
	}

	private void showRequestUriPermissionDialog() {
		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(R.string.dialog_need_uri_permission_message)
				.setPositiveButton(R.string.dialog_button_request_permission, (dialog, which) -> {
					FileTools.requestUriPermission(this, FileTools.dataPath);
				})
				.setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
				}).create().show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == RequestCode.STORAGE) {
			onStoragePermissionResult(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RequestCode.STORAGE) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				onStoragePermissionResult(Environment.isExternalStorageManager());
			}
		} else if (requestCode == RequestCode.DOCUMENT) {
			Uri uri;
			if (data != null && (uri = data.getData()) != null) {
				getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				onDocumentPermissionResult(true);
			} else {
				onDocumentPermissionResult(false);
			}
		}
	}

	protected void onStoragePermissionResult(boolean granted) {
		if (granted) {
			ToastUtils.shortCall(R.string.toast_permission_granted);
			if (FileTools.shouldRequestUriPermission(FileTools.dataPath)) {
				showRequestUriPermissionDialog();
			}
		} else {
			ToastUtils.shortCall(R.string.toast_permission_not_granted);
			showStoragePermissionDialog();
		}
	}

	protected void onDocumentPermissionResult(boolean granted) {
		if (granted) {
			ToastUtils.shortCall(R.string.toast_permission_granted);
			textViewExperienceLevel.setText(GameJSON.currentLevel(this));
		} else {
			ToastUtils.shortCall(R.string.toast_permission_not_granted);
		}
	}

	@Override
	public void updateProcessText(String text) {
		String s = text + "\n" + progressField.getText();
		((TextView) findViewById(R.id.field_process)).setText(s);
	}

}
