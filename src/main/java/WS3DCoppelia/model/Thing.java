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
    private Long thingHandle;
    private List<Float> pos;
    public boolean removed = false;

    public float width = (float) 0.1, depth = (float) 0.1;

    private ThingsType category;

    private boolean initialized = false;

    public Thing(RemoteAPIObjects._sim sim_, ThingsType category_, float x, float y){
        sim = sim_;
        pos = Arrays.asList(new Float[]{x, y, (float) 0.05});
        category = category_;

    }

    public Thing(RemoteAPIObjects._sim sim_, ThingsType category_, float x1, float y1, float x2, float y2){
        sim = sim_;
        width = Math.abs(x1 - x2);
        depth = Math.abs(y1 - y2);
        if( category_ instanceof Constants.BrickTypes)
            pos = Arrays.asList(new Float[]{(x1 + x2) / 2, (y1 + y2) / 2, Constants.BRICK_HEIGTH / 2});
        else
            pos = Arrays.asList(new Float[]{(x1 + x2) / 2, (y1 + y2) / 2, (float) 0.05});
        category = category_;

    }

    private void init(){

        try {
            List<Float> size;
            if (category instanceof Constants.BrickTypes){
                size = Arrays.asList(new Float[]{width, depth, Constants.BRICK_HEIGTH});
            } else {
                size = THING_SIZE;
            }

            Long floorHandle =  sim.getObject("/Floor");
            Long script = sim.getScript(sim.scripttype_childscript, floorHandle, "");
            thingHandle = (Long) sim.callScriptFunction("init_thing", script, category.shape(), size, pos, category.color().rgb());
        } catch (CborException ex) {
            Logger.getLogger(Thing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void bulkInit(List<Thing> things, RemoteAPIObjects._sim sim_){
        List<Integer> shapes = things.stream().map(t->t.getShape()).collect(Collectors.toList());
        List<List<Float>> poss = things.stream().map(t->t.getPos()).collect(Collectors.toList());
        List<List<Float>> sizes = things.stream().map(t->t.getSize()).collect(Collectors.toList());
        List<List<Float>> colors = things.stream().map(t->t.getColor()).collect(Collectors.toList());
        List<Integer> categories = things.stream().map(t->t.isFood() ? 1 : t.isJewel() ? 2 : t.isBrick() ? 3 : 0).collect(Collectors.toList());

        try{
            Long floorHandle =  sim_.getObject("/Floor");
            Long script = sim_.getScript(sim_.scripttype_childscript, floorHandle, "");
            ArrayList<Long> thingHandles = (ArrayList<Long>) sim_.callScriptFunction("bulk_init", script, shapes, sizes, poss, colors, categories);
            // System.out.println(sim_.callScriptFunction("bulk_init", script, shapes, sizes, poss, colors));
            // System.out.println(thingHandles);
            for(int i = 0; i < things.size(); i++){
                things.get(i).setHandle(thingHandles.get(i));
                things.get(i).setInitialized();
            }
        } catch(CborException ex){
            Logger.getLogger(Thing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run(){
        if (!initialized){
            this.init();
            initialized = true;
        }
    }

    public List<Float> getPos(){
        return pos;
    }

    public ThingsType thingType(){
        return category;
    }

    public boolean isFood() { return category instanceof Constants.FoodTypes; }

    public boolean isJewel() { return category instanceof Constants.JewelTypes; }

    public boolean isBrick() { return category instanceof Constants.BrickTypes; }

    public float energy() {
        if (category instanceof Constants.FoodTypes)
            return ((Constants.FoodTypes) category).energy();
        return 0;
    }

    public void remove() throws CborException{
        sim.removeObjects(Arrays.asList(new Long[]{thingHandle}));
        removed = true;
    }

    public List<Float> getRelativePos(List<Float> source_pos) {
        List<Float> relPos = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            relPos.add(pos.get(i) - source_pos.get(i));
        }
        return relPos;
    }

    public boolean isInOccupancyArea(float x, float y){
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

    public boolean isIncluded(List<Long> handleList){
        return handleList.contains(thingHandle);
    }

    public List<Float> getSize(){
        List<Float> size;
        if (category instanceof Constants.BrickTypes){
            size = Arrays.asList(new Float[]{width, depth, Constants.BRICK_HEIGTH});
        } else {
            size = THING_SIZE;
        }
        return size;
    }

    public List<Float> getColor(){
        return category.color().rgb();
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

    public float getWidth() {
        return width;
    }

    public float getDepth() {
        return depth;
    }
}
