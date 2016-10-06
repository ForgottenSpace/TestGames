/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ractoc.fs.games.thehuntison.textures;

import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Class for generating a star texture. It generates a single layer of the
 * starfield generated in the StarFieldAppState.
 *
 * @author ractoc
 * @since 0.1
 */
public final class StarField {

    private int width, height, density, size = 1, randomStarColorInterval,
            previousRandomStarColor, nextRandomStarColor,
            randomStarSizeInterval, previousRandomStarSize, nextRandomStarSize,
            randomStarSizeShift;
    private Color starColor;
    private Color[][] stars;
    private static Random rand = new Random(System.currentTimeMillis());
    private float visibility = 1f;

    /**
     * Basic constructor, takes all the mandatory parameters plus the optional
     * parameter starColorParam.
     * <p/>
     * @param widthParam     The width of the texture being generated.
     * @param heightParam    The height of the texture being generated.
     * @param densityParam   The number of stars on the texture being generated.
     * @param starColorParam The color of the stars on the texture being
     *                       generated.
     */
    public StarField(final int widthParam, final int heightParam,
                     final int densityParam,
                     final Color starColorParam) {
        this.width = widthParam;
        this.height = heightParam;
        this.density = densityParam;
        this.starColor = starColorParam;
    }

    /**
     * Basic constructor, takes all the mandatory parameters.
     * <p/>
     * @param widthParam   The width of the texture being generated.
     * @param heightParam  The height of the texture being generated.
     * @param densityParam The number of stars on the texture being generated.
     *                     generated.
     */
    public StarField(final int widthParam, final int heightParam,
                     final int densityParam) {
        this.width = widthParam;
        this.height = heightParam;
        this.density = densityParam;
    }

    /**
     * Get the random star interval. This is the interval with which a star will
     * get a random color. This is an optional setting.
     * <p/>
     * @return The random star interval.
     */
    public int getRandomStarColorInterval() {
        return randomStarColorInterval;
    }

    /**
     * Set the random star interval. This is the interval with which a star will
     * get a random color. This is an optional setting.
     * <p/>
     * @param rndStarInterval The random star interval.
     */
    public void setRandomStarColorInterval(final int rndStarInterval) {
        this.randomStarColorInterval = rndStarInterval;
    }

    /**
     * Get the random star size interval. This is the interval with which a star
     * will get a slightly larger or smaller size then the rest of the stars in
     * the layer. The amount of the size difference is dictated via the
     * randomStarSizeShift variable.
     * <p/>
     * @return The random star size interval.
     */
    public int getRandomStarSizeInterval() {
        return randomStarSizeInterval;
    }

    /**
     * Set the random star size interval. This is the interval with which a star
     * will get a slightly larger or smaller size then the rest of the stars in
     * the layer. The amount of the size difference is dictated via the
     * randomStarSizeShift variable.
     * <p/>
     * @param randomSizeInterval The random star size interval.
     */
    public void setRandomStarSizeInterval(final int randomSizeInterval) {
        this.randomStarSizeInterval = randomSizeInterval;
    }

    /**
     * Get the size shift for the stars with a random size. This shift can be
     * either bigger or smaller and is a shift in pixels. Note that the shift
     * will never make the star dissapear. If the shift set the star to a size
     * of 0 or smaller, it is forced to a size of 1.
     * <p/>
     * @return The random star size shift.
     */
    public int getRandomStarSizeShift() {
        return randomStarSizeShift;
    }

    /**
     * Set the size shift for the stars with a random size. This shift can be
     * either bigger or smaller and is a shift in pixels. Note that the shift
     * will never make the star dissapear. If the shift set the star to a size
     * of 0 or smaller, it is forced to a size of 1.
     * <p/>
     * @param randomSizeShift The random star size shift.
     */
    public void setRandomStarSizeShift(final int randomSizeShift) {
        this.randomStarSizeShift = randomSizeShift;
    }

    /**
     * Get the basic star density. This density can vary between the different
     * layers.
     * <p/>
     * @return The basic star density.
     */
    public int getDensity() {
        return density;
    }

    /**
     * Get the height of the texture being generated.
     * <p/>
     * @return The height of the texture being generated.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the width of the texture being generated.
     * <p/>
     * @return The width of the texture being generated.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the size of the stars being added in pixels.
     * <p/>
     * @return the size of the stars being added in pixels.
     */
    public int getSize() {
        return size;
    }

    /**
     * Set the size of the stars being added in pixels.
     * <p/>
     * @param sz the size of the stars being added in pixels.
     */
    public void setSize(final int sz) {
        this.size = sz;
    }

    /**
     * Get the color of the stars being generated. If no color is set, each star
     * will get a random color.
     * <p/>
     * @return The color of the stars being generated, null of no color has been
     *         set.
     */
    public Color getStarColor() {
        return starColor;
    }

    /**
     * Set the color of the stars being generated. If no color is set, each star
     * will get a random color.
     * <p/>
     * @param color The color of the stars being generated, null of no color has
     *              been set.
     */
    public void setStarColor(final Color color) {
        this.starColor = color;
    }

    /**
     * Generates the actual starfield texture.
     * <p/>
     * @return The generated starfield texture.
     */
    public Texture generate() {
        Texture2D t = new Texture2D();
        Image i = new Image(Image.Format.RGBA8, width, height, addStars());
        t.setImage(i);
        return t;
    }

    private ByteBuffer addStars() {
        stars = new Color[width][height];
        for (int star = 0; star < density; star++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            createStar(x, y, star);
        }
        ByteBuffer data = BufferUtils.createByteBuffer(width * height * 4);
        // no alpha
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (stars[x][y] != null) {
                    int rgb = stars[x][y].getRGB();
                    byte a = (byte) ((rgb & 0xFF000000) >> 24);
                    byte r = (byte) ((rgb & 0x00FF0000) >> 16);
                    byte g = (byte) ((rgb & 0x0000FF00) >> 8);
                    byte b = (byte) ((rgb & 0x000000FF));
                    data.put(r).put(g).put(b).put(a);
                } else {
                    byte a = (byte) 0x00;
                    byte r = (byte) 0x00;
                    byte g = (byte) 0x00;
                    byte b = (byte) 0x00;
                    data.put(r).put(g).put(b).put(a);
                }
            }
        }
        data.flip();
        return data;
    }

    private void createStar(final int x, final int y, final int starNumber) {
        Color color = determineStarColor(starNumber);
        int starSize = determineStarSize(starNumber);
        if (x + starSize < width && x - starSize >= 0 && y + starSize < height
                && y - starSize >= 0) {
            circle(x, y, starSize, color);
        }
    }

    private void circle(final int x0, final int y0, final int radius, final Color color) {
        int f = 1 - radius;
        int ddFx = 1;
        int ddFy = -2 * radius;
        int x = 0;
        int y = radius;

        setPixel(x0, y0 + radius, color);
        setPixel(x0, y0 - radius, color);
        setPixel(x0 + radius, y0, color);
        setPixel(x0 - radius, y0, color);

        for (int xl = x0 - radius; xl < x0 + radius; xl++) {
            setPixel(xl, y0, color);
            setPixel(xl, y0, color);
        }

        for (int yl = y0 - radius; yl <= y0 + radius; yl++) {
            setPixel(x0, yl, color);
            setPixel(x0, yl, color);
        }

        while (x < y) {
            // ddF_x == 2 * x + 1;
            // ddF_y == -2 * y;
            // f == x*x + y*y - radius*radius + 2*x - y + 1;
            if (f >= 0) {
                y--;
                ddFy += 2;
                f += ddFy;
            }
            x++;
            ddFx += 2;
            f += ddFx;
            setPixel(x0 + x, y0 + y, color);
            setPixel(x0 - x, y0 + y, color);
            setPixel(x0 + x, y0 - y, color);
            setPixel(x0 - x, y0 - y, color);
            setPixel(x0 + y, y0 + x, color);
            setPixel(x0 - y, y0 + x, color);
            setPixel(x0 + y, y0 - x, color);
            setPixel(x0 - y, y0 - x, color);

            for (int xl = x0 - y; xl <= x0 + y; xl++) {
                setPixel(xl, y0 + x, color);
                setPixel(xl, y0 - x, color);
            }

            for (int yl = y0 - y; yl <= y0 + y; yl++) {
                setPixel(x0 - x, yl, color);
                setPixel(x0 + x, yl, color);
            }
        }
    }

    private void setPixel(final int x, final int y, final Color color) {
        stars[x][y] = color;
    }

    private Color determineStarColor(final int starNumber) {
        Color s = starColor;
        if (starColor == null
                || (randomStarColorInterval != 0
                    && starNumber - previousRandomStarColor
                    == nextRandomStarColor)) {
            s = createRandomColor();
            previousRandomStarColor = nextRandomStarColor;
            nextRandomStarColor += rand.nextInt(randomStarColorInterval);
        }
        return s;
    }

    private int determineStarSize(final int starNumber) {
        int s = size;
        if (randomStarSizeInterval != 0
                && starNumber - previousRandomStarSize == nextRandomStarSize) {
            int randomShift = rand.nextInt(randomStarSizeShift * 2)
                    - randomStarSizeShift;
            s = size + randomShift;
            // the size is NEVER 0 or smaller, the star is ALWAYS drawn.
            if (s <= 0) {
                s = 1;
            }
            previousRandomStarSize = nextRandomStarSize;
            nextRandomStarSize += rand.nextInt(randomStarSizeInterval);
        }
        return s;
    }

    private Color createRandomColor() {
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        int a = (int) (0XFF * visibility);
        return new Color(r, g, b, a);
    }

    public void setVisibility(float visibility) {
        this.visibility = visibility;
    }
}
