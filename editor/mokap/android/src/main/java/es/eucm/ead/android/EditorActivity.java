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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import es.eucm.ead.editor.MokapApplicationListener;
import es.eucm.ead.editor.platform.MokapPlatform;
import es.eucm.mokap.R;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EditorActivity extends BaseActivity {

    /**
     * DRIVE_OPEN Intent action.
     */
    private static final String ACTION_DRIVE_OPEN = "com.google.android.apps.drive.DRIVE_OPEN";
    /**
     * Drive file ID key.
     */
    private static final String EXTRA_FILE_ID = "resourceId";
    /**
     * Drive file ID.
     */
    private String mFileId;

    private static Drive mGOOSvc;

    private Map<Integer, ActivityResultListener> listeners;

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
        mFileId = null;
        if (intent != null) {
            String action = intent.getAction();
            if (Intent.ACTION_VIEW.equals(action)) {
                Uri data = intent.getData();
                if (data != null) {
                    String path = data.getPath();
                    platform.setApplicationArguments(path);
                }
            } else if (ACTION_DRIVE_OPEN.equals(action)) {
                // Get the Drive file ID.
                mFileId = intent.getStringExtra(EXTRA_FILE_ID);
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
        super.onActivityResult(requestCode, resultCode, data);
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


    private static final String TAG = "CreateFileActivity";

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        // create new contents resource
//        Drive.DriveApi.newDriveContents(getGoogleApiClient())
//                .setResultCallback(driveContentsCallback);

//        Query query = new Query.Builder()
//                .addFilter(Filters.eq(SearchableField.TITLE, "test_elem"))
//                .build();
//        Drive.DriveApi.query(getGoogleApiClient(), query).setResultCallback(metadataCallback);

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("New folder").build();
        Drive.DriveApi.getRootFolder(getGoogleApiClient()).createFolder(
                getGoogleApiClient(), changeSet).setResultCallback(folderCreatedCallback);

        Drive.DriveApi.fetchDriveId(getGoogleApiClient(), "0BzxXsuMMSYHPT2hkeHlxcHA3NHM")
                .setResultCallback(folderIdCallback);
        // No funciona porque La app de Drive lanza un "error interno"
        if (mFileId != null) {
            Drive.DriveApi.fetchDriveId(getGoogleApiClient(), mFileId)
                    .setResultCallback(idCallback);
        }

    }

    private ResultCallback<DriveFolder.DriveFolderResult> folderCreatedCallback = new
            ResultCallback<DriveFolder.DriveFolderResult>() {
                @Override
                public void onResult(DriveFolder.DriveFolderResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the folder");
                        return;
                    }
                    showMessage("Created a folder: " + result.getDriveFolder().getDriveId());
//                    DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(), result.getDriveFolder().getDriveId());
//                    folder.listChildren(getGoogleApiClient()).setResultCallback(childrenRetrievedCallback);

                    Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, "New folder"))
                    .build();
                    Drive.DriveApi.query(getGoogleApiClient(), query).setResultCallback(metadataCallback);
                }
            };

    final private ResultCallback<DriveApi.MetadataBufferResult> metadataCallback = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Problem while retrieving results");
                        return;
                    }
                    MetadataBuffer metadataBuffer = result.getMetadataBuffer();
                    for (Metadata metadata : metadataBuffer) {
                        showMessage("file found::" + metadata.getTitle());
                        DriveId driveId = metadata.getDriveId();
                        if (driveId != null) {
                            Drive.DriveApi.fetchDriveId(getGoogleApiClient(), mFileId)
                                    .setResultCallback(folderIdCallback);
                        }
                    }
                }
            };

    final private ResultCallback<DriveApi.DriveIdResult> idCallback = new ResultCallback<DriveApi.DriveIdResult>() {
        @Override
        public void onResult(DriveApi.DriveIdResult result) {
            new RetrieveDriveFileContentsAsyncTask(
                    EditorActivity.this).execute(result.getDriveId());
        }
    };

    final private ResultCallback<DriveApi.DriveIdResult> folderIdCallback = new ResultCallback<DriveApi.DriveIdResult>() {
        @Override
        public void onResult(DriveApi.DriveIdResult result) {
            if(!result.getStatus().isInterrupted()){
                Gdx.app.log(TAG, "failed to get id");
                return;
            }
            DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(), result.getDriveId());
            folder.listChildren(getGoogleApiClient()).setResultCallback(childrenRetrievedCallback);
        }
    };

    final private ResultCallback<DriveApi.MetadataBufferResult> childrenRetrievedCallback = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Problem while retrieving files");
                        return;
                    }
                    for (Metadata metadata : result.getMetadataBuffer()) {
                        showMessage("Folder child " + metadata.getTitle());

                    }
                }
            };

    final private class RetrieveDriveFileContentsAsyncTask
            extends ApiClientAsyncTask<DriveId, Boolean, String> {

        public RetrieveDriveFileContentsAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected String doInBackgroundConnected(DriveId... params) {
            String contents = null;
            DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(), params[0]);
            DriveApi.DriveContentsResult driveContentsResult =
                    file.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                return null;
            }
            DriveContents driveContents = driveContentsResult.getDriveContents();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(driveContents.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                contents = builder.toString();
            } catch (IOException e) {
                Gdx.app.log(TAG, "IOException while reading from the stream", e);
            }

            driveContents.discard(getGoogleApiClient());
            return contents;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                showMessage("Error while reading from the file");
                return;
            }
            showMessage("File contents: " + result);
        }
    }

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create new file contents");
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();

                    // Perform I/O off the UI thread.
                    new Thread() {
                        @Override
                        public void run() {
                            // write content to DriveContents
                            OutputStream outputStream = driveContents.getOutputStream();
                            Writer writer = new OutputStreamWriter(outputStream);
                            try {
                                writer.write("Hello World!");
                                writer.close();
                            } catch (IOException e) {
                                Gdx.app.log(TAG, e.getMessage());
                            }

                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setTitle("New file")
                                    .setMimeType("text/plain")
                                    .setStarred(true).build();

                            // create a file on root folder
                            Drive.DriveApi.getRootFolder(getGoogleApiClient())
                                    .createFile(getGoogleApiClient(), changeSet, driveContents)
                                    .setResultCallback(fileCallback);
                        }
                    }.start();
                }
            };

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the file");
                        return;
                    }
                    showMessage("Created a file with content: " + result.getDriveFile().getDriveId());
                }
            };

}
