package com.ractoc.fs.games.thehuntison.appstates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.forgottenspace.es.ComponentTypeCriteria;
import com.forgottenspace.es.Entities;
import com.forgottenspace.es.Entity;
import com.forgottenspace.es.EntityException;
import com.forgottenspace.es.EntityResultSet;
import com.forgottenspace.es.components.ControlledComponent;
import com.forgottenspace.es.components.LocationComponent;
import com.forgottenspace.es.components.RenderComponent;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;
import com.ractoc.fs.games.thehuntison.textures.StarField;

/**
 * AppState for generating a multi-layer starfield. Each layer of the starfield
 * will have a different size of stars and will move at a different speed. This
 * gives the illusion of depth even though all the different starfield layers
 * are actually in the same plane, just on different quads.
 *
 * The variables are set on the complete starfield. Some variables are constant
 * across all layers of the starfield, width, height, density. Others are based
 * on the layer and are calculated from a base value in relation to the layer.
 * layerBaseSize, layerBaseSpeed. Here the size decreases with the increase in
 * layer while the speed increases. The decrease / increases is calculated as
 * follows:
 *
 * layerBaseSize - (layerBaseSize / nrLayers) * layerNumber. layerBaseSpeed +
 * (layerBaseSpeed / nrLayers) * layerNumber.
 *
 * @author ractoc
 * @since 0.1
 */
public final class StarFieldAppState extends AbstractAppState {

    private int width;
    private int height;
    private int density;
    private int nrLayers;
    private int layerBaseSize;
    private int starFieldDistance;
    private int randomStarColorInterval;
    private int randomStarSizeInterval;
    private int randomStarSizeShift;
    private SimpleApplication sApp;
    private List<Material> layers = new ArrayList<>();
    private Node starfield = new Node("starfield");
    private float layerBaseSpeed;
    private float visibility = 1f;
    private final Entities entities;
    private final EntityResultSet entSet;
    private Entity entity;

    /**
     * Constructor which overrides all the defaults.
     * <p/>
     * @param screenWidth        The width of the starfield.
     * @param screenHeight       The height of the starfield.
     * @param starDensity        The density of the stars in a layer. This is for all
     *                           layers.
     * @param nrStarLayers       The number of layers.
     * @param layerBaseStarSpeed The base movement speed of the stars in the top
     *                           layer.
     * @param layerBaseStarSize  The base size of the stars in the top layer.
     * @param starLayerDistance  The distance of the star field. All layers are
     *                           at the same distance.
     */
    public StarFieldAppState(final Entities entities) {
        this.entities = entities;
        this.entSet = entities.queryEntities(new ComponentTypeCriteria(RenderComponent.class, LocationComponent.class, ControlledComponent.class));
    }

    @Override
    public void initialize(final AppStateManager asm,
                           final Application app) {
        super.initialize(asm, app);
        if (shouldBeInitialized()) {
            this.sApp = (SimpleApplication) app;
            createLayers();
            sApp.getRootNode().attachChild(starfield);
        }
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        super.stateAttached(stateManager);
        if (sApp != null) {
            sApp.getRootNode().attachChild(starfield);
        }
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        super.stateDetached(stateManager);
        sApp.getRootNode().detachChild(starfield);
    }

    private void createLayer(final int layerIndex) {
        double divider =
               1 + starFieldDistance / sApp.getCamera().getLocation().y;

        double dScreenHeight = 2 * (height / divider) + height;
        int screenHeight = (int) dScreenHeight;

        double dScreenWidth = 2 * (width / divider) + width;
        int screenWidth = (int) dScreenWidth;

        double dScreenDensity =
               ((dScreenWidth * dScreenHeight) / (width * height))
                * density;
        int screenDensity = (int) dScreenDensity;
        int starFieldDensity = screenDensity - (screenDensity / nrLayers) * (nrLayers - (layerIndex + 1));

        StarField sf = new StarField(screenWidth, screenHeight, starFieldDensity, Color.white);
        sf.setSize(layerBaseSize - (layerBaseSize / nrLayers)
                * layerIndex);
        sf.setRandomStarColorInterval(randomStarColorInterval
                * (layerIndex + 1));
        sf.setRandomStarSizeInterval(randomStarSizeInterval
                * (layerIndex + 1));
        sf.setRandomStarSizeShift(randomStarSizeShift
                - (randomStarSizeShift / nrLayers)
                * layerIndex);
        sf.setVisibility(visibility);
        Quad starQuad = new Quad(screenWidth, screenHeight);
        Geometry field = new Geometry("starField_" + layerIndex, starQuad);
        Texture stars = sf.generate();
        stars.setMinFilter(MinFilter.NearestNoMipMaps);
        stars.setMagFilter(MagFilter.Nearest);
        stars.setWrap(WrapMode.Repeat);
        Material mat1 = new Material(sApp.getAssetManager(),
                                     "MatDefs/MovingTexture.j3md");
        mat1.setTexture("ColorMap", stars);
        float matVisibility = visibility;
        if (layerIndex > 0) {
        	matVisibility = visibility / layerIndex;
        }
        
        mat1.setFloat("visibility", matVisibility);
        mat1.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat1.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        layers.add(layerIndex, mat1);
        field.setMaterial(mat1);
        field.rotate(-FastMath.DEG_TO_RAD * 90, 0f, 0f);
        field.setLocalTranslation(
                -screenWidth / 2F,
                -starFieldDistance,
                screenHeight / 2F);
        starfield.attachChild(field);
    }

    @Override
    public void update(final float tpf) {
        EntityResultSet.UpdateProcessor processor = entSet.getUpdateProcessor();
        List<Entity> removeEntities = processor.getRemovedEntities();
        List<Entity> addEntities = processor.getAddedEntities();
        if (!removeEntities.isEmpty()) {
            if (!entity.getId().equals(removeEntities.get(0).getId())) {
                throw new EntityException("Unknown entity. Expected "
                        + entity.getId() + " found "
                        + removeEntities.get(0).getId());
            } else {
                entity = null;
            }
        }
        if (!addEntities.isEmpty()) {
            if (entity != null) {
                throw new EntityException("Already have entity " + entity.getId()
                        + " with focus.");
            } else {
                entity = addEntities.get(0);
            }
        }
        processor.finalizeUpdates();
        if (entity != null) {
            LocationComponent locComp = entities.loadComponentForEntity(entity, LocationComponent.class);
            for (int i = 0; i < layers.size(); i++) {
                moveLayer(i, locComp);
            }
        }
    }

    private void moveLayer(final int layerIndex,
                           final LocationComponent locComp) {
        layers.get(layerIndex).setVector2("posDelta",
                                          new Vector2f(locComp.getTranslation().x,
                                                       -locComp.getTranslation().z));
        layers.get(layerIndex).setFloat("parallaxScale", (nrLayers - layerIndex)
                * layerBaseSpeed);
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
     * Set the basic star density. This density can vary between the different
     * layers.
     * <p/>
     * @param starDensity The basic star density.
     */
    public void setDensity(final int starDensity) {
        this.density = starDensity;
    }

    /**
     * Get the height of the starfield. This is later calculated in relation to
     * the actual starfield distance to the camera.
     * <p/>
     * @return The height of the starfield.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Set the height of the starfield. This is later calculated in relation to
     * the actual starfield distance to the camera.
     * <p/>
     * @param screenHeight The height of the starfield.
     */
    public void setHeight(final int screenHeight) {
        this.height = screenHeight;
    }

    /**
     * Get the width of the starfield. This is later calculated in relation to
     * the actual starfield distance to the camera.
     * <p/>
     * @return The width of the starfield.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set the width of the starfield. This is later calculated in relation to
     * the actual starfield distance to the camera.
     * <p/>
     * @param screenWidth The width of the starfield.
     */
    public void setWidth(final int screenWidth) {
        this.width = screenWidth;
    }

    /**
     * Get the base size of a star. This will later be related to the layer the
     * star is in.
     * <p/>
     * @return The base size of a star in pixels.
     */
    public int getLayerBaseSize() {
        return layerBaseSize;
    }

    /**
     * Set the base size of a star. This will later be related to the layer the
     * star is in.
     * <p/>
     * @param layerBaseStarSize The base size of a star in pixels.
     */
    public void setLayerBaseSize(final int layerBaseStarSize) {
        this.layerBaseSize = layerBaseStarSize;
    }

    /**
     * Get the base layer movement speed. This will later be related to the
     * layer being moved. Deeper layers move faster.
     * <p/>
     * @return the base layer movement speed.
     */
    public float getLayerBaseSpeed() {
        return layerBaseSpeed;
    }

    /**
     * Set the base layer movement speed. This will later be related to the
     * layer being moved. Deeper layers move faster.
     * <p/>
     * @param layerBaseStarSpeed the base layer movement speed.
     */
    public void setLayerBaseSpeed(final float layerBaseStarSpeed) {
        this.layerBaseSpeed = layerBaseStarSpeed;
    }

    /**
     * Get the number of layers the starfield consists off.
     * <p/>
     * @return The number of layers.
     */
    public int getNrLayers() {
        return nrLayers;
    }

    /**
     * Set the number of layers the starfield consists off.
     * <p/>
     * @param nrStarLayers The number of layers.
     */
    public void setNrLayers(final int nrStarLayers) {
        this.nrLayers = nrStarLayers;
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
     * Set the random star color interval. This is the interval with which a
     * star will get a random color. This is an optional setting.
     * <p/>
     * @param randomColorInterval The random star color interval.
     */
    public void setRandomStarColorInterval(final int randomColorInterval) {
        this.randomStarColorInterval = randomColorInterval;
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
     * Get the starfield distance from 0,0,0. This distance is along the Z-axis.
     * <p/>
     * @return The starfield distance along the Z-axis.
     */
    public int getStarFieldDistance() {
        return starFieldDistance;
    }

    /**
     * Set the starfield distance from 0,0,0. This distance is along the Z-axis.
     * <p/>
     * @param starLayerDistance The starfield distance along the Z-axis.
     */
    public void setStarFieldDistance(final int starLayerDistance) {
        this.starFieldDistance = starLayerDistance;
    }

    public void setVisibility(float visibility) {
        this.visibility = visibility;
    }

    private boolean shouldBeInitialized() {
        return this.sApp == null;
    }

    private void createLayers() {
        for (int i = 0; i < nrLayers; i++) {
            createLayer(i);
        }
    }
}
