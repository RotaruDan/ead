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

import android.graphics.Bitmap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.MetadataChangeSet.Builder;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.model.ParentReference;
import es.eucm.ead.android.EditorActivity;

public final class GooDrive {
	private GooDrive() {
	}

	private static GoogleApiClient mGAC;
	private static com.google.api.services.drive.Drive mGOOSvc;

	/************************************************************************************************
	 * initialize Google Drive Api
	 * 
	 * @param ctx
	 *            activity context
	 * @param email
	 *            GOO account
	 */
	public static void init(EditorActivity ctx, String email) {
		UT.lg("init GDAA ");
		if (ctx != null && email != null) {
			// connect(false);
			mGAC = new GoogleApiClient.Builder(ctx).addApi(Drive.API)
					.addScope(Drive.SCOPE_FILE).setAccountName(email)
					.addConnectionCallbacks(ctx)
					.addOnConnectionFailedListener(ctx).build();

			mGOOSvc = new com.google.api.services.drive.Drive.Builder(
					AndroidHttp.newCompatibleTransport(), new GsonFactory(),
					GoogleAccountCredential.usingOAuth2(ctx,
							Arrays.asList(DriveScopes.DRIVE_FILE))
							.setSelectedAccountName(email)).build();
		}
	}

	/************************************************************************************************
	 * connect / disconnect
	 */
	public static void connect(boolean bOn) {
		if (mGAC != null) {
			if (bOn) {
				if (!mGAC.isConnected()) {
					UT.lg("connect ");
					mGAC.connect();
				}
			} else {
				if (mGAC.isConnected()) {
					UT.lg("disconnect ");
					mGAC.disconnect();
				}
			}
		}
	}

	private static boolean isConnected() {
		return mGAC != null && mGAC.isConnected();
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////
	// (S)CRU(D) implementation of Google Drive Android API (GDAA)
	// ////////////////////////////
	/************************************************************************************************
	 * find file/folder in GOODrive
	 * 
	 * @param prId
	 *            parent ID (optional), null searches full drive (within SCOPE)
	 * @param titl
	 *            file/folder name (optional), null gets all file names
	 * @param mime
	 *            file/folder mime type (optional), null gets all mime types
	 * @return list of file/folder IDs / null on fail
	 */
	static ArrayList<GF> search(DriveId prId, String titl, String mime) {
		ArrayList<GF> gfs = new ArrayList<GF>();

		if (isConnected()) {
			// add query conditions, build query
			ArrayList<Filter> fltrs = new ArrayList<Filter>();
			if (prId != null)
				fltrs.add(Filters.in(SearchableField.PARENTS, prId));
			if (titl != null)
				fltrs.add(Filters.eq(SearchableField.TITLE, titl));
			if (mime != null)
				fltrs.add(Filters.eq(SearchableField.MIME_TYPE, mime));
			Query qry = new Query.Builder().addFilter(Filters.and(fltrs))
					.build();

			// fire the query
			MetadataBufferResult rslt = Drive.DriveApi.query(mGAC, qry).await();
			if (rslt.getStatus().isSuccess()) {
				MetadataBuffer mdb = null;
				try {
					mdb = rslt.getMetadataBuffer();
					for (Metadata md : mdb) {
						if (md == null || !md.isDataValid() || md.isTrashed())
							continue;
						gfs.add(new GF(md.getTitle(), md.getDriveId()
								.encodeToString()));
					}
				} finally {
					if (mdb != null)
						mdb.close();
				}
			}
		}
		return gfs;
	}

	/************************************************************************************************
	 * create file/folder in GOODrive
	 * 
	 * @param prId
	 *            parent's ID, null for root
	 * @param titl
	 *            file name
	 * @param mime
	 *            file mime type
	 * @param buf
	 *            file contents (optional, if null, create folder)
	 * @return file id / null on fail
	 */
	static DriveId create(DriveId prId, String titl, String mime, byte[] buf) {
		if (titl == null || !isConnected())
			return null; // ------------->>>
		DriveId dId = null;
		DriveFolder pFldr = (prId == null) ? Drive.DriveApi.getRootFolder(mGAC)
				: Drive.DriveApi.getFolder(mGAC, prId);
		if (pFldr == null)
			return null; // ----------------->>>

		MetadataChangeSet meta;
		if (buf != null) { // create file
			if (mime != null) { // file must have mime
				DriveContentsResult r1 = Drive.DriveApi.newDriveContents(mGAC)
						.await();
				if (r1 == null || !r1.getStatus().isSuccess())
					return null; // -------->>>

				meta = new MetadataChangeSet.Builder().setTitle(titl)
						.setMimeType(mime).build();
				DriveFileResult r2 = pFldr.createFile(mGAC, meta,
						r1.getDriveContents()).await();
				DriveFile dFil = r2 != null && r2.getStatus().isSuccess() ? r2
						.getDriveFile() : null;
				if (dFil == null)
					return null; // ---------->>>

				r1 = dFil.open(mGAC, DriveFile.MODE_WRITE_ONLY, null).await();
				if ((r1 != null) && (r1.getStatus().isSuccess()))
					try {
						Status stts = bytes2Cont(r1.getDriveContents(), buf)
								.commit(mGAC, meta).await();
						if ((stts != null) && stts.isSuccess()) {
							MetadataResult r3 = dFil.getMetadata(mGAC).await();
							if (r3 != null && r3.getStatus().isSuccess()) {
								dId = r3.getMetadata().getDriveId();
							}
						}
					} catch (Exception e) {
						UT.le(e);
					}
			}

		} else {
			meta = new MetadataChangeSet.Builder().setTitle(titl)
					.setMimeType(UT.MIME_FLDR).build();
			DriveFolderResult r1 = pFldr.createFolder(mGAC, meta).await();
			DriveFolder dFld = (r1 != null) && r1.getStatus().isSuccess() ? r1
					.getDriveFolder() : null;
			if (dFld != null) {
				MetadataResult r2 = dFld.getMetadata(mGAC).await();
				if ((r2 != null) && r2.getStatus().isSuccess()) {
					dId = r2.getMetadata().getDriveId();
				}
			}
		}
		return dId;
	}

	/************************************************************************************************
	 * get file contents
	 * 
	 * @param dId
	 *            file driveId
	 * @return file's content / null on fail
	 */
	static byte[] read(DriveId dId) {
		byte[] buf = null;
		if (isConnected()) {
			DriveFile df = Drive.DriveApi.getFile(mGAC, dId);
			DriveContentsResult rslt = df.open(mGAC, DriveFile.MODE_READ_ONLY,
					null).await();
			if ((rslt != null) && rslt.getStatus().isSuccess()) {
				DriveContents cont = rslt.getDriveContents();
				buf = cont2Bytes(cont); // UT.is2Bytes(cont.getInputStream());
				cont.discard(mGAC); // or cont.commit(); they are equiv if
									// READONLY
			}
		}
		return buf;
	}

	/************************************************************************************************
	 * update file in GOODrive
	 * 
	 * @param dId
	 *            file id
	 * @param titl
	 *            new file name (optional)
	 * @param mime
	 *            new file mime type (optional, null or MIME_FLDR indicates
	 *            folder)
	 * @param buf
	 *            new file contents (optional)
	 * @return success status
	 */
	static boolean update(DriveId dId, String titl, String mime, String desc,
			byte[] buf) {
		if (dId == null || !isConnected())
			return false; // ------------>>>

		Boolean bOK = false;
		Builder mdBd = new MetadataChangeSet.Builder();
		if (titl != null)
			mdBd.setTitle(titl);
		if (mime != null)
			mdBd.setMimeType(mime);
		if (desc != null)
			mdBd.setDescription(desc);
		MetadataChangeSet meta = mdBd.build();

		if (mime == null || UT.MIME_FLDR.equals(mime)) {
			DriveFolder dFldr = Drive.DriveApi.getFolder(mGAC, dId);
			MetadataResult r1 = dFldr.updateMetadata(mGAC, meta).await();
			bOK = (r1 != null) && r1.getStatus().isSuccess();

		} else {
			DriveFile dFile = Drive.DriveApi.getFile(mGAC, dId);
			MetadataResult r1 = dFile.updateMetadata(mGAC, meta).await();
			if ((r1 != null) && r1.getStatus().isSuccess() && buf != null) {
				DriveContentsResult r2 = dFile.open(mGAC,
						DriveFile.MODE_WRITE_ONLY, null).await();
				if (r2.getStatus().isSuccess()) {
					Status r3 = bytes2Cont(r2.getDriveContents(), buf).commit(
							mGAC, meta).await();
					bOK = (r3 != null && r3.isSuccess());
				}
			}
		}
		return bOK;
	}

	/************************************************************************************************
	 * builds a file tree MYROOT/yyyy-mm/yymmdd-hhmmss.jpg
	 * 
	 * @param root
	 *            MYROOT
	 * @param titl
	 *            new file name
	 * @param buf
	 *            new file contents
	 * @return file Id
	 */
	static DriveId createTreeGDAA(String root, String titl, byte[] buf) {
		if (root == null || titl == null)
			return null;
		DriveId dId = findOrCreateFolder((DriveId) null, root);
		if (dId != null) {
			dId = findOrCreateFolder(dId, UT.titl2Month(titl));
			if (dId != null) {
				return create(dId, titl + UT.JPEG_EXT, UT.MIME_JPEG, buf);
			}
		}
		return null;
	}

	/************************************************************************************************
	 * runs a test scanning GooDrive, downloading and updating jpegs
	 * 
	 * @param root
	 *            MYROOT
	 */
	static ArrayList<GF> testTreeGDAA(String root) {
		ArrayList<GF> gfs0 = search((DriveId) null, root, null);
		if (gfs0 != null)
			for (GF gf0 : gfs0) {
				ArrayList<GF> gfs1 = search(DriveId.decodeFromString(gf0.id),
						null, null);
				if (gfs1 != null)
					for (GF gf1 : gfs1) {
						gfs0.add(gf1);
						ArrayList<GF> gfs2 = search(
								DriveId.decodeFromString(gf1.id), null, null);
						if (gfs2 != null)
							for (GF gf2 : gfs2) {
								byte[] buf = read(DriveId
										.decodeFromString(gf2.id));
								if (buf != null) {
									gf2.titl += (", " + (buf.length / 1024) + " kB ");
								} else {
									gf2.titl += (" failed to download ");
								}
								update(DriveId.decodeFromString(gf2.id), null,
										null, "seen " + UT.time2Titl(null),
										null);
								gfs0.add(gf2);
							}
					}
			}
		return gfs0;
	}

	private static DriveId findOrCreateFolder(DriveId prnt, String titl) {
		ArrayList<GF> gfs = search(prnt, titl, UT.MIME_FLDR);
		if (gfs.size() > 0) {
			return DriveId.decodeFromString(gfs.get(0).id);
		}
		return create(prnt, titl, null, null);
	}

	private static DriveContents bytes2Cont(DriveContents driveContents,
			byte[] buf) {
		OutputStream os = driveContents.getOutputStream();
		try {
			os.write(buf);
		} catch (IOException e) {
			UT.le(e);
		} finally {
			try {
				os.close();
			} catch (Exception e) {
				UT.le(e);
			}
		}
		return driveContents;
	}

	private static byte[] cont2Bytes(DriveContents contents) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				contents.getInputStream()));
		StringBuilder builder = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			return builder.toString().getBytes();
		} catch (Exception e) {
			UT.le(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				UT.le(e);
			}
		}
		return null;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////
	// (S)CRU(D) implementation of Google APIs Java Client (REST)
	// /////////////////////////
	/************************************************************************************************
	 * find file/folder in GOODrive
	 * 
	 * @param prId
	 *            parent folder resource id (optional)
	 * @param titl
	 *            file/folder name (optional)
	 * @param mime
	 *            file/folder mime type (optional)
	 * @return arraylist of found objects
	 */
	static ArrayList<GF> search(String prId, String titl, String mime) {
		ArrayList<GF> gfs = new ArrayList<GF>();
		if (isConnected()) {
			String qryClause = "'me' in owners and ";
			if (prId != null)
				qryClause += "'" + prId + "' in parents and ";
			if (titl != null)
				qryClause += "title = '" + titl + "' and ";
			if (mime != null)
				qryClause += "mimeType = '" + mime + "' and ";
			qryClause = qryClause.substring(0,
					qryClause.length() - " and ".length());
			List qry = null;
			try {
				qry = mGOOSvc
						.files()
						.list()
						.setQ(qryClause)
						.setFields(
								"items(id, labels/trashed, title), nextPageToken");
				gfs = search(gfs, qry);
			} catch (GoogleAuthIOException gaiEx) { // WTF ???
				try {
					gfs = search(gfs, qry);
				} catch (Exception g) {
					UT.le(g);
				}
			} catch (Exception e) {
				UT.le(e);
			}
		}
		return gfs;
	}

	/************************************************************************************************
	 * create file/folder in GOODrive
	 * 
	 * @param prId
	 *            parent's ID, null for root
	 * @param titl
	 *            file name
	 * @param mime
	 *            file mime type
	 * @param buf
	 *            file content (optional, if null, create folder)
	 * @return file id / null on fail
	 */
	static String create(String prId, String titl, String mime, byte[] buf) {
		String rsid = null;
		if (titl != null && isConnected()) {

			File meta = new File();
			meta.setParents(Arrays.asList(new ParentReference()
					.setId(prId == null ? "root" : prId)));
			meta.setTitle(titl);

			File gFl = null;
			if (buf != null) { // create file
				if (mime != null) { // file must have mime
					meta.setMimeType(mime);
					java.io.File jvFl;
					try {
						jvFl = UT.bytes2File(buf, java.io.File.createTempFile(
								UT.TMP_FILENM, null, UT.acx.getCacheDir()));
						gFl = mGOOSvc.files()
								.insert(meta, new FileContent(mime, jvFl))
								.execute();
					} catch (Exception e) {
						UT.le(e);
					}
					if (gFl != null && gFl.getId() != null) {
						rsid = gFl.getId();
					}
				}
			} else { // create folder
				meta.setMimeType(UT.MIME_FLDR);
				try {
					gFl = mGOOSvc.files().insert(meta).execute();
				} catch (Exception e) {
					UT.le(e);
				}
				if (gFl != null && gFl.getId() != null) {
					rsid = gFl.getId();
				}
			}
		}
		return rsid;
	}

	/************************************************************************************************
	 * get file contents, specifically image. The thmbSz, if not zero attepmts
	 * to retrieve an image thumbnail that fits into a square envelope of thmbSz
	 * pixels
	 * 
	 * @param rsId
	 *            file id
	 * @param thmbSz
	 *            reduced size envelope (optional value, 0 means full size image
	 *            / file) tested with:
	 *            128,220,320,400,512,640,720,800,1024,1280,1440,1600
	 * @return file's content / null on fail
	 */
	static byte[] read(String rsId, int thmbSz) {
		byte[] buf = null;
		if (isConnected() && rsId != null)
			try {
				File gFl = (thmbSz == 0) ? mGOOSvc.files().get(rsId)
						.setFields("downloadUrl").execute() : mGOOSvc.files()
						.get(rsId).setFields("thumbnailLink").execute();
				if (gFl != null) {
					String strUrl;
					if (thmbSz == 0)
						strUrl = gFl.getDownloadUrl();
					else {
						strUrl = gFl.getThumbnailLink();
						if (!strUrl.endsWith("s220"))
							return null; // --- OOPS ------------>>>
						strUrl = strUrl.substring(0, strUrl.length() - 3)
								+ Integer.toString(thmbSz);
					}
					InputStream is = mGOOSvc.getRequestFactory()
							.buildGetRequest(new GenericUrl(strUrl)).execute()
							.getContent();
					buf = UT.is2Bytes(is);
				}
			} catch (Exception e) {
				UT.le(e);
			}
		return buf;
	}

	/************************************************************************************************
	 * update file in GOODrive
	 * 
	 * @param rsid
	 *            file id
	 * @param titl
	 *            new file name (optional)
	 * @param mime
	 *            new file mime type (optional, null or MIME_FLDR indicates
	 *            folder)
	 * @param buf
	 *            new file content (optional)
	 * @return success status
	 */
	static boolean update(String rsid, String titl, String mime, String desc,
			byte[] buf) {
		Boolean bOK = false;
		java.io.File jvFl = null;
		if (mGOOSvc != null && rsid != null)
			try {
				File body = new File();
				if (titl != null)
					body.setTitle(titl);
				if (mime != null)
					body.setMimeType(mime);
				if (desc != null)
					body.setDescription(desc);
				jvFl = UT.bytes2File(
						buf,
						java.io.File.createTempFile(UT.TMP_FILENM, null,
								UT.acx.getCacheDir()));
				FileContent cont = jvFl != null ? new FileContent(mime, jvFl)
						: null;
				File gFl = (cont == null) ? mGOOSvc.files().patch(rsid, body)
						.execute() : mGOOSvc.files().update(rsid, body, cont)
						.setOcr(false).execute();
				bOK = gFl != null && gFl.getId() != null;
			} catch (Exception e) {
				UT.le(e);
			} finally {
				if (jvFl != null)
					jvFl.delete();
			}
		return bOK;
	}

	/************************************************************************************************
	 * builds a file tree MYROOT/yyyy-mm/yymmdd-hhmmss.jpg
	 * 
	 * @param root
	 *            MYROOT
	 * @param titl
	 *            new file name
	 * @param buf
	 *            new file contents
	 */
	static void createTreeREST(String root, String titl, byte[] buf) {
		if (root != null && titl != null) {
			String rsid = findOrCreateFolder("root", root);
			if (rsid != null) {
				rsid = findOrCreateFolder(rsid, UT.titl2Month(titl));
				if (rsid != null) {
					create(rsid, titl + UT.JPEG_EXT, UT.MIME_JPEG, buf);
				}
			}
		}
	}

	/************************************************************************************************
	 * runs a test scanning GooDrive, downloading and updating jpegs
	 * 
	 * @param root
	 *            MYROOT
	 */
	static ArrayList<GF> testTreeREST(String root) {
		ArrayList<GF> gfs0 = search((String) null, root, null);
		if (gfs0 != null) {
			for (GF gf0 : gfs0) {
				ArrayList<GF> gfs1 = search(gf0.id, null, null);
				if (gfs1 != null) {
					gfs0.addAll(gfs1);
					for (GF gf1 : gfs1) {
						ArrayList<GF> gfs2 = search(gf1.id, null, null);
						if (gfs2 != null) {
							for (GF gf2 : gfs2) {
								byte[] buf = read(gf2.id, 720);
								if (buf != null) {
									Bitmap bm = UT.jpg2Bmp(buf);
									if (bm != null)
										gf2.titl += (", " + (buf.length / 1024)
												+ " kB " + bm.getWidth() + "x" + bm
												.getHeight());
									else
										gf2.titl += (", " + (buf.length / 1024) + " kB ");
								} else {
									gf2.titl += (" failed to download ");
								}
								update(gf2.id, null, null,
										"seen " + UT.time2Titl(null), null);
								gfs0.add(gf2);
							}
						}
					}
				}
			}
		}
		return gfs0;
	}

	private static ArrayList<GF> search(ArrayList<GF> gfs, List qry)
			throws IOException {
		String npTok = null;
		if (qry != null)
			do {
				FileList gLst = qry.execute();
				if (gLst != null) {
					for (File gFl : gLst.getItems()) {
						if (gFl.getLabels().getTrashed())
							continue;
						gfs.add(new GF(gFl.getTitle(), gFl.getId()));
					} // else UT.lg("failed " + gFl.getTitle());
					npTok = gLst.getNextPageToken();
					qry.setPageToken(npTok);
				}
			} while (npTok != null && npTok.length() > 0); // UT.lg("found " +
															// vlss.size());
		return gfs;
	}

	private static String findOrCreateFolder(String prnt, String titl) {
		ArrayList<GF> gfs = search(prnt, titl, UT.MIME_FLDR);
		if (gfs.size() > 0) {
			return gfs.get(0).id;
		}
		return create(prnt, titl, null, null);
	}

	final static class GF {
		String titl, id;

		GF(String t, String i) {
			titl = t;
			id = i;
		}
	}

}

/***
 * //DriveId dId = md.getDriveId();
 * //DriveId.decodeFromString(dId.encodeToString()) //dId.getResourceId()
 * //md.getDescription(), md.getTitle(), //md.getAlternateLink(),
 * md.getEmbedLink(), md.getWebContentLink(), md.getWebViewLink()
 ***/
