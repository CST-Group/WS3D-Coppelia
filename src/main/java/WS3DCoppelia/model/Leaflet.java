/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.model;

import WS3DCoppelia.util.Constants;
import WS3DCoppelia.util.Constants.JewelTypes;
import WS3DCoppelia.util.Constants.ThingsType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Models a jewels collection objective. A leaflet is composed of a list of required jewels and the
 * reward value for its completion.
 *
 * @author bruno
 */
public class Leaflet extends Identifiable {
    private int payment;
    private boolean completed;
    private boolean delivered = false;
    
    private Map<JewelTypes, Integer> requirements = new HashMap();
    
    public Leaflet(){
        Random rng = new Random();
        
        //List with random indexes for jewel type
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i=0; i<6; i++) list.add(i);
        Collections.shuffle(list);
        
        for (int i = 0; i< Constants.LEAFLET_NUMBER_OF_ITEMS; i++){
            //Select a random amount of jewels
            int num = rng.nextInt(Constants.MAX_NUMBER_ITEMS_PER_COLOR - 1) + 1;
            
            requirements.put(JewelTypes.values()[list.get(i)], num);
            
            payment += Constants.getPaymentColor(JewelTypes.values()[list.get(i)]);
        }
    }

    public int getPayment() {
        return payment;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Map<JewelTypes, Integer> getRequirements() {
        return requirements;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }
    
    public int getRequiredAmountOf(JewelTypes type){
        return requirements.getOrDefault(type, 0);
    }
    
    public void updateProgress(Bag bag){
        boolean check = true;
        for(JewelTypes required : requirements.keySet()){
            int collected = bag.getTotalCountOf(required);
            if(collected < requirements.get(required))
                check = false;
        }
        completed = check;
    }

    public int getNumberOfItemTypes(){
        return requirements.keySet().size();
    }

    public String toStringFormatted(){
        String ret = " ";
        for (JewelTypes type : requirements.keySet()) {
            String str = type.color().getName();
            ret = ret + str + " ";
            ret = ret + (requirements.get(type)).toString() + " ";
        }

        ret  = ret+payment+" "+completed+" ";
        return ret;

    }
}
