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
package es.eucm.ead.editor.view.widgets.editionview.composition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import es.eucm.ead.editor.view.widgets.AbstractWidget;

public class SlideColorPicker extends AbstractWidget {

	private static final float HEIGHT_CM = 1.5f;
	private static final float WIDTH_CM = 3f;

	private static final InputListener listener = new InputListener() {

		public boolean touchDown(InputEvent event, float x, float y,
				int pointer, int button) {
			return true;
		}

		public void touchDragged(InputEvent event, float x, float y, int pointer) {
			Slider slider = (Slider) event.getListenerActor();
			SlideColorPicker picker = (SlideColorPicker) slider.getUserObject();
			picker.updateColor();
			picker.updateAllExcept(slider);
			picker.colorChanged(picker.color);
		}

		public void touchUp(InputEvent event, float x, float y, int pointer,
				int button) {
			touchDragged(event, 0f, 0f, 0);
		}
	};

	private final float[] tempValues = new float[3];
	private final Color color = new Color();

	private Pixmap huePixmap;
	private Pixmap saturationPixmap;
	private Pixmap brightnessPixmap;

	private Slider hueSlider;
	private Slider saturationSlider;
	private Slider brightnessSlider;

	private Texture hueTexture;
	private Texture saturationTexture;
	private Texture brightnessTexture;

	/**
	 * Create a {@link SlideColorPicker} with default style.
	 * 
	 * @param skin
	 *            the skin to use
	 */
	public SlideColorPicker(Skin skin) {
		this(skin, "colorPicker-horizontal");
	}

	/**
	 * Create a {@link SlideColorPicker} with defined style.
	 * 
	 * @param skin
	 *            the skin to use
	 * @param styleName
	 *            the style to use
	 */
	public SlideColorPicker(Skin skin, String styleName) {
		super();
		SliderStyle sliderStyle = skin.get(styleName, SliderStyle.class);
//		hueSlider = new Slider(MIN_COLOR, MAX_COLOR, 1, false, new SliderStyle(
//				sliderStyle)) {
//			@Override
//			public float getPrefHeight() {
//				return SlideColorPicker.this.getPrefHeight();
//			}
//
//			@Override
//			public float getPrefWidth() {
//				return SlideColorPicker.this.getPrefWidth();
//			}
//		};
		hueSlider.setUserObject(this);
		hueSlider.addListener(listener);

//		saturationSlider = new Slider(MIN_COLOR, MAX_COLOR, 1, false,
//				new SliderStyle(sliderStyle)) {
//			@Override
//			public float getPrefHeight() {
//				return SlideColorPicker.this.getPrefHeight();
//			}
//
//			@Override
//			public float getPrefWidth() {
//				return SlideColorPicker.this.getPrefWidth();
//			}
//		};
		saturationSlider.setUserObject(this);
		saturationSlider.addListener(listener);

//		brightnessSlider = new Slider(MIN_COLOR, MAX_COLOR, 1, false,
//				new SliderStyle(sliderStyle)) {
//			@Override
//			public float getPrefHeight() {
//				return SlideColorPicker.this.getPrefHeight();
//			}
//
//			@Override
//			public float getPrefWidth() {
//				return SlideColorPicker.this.getPrefWidth();
//			}
//		};
		brightnessSlider.setUserObject(this);
		brightnessSlider.addListener(listener);

		addActor(hueSlider);
		addActor(saturationSlider);
		addActor(brightnessSlider);
		initialize();
	}

	/**
	 * Generate the slider background.
	 */
	private void initialize() {
		int width = MathUtils.round(getPrefWidth()), height = MathUtils
				.round(getPrefHeight());

		huePixmap = new Pixmap(width, height, Format.RGBA8888);
		saturationPixmap = new Pixmap(width, height, Format.RGBA8888);
		brightnessPixmap = new Pixmap(width, height, Format.RGBA8888);

		hueTexture = new Texture(huePixmap);
		saturationTexture = new Texture(saturationPixmap);
		brightnessTexture = new Texture(brightnessPixmap);

		hueSlider.getStyle().background = new TextureRegionDrawable(
				new TextureRegion(hueTexture));
		saturationSlider.getStyle().background = new TextureRegionDrawable(
				new TextureRegion(saturationTexture = new Texture(
						saturationPixmap)));
		brightnessSlider.getStyle().background = new TextureRegionDrawable(
				new TextureRegion(brightnessTexture));

		invalidateHierarchy();
		updateAllExcept(null);
		colorChanged(color);
	}

	private void updateAllExcept(Slider slider) {

		int width = brightnessPixmap.getWidth();

		float[] hsb = RGBtoHSB(MathUtils.round(color.r * 255f),
				MathUtils.round(color.g * 255f),
				MathUtils.round(color.b * 255f), tempValues);
		float h = hsb[0];
		float s = hsb[1];
		float b = hsb[2];

		for (int i = 0; i < width; i++) {
			float percentageCompletion = i / (float) width;

			if (slider != hueSlider) {
				float[] rgb = HSBtoRGB(percentageCompletion, s, b, tempValues);

				huePixmap.setColor(rgb[0], rgb[1], rgb[2], 1f);
				huePixmap.drawLine(i, 0, i, huePixmap.getHeight());
			}

			if (slider != saturationSlider) {
				float[] rgb = HSBtoRGB(h, percentageCompletion, b, tempValues);

				saturationPixmap.setColor(rgb[0], rgb[1], rgb[2], 1f);
				saturationPixmap.drawLine(i, 0, i, huePixmap.getHeight());
			}

			if (slider != brightnessSlider) {
				float[] rgb = HSBtoRGB(h, s, percentageCompletion, tempValues);
				brightnessPixmap.setColor(rgb[0], rgb[1], rgb[2], 1f);
				brightnessPixmap
						.drawLine(i, 0, i, brightnessPixmap.getHeight());
			}

		}

		if (slider != hueSlider) {
			hueTexture.draw(huePixmap, 0, 0);
		}
		if (slider != saturationSlider) {
			saturationTexture.draw(saturationPixmap, 0, 0);
		}
		if (slider != brightnessSlider) {
			brightnessTexture.draw(brightnessPixmap, 0, 0);
		}
	}

	private void updateColor() {
		float[] rgb = HSBtoRGB(
				(hueSlider.getValue() / hueSlider.getMaxValue()),
				(saturationSlider.getValue() / saturationSlider.getMaxValue()),
				(brightnessSlider.getValue() / brightnessSlider.getMaxValue()),
				tempValues);
		color.set(rgb[0], rgb[1], rgb[2], 1f);
	}

	public void updatePosition(Color color) {
		// if (this.color.r == color.r && this.color.g == color.g
		// && this.color.b == color.b) {
		// return;
		// }
		// this.color.set(color);
		// int brightnessWidth = brightnessPixmap.getWidth();
		// float[] hsl = RGBtoHSL(color, tempValues);
		// brightnessSlider
		// .setValue(-(hsl[2] * brightnessWidth) + brightnessWidth);
		//
		// HSLtoRGB(hsl[0], hsl[1], .5f, tempValues);
		//
		// int threshold = 10000000;
		// int rgba8888 = Color.rgba8888(tempValues[0], tempValues[1],
		// tempValues[2], 1f);
		// int minColor = rgba8888 - threshold;
		// int maxColor = rgba8888 + threshold;
		// int roundI = -1;
		// int difference = Integer.MAX_VALUE;
		// int pixmapWidth = colorPixmap.getWidth();
		// for (int i = 0; i < pixmapWidth; ++i) {
		// int pixel = colorPixmap.getPixel(i, 0);
		// if (pixel == rgba8888) {
		// colorSlider.setValue((i / (float) pixmapWidth) * MAX_COLOR);
		// roundI = -1;
		// break;
		// } else if (pixel > minColor && pixel < maxColor) {
		// int currentDifference = Math.abs(pixel - rgba8888);
		// if (currentDifference < difference) {
		// difference = currentDifference;
		// roundI = i;
		// }
		// }
		// }
		//
		// if (roundI != -1) {
		// colorSlider.setValue((roundI / (float) pixmapWidth) * MAX_COLOR);
		// }
		//
		// updateBrightness();
		//
		// colorChanged(this.color);
	}

	protected void colorChanged(Color newColor) {

	}

	public void updateTexture() {
		if (hueTexture != null) {
			hueTexture.draw(huePixmap, 0, 0);
		}
		if (brightnessTexture != null) {
			brightnessTexture.draw(brightnessPixmap, 0, 0);
		}
	}

//	@Override
//	public float getPrefHeight() {
//		return getp;
//	}

//	@Override
//	public float getPrefWidth() {
//		return PREF_WIDTH * Gdx.graphics.getWidth();
//	}

	public Color getPickedColor() {
		return color;
	}

	/**
	 * Converts the components of a color, as specified by the HSB model, to an
	 * equivalent set of values for the default RGB model.
	 * <p>
	 * The <code>saturation</code> and <code>brightness</code> components should
	 * be floating-point values between zero and one (numbers in the range
	 * 0.0-1.0). The <code>hue</code> component can be any floating-point
	 * number. The floor of this number is subtracted from it to create a
	 * fraction between 0 and 1. This fractional number is then multiplied by
	 * 360 to produce the hue angle in the HSB color model.
	 * <p>
	 * The integer that is returned by <code>HSBtoRGB</code> encodes the value
	 * of a color in bits 0-23 of an integer value that is the same format used
	 * by the method {@link #getRGB() <code>getRGB</code>}. This integer can be
	 * supplied as an argument to the <code>Color</code> constructor that takes
	 * a single integer argument.
	 * 
	 * @param hue
	 *            the hue component of the color
	 * @param saturation
	 *            the saturation of the color
	 * @param brightness
	 *            the brightness of the color
	 * @return the RGB value of the color with the indicated hue, saturation,
	 *         and brightness.
	 * @see java.awt.Color#getRGB()
	 * @see java.awt.Color#Color(int)
	 * @see java.awt.image.ColorModel#getRGBdefault()
	 * @since JDK1.0
	 */
	public static float[] HSBtoRGB(float hue, float saturation,
			float brightness, float[] values) {
		int r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0f + 0.5f);
		} else {
			float h = (hue - (float) Math.floor(hue)) * 6.0f;
			float f = h - (float) java.lang.Math.floor(h);
			float p = brightness * (1.0f - saturation);
			float q = brightness * (1.0f - saturation * f);
			float t = brightness * (1.0f - (saturation * (1.0f - f)));
			switch ((int) h) {
			case 0:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (t * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 1:
				r = (int) (q * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 2:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (t * 255.0f + 0.5f);
				break;
			case 3:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (q * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 4:
				r = (int) (t * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 5:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (q * 255.0f + 0.5f);
				break;
			}
		}
		values[0] = r / 255f;
		values[1] = g / 255f;
		values[2] = b / 255f;
		return values;
	}

	/**
	 * Converts the components of a color, as specified by the default RGB
	 * model, to an equivalent set of values for hue, saturation, and brightness
	 * that are the three components of the HSB model.
	 * <p>
	 * If the <code>hsbvals</code> argument is <code>null</code>, then a new
	 * array is allocated to return the result. Otherwise, the method returns
	 * the array <code>hsbvals</code>, with the values put into that array.
	 * 
	 * @param r
	 *            the red component of the color
	 * @param g
	 *            the green component of the color
	 * @param b
	 *            the blue component of the color
	 * @param hsbvals
	 *            the array used to return the three HSB values, or
	 *            <code>null</code>
	 * @return an array of three elements containing the hue, saturation, and
	 *         brightness (in that order), of the color with the indicated red,
	 *         green, and blue components.
	 * @see java.awt.Color#getRGB()
	 * @see java.awt.Color#Color(int)
	 * @see java.awt.image.ColorModel#getRGBdefault()
	 * @since JDK1.0
	 */
	public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
		float hue, saturation, brightness;
		if (hsbvals == null) {
			hsbvals = new float[3];
		}
		int cmax = (r > g) ? r : g;
		if (b > cmax)
			cmax = b;
		int cmin = (r < g) ? r : g;
		if (b < cmin)
			cmin = b;

		brightness = ((float) cmax) / 255.0f;
		if (cmax != 0)
			saturation = ((float) (cmax - cmin)) / ((float) cmax);
		else
			saturation = 0;
		if (saturation == 0)
			hue = 0;
		else {
			float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
			float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
			float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
			if (r == cmax)
				hue = bluec - greenc;
			else if (g == cmax)
				hue = 2.0f + redc - bluec;
			else
				hue = 4.0f + greenc - redc;
			hue = hue / 6.0f;
			if (hue < 0)
				hue = hue + 1.0f;
		}
		hsbvals[0] = hue;
		hsbvals[1] = saturation;
		hsbvals[2] = brightness;
		return hsbvals;
	}
}
