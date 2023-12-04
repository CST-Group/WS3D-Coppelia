/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Demo;

/**
 *
 * @author bruno
 */

import WS3DCoppelia.WS3DCoppelia;
import WS3DCoppelia.model.Creature;
import WS3DCoppelia.util.ResourceGenerator;
import co.nstant.in.cbor.CborException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Environment {
    
    public WS3DCoppelia world;
    public Creature creature;
    private ResourceGenerator rg;
    
    public Environment(){
        world = new WS3DCoppelia(4,4);
        creature = world.createAgent(1,1);
        world.createAgent(1.5F,1.5F);
        
        rg = new ResourceGenerator(world, 1, world.getWorldWidth(), world.getWorldHeigth());
        //rg.disableJewel();
        rg.start();
        
        try {
            world.startSimulation();
        } catch (IOException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CborException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
