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
package es.eucm.ead.editor.view.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Performance extends Label {
	/**
	 * 1000 ms ~> 1 second
	 */
	private final static long HIT_TIME = 1000;

	private long maxMemory, startTime;

	public Performance(Skin skin) {
		super("", skin);
		maxMemory = Runtime.getRuntime().maxMemory() / 1048576;
		startTime = System.currentTimeMillis();
	}

	@Override
	public float getPrefHeight() {
		return getStyle().font.getLineHeight();
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		final long currentTime = System.currentTimeMillis();
		if (currentTime - startTime > HIT_TIME) {
			setText("FPS: " + Gdx.graphics.getFramesPerSecond() + "/ Mem: "
					+ Gdx.app.getJavaHeap() / 1048576 + " of " + maxMemory
					+ " MB");
			startTime = currentTime;
		}
	}

}
