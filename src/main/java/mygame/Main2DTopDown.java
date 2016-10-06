package mygame;

import com.forgottenspace.appstates.SceneAppState;
import com.forgottenspace.es.Entities;
import com.forgottenspace.es.components.LocationComponent;
import com.forgottenspace.es.components.RenderComponent;
import com.forgottenspace.es.componentstorages.InMemoryComponentStorage;
import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class Main2DTopDown extends SimpleApplication {
    
    private static Entities entities = Entities.getInstance();

    public static void main(String[] args) {
        Main2DTopDown app = new Main2DTopDown();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        rootNode.addLight(new DirectionalLight());
        setupEntitySystem();
        setupAppStates();
        setupCamera();
        spawnPlane();
    }

    private void setupEntitySystem() {         
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                RenderComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                LocationComponent.class);
    }

    private void setupAppStates() {
        stateManager.attach(new SceneAppState());
    }

    private void setupCamera() {
        getCamera().setLocation(new Vector3f(0, 60, 0));
        getCamera().lookAt(Vector3f.ZERO, Vector3f.UNIT_Z);
    }

    private void spawnPlane() {
        entities.createEntity(new RenderComponent("Models/Plane1.j3o"), new LocationComponent(Vector3f.ZERO, new Quaternion(), new Vector3f(1, 1, 1)));
    }
}
