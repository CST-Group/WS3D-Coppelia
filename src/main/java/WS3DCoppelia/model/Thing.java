/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.model;

import WS3DCoppelia.util.Constants;
import static WS3DCoppelia.util.Constants.THING_SIZE;
import WS3DCoppelia.util.Constants.ThingsType;
import co.nstant.in.cbor.CborException;
import com.coppeliarobotics.remoteapi.zmq.RemoteAPIObjects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Models all types of static objects in the environment.
 *
 * @see ThingsType
 *
 * @author bruno
 */
public class Thing extends Identifiable {
    private RemoteAPIObjects._sim sim;
    private long thingHandle;
    private List<Double> pos;
    public boolean removing = false;
    public boolean removed = false;

    public double width = 0.1, depth = 0.1;
    private double scale = 1.0F;

    private ThingsType category;
    private List<Double> color = new ArrayList<>();

    private boolean initialized = false;

    public Thing(RemoteAPIObjects._sim sim_, ThingsType category_, double x, double y){
        sim = sim_;
        pos = Arrays.asList(new Double[]{x, y, 0.05});
        category = category_;
        List<Double> colorCat = category.color().rgb();
        for (Double colorElem : colorCat){
            color.add(Math.min(1f,Math.max(0f, colorElem + (new Random().nextDouble()-0.5f)*0.1f)));
        }

    }

    public Thing(RemoteAPIObjects._sim sim_, ThingsType category_, double x1, double y1, double x2, double y2){
        sim = sim_;
        width = Math.abs(x1 - x2);
        depth = Math.abs(y1 - y2);
        if( category_ instanceof Constants.BrickTypes)
            pos = Arrays.asList(new Double[]{(x1 + x2) / 2, (y1 + y2) / 2, Constants.BRICK_HEIGTH / 2});
        else
            pos = Arrays.asList(new Double[]{(x1 + x2) / 2, (y1 + y2) / 2, (double) 0.05});
        category = category_;
        List<Double> colorCat = category.color().rgb();
        for (Double colorElem : colorCat){
            color.add(Math.min(1f,Math.max(0f, colorElem + (new Random().nextDouble()-0.5f)*0.1f)));
        }

    }

    private void init(){

        try {
            List<Double> size;
            if (category instanceof Constants.BrickTypes){
                size = Arrays.asList(new Double[]{width, depth, Constants.BRICK_HEIGTH});
            } else {
                size = THING_SIZE;
            }

            Long floorHandle =  sim.getObject("/Floor");
            Long script = sim.getScript(sim.scripttype_childscript, floorHandle, "");
            Object[] response = sim.callScriptFunction("init_thing", script, category.shape(), size, pos, color);
            System.out.println(response.getClass());
            thingHandle = (Long) response[0];
        } catch (CborException ex) {
            Logger.getLogger(Thing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void bulkInit(List<Thing> things, RemoteAPIObjects._sim sim_){
        List<Integer> shapes = things.stream().map(t->t.getShape()).collect(Collectors.toList());
        List<List<Double>> poss = things.stream().map(t->t.getPos()).collect(Collectors.toList());
        List<List<Double>> sizes = things.stream().map(t->t.getSize()).collect(Collectors.toList());
        List<List<Double>> colors = things.stream().map(t->t.getColor()).collect(Collectors.toList());
        List<Integer> categories = things.stream().map(t->t.isFood() ? 1 : t.isJewel() ? 2 : t.isBrick() ? 3 : 0).collect(Collectors.toList());

        try{
            Long floorHandle =  sim_.getObject("/Floor");
            Long script = sim_.getScript(sim_.scripttype_childscript, floorHandle, "");
            Object[] responseObject = sim_.callScriptFunction("bulk_init", script, shapes, sizes, poss, colors, categories);
            ArrayList<Long> thingHandles = (ArrayList<Long>) responseObject[0];
            // System.out.println(sim_.callScriptFunction("bulk_init", script, shapes, sizes, poss, colors));
            //System.out.println(((ArrayList)thingHandles[0]).get(0).getClass());
            for(int i = 0; i < things.size(); i++){
                things.get(i).setHandle(thingHandles.get(i));
                things.get(i).setInitialized();
            }
        } catch(CborException ex){
            Logger.getLogger(Thing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateState(){
        if (removing) {
            try {
                if (sim.isHandle(this.thingHandle))
                    this.scale = sim.getObjectSizeFactor(this.thingHandle);
                else
                    removed = true;
            } catch (CborException ex) {
                Logger.getLogger(Thing.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RuntimeException ex){
                removed = true;
            }
        }
    }

    public void run(){
        if (!initialized){
            this.init();
            initialized = true;
        } else {
            this.updateState();
        }
    }

    public List<Double> getPos(){
        return pos;
    }

    public ThingsType thingType(){
        return category;
    }

    public boolean isFood() { return category instanceof Constants.FoodTypes; }

    public boolean isJewel() { return category instanceof Constants.JewelTypes; }

    public boolean isBrick() { return category instanceof Constants.BrickTypes; }

    public double energy() {
        if (category instanceof Constants.FoodTypes)
            return ((Constants.FoodTypes) category).energy();
        return 0;
    }

    public void remove() throws CborException{
        //sim.removeObjects(Arrays.asList(new Long[]{thingHandle}));
        Long floorHandle =  sim.getObject("/Floor");
        Long script = sim.getScript(sim.scripttype_childscript, floorHandle, "");
        sim.callScriptFunction("remove_thing", script, this.thingHandle);
        removing = true;
    }

    public List<Double> getRelativePos(List<Double> source_pos) {
        List<Double> relPos = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            relPos.add(pos.get(i) - source_pos.get(i));
        }
        return relPos;
    }

    public boolean isInOccupancyArea(double x, double y){
        return Math.hypot( Math.abs(pos.get(0) - x),
                Math.abs(pos.get(1) - y))
                <= Constants.THING_OCCUPANCY_RADIUS;
    }

    public boolean isInitialized(){
        return initialized;
    }

    public String getTypeName(){
        return category.typeName();
    }

    public boolean isIncluded(List<Object> handleList){
        return handleList.contains(thingHandle);
    }

    public List<Double> getSize(){
        List<Double> size;
        if (category instanceof Constants.BrickTypes){
            size = Arrays.asList(new Double[]{width, depth, Constants.BRICK_HEIGTH});
        } else {
            size = THING_SIZE;
        }
        return size.stream().map(e -> e*scale).collect(Collectors.toList());
    }

    public List<Double> getColor(){
        return color;
    }

    public int getShape(){
        return category.shape();
    }

    public void setInitialized(){
        this.initialized = true;
    }

    public void setHandle(long handle){
        this.thingHandle = handle;
    }

    public double getWidth() {
        return width * scale;
    }

    public double getDepth() {
        return depth * scale;
    }

    public boolean isPresent() {
        return !removed && !removing;
    }
}
