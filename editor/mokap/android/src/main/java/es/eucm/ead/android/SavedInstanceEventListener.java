package es.eucm.ead.android;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import es.eucm.ead.editor.platform.ApplicationArguments;

/**
 * Invoked when we've got killed after a startActivityForResult() call.
 */
public class SavedInstanceEventListener implements AndroidEventListener {

    private AndroidPlatform platform;
    private String pathColumn;

    public SavedInstanceEventListener(AndroidPlatform platform) {
        this.platform = platform;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean resultOK = resultCode == Activity.RESULT_OK;
        platform.setApplicationArgument(ApplicationArguments.RESULT_OK, resultOK);
        String stringFromIntent = resultOK ? platform.getStringFromIntent((Activity)Gdx.app, data, pathColumn) : null;
        Log.d("Mokap test", "AndroidEventListener - On activity result, adding element from path argument: " + stringFromIntent);
        platform.setApplicationArgument(ApplicationArguments.ADD_SCENE_ELEMENT_PATH, stringFromIntent);
    }

    public void setPathColumn(String pathColumn) {
        this.pathColumn = pathColumn;
    }
}
