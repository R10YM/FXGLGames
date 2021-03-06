package com.almasb.flappy;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.ecs.AbstractControl;
import com.almasb.fxgl.ecs.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.component.PositionComponent;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class PlayerControl extends AbstractControl {

    private Vec2 acceleration = new Vec2(6, 0);

    private PositionComponent position;

    @Override
    public void onAdded(Entity entity) {
        position = Entities.getPosition(entity);
    }

    @Override
    public void onUpdate(Entity entity, double tpf) {
        acceleration.x += tpf * 0.1;
        acceleration.y += tpf * 10;

        if (acceleration.y < -5)
            acceleration.y = -5;

        if (acceleration.y > 5)
            acceleration.y = 5;

        position.translate(acceleration.x, acceleration.y);
    }

    public void jump() {
        acceleration.addLocal(0, -5);

        FXGL.getAudioPlayer().playSound("jump.wav");
    }
}
