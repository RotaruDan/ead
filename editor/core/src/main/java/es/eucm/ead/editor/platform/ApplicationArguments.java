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
package es.eucm.ead.editor.platform;

public class ApplicationArguments {

	/**
     * Value type: (String) <p>
	 * Absolute path to a file with
	 * {@link es.eucm.ead.editor.utils.ProjectUtils#ZIP_EXTENSION} that should
	 * be imported to the {@link Platform#getDefaultProjectsFolder()}, or null
	 * if the application was initiated normally.
	 */
	public static final String IMPORT_PROJECT_PATH = "import_project_path";

	/**
	 * Value type: (Boolean) <p> A Boolean indicating if the debug should be activated or not in desktop.
	 */
	public static final String DEBUG_FLAG = "debug_flag";

	/**
	 * Value type: (String) <p> The edit scene identifier right before leaving the application.
	 */
	public static final String EDIT_SCENE = "edit_scene";

    /**
     * Value type: (String) <p> If this value isn't null when the {@link es.eucm.ead.editor.platform.ApplicationArguments#EDIT_SCENE} is set,
     * then a scene element from this path will be added to the scene.
     */
    public static final String ADD_SCENE_ELEMENT_PATH = "add_scene_element_path";

    /**
     * Value type: (String) <p> If this value isn't null when the {@link es.eucm.ead.editor.platform.ApplicationArguments#EDIT_SCENE} is set,
     * then if {@link es.eucm.ead.editor.platform.ApplicationArguments#RESULT_OK} is true a scene element from will be added to the scene
     * otherwise the file pointed by this path will be deleted.
     */
    public static final String ADD_PICTURE_PATH = "add_picture_path";

    /**
     * Value type: (Boolean) <p> A Boolean indicating if the action from onActivityResult was successful.
     */
    public static final String RESULT_OK = "result_ok";

}
