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

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import es.eucm.ead.editor.MokapApplicationListener;
import es.eucm.ead.editor.control.Selection;
import es.eucm.ead.editor.model.Model;
import es.eucm.ead.editor.platform.ApplicationArguments;
import es.eucm.mokap.R;

public class EditorActivity extends AndroidApplication {

	private final String SAVED_INSTANCE_STATE_CONSUMED_INTENT = "SAVED_INSTANCE_STATE_CONSUMED_INTENT";
	private final String SAVED_INSTANCE_STATE_FROM_ACTIVITY = "SAVED_INSTANCE_STATE_FROM_ACTIVITY";
	private final String SAVED_INSTANCE_STATE_EDIT_SCENE = "SAVED_INSTANCE_STATE_EDIT_SCENE";
    private final String SAVED_INSTANCE_STATE_PATH_COLUMN = "SAVED_INSTANCE_STATE_PATH_COLUMN";
    private final String SAVED_INSTANCE_STATE_PICTURE_PATH = "SAVED_INSTANCE_STATE_PICTURE_PATH";

	private Map<Integer, ActivityResultListener> listeners;
    /**
     * True if the intent that has the path to the project that has to be imported has been consumed or not.
     */
	private boolean consumedIntent = false;
    /**
     * True if we're invoking onCreate after coming from an activity we've launched via #startActivityForResult().
     */
    private boolean fromActivity = false;
    /**
     * If we have to retrieve a string from a result via a {@link es.eucm.ead.android.SavedInstanceEventListener} we need to know what column we should look for (images or audio).
     */
    private String pathColumn;
    /**
     * If we've taken a picture and our app has been killed this is the path where the picture has been taken.
     */
    private String picturePath;
	private AndroidPlatform platform;
	private MokapApplicationListener mokapApplicationListener;
    private SavedInstanceEventListener eventListener;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(tag, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putBoolean(SAVED_INSTANCE_STATE_CONSUMED_INTENT,
				consumedIntent);
		outState.putBoolean(SAVED_INSTANCE_STATE_FROM_ACTIVITY, fromActivity);
        outState.putString(SAVED_INSTANCE_STATE_PATH_COLUMN, pathColumn);
        outState.putString(SAVED_INSTANCE_STATE_PICTURE_PATH, picturePath);
		Model model = mokapApplicationListener.getController().getModel();
		Object sceneSelection = model.getSelection().getSingle(Selection.SCENE);
		if (sceneSelection != null) {
			String editSceneId = model.getIdFor(sceneSelection);
			if (editSceneId != null) {
				outState.putString(SAVED_INSTANCE_STATE_EDIT_SCENE, editSceneId);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(tag, "onCreate");
		super.onCreate(savedInstanceState);

		GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
		Tracker tracker = analytics.newTracker(R.xml.tracker);
		platform = new AndroidPlatform(getContext(), tracker);
		if (savedInstanceState != null) {
			consumedIntent = savedInstanceState
					.getBoolean(SAVED_INSTANCE_STATE_CONSUMED_INTENT);
			fromActivity = savedInstanceState
					.getBoolean(SAVED_INSTANCE_STATE_FROM_ACTIVITY);

            if(fromActivity) {
                fromActivity = false;
                pathColumn = savedInstanceState.getString(SAVED_INSTANCE_STATE_PATH_COLUMN);
                eventListener = new SavedInstanceEventListener(platform);
                eventListener.setPathColumn(pathColumn);
                addAndroidEventListener(eventListener);
                if(pathColumn == null) {
                    picturePath = savedInstanceState.getString(SAVED_INSTANCE_STATE_PICTURE_PATH);
                    platform.setApplicationArgument(ApplicationArguments.ADD_PICTURE_PATH, picturePath);
                }
            }

			platform.setApplicationArgument(ApplicationArguments.EDIT_SCENE,
					savedInstanceState
							.getString(SAVED_INSTANCE_STATE_EDIT_SCENE));
		}

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useImmersiveMode = false;
		config.hideStatusBar = true;
		config.useWakelock = true;
		config.useCompass = false;

		this.listeners = new HashMap<Integer, ActivityResultListener>();
		handleIntent();
		mokapApplicationListener = new MokapApplicationListener(platform);
		initialize(mokapApplicationListener, config);
		analytics.reportActivityStart(this);
	}

	private void handleIntent() {
		if (consumedIntent) {
			return;
		}
		Intent intent = getIntent();
		if (intent != null) {
			boolean launchedFromHistory = (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
			if (!launchedFromHistory) {
				String action = intent.getAction();
				if (Intent.ACTION_VIEW.equals(action)) {
					Uri data = intent.getData();
					if (data != null) {
						String path = data.getPath();
						platform.setApplicationArgument(
								ApplicationArguments.IMPORT_PROJECT_PATH, path);
						consumedIntent = true;
					}
				} else if (Intent.ACTION_MAIN.equals(action)) {
					consumedIntent = true;
				}
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(tag, "onNewIntent");
		super.onNewIntent(intent);
		setIntent(intent);
		consumedIntent = false;
	}

	public void startActivityForResult(Intent intent, int requestCode, String pathColumn, String picturePath,
			ActivityResultListener listener) {
		Log.d(tag, "startActivityForResult, request code: " + requestCode);
		fromActivity = true;
        this.pathColumn = pathColumn;
        this.picturePath = picturePath;
		this.listeners.put(requestCode, listener);
		super.startActivityForResult(intent, requestCode);
	}

	String tag = "Mokap test";

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(tag, "On activity result, request code: " + requestCode
                + ", result code: " + resultCode + ", data: " + data);
        super.onActivityResult(requestCode, resultCode, data);
		ActivityResultListener listener = this.listeners.get(requestCode);
		if (listener != null) {
			Log.d(tag, "eventListener is NOT null");
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
	protected void onResume() {
		Log.d(tag, "onResume");
        if(eventListener != null){
            removeAndroidEventListener(eventListener);
        }
		handleIntent();
		super.onResume();
		// This is necessary because we are using non-continuous rendering and
		// sometimes the screen stops rendering after onResume(). Probably a
		// libGDX bug.
		Gdx.graphics.requestRendering();
	}
}
