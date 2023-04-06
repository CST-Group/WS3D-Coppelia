/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia;

import Demo.Environment;
import WS3DCoppelia.model.*;
import WS3DCoppelia.util.Constants.BrickTypes;
import WS3DCoppelia.util.Constants.ThingsType;
import co.nstant.in.cbor.CborException;
import com.coppeliarobotics.remoteapi.zmq.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import support.NativeUtils;

/**
 *
 * @author bruno
 */
public class WS3DCoppelia {
    
    private RemoteAPIClient client;
    private RemoteAPIObjects._sim sim;
    private List<Agent> inWorldAgents = Collections.synchronizedList(new ArrayList());
    private List<Thing> inWorldThings = Collections.synchronizedList(new ArrayList());
    private float width = 5, heigth = 5;
    private Long worldScript;
    
    
    public WS3DCoppelia(){
        client = new RemoteAPIClient();
        sim = client.getObject().sim();
        try{
            NativeUtils.loadFileFromJar("/workspace/agent_model.ttm");
            NativeUtils.loadFileFromJar("/workspace/floor.ttm");
        } catch(IOException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public WS3DCoppelia(float width_, float heigth_){
        client = new RemoteAPIClient();
        sim = client.getObject().sim();
        try{
            NativeUtils.loadFileFromJar("/workspace/agent_model.ttm");
            NativeUtils.loadFileFromJar("/workspace/floor.ttm");
        } catch(IOException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        width = width_;
        heigth = heigth_;  
    }
        
    
    class mainTimerTask extends TimerTask {

        WS3DCoppelia wov;
        boolean enabled = true;

        public mainTimerTask(WS3DCoppelia wovi) {
            wov = wovi;
        }

        public void run() {
            if (enabled) {
                wov.updateState();
            }
        }

        public void setEnabled(boolean value) {
            enabled = value;
        }
    }
    
    public void updateState(){
        synchronized(inWorldThings){
            List<Thing> excludedThings = inWorldThings.stream().filter(t->t.removed).collect(Collectors.toList());
            List<Thing> notInitialized = inWorldThings.stream().filter(t->!t.isInitialized()).collect(Collectors.toList());
            inWorldThings.removeAll(excludedThings);
            if(notInitialized.size() > 2) Thing.bulkInit(notInitialized, sim);
            for(Thing thg : inWorldThings){
                thg.run();
            }
        }
        synchronized(inWorldAgents){
            for(Agent agt : inWorldAgents){
                agt.run(inWorldThings, worldScript);
            }
        }
    }
    
    public void startSimulation() throws java.io.IOException, CborException{
        client.setStepping(false);
        sim.startSimulation();
        
        float startTime = sim.getSimulationTime();
        while(sim.getSimulationTime() - startTime < 1){}
        
        Long floorHandle;
        try{
            floorHandle = sim.getObject("/Floor");
            sim.removeModel(floorHandle);
        } catch(RuntimeException ex){
            System.out.println("No default floor to exclude");
        }
        floorHandle = sim.loadModel(System.getProperty("user.dir") + "/floor.ttm");
        List<Float> floorSize = sim.getShapeBB(floorHandle);
        floorSize.set(0, width);
        floorSize.set(1, heigth);
        sim.setShapeBB(floorHandle, floorSize);
        List<Float> floorPos = Arrays.asList(new Float[]{width/2, heigth/2, (float) -0.02});
        sim.setObjectPosition(floorHandle, sim.handle_world, floorPos);
        
        worldScript = sim.getScript(sim.scripttype_childscript, floorHandle, "");

        Long brickTreeHandle = sim.createDummy(0.01);
        sim.setObjectAlias(brickTreeHandle, "Bricks");
        Long foodTreeHandle = sim.createDummy(0.01);
        sim.setObjectAlias(foodTreeHandle , "Foods");
        Long jewelTreeHandle = sim.createDummy(0.01);
        sim.setObjectAlias(jewelTreeHandle , "Jewels");
        
        
        updateState();
        startTime = sim.getSimulationTime();
        while(sim.getSimulationTime() - startTime < 2){}
        
        Timer t = new Timer();
        WS3DCoppelia.mainTimerTask tt = new WS3DCoppelia.mainTimerTask(this);
        t.scheduleAtFixedRate(tt, 100, 100);
    }
    
    public void stopSimulation() throws CborException{
        sim.stopSimulation();
    }
    
    public Agent createAgent(float x, float y){
        //Ensures limit
        x = (x > width) ? width : (x < 0.05f ? 0.05f: x );
        y = (y > heigth) ? heigth : (y < 0.05f ? 0.05f: y );
        
        Agent newAgent = new Agent(sim, x, y, width, heigth);
        synchronized(inWorldAgents){
            inWorldAgents.add(newAgent);
        }
        return newAgent;
    }
    
    public Thing createThing(ThingsType category, float x, float y){
        //Ensures limit
        x = (x > width) ? width : (x < 0 ? 0: x );
        y = (y > heigth) ? heigth : (y < 0 ? 0: y );
        
        Thing newThing = new Thing(sim, category, x, y);
        synchronized (inWorldThings) {
            inWorldThings.add(newThing);
        }
        return newThing;
    }
    
    public Thing createBrick(BrickTypes type, float x1, float y1, float x2, float y2){
        Thing newBrick = new Thing(sim, type, x1, y1, x2, y2);
        synchronized (inWorldThings) {
            inWorldThings.add(newBrick);
        }
        return newBrick;
    }
    
    public boolean isOccupied(float x, float y){
        synchronized(inWorldThings){
            for(Thing thg : inWorldThings){
                if (thg.isInOccupancyArea(x, y)) return true;
            }
        }
        synchronized(inWorldAgents){
            for(Agent agt : inWorldAgents){
                if (agt.isInOccupancyArea(x, y)) return true;
            }
        }
        
        return false;
    }
    
    public float getWorldWidth(){
        return width;
    }
    
    public float getWorldHeigth(){
        return heigth;
    }
    
}