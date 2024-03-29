/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.model;

import WS3DCoppelia.util.Constants;
import WS3DCoppelia.util.Constants.*;
import co.nstant.in.cbor.CborException;
import com.coppeliarobotics.remoteapi.zmq.RemoteAPIObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that encapsulate the agent information and its available actions.
 *
 * @author bruno
 */
public class Agent extends Identifiable {
    private RemoteAPIObjects._sim sim;
    private long agentHandle;
    private long worldScript;
    private long agentScript;

    private List<Double> pos;
    private List<Double> ori;
    private double fuel;
    private List<Identifiable> thingsInVision = Collections.synchronizedList(new ArrayList());
    private Bag bag = new Bag();
    private int score = 0;
    private Leaflet[] leaflets = new Leaflet[Constants.NUM_LEAFLET_PER_AGENTS];
    private Color color;
    private List<Double> currColor;
    private boolean verbose = false;

    private boolean initialized = false;
    private double fovAngle = 0.5;
    private int maxFov = 100;
    private boolean rotate = false;
    private final double xLimit;
    private final double yLimit;

    private Map<String, Object> commandQueue = Collections.synchronizedMap(new LinkedHashMap());
    private boolean isNPC = false;
    private boolean remove = false;
    public boolean removed = false;

    /**
     * @param sim_   The CoppeliaSim api connector.
     * @param x      Initial x coordinate.
     * @param y      Initial y coordinate.
     * @param width  Environment width for determining movement limits.
     * @param heigth Environment height for determining movement limits.
     */
    public Agent(RemoteAPIObjects._sim sim_, double x, double y, double width, double heigth) {
        color = Color.AGENT_GREEN;
        currColor = color.rgb();
        sim = sim_;
        pos = Arrays.asList(new Double[]{x, y,  0.16});
        ori = Arrays.asList(new Double[]{ 0.0,  0.0,  0.0});
        xLimit = width;
        yLimit = heigth;
        for (int i = 0; i < Constants.NUM_LEAFLET_PER_AGENTS; i++) {
            leaflets[i] = new Leaflet();
        }
    }

    public Agent(RemoteAPIObjects._sim sim_, double x, double y, double width, double heigth, boolean verbose) {
        this(sim_, x, y, width, heigth);
        this.verbose = verbose;
    }

    public Agent(RemoteAPIObjects._sim sim_, double x, double y, double width, double heigth, Color color_) {
        color = color_;
        currColor = color.rgb();
        sim = sim_;
        pos = Arrays.asList(new Double[]{x, y,  0.16});
        ori = Arrays.asList(new Double[]{ 0.0,  0.0,  0.0});
        xLimit = width;
        yLimit = heigth;
        for (int i = 0; i < Constants.NUM_LEAFLET_PER_AGENTS; i++) {
            leaflets[i] = new Leaflet();
        }
    }

    public Agent(RemoteAPIObjects._sim sim_, double x, double y, double width, double heigth, Color color_, boolean verbose) {
        this(sim_, x, y, width, heigth, color_);
        this.verbose = verbose;
    }

    private void init() {
        try {
            agentHandle = sim.loadModel(System.getProperty("user.dir") + "/agent_model.ttm");

            Object[] response = sim.callScriptFunction("init_agent", worldScript, agentHandle, pos, ori, Constants.BASE_SCRIPT, color.rgb(), isNPC ? "True" : "False");
            agentScript = (long) response[0];
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateState(List<Thing> inWorldThings, List<Agent> inWorldAgents) {
        if (remove) {
            try {
                long childHandle = sim.getObjectChild(agentHandle, 0);
                while (childHandle != -1){
                    sim.removeObjects(Arrays.asList(new Object[]{childHandle}));
                    childHandle = sim.getObjectChild(agentHandle, 0);
                }
                sim.removeObjects(Arrays.asList(new Object[]{agentHandle}));
                removed = true;
            } catch (CborException ex) {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            List<Object> objectsInVision = new ArrayList<Object>();
            try {
                if (isNPC) {
                    Object[] responseObject = sim.callScriptFunction("status", agentScript, score, new ArrayList<>(), color.hls());
                    List<Object> response = (List<Object>) responseObject[0];
                    if (response.get(0) instanceof List){
                        pos = (List<Double>) response.get(0);
                    } else {
                        pos = new ArrayList<>();
                        for (Object dim : response){
                            pos.add(Double.parseDouble(dim.toString()));
                        }
                    }
                    ori = (List<Double>) response.get(1);
                    objectsInVision = (List<Object>) response.get(3);
                    List<Identifiable> thingsSeen = new ArrayList<>();
                    synchronized (inWorldThings) {
                        for (Thing thing : inWorldThings) {
                            if (thing.isIncluded(objectsInVision)) {
                                thingsSeen.add(thing);
                            }
                        }
                    }
                    synchronized (thingsInVision) {
                        thingsInVision.clear();
                        thingsInVision.addAll(thingsSeen);
                    }
                } else {
                    List<List<Integer>> leafletInfo = new ArrayList<>();
                    List<Integer> bagInfo = new ArrayList<>();
                    for (JewelTypes jewel : JewelTypes.values()) {
                        bagInfo.add(bag.getTotalCountOf(jewel));
                    }
                    leafletInfo.add(bagInfo);

                    for (Leaflet l : leaflets) {
                        List<Integer> lInfo = new ArrayList<>();
                        for (JewelTypes jewel : JewelTypes.values()) {
                            lInfo.add(l.getRequiredAmountOf(jewel));
                        }
                        lInfo.add(l.isDelivered() ? 1 : 0);
                        lInfo.add(l.getPayment());
                        leafletInfo.add(lInfo);
                    }
                    Object[] responseObject = sim.callScriptFunction("status", agentScript, score, leafletInfo, color.hls());
                    List<Object> response = (List<Object>) responseObject[0];
                    if (response.get(0) instanceof Long){
                        pos = new ArrayList<>();
                        for (Object dim : response){
                            pos.add(Double.parseDouble(dim.toString()));
                        }
                    } else {
                        pos = (List<Double>) response.get(0);
                    }
                    ori = (List<Double>) response.get(1);
                    fuel = (double) response.get(2);
                    objectsInVision = (List<Object>) response.get(3);
                    currColor = (List<Double>) response.get(4);

                    List<Identifiable> thingsSeen = new ArrayList<>();
                    synchronized (inWorldThings) {
                        for (Thing thing : inWorldThings) {
                            if (thing.isIncluded(objectsInVision)) {
                                thingsSeen.add(thing);
                            }
                        }
                    }
                    synchronized (inWorldAgents) {
                        for (Agent agent : inWorldAgents) {
                            if (agent.initialized)
                                if (agent.isIncluded(objectsInVision)) {
                                    thingsSeen.add(agent);
                                }
                        }
                    }

                    synchronized (thingsInVision) {
                        thingsInVision.clear();
                        thingsInVision.addAll(thingsSeen);
                    }
                }
            } catch (CborException ex) {
                if (verbose)
                    Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ArrayIndexOutOfBoundsException | ClassCastException ex) {
                if (verbose)
                    Logger.getLogger(Agent.class.getName()).log(Level.WARNING, "Agent missed an update step");
            }

            if (rotate)
                execRotate();
        }

    }

    private void execCommands(List<Thing> inWorldThings) {
        synchronized (commandQueue) {
            List<String> executed = new ArrayList<>();
            for (String command : commandQueue.keySet()) {
                executed.add(command);
                Thing thing = null;
                switch (command) {
                    case "move":
                        this.execMove((List<Double>) commandQueue.get(command));
                        break;
                    case "eat":
                        if (commandQueue.get(command) instanceof Integer)
                            thing = inWorldThings.stream()
                                    .filter(e -> e.checkId((int) commandQueue.get(command)))
                                    .findFirst()
                                    .orElse(null);
                        else
                            thing = (Thing) commandQueue.get(command);
                        if (thing != null)
                            if (thing.isFood())
                                this.execEatIt(thing);
                        break;
                    case "rotate":
                        this.execRotate();
                        rotate = true;
                        break;
                    case "sackIt":
                        if (commandQueue.get(command) instanceof Integer)
                            thing = inWorldThings.stream()
                                    .filter(e -> e.checkId((int) commandQueue.get(command)))
                                    .findFirst()
                                    .orElse(null);
                        else
                            thing = (Thing) commandQueue.get(command);
                        if (thing != null)
                            this.execSackIt(thing);
                        break;
                    case "deliver":
                        System.out.println("Exec Deliver");
                        this.execDeliver((Integer) commandQueue.get(command));
                        break;
                    case "stop":
                        this.execStop();
                        break;
                    default:
                }
            }
            for (String c : executed)
                commandQueue.remove(c);
        }
    }

    public void run(List<Thing> inWorldThings, List<Agent> inWorldAgents, long worldScript_) {
        if (!initialized) {
            worldScript = worldScript_;
            this.init();
            initialized = true;
        } else {
            this.updateState(inWorldThings, inWorldAgents);
            this.execCommands(inWorldThings);
        }
    }

    /**
     * Command to control creature movement through the environment.
     *
     * @param x The x coordinate of the destination.
     * @param y The y coordinate of the destination.
     */
    public void moveTo(double x, double y) {
        synchronized (commandQueue) {
            commandQueue.put("move", Arrays.asList(new Double[]{x, y}));
        }
    }

    /**
     * Command for agent to consume a food from the environment.
     *
     * @param thing The object instance of the food to be consumed.
     */
    public void eatIt(Thing thing) {
        synchronized (commandQueue) {
            if (thing.isFood())
                commandQueue.put("eat", thing);
        }
    }

    /**
     * Command for agent to consume a food from the environment.
     *
     * @param thingId The ID of the food to be consumed.
     */
    public void eatIt(int thingId) {
        synchronized (commandQueue) {
            commandQueue.put("eat", thingId);
        }
    }

    /**
     * Command the agent to rotate along its own axis.
     */
    public void rotate() {
        synchronized (commandQueue) {
            commandQueue.put("rotate", "");
        }
    }

    /**
     * Command the agent to stop its movement.
     */
    public void stop() {
        synchronized (commandQueue) {
            commandQueue.put("stop", "");
        }
    }

    /**
     * Command to insert an object inside the agent Bag
     *
     * @param thing The object instance of the thing to be collected.
     * @see Bag
     */
    public void sackIt(Thing thing) {
        synchronized (commandQueue) {
            commandQueue.put("sackIt", thing);
        }
    }

    /**
     * Command to insert an object inside the agent Bag
     *
     * @param thingId The ID of the thing to be collected.
     * @see Bag
     */
    public void sackIt(int thingId) {
        synchronized (commandQueue) {
            commandQueue.put("sackIt", thingId);
        }
    }

    /**
     * Command to deliver the required jewels of a leaflet. The jewels are removed
     * form agent's bag and the leaflet payment is added to agent's score.
     *
     * @param leafletId The leaflet ID to be delivered.
     * @see Leaflet
     */
    public void deliver(int leafletId) {
        synchronized (commandQueue) {
            System.out.println(String.format("Deliver leaflet %d", leafletId));
            commandQueue.put("deliver", leafletId);
        }
    }

    private void execMove(List<Double> params) {
        try {
            double goalX = params.get(0);
            double goalY = params.get(1);
            goalX = (goalX > xLimit) ? xLimit : (goalX < 0.1f ? 0.1f : goalX);
            goalY = (goalY > yLimit) ? yLimit : (goalY < 0.1f ? 0.1f : goalY);
            double goalPitch = Math.atan2(goalY - pos.get(1), goalX - pos.get(0));

            List<Double> targetPos = Arrays.asList(new Double[]{goalX, goalY,  0.0});
            List<Double> targetOri = new ArrayList<>(ori);
            targetOri.set(2,  goalPitch);

            sim.callScriptFunction("move_agent", agentScript, targetPos, targetOri);

            rotate = false;
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (verbose)
                Logger.getLogger(Agent.class.getName()).log(Level.INFO, "Missed Move command return");
        }
    }

    private void execEatIt(Thing food) {
        try {
            if (food.isPresent()) {
                food.remove();
                sim.callScriptFunction("increase_fuel", agentScript, food.energy());
            }

            rotate = false;
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (verbose)
                Logger.getLogger(Agent.class.getName()).log(Level.INFO, "Missed Eat command return");
        }
    }

    private void execRotate() {
        try {
            sim.callScriptFunction("rotate_agent", agentScript);
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (verbose)
                Logger.getLogger(Agent.class.getName()).log(Level.INFO, "Missed Rotate command return");
        }

    }

    private void execStop() {
        try {
            sim.callScriptFunction("stop_agent", agentScript);
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (verbose)
                Logger.getLogger(Agent.class.getName()).log(Level.INFO, "Missed Stop command return");
        }

    }

    private void execSackIt(Thing thing) {
        try {
            if (!thing.removed) {
                thing.remove();
                bag.insertItem(thing.thingType(), 1);
                for (int i = 0; i < Constants.NUM_LEAFLET_PER_AGENTS; i++) {
                    leaflets[i].updateProgress(bag);
                }
            }
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void execDeliver(int leafletId) {
        boolean deliverable = false;

        int pos = 0;
        for (int i = 0; i < Constants.NUM_LEAFLET_PER_AGENTS; i++) {
            if (leaflets[i].checkId(leafletId) && leaflets[i].isCompleted()) {
                pos = i;
                deliverable = true;
            }
        }

        if (deliverable) {
            System.out.println("Delivering");
            score += leaflets[pos].getPayment();
            leaflets[pos].setDelivered(true);
            for (Entry<Constants.JewelTypes, Integer> requirement : leaflets[pos].getRequirements().entrySet()) {
                bag.removeItem(requirement.getKey(), requirement.getValue());
            }
        } else {
            System.out.println("Not completed");
        }
    }

    public double getFuel() {
        return fuel;
    }

    public double getPitch() {
        return ori.get(2);
    }

    /**
     * @return A list containing two double values. First is the x coordinate and second the y coordinate of agent's position
     */
    public List<Double> getPosition() {
        return pos;
    }

    public List<Identifiable> getThingsInVision() {
        return thingsInVision;
    }

    public List<Double> getColor() {
        return currColor;
    }

    public String getColorName() {
        String[] split = color.name().split("_");
        return split.length > 1 ? split[1] : color.name();
    }

    public boolean isInOccupancyArea(double x, double y) {
        return Math.hypot(Math.abs(pos.get(0) - x),
                Math.abs(pos.get(1) - y))
                <= Constants.AGENT_OCCUPANCY_RADIUS;
    }

    public Bag getBag() {
        return bag;
    }

    public Leaflet[] getLeaflets() {
        return leaflets;
    }

    public void generateNewLeaflets() {
        for (int i = 0; i < Constants.NUM_LEAFLET_PER_AGENTS; i++) {
            leaflets[i] = new Leaflet();
            leaflets[i].updateProgress(bag);
        }
    }

    public List<Double> getRelativePosition(List<Double> targetPos) {
        return Arrays.asList(
                targetPos.get(0) - pos.get(0),
                targetPos.get(1) - pos.get(1),
                targetPos.get(1) - pos.get(2)
        );
    }

    public boolean isIncluded(List<Object> handleList) {
        //The visible object detected by the camera is the sub-object
        //with the agent mesh, not the invisible one containing the
        //agent's script, thus the +1
        return handleList.contains(agentHandle+1);
    }

    public void setNPC(boolean NPC) {
        this.isNPC = NPC;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }
}
