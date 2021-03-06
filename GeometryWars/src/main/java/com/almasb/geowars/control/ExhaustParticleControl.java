/*
 * The MIT License (MIT)
 *
 * FXGL - JavaFX Game Library
 *
 * Copyright (c) 2015-2016 AlmasB (almaslvl@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.almasb.geowars.control;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.core.collection.ObjectMap;
import com.almasb.fxgl.ecs.AbstractControl;
import com.almasb.fxgl.ecs.Entity;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import static java.lang.Math.min;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class ExhaustParticleControl extends AbstractControl {

    private static final Image PARTICLE_IMAGE;
    private static final ObjectMap<Color, Image> coloredImages = new ObjectMap<>();

    static {
        PARTICLE_IMAGE = FXGL.getAssetLoader().loadTexture("Glow.png").getImage();
    }

    private Point2D velocity;
    private float lifespan;
    private long spawnTime;
    private Color color;

    public ExhaustParticleControl(Point2D velocity, float lifespan, Color color) {
        this.velocity = velocity;
        this.lifespan = lifespan;
        spawnTime = System.currentTimeMillis();
        this.color = color;

        if (!coloredImages.containsKey(color)) {
            colorImage();
        }
    }

    private void colorImage() {
        int w = (int) PARTICLE_IMAGE.getWidth();
        int h = (int) PARTICLE_IMAGE.getHeight();

        WritableImage coloredImage = new WritableImage(w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                Color c = PARTICLE_IMAGE.getPixelReader().getColor(x, y);
                c = Color.color(
                        c.getRed() * color.getRed(),
                        c.getGreen() * color.getGreen(),
                        c.getBlue() * color.getBlue(),
                        c.getOpacity() * color.getOpacity()
                );

                coloredImage.getPixelWriter().setColor(x, y, c);
            }
        }

        coloredImages.put(color, coloredImage);
    }

    @Override
    public void onAdded(Entity e) {
        GameEntity entity = (GameEntity) e;
        entity.setView(new Texture(coloredImages.get(color)));
    }

    @Override
    public void onUpdate(Entity e, double tpf) {
        GameEntity entity = (GameEntity) e;

        // movement
        entity.translate(velocity.multiply(tpf * 3f));
        velocity = velocity.multiply(1 - 3f * tpf);
        if (Math.abs(velocity.getX()) + Math.abs(velocity.getY()) < 0.001f) {
            velocity = new Point2D(0, 0);
        }

        // rotation and scale
        if (velocity.getX() != 0 && velocity.getY() != 0) {
            entity.rotateToVector(velocity);
        }

        // alpha
        double speed = velocity.magnitude();
        long difTime = System.currentTimeMillis() - spawnTime;
        float percentLife = 1 - difTime / lifespan;
        double alpha = min(1.5f, min(percentLife * 2, speed));
        alpha *= alpha;

        final double opacity = alpha;
        entity.getView().setOpacity(opacity);

        // is particle expired?
        if (difTime > lifespan) {
            entity.removeFromWorld();
        }
    }

//
//    public void applyGravity(Vector3f gravity, float distance) {
//        Vector3f additionalVelocity = gravity
//                .mult(1000f / (distance * distance + 10000f));
//        velocity.addLocal(additionalVelocity);
//
//        if (distance < 400) {
//            additionalVelocity = new Vector3f(gravity.y, -gravity.x, 0)
//                    .mult(3f / (distance + 100));
//            velocity.addLocal(additionalVelocity);
//        }
//    }
}