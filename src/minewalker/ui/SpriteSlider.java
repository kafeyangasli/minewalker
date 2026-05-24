package minewalker.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JSlider;

class SpriteSlider extends JSlider {
   private static final long serialVersionUID = 1L;
   private static final int HORIZONTAL_PADDING = 0 ;
   private static final int TRACK_HEIGHT = 31;
   private static final int LABEL_AREA_HEIGHT = 24;
   private final TextureManager textures = TextureManager.get();

   SpriteSlider(int min, int max, int value) {
      super(min, max, value);

      setOpaque(false);
      setFocusable(false);
      setPaintTicks(false);
      setPaintLabels(false);
      setBorder(null);
      setFont(ScreenStyles.pixelFont(Font.PLAIN, 12));
      setPreferredSize(new Dimension(240, TRACK_HEIGHT + LABEL_AREA_HEIGHT));

      MouseAdapter mouseHandler = new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            updateValueFromMouse(e.getX());
         }

         @Override 
         public void mouseDragged(MouseEvent e) {
            updateValueFromMouse(e.getX());
         }
      };

      addMouseListener(mouseHandler);
      addMouseMotionListener(mouseHandler);
   }

   private void updateValueFromMouse(int mouseX) {
      int trackStart = getTrackStart();
      int trackWidth = getTrackWidth();

      double ratio = (mouseX - trackStart) / (double) trackWidth;
      ratio = Math.max(0, Math.min(1, ratio));

      int value = getMinimum() + (int) Math.round(ratio * (getMaximum() - getMinimum()));
      setValue(value);
      repaint();
   }

   private int getTrackStart() {
      return HORIZONTAL_PADDING + getPointerWidth() / 2;
   }

   private int getTrackWidth() {
      return Math.max(1, getWidth() - HORIZONTAL_PADDING * 2 - getPointerWidth());
   }

   private int getTrackHeight() {
      int labelSpace = getPaintLabels() || getPaintTicks() ? LABEL_AREA_HEIGHT : 0;
      int availableHeight = Math.max(15, getHeight() - labelSpace);
      return Math.min(TRACK_HEIGHT, availableHeight);
   }

   private int getTrackY() {
      int labelSpace = getPaintLabels() || getPaintTicks() ? LABEL_AREA_HEIGHT : 0;
      int drawableHeight = Math.max(15, getHeight() - labelSpace);
      return Math.max(0, (drawableHeight - getTrackHeight()) / 2);
   }

   private int getThumbCenterX() {
      double ratio = (getValue() - getMinimum()) / (double) (getMaximum() - getMinimum());
      return getTrackStart() + (int) Math.round(ratio * getTrackWidth());
   }

   private int getPointerWidth() {
      Image pointer = textures.sliderPointer();
      return pointer == null ? 28 : pointer.getWidth(null);
   }

   private int getPointerHeight() {
      Image pointer = textures.sliderPointer();
      return pointer == null ? 36 : pointer.getHeight(null);
   }

   @Override
   protected void paintComponent(Graphics graphics) {
      Graphics2D g = (Graphics2D) graphics.create();

      g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
      );
      g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
      );

      paintSpriteTrack(g);
      paintSpritePointer(g);
      paintTicksAndLabels(g);

      g.dispose();
   }

   private void paintSpriteTrack(Graphics2D g) {
      Image left = textures.sliderLeft();
      Image centerLeft = textures.sliderCenterLeft();
      Image centerRight = textures.sliderCenterRight();
      Image right = textures.sliderRight();

      int trackX = getTrackStart();
      int trackY = getTrackY();
      int trackW = getTrackWidth();
      int trackH = getTrackHeight();

      int unit = Math.max(15, trackH);

      Shape oldClip = g.getClip();
      g.setClip(new RoundRectangle2D.Float(trackX, trackY, trackW, trackH, 14, 14));

      if (left != null && centerLeft != null && centerRight != null && right != null) {
         g.drawImage(left, trackX, trackY, unit, trackH, null);

         int x = trackX + unit;
         boolean useLeftCenter = true;

         while (x < trackX + trackW - unit) {
            Image center = useLeftCenter ? centerLeft : centerRight;

            int width = Math.min(unit, trackX + trackW - unit - x);
            g.drawImage(center, x, trackY, width, trackH, null);

            x += unit;
            useLeftCenter = !useLeftCenter;
         }

         g.drawImage(right, trackX + trackW - unit, trackY, unit, trackH, null);
      } else {
         g.setColor(ScreenStyles.PANEL);
         g.fillRoundRect(trackX, trackY, trackW, trackH, 14, 14);
      }

      g.setClip(oldClip);
   }

   private void paintSpritePointer(Graphics2D g) {
      Image pointer = textures.sliderPointer();

      int pointerW = getPointerWidth();
      int pointerH = getPointerHeight();

      int x = getThumbCenterX() - pointerW / 2;
      int y = (getHeight() - pointerH) / 2;

      if (pointer != null) {
         g.drawImage(pointer, x, y, pointerW, pointerH, null);
      } else {
         g.setColor(ScreenStyles.ACCENT);
         g.fillOval(x, y, pointerW, pointerH);
      }
   }

   private void paintTicksAndLabels(Graphics2D g) {
      if (!getPaintTicks() && !getPaintLabels()) {
         return;
      }

      g.setFont(getFont().deriveFont(Font.BOLD, 11f));
      g.setColor(getForeground());
      FontMetrics metrics = g.getFontMetrics();
      int tickTop = getTrackY() + getTrackHeight() + 2;
      int labelBaseline = tickTop + 6 + metrics.getAscent();
      int spacing = getMajorTickSpacing() > 0 ? getMajorTickSpacing() : getMaximum() - getMinimum();

      for (int value = getMinimum(); value <= getMaximum(); value += spacing) {
         drawTickAndLabel(g, metrics, value, tickTop, labelBaseline);
      }
      if ((getMaximum() - getMinimum()) % spacing != 0) {
         drawTickAndLabel(g, metrics, getMaximum(), tickTop, labelBaseline);
      }
   }

   private void drawTickAndLabel(Graphics2D g, FontMetrics metrics, int value, int tickTop, int labelBaseline) {
      int x = xForValue(value);
      if (getPaintTicks()) {
         g.drawLine(x, tickTop, x, tickTop + 5);
      }
      if (getPaintLabels()) {
         String text = Integer.toString(value);
         int textWidth = metrics.stringWidth(text);
         g.drawString(text, x - textWidth / 2, labelBaseline);
      }
   }

   private int xForValue(int value) {
      double ratio = (value - getMinimum()) / (double) (getMaximum() - getMinimum());
      return getTrackStart() + (int) Math.round(ratio * getTrackWidth());
   }
}
