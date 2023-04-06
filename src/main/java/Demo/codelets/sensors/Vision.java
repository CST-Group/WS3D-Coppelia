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

package Demo.codelets.sensors;

import Demo.Environment;
import WS3DCoppelia.model.Agent;
import WS3DCoppelia.model.Thing;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.List;

/**
 * Vision codelet is responsible for getting vision information 
 * from the environment. It returns all objects in the visual field
 *  an its properties.
 *  
 *  @author klaus
 */
//TODO How about implementing getvs 0 in Client?
public class Vision extends Codelet{
    
	private Memory visionMO;
        private Agent creature;


	public Vision(Agent c) {
            creature = c;		
            this.name = "Vision";
	}

	@Override
	public void accessMemoryObjects() {
		visionMO=(MemoryObject)this.getOutput("VISION");
	}

	@Override
	public void proc() {
             
             synchronized (visionMO) {
                List<Thing> lt = creature.getThingsInVision();
                visionMO.setI(lt);
             }
	}//end proc()

	@Override
	public void calculateActivation() {

	}



}// class end		





