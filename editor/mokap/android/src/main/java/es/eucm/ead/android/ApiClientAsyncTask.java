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
import android.os.AsyncTask;
import android.os.Bundle;
import com.badlogic.gdx.Gdx;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.util.concurrent.CountDownLatch;

/**
 * An AsyncTask that maintains a connected client.
 */
public abstract class ApiClientAsyncTask<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> {

	private GoogleApiClient mClient;

	public ApiClientAsyncTask(Context context) {
		GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
				.addApi(Drive.API).addScope(Drive.SCOPE_FILE);
		mClient = builder.build();
	}

	@Override
	protected final Result doInBackground(Params... params) {
		Gdx.app.log("TAG", "in background");
		final CountDownLatch latch = new CountDownLatch(1);
		mClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
			@Override
			public void onConnectionSuspended(int cause) {
			}

			@Override
			public void onConnected(Bundle arg0) {
				latch.countDown();
			}
		});
		mClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
			@Override
			public void onConnectionFailed(ConnectionResult arg0) {
				latch.countDown();
			}
		});
		mClient.connect();
		try {
			latch.await();
		} catch (InterruptedException e) {
			return null;
		}
		if (!mClient.isConnected()) {
			return null;
		}
		try {
			return doInBackgroundConnected(params);
		} finally {
			mClient.disconnect();
		}
	}

	/**
	 * Override this method to perform a computation on a background thread,
	 * while the client is connected.
	 */
	protected abstract Result doInBackgroundConnected(Params... params);

	/**
	 * Gets the GoogleApliClient owned by this async task.
	 */
	protected GoogleApiClient getGoogleApiClient() {
		return mClient;
	}
}
