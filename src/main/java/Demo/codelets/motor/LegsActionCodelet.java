/*****************************************************************************
 * Copyright 2007-2015 DCA-FEEC-UNICAMP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *    Klaus Raizer, Andre Paraense, Ricardo Ribeiro Gudwin
 *****************************************************************************/

package Demo.codelets.motor;


import WS3DCoppelia.model.Creature;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import java.util.Random;
import java.util.logging.Logger;
import org.json.JSONException;
/**
 *  Legs Action Codelet monitors working storage for instructions and acts on the World accordingly.
 *  
 * @author klaus
 *
 */

public class LegsActionCodelet extends Codelet{

	private Memory legsActionMO;
	private double previousTargetx=0;
	private double previousTargety=0;
	private String previousLegsAction="";
        private Creature creature;
        double old_angle = 0;
        int k=0;
        static Logger log = Logger.getLogger(LegsActionCodelet.class.getCanonicalName());

	public LegsActionCodelet(Creature c) {
                creature = c;
                this.name = "LegsActionCodelet";
	}
	
	@Override
	public void accessMemoryObjects() {
		legsActionMO=(MemoryContainer)this.getInput("LEGS");
	}
	
	@Override
	public void proc() {
            
                String comm = (String) legsActionMO.getI();
                if (comm == null) comm = "";
                Random r = new Random();
		
		if(!comm.equals("") ){
			
			try {
				JSONObject command=new JSONObject(comm);
                                if (command.has("ACTION")) {
                                    int x=0,y=0;
                                    String action=command.getString("ACTION");
                                    if(action.equals("FORAGE")){
                                               if (!comm.equals(previousLegsAction)) { 
                                               //if (!comm.equals(previousLegsAction)) 
                                                    log.info("Sending Forage command to agent");
                                                try {  
                                                    creature.rotate();    
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                               } 
					}
                                    else if(action.equals("GOTO")){
                                        if (!comm.equals(previousLegsAction)) {
                                            double speed=command.getDouble("SPEED");
					    double targetx=command.getDouble("X");
					    double targety=command.getDouble("Y");
					    if (!comm.equals(previousLegsAction))
                                                log.info("Sending move command to agent: ["+targetx+","+targety+"]");
                                            try {
                                                creature.moveTo((float) targetx, (float) targety);
                                            } catch(Exception e) {
                                                e.printStackTrace();
                                            }
					    previousTargetx=targetx;
					    previousTargety=targety;
                                        }
                                        
				    } else {
					log.info("Sending stop command to agent");
                                        try {
                                             creature.stop();
                                        } catch(Exception e) {
                                            e.printStackTrace();
                                        }  
				    }
                                }
			previousLegsAction=comm;
                        k++;	
			} catch (JSONException e) {e.printStackTrace();}
		}
                else {
			previousLegsAction = comm;
                        log.info("Sending stop command to agent");
                        try {
                             //c.moveto(0,0,0);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }  
                    }
	}//end proc

    @Override
    public void calculateActivation() {
        
    }


}
