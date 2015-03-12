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
package es.eucm.ead.editor.control.actions.editor.workers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import es.eucm.ead.editor.control.actions.EditorAction;
import es.eucm.ead.editor.control.actions.editor.ExecuteWorker;
import es.eucm.ead.editor.control.actions.model.SetSelection;
import es.eucm.ead.editor.control.actions.model.scene.SetEditedScene;
import es.eucm.ead.editor.control.commands.ResourceCommand.AddResourceCommand;
import es.eucm.ead.editor.control.workers.Worker;
import es.eucm.ead.editor.control.workers.Worker.WorkerListener;
import es.eucm.ead.editor.platform.ApplicationArguments;
import es.eucm.ead.schema.entities.ModelEntity;
import es.eucm.ead.schemax.ModelStructure;
import es.eucm.ead.schemax.entities.ResourceCategory;

/**
 * Loads the project scenes with a worker, and adds it to the model
 */
public class LoadScenes extends EditorAction implements WorkerListener {

	@Override
	public void perform(Object... args) {
		controller.action(ExecuteWorker.class, LoadScenesWorker.class, this);
	}

	@Override
	public void start() {

	}

	@Override
	public void result(Object... results) {
		String id = (String) results[0];
		ModelEntity scene = (ModelEntity) results[1];
		AddResourceCommand addScene = new AddResourceCommand(
				controller.getModel(), id, scene, ResourceCategory.SCENE);
		addScene.setCreateResourceModified(false);
		controller.getCommands().doCommand(addScene);
		Boolean select = (Boolean) results[2];
		if (select) {
			controller.action(SetEditedScene.class, id, scene);
		}
	}

	@Override
	public void done() {

	}

	@Override
	public void error(Throwable ex) {

	}

	@Override
	public void cancelled() {

	}

	public static class LoadScenesWorker extends Worker {

		private Array<String> scenes;

		private boolean select = false;

		public LoadScenesWorker() {
			super(true, true);
		}

		@Override
		protected void prepare() {
			scenes = new Array<String>();
			FileHandle scenesFolder = controller.getEditorGameAssets().resolve(
					ModelStructure.SCENES_PATH);
			Object editSceneObj = controller.getPlatform()
					.getApplicationArgument(ApplicationArguments.EDIT_SCENE);
			for (FileHandle child : scenesFolder.list()) {
				if ("json".equals(child.extension())) {
					String path = child.path();
					scenes.add(path.substring(controller.getEditorGameAssets()
							.getLoadingPath().length()));
					if (editSceneObj != null) {
						if (path.endsWith((String) editSceneObj)) {
							String editScene = scenes.pop();
							scenes.insert(0, editScene);
							select = true;
						}
					}
				}
			}
		}

		@Override
		protected boolean step() {
			if (scenes == null || scenes.size == 0) {
				return true;
			}
			String sceneId = scenes.removeIndex(0);
			ModelEntity scene = controller.getEditorGameAssets().fromJsonPath(
					ModelEntity.class, sceneId);
			result(sceneId, scene, select);
			select = false;
			return scenes.size == 0;
		}
	}
}
