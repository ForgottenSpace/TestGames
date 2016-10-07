package com.ractoc.fs.games.thehuntison;

import com.forgottenspace.appstates.AiAppState;
import com.forgottenspace.appstates.DamageAppState;
import com.forgottenspace.appstates.FlightAppState;
import com.forgottenspace.appstates.FlightControlAppState;
import com.forgottenspace.appstates.SceneAppState;
import com.forgottenspace.appstates.ShootingAppState;
import com.forgottenspace.es.Entities;
import com.forgottenspace.es.Entity;
import com.forgottenspace.es.EntityComponent;
import com.forgottenspace.es.components.AiComponent;
import com.forgottenspace.es.components.BoundedEntityComponent;
import com.forgottenspace.es.components.CanMoveComponent;
import com.forgottenspace.es.components.ControlledComponent;
import com.forgottenspace.es.components.Controls;
import com.forgottenspace.es.components.DamageComponent;
import com.forgottenspace.es.components.HasFocusComponent;
import com.forgottenspace.es.components.LocationComponent;
import com.forgottenspace.es.components.MovementComponent;
import com.forgottenspace.es.components.OriginComponent;
import com.forgottenspace.es.components.RenderComponent;
import com.forgottenspace.es.components.ShootMainComponent;
import com.forgottenspace.es.components.SpeedComponent;
import com.forgottenspace.es.components.StructureComponent;
import com.forgottenspace.es.componentstorages.InMemoryComponentStorage;
import com.forgottenspace.parsers.ParserException;
import com.forgottenspace.parsers.ai.AiScriptLoader;
import com.forgottenspace.parsers.entitytemplate.EntityTemplate;
import com.forgottenspace.parsers.entitytemplate.TemplateLoader;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.ractoc.fs.games.thehuntison.appstates.StarFieldAppState;

public class Main extends SimpleApplication {

    private static Entities entities = Entities.getInstance();

    public Main() {
        super((AppState) null);
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        rootNode.addLight(new DirectionalLight());

        setupKeys();
        setupEntitySystem();
        setupAppStates();
        setupCamera();
        //setupStarField();
        spawnPlayer();
    }

    // disabled for now since there is a bug in the starfield software somewhere
    private void setupStarField() {
        StarFieldAppState sfas = new StarFieldAppState(entities);
        sfas.setWidth(settings.getWidth());
        sfas.setHeight(settings.getHeight());
        sfas.setDensity(150);
        sfas.setNrLayers(3);
        sfas.setLayerBaseSpeed(0.01f);
        sfas.setLayerBaseSize(3);
        sfas.setStarFieldDistance(600);
        sfas.setRandomStarColorInterval(5);
        sfas.setRandomStarSizeInterval(5);
        sfas.setRandomStarSizeShift(5);
        sfas.setVisibility(0.75f);
        stateManager.attach(sfas);
    }

    private void setupCamera() {
        getCamera().setLocation(new Vector3f(0, 60, 0));
        getCamera().lookAt(Vector3f.ZERO, Vector3f.UNIT_Z);
    }

    private void spawnPlayer() {
        EntityTemplate template = (EntityTemplate) assetManager.loadAsset("/Templates/Entity/BasicShipTemplate.etpl");

        if (template.getComponents() != null && !template.getComponents().isEmpty()) {
            EntityComponent[] components = template.getComponentsAsArray();
            Entity entity = entities.createEntity(components);
            entities.addComponentsToEntity(entity, new LocationComponent(Vector3f.ZERO, new Quaternion(), new Vector3f(1, 1, 1)), new ControlledComponent(), new BoundedEntityComponent());
        } else {
            throw new ParserException("No components for template /Templates/Entity/BasicShipTemplate.etpl");
        }
    }

    private void setupEntitySystem() {
        TemplateLoader.setClassLoader(this.getClass().getClassLoader());
        assetManager.registerLoader(TemplateLoader.class, "etpl", "ETPL");
        assetManager.registerLoader(AiScriptLoader.class, "ais", "AIS");

        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          AiComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          BoundedEntityComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          CanMoveComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          ControlledComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          DamageComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          HasFocusComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          LocationComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          MovementComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          RenderComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          ShootMainComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          SpeedComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          StructureComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          OriginComponent.class);
    }

    private void setupAppStates() {
        SceneAppState sceneAppState = new SceneAppState("Scenes/TriggerTest.j3o");
        sceneAppState.setPlayerCentric(false);
        stateManager.attach(sceneAppState);
        FlightControlAppState flightControlAppState = new FlightControlAppState();
        stateManager.attach(flightControlAppState);
        FlightAppState flightAppState = new FlightAppState();
        flightAppState.setBounded(true);
        stateManager.attach(flightAppState);
        AiAppState triggerAppState = new AiAppState();
        stateManager.attach(triggerAppState);
        stateManager.attach(new ShootingAppState());
        stateManager.attach(new DamageAppState());
    }

    private void setupKeys() {
        inputManager.addMapping(Controls.MOVE_FORWARD.name(),
                                new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(Controls.MOVE_BACKWARDS.name(),
                                new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(Controls.STRAFE_LEFT.name(),
                                new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping(Controls.STRAFE_RIGHT.name(),
                                new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping(Controls.ROTATE_LEFT.name(),
                                new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(Controls.ROTATE_RIGHT.name(),
                                new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(Controls.SHOOT_MAIN.name(),
                                new KeyTrigger(KeyInput.KEY_SPACE));
    }
}
