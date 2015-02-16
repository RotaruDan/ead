/**
 * eAdventure is a research project of the
 *    e-UCM research group.
 *
 *    Copyright 2005-2014 e-UCM research group.
 *
 *    You can access a list of all the contributors to eAdventure at:
 *          http://e-adventure.e-ucm.es/contributors
 *
 *    e-UCM is a research group of the Department of Software Engineering
 *          and Artificial Intelligence at the Complutense University of Madrid
 *          (School of Computer Science).
 *
 *          CL Profesor Jose Garcia Santesmases 9,
 *          28040 Madrid (Madrid), Spain.
 *
 *          For more info please visit:  <http://e-adventure.e-ucm.es> or
 *          <http://www.e-ucm.es>
 *
 * ****************************************************************************
 *
 *  This file is part of eAdventure
 *
 *      eAdventure is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      eAdventure is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with eAdventure.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.eucm.ead.android;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Button;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import es.eucm.ead.editor.MokapApplicationListener;
import es.eucm.ead.editor.platform.MokapPlatform;
import es.eucm.mokap.R;

public class EditorActivity extends BaseActivity {

	private Map<Integer, ActivityResultListener> listeners;

    private static final String TAG = "MainActivity";

    // Instance variables used for DriveFile and DriveContents to help initiate file conflicts.
    protected DriveFile groceryListFile;
    protected DriveContents groceryListContents;

    // Receiver used to update the EditText once conflicts have been resolved.
    protected BroadcastReceiver broadcastReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useImmersiveMode = false;
		config.hideStatusBar = true;
		config.useWakelock = true;
		config.useCompass = false;

		this.listeners = new HashMap<Integer, ActivityResultListener>();
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
		Tracker tracker = analytics.newTracker(R.xml.tracker);
		analytics.reportActivityStart(this);
		initialize(
				new MokapApplicationListener(handleIntent(getIntent(),
						new AndroidPlatform(getContext(), tracker))), config);
	}

	private MokapPlatform handleIntent(Intent intent, MokapPlatform platform) {

		if (intent != null) {
			String action = intent.getAction();
			if (Intent.ACTION_VIEW.equals(action)) {
				Uri data = intent.getData();
				if (data != null) {
					String path = data.getPath();
					platform.setApplicationArguments(path);
				}
			}
		}
		return platform;
	}

	public void startActivityForResult(Intent intent, int requestCode,
			ActivityResultListener l) {
		this.listeners.put(requestCode, l);
		super.startActivityForResult(intent, requestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ActivityResultListener listener = this.listeners.get(requestCode);
		if (listener != null) {
			listener.result(resultCode, data);
		}
	}

	public void post(Runnable run) {
		super.handler.post(run);
	}

	public interface ActivityResultListener {
		void result(int resultCode, Intent data);
	}
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(ConflictResolver.CONFLICT_RESOLVED));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        // Syncing to help devices use the same file.
        Drive.DriveApi.requestSync(getGoogleApiClient()).setResultCallback(syncCallback);
    }

    // Callback when requested sync returns.
    private ResultCallback<Status> syncCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            if (!status.isSuccess()) {
                Log.e(TAG, "Unable to sync.");
            }
            Query query = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TITLE, "TEST_TITLE_1"))
                    .build();
            Drive.DriveApi.query(getGoogleApiClient(), query).setResultCallback(metadataCallback);
        }
    };

    // Callback when search for the grocery list file returns. It sets {@code groceryListFile} if
    // it exists or initiates the creation of a new file if no file is found.
    private ResultCallback<DriveApi.MetadataBufferResult> metadataCallback =
            new ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                    if (!metadataBufferResult.getStatus().isSuccess()) {
                        showMessage("Problem while retrieving results.");
                        return;
                    }
                    int results = metadataBufferResult.getMetadataBuffer().getCount();
                    if (results > 0) {
                        // If the file exists then use it.
                        DriveId driveId = metadataBufferResult.getMetadataBuffer().get(0).getDriveId();
                        groceryListFile = Drive.DriveApi.getFile(getGoogleApiClient(), driveId);
                        groceryListFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                                .setResultCallback(driveContentsCallback);
                    } else {
                        // If the file does not exist then create one.
                        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                                .setResultCallback(newContentsCallback);
                    }
                }
            };

    // Callback when {@code groceryListContents} is reopened for writing.
    private ResultCallback<DriveApi.DriveContentsResult> updateDriveContensCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                    if (!driveContentsResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Unable to updated grocery list.");
                        return;
                    }
                    DriveContents driveContents = driveContentsResult.getDriveContents();
                    OutputStream outputStream = driveContents.getOutputStream();
                    Writer writer = new OutputStreamWriter(outputStream);
                    try {
                        writer.write("TEST_WRITE_1");
                        writer.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }

                    // ExecutionOptions define the conflict strategy to be used.
                    ExecutionOptions executionOptions = new ExecutionOptions.Builder()
                            .setNotifyOnCompletion(true)
                            .setConflictStrategy(ExecutionOptions.CONFLICT_STRATEGY_KEEP_REMOTE)
                            .build();
                    driveContents.commit(getGoogleApiClient(), null, executionOptions)
                            .setResultCallback(fileWrittenCallback);

                    Log.d(TAG, "Saving file.");
                }
            };

    // Callback when file has been written locally.
    private ResultCallback<Status> fileWrittenCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            if (!status.isSuccess()) {
                Log.e(TAG, "Unable to write grocery list.");
            }
            Log.d(TAG, "File saved locally.");
            groceryListFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                    .setResultCallback(driveContentsCallback);
        }
    };

    // Callback when {@code DriveApi.DriveContentsResult} for the creation of a new
    // {@code DriveContents} has been returned.
    private ResultCallback<DriveApi.DriveContentsResult> newContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                    if (!driveContentsResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Unable to create grocery list file contents.");
                        return;
                    }
                    Log.d(TAG, "grocery_list new file contents returned.");
                    groceryListContents = driveContentsResult.getDriveContents();

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("TITLE_TEST_1")
                            .setMimeType("text/plain")
                            .build();
                    // create a file on root folder
                    Drive.DriveApi.getRootFolder(getGoogleApiClient())
                            .createFile(getGoogleApiClient(), changeSet, groceryListContents)
                            .setResultCallback(groceryListFileCallback);
                }
            };

    // Callback when request to create grocery list file is returned.
    private ResultCallback<DriveFolder.DriveFileResult> groceryListFileCallback =
            new ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                    if (!driveFileResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Unable to create grocery list file.");
                        return;
                    }
                    Log.d(TAG, "Grocery list file returned.");
                    groceryListFile = driveFileResult.getDriveFile();
                    // Open {@code groceryListFile} in read only mode to update
                    // {@code groceryListContents} to current base state.
                    groceryListFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                            .setResultCallback(driveContentsCallback);
                }
            };

    // Callback when request to open {@code groceryListFile} in read only mode is returned.
    private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                    if (!driveContentsResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Unable to load grocery list data.");

                        // Try to open {@code groceryListFile} again.
                        groceryListFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                                .setResultCallback(driveContentsCallback);
                        return;
                    }
                    groceryListContents = driveContentsResult.getDriveContents();
                    InputStream inputStream = groceryListContents.getInputStream();
                    String groceryListStr = ConflictUtil.getStringFromInputStream(inputStream);


                }
            };
}
