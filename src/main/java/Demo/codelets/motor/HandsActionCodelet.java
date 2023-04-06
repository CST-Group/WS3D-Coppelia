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


import Demo.Environment;
import static Demo.codelets.motor.LegsActionCodelet.log;
import WS3DCoppelia.model.Agent;
import WS3DCoppelia.model.Thing;
import org.json.JSONException;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 *  Hands Action Codelet monitors working storage for instructions and acts on the World accordingly.
 *  
 * @author klaus
 *
 */


public class HandsActionCodelet extends Codelet{

	private Memory handsMO;
	private String previousHandsCommand="";
        private Agent creature;
        private Random r = new Random();
        static Logger log = Logger.getLogger(HandsActionCodelet.class.getCanonicalName());

	public HandsActionCodelet(Agent c) {
                creature = c;
                this.name = "HandsActionCodelet";
	}
	
        @Override
	public void accessMemoryObjects() {
		handsMO=(MemoryObject)this.getInput("HANDS");
	}
	public void proc() {
            
                List<Object> action = (List<Object>) handsMO.getI();
                if(action.size() > 0){
                    String command = (String) action.get(0);
                    if(!command.equals("NOTHING") && (!command.equals(previousHandsCommand))){
                        if(command.equals("EATIT")){
                            Thing food = (Thing) action.get(1);
                            creature.eatIt(food);	
                            log.info("Sending Eat It command to agent");
                        }

                    }
                    //System.out.println("OK_hands");
                    List<Object> previousAction = (List<Object>) handsMO.getI();
                    previousHandsCommand = (String) action.get(0);
                }
	}//end proc

    @Override
    public void calculateActivation() {
        
    }


}
