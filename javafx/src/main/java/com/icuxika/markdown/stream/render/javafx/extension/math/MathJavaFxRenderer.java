package com.icuxika.markdown.stream.render.javafx.extension.math;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.extension.math.MathNode;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxNodeRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxNodeRendererContext;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Set;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javax.swing.JLabel;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public class MathJavaFxRenderer implements JavaFxNodeRenderer {
	private final JavaFxNodeRendererContext context;

	public MathJavaFxRenderer(JavaFxNodeRendererContext context) {
		this.context = context;
	}

	@Override
	public Set<Class<? extends Node>> getNodeTypes() {
		return Collections.singleton(MathNode.class);
	}

	@Override
	public void render(Node node) {
		MathNode math = (MathNode) node;
		String latex = math.getContent();

		if (latex == null || latex.trim().isEmpty()) {
			return;
		}

		try {
			// 1. Create TeXFormula
			TeXFormula formula = new TeXFormula(latex);

			// 2. High-DPI Rendering Strategy
			// Render at a higher resolution (e.g., 4x scale) to ensure sharpness on
			// High-DPI screens.
			float scaleFactor = 4.0f; // Scale factor for high definition
			float fontSize = 20f; // Base font size

			// Create TeXIcon with scaled font size
			TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, fontSize * scaleFactor);

			// 3. Create BufferedImage with transparency
			// The image dimensions will be naturally larger due to the scaled font size
			BufferedImage bimg = new BufferedImage(
					icon.getIconWidth(),
					icon.getIconHeight(),
					BufferedImage.TYPE_INT_ARGB);

			// 4. Paint Icon to BufferedImage
			Graphics2D g2 = bimg.createGraphics();
			// Enable high-quality rendering hints
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

			// Draw icon
			icon.paintIcon(new JLabel(), g2, 0, 0);
			g2.dispose();

			// 5. Convert to JavaFX Image
			WritableImage fxImage = SwingFXUtils.toFXImage(bimg, null);

			// 6. Draw Image on Canvas
			// When drawing, scale it DOWN by the scaleFactor.
			// This compresses the high-res pixels into the target display area, resulting
			// in crisp rendering.
			double displayWidth = fxImage.getWidth() / scaleFactor;
			double displayHeight = fxImage.getHeight() / scaleFactor;

			Canvas canvas = new Canvas(displayWidth, displayHeight);
			GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.drawImage(fxImage, 0, 0, displayWidth, displayHeight);

			// Add class for potential external styling (margins, etc.)
			canvas.getStyleClass().add("markdown-math-canvas");

			// Wrap in a VBox to behave like a block element if needed, or just add directly
			// MathNode can be inline or block.
			// Ideally we should check if it's inline math or display math.
			// But for now, let's just add the canvas.
			context.getCurrentContainer().getChildren().add(canvas);

		} catch (Exception e) {
			// Fallback to text rendering if LaTeX parsing fails
			Label label = new Label(latex);
			label.getStyleClass().add("markdown-math-error");
			label.setTooltip(new javafx.scene.control.Tooltip(e.getMessage()));
			context.getCurrentContainer().getChildren().add(label);
		}
	}
}
