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
package es.eucm.ead.editor.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Polygon;
import es.eucm.ead.editor.control.Preferences;
import es.eucm.ead.engine.components.renderers.ImageComponent;

/**
 * Created by angel on 12/05/14.
 */
public class EditorImageComponent extends ImageComponent {

	public static boolean DRAW_DEBUG = true;

	private ShapeRenderer shapeRenderer;

	private Preferences preferences;

	public void setShapeRenderer(ShapeRenderer shapeRenderer) {
		this.shapeRenderer = shapeRenderer;
	}

	public void setPreferences(Preferences preferences) {
		this.preferences = preferences;
	}

	@Override
	public void draw(Batch batch) {
		super.draw(batch);
		if (DRAW_DEBUG && getCollider() != null) {
			batch.end();
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_DST_COLOR);
			Gdx.gl.glBlendEquation(GL20.GL_FUNC_SUBTRACT);
			shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
			shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(Color.WHITE);
			for (Polygon polygon : getCollider()) {
				float[] vertices = polygon.getVertices();
				for (int i = 0, length = vertices.length - 2; i < length; i += 2) {
					float x1 = vertices[i];
					float y1 = vertices[i + 1];
					float x2 = vertices[i + 2];
					float y2 = vertices[i + 3];
					shapeRenderer.line(x1, y1, x2, y2);
				}
				shapeRenderer
						.line(vertices[vertices.length - 2],
								vertices[vertices.length - 1], vertices[0],
								vertices[1]);
			}
			shapeRenderer.end();
			Gdx.gl.glBlendEquation(GL20.GL_FUNC_ADD);
			Gdx.gl.glDisable(GL20.GL_BLEND);
			batch.begin();
		}
	}
}
