/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia;

import WS3DCoppelia.model.*;
import WS3DCoppelia.util.Constants.*;
import WS3DCoppelia.util.NativeUtils;
import co.nstant.in.cbor.CborException;
import com.coppeliarobotics.remoteapi.zmq.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class is responsible for creating and maintaining the connection
 * to CoppeliaSim simulator. This class should be instantiated only once
 * and maintained during the simulation.
 *
 * @author bruno
 */
public class WS3DCoppelia {
    
    private RemoteAPIClient client;
    private RemoteAPIObjects._sim sim;
    private Timer t;
    private List<Creature> inWorldCreatures = Collections.synchronizedList(new LinkedList<>());
    private List<Thing> inWorldThings = Collections.synchronizedList(new LinkedList<>());
    private double width = 12, heigth = 12;
    private Long worldScript;
    private boolean running = false;

    /**
     * A connection to CoppeliaSim is created and necessary model files are loaded.
     */
    public WS3DCoppelia(){
        client = new RemoteAPIClient();
        sim = client.getObject().sim();
        try{
            NativeUtils.loadFileFromJar("/workspace/agent_model.ttm");
            NativeUtils.loadFileFromJar("/workspace/floor.ttm");
        } catch(IOException ex) {
            Logger.getLogger(Creature.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * A connection to CoppeliaSim is created and necessary model files are loaded.
     *
     * @param width_ Set the width of floor for the simulation
     * @param heigth_ Set the heigth of floor for the simulation
     *
     * @author bruno
     */
    public WS3DCoppelia(double width_, double heigth_){
        client = new RemoteAPIClient();
        sim = client.getObject().sim();
        try{
            NativeUtils.loadFileFromJar("/workspace/agent_model.ttm");
            NativeUtils.loadFileFromJar("/workspace/floor.ttm");
        } catch(IOException ex) {
            Logger.getLogger(Creature.class.getName()).log(Level.SEVERE, null, ex);
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
        try {
            synchronized (inWorldThings) {
                List<Thing> excludedThings = inWorldThings.stream().filter(t -> t.removed).collect(Collectors.toList());
                List<Thing> notInitialized = inWorldThings.stream().filter(t -> !t.isInitialized()).collect(Collectors.toList());
                inWorldThings.removeAll(excludedThings);
                if (notInitialized.size() > 2) Thing.bulkInit(notInitialized, sim);
                for (Thing thg : inWorldThings) {
                    thg.run();
                }
            }
            synchronized (inWorldCreatures) {
                List<Creature> excludedAgents = inWorldCreatures.stream().filter(t -> t.removed).collect(Collectors.toList());
                inWorldCreatures.removeAll(excludedAgents);
                for (Creature agt : inWorldCreatures) {
                    agt.run(inWorldThings, inWorldCreatures, worldScript);
                }
            }
        }
        catch (RuntimeException e){
            System.out.println("Simulation crashed. Stopping simulation");
            inWorldCreatures = Collections.synchronizedList(new LinkedList<>());
            inWorldThings = Collections.synchronizedList(new LinkedList<>());
            try {
                sim.stopSimulation();
            } catch (CborException ex) {
                System.out.println("Already Stopped");
            }
            t.cancel();
        }
    }

    /**
     * Starts the CoppeliaSim simulation and a timer taks to periodically update
     * agents and things information.
     *
     * @throws java.io.IOException
     * @throws CborException
     */
    public void startSimulation() throws java.io.IOException, CborException{
        client.setStepping(false);
        sim.startSimulation();
        
        double startTime = sim.getSimulationTime();
        while(sim.getSimulationTime() - startTime < 1){}
        
        Long floorHandle;
        try{
            floorHandle = sim.getObject("/Floor");
            sim.removeModel(floorHandle);
        } catch(RuntimeException ex){
            System.out.println("No default floor to exclude");
        }
        floorHandle = sim.loadModel(System.getProperty("user.dir") + "/floor.ttm");
        List<Double> floorSize = sim.getShapeBB(floorHandle);
        floorSize.set(0, width);
        floorSize.set(1, heigth);
        sim.setShapeBB(floorHandle, floorSize);
        List<Double> floorPos = Arrays.asList(new Double[]{width/2, heigth/2, -0.02});
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

        t = new Timer();
        WS3DCoppelia.mainTimerTask tt;
        tt = new mainTimerTask(this);
        t.scheduleAtFixedRate(tt, 100, 75);

        running = true;
    }

    /**
     * Stops CoppeliaSim simulation. Objects added to the environment are removed
     * automatically by CoppeliaSim.
     *
     * @throws CborException
     */
    public void stopSimulation() throws CborException{
        sim.stopSimulation();
    }

    /**
     * Insert an agent in the simulation.
     *
     * @param x The x coordinate to initialize the agent.
     * @param y The y coordinate to initialize the agent.
     * @return The agent object to access its information and action execution.
     *
     * @see Creature
     */
    public Creature createAgent(double x, double y){
        //Ensures limit
        x = (x > width) ? width : (x < 0.05f ? 0.05f: x );
        y = (y > heigth) ? heigth : (y < 0.05f ? 0.05f: y );
        
        Creature newAgent = new Creature(sim, x, y, width, heigth);
        synchronized(inWorldCreatures){
            inWorldCreatures.add(newAgent);
        }
        return newAgent;
    }

    /**
     * Insert an agent in the simulation.
     *
     * @param x The x coordinate to initialize the agent.
     * @param y The y coordinate to initialize the agent.
     * @return The agent object to access its information and action execution.
     *
     * @see Creature
     */
    public Creature createAgent(double x, double y, Color color){
        //Ensures limit
        x = (x > width) ? width : (x < 0.05f ? 0.05f: x );
        y = (y > heigth) ? heigth : (y < 0.05f ? 0.05f: y );

        Creature newAgent = new Creature(sim, x, y, width, heigth, color);
        synchronized(inWorldCreatures){
            inWorldCreatures.add(newAgent);
        }
        return newAgent;
    }

    public Creature createNPCAgent(double x, double y, Color color){
        //Ensures limit
        x = (x > width) ? width : (x < 0.05f ? 0.05f: x );
        y = (y > heigth) ? heigth : (y < 0.05f ? 0.05f: y );

        Creature newAgent = new Creature(sim, x, y, width, heigth, color);
        newAgent.setNPC(true);
        synchronized(inWorldCreatures){
            inWorldCreatures.add(newAgent);
        }
        return newAgent;
    }

    /**
     * Insert an object in the environment.
     *
     * @param category The type of thing to be inserted.
     * @param x The x coordinate to initialize the thing.
     * @param y The y coordinate to initialize the thing.
     * @return The thing object to access its information
     *
     * @see Thing
     * @see ThingsType
     */
    public Thing createThing(ThingsType category, double x, double y){
        //Ensures limit
        x = (x > width) ? width : (x < 0 ? 0: x );
        y = (y > heigth) ? heigth : (y < 0 ? 0: y );
        
        Thing newThing = new Thing(sim, category, x, y);
        synchronized (inWorldThings) {
            inWorldThings.add(newThing);
        }
        return newThing;
    }

    /**
     * A specific method to insert an object of the type Brick.
     *
     * @param type The brick type to be inserted.
     * @param x1 The x coordinate of the first corner of the brick.
     * @param y1 The y coordinate of the first corner of the brick.
     * @param x2 The x coordinate of the second corner of the brick.
     * @param y2 The y coordinate of the second corner of the brick.
     * @return The brick object to access its information.
     *
     * @see Thing
     * @see BrickTypes
     */
    public Thing createBrick(BrickTypes type, double x1, double y1, double x2, double y2){
        Thing newBrick = new Thing(sim, type, x1, y1, x2, y2);
        synchronized (inWorldThings) {
            inWorldThings.add(newBrick);
        }
        return newBrick;
    }
    
    public boolean isOccupied(double x, double y){
        synchronized(inWorldThings){
            for(Thing thg : inWorldThings){
                if (thg.isInOccupancyArea(x, y)) return true;
            }
        }
        synchronized(inWorldCreatures){
            for(Creature agt : inWorldCreatures){
                if (agt.isInOccupancyArea(x, y)) return true;
            }
        }
        
        return false;
    }
    
    public double getWorldWidth(){
        return width;
    }
    
    public double getWorldHeigth(){
        return heigth;
    }

    public boolean isRunning(){ return running; }

    public List<Creature> getAllCreatures(){
        return inWorldCreatures;
    }

    public List<Thing> getAllThings() { return inWorldThings;}

    public void setWidth(double width){
        if (!running)
            this.width = width / 100.0;
    }

    public void setHeigth(double heigth){
        if (!running)
            this.heigth = heigth / 100.0;
    }
}
