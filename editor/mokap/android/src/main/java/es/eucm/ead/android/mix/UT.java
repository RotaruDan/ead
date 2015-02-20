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
package es.eucm.ead.android.mix;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final public class UT {
	private UT() {
	}

	private static final String L_TAG = "_";

	static final String MYROOT = "GDAADemoRoot";
	static final String TMP_FILENM = "temp";
	static final String JPEG_EXT = ".jpg";
	static final String MIME_JPEG = "image/jpeg";
	static final String MIME_FLDR = "application/vnd.google-apps.folder";

	static final String TITL_FMT = "yyMMdd-HHmmss";

	static final int BUF_SZ = 2048;

	private static UT mInst;
	static SharedPreferences pfs;
	static Context acx;

	public static UT init(Context ctx) {
		if (mInst == null) {
			acx = ctx.getApplicationContext();
			pfs = PreferenceManager.getDefaultSharedPreferences(acx);
			mInst = new UT(); // lg("img cache " + ccheSz);
		}
		return mInst;
	}

	public final static class AM {
		private AM() {
		}

		private static final String ACC_NAME = "account_name";
		static final int FAIL = -1;
		static final int UNCHANGED = 0;
		public static final int CHANGED = +1;

		private static String mCurrEmail = null; // cache locally

		static Account[] getAllAccnts() {
			return AccountManager.get(UT.acx).getAccountsByType(
					GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		}

		public static Account getPrimaryAccnt(boolean bOneOnly) {
			Account[] accts = getAllAccnts();
			if (bOneOnly)
				return accts == null || accts.length != 1 ? null : accts[0];
			return accts == null || accts.length == 0 ? null : accts[0];
		}

		public static Account getActiveAccnt() {
			return emil2Accnt(getActiveEmil());
		}

		public static String getActiveEmil() {
			if (mCurrEmail != null) {
				return mCurrEmail;
			}
			return UT.pfs.getString(ACC_NAME, null);
		}

		static Account emil2Accnt(String emil) {
			if (emil != null) {
				Account[] accounts = AccountManager.get(UT.acx)
						.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
				for (Account account : accounts) {
					if (emil.equalsIgnoreCase(account.name)) {
						return account;
					}
				}
			}
			return null;
		}

		/**
		 * Stores a new email in persistent app storage, reporting result
		 * 
		 * @param newEmil
		 *            new email, optionally null
		 * @return FAIL, CHANGED or UNCHANGED (based on the following table) OLD
		 *         NEW SAVED RESULT ERROR FAIL null null null FAIL null new new
		 *         CHANGED old null old UNCHANGED old != new new CHANGED old ==
		 *         new new UNCHANGED
		 */
		public static int setEmil(String newEmil) {
			int result = FAIL; // 0 0

			String prevEmil = getActiveEmil();
			if ((prevEmil == null) && (newEmil != null)) {
				result = CHANGED;
			} else if ((prevEmil != null) && (newEmil == null)) {
				result = UNCHANGED;
			} else if (prevEmil != null) { // && newEmil != null) {
				result = prevEmil.equalsIgnoreCase(newEmil) ? UNCHANGED
						: CHANGED;
			}
			if (result == CHANGED) {
				mCurrEmail = newEmil;
				UT.pfs.edit().putString(ACC_NAME, newEmil).apply();
			}
			return result;
		}

		public static void removeActiveAccnt() {
			mCurrEmail = null;
			UT.pfs.edit().remove(ACC_NAME).apply();
		}
	}

	static File bytes2File(byte[] buf, File fl) {
		if (buf == null || fl == null)
			return null;
		BufferedOutputStream bs = null;
		try {
			bs = new BufferedOutputStream(new FileOutputStream(fl));
			bs.write(buf);
		} catch (Exception e) {
			le(e);
		} finally {
			if (bs != null)
				try {
					bs.close();
				} catch (Exception e) {
					le(e);
				}
		}
		return fl;
	}

	static byte[] file2Bytes(File file) {
		if (file != null)
			try {
				return is2Bytes(new FileInputStream(file));
			} catch (Exception e) {
				le(e);
			}
		return null;
	}

	static byte[] is2Bytes(InputStream is) {
		byte[] buf = null;
		BufferedInputStream bufIS = null;
		if (is != null)
			try {
				ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
				bufIS = new BufferedInputStream(is);
				buf = new byte[BUF_SZ];
				int cnt;
				while ((cnt = bufIS.read(buf)) >= 0) {
					byteBuffer.write(buf, 0, cnt);
				}
				buf = byteBuffer.size() > 0 ? byteBuffer.toByteArray() : null;
			} catch (Exception e) {
				le(e);
			} finally {
				try {
					if (bufIS != null)
						bufIS.close();
				} catch (Exception e) {
					le(e);
				}
			}
		return buf;
	}

	static Bitmap jpg2Bmp(byte[] buf) {
		return buf == null ? null : BitmapFactory.decodeByteArray(buf, 0,
				buf.length);
	}

	static String time2Titl(Long milis) { // time -> yymmdd-hhmmss
		Date dt = (milis == null) ? new Date() : (milis >= 0) ? new Date(milis)
				: null;
		return (dt == null) ? null : new SimpleDateFormat(TITL_FMT, Locale.US)
				.format(dt);
	}

	static String titl2Month(String titl) {
		return titl == null ? null : ("20" + titl.substring(0, 2) + "-" + titl
				.substring(2, 4));
	}

	private static String stack2String(Throwable ex) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		try {
			ex.printStackTrace(printWriter);
			return result.toString();
		} finally {
			printWriter.close();
		}
	}

	static void le(Throwable ex) {
		String msg = (ex == null || ex.getMessage() == null) ? "" : ex
				.getMessage() + "\n";
		try {
			Log.e(L_TAG, msg + stack2String(ex));
		} catch (Exception e) {
		}
	}

	public static void lg(String msg) {
		if (msg != null) {
			Log.d(L_TAG, msg);
		}
	}

}
