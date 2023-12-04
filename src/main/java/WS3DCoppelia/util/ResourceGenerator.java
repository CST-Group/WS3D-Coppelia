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
 *    Patricia Rocha de Toro, Elisa Calhau de Castro, Ricardo Ribeiro Gudwin
 *****************************************************************************/

package WS3DCoppelia.util;

import WS3DCoppelia.WS3DCoppelia;
import WS3DCoppelia.model.Thing;
import WS3DCoppelia.util.Constants.FoodTypes;
import WS3DCoppelia.util.Constants.JewelTypes;
import WS3DCoppelia.util.Constants.ThingsType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author bruno
 */
public class ResourceGenerator extends Thread {

    private int timeInMinutes;
    private List<Thing> allThings = new ArrayList<Thing>();
    private double width;
    private double height;
    private WS3DCoppelia world;
    private boolean createFood = true, createJewel = true;
    
    public ResourceGenerator(WS3DCoppelia world_, int timeframe, double envWidth, double envHeight) {
        super("ResourcesGenerator");
        if (timeframe == 0) timeInMinutes = Constants.TIMEFRAME;
        else timeInMinutes = timeframe;
        width = envWidth;
        height = envHeight;
        world = world_;
    }

    public void run() {
        while (true) {
            try {
                if(createFood){
                //perishable
                generateFood(FoodTypes.PFOOD);
                //non-perishable
                generateFood(FoodTypes.NPFOOD);
                }
                ///generate jewels
                if(createJewel){
                generateJewel(JewelTypes.RED_JEWEL);
                generateJewel(JewelTypes.GREEN_JEWEL);
                generateJewel(JewelTypes.BLUE_JEWEL);
                generateJewel(JewelTypes.YELLOW_JEWEL);
                generateJewel(JewelTypes.MAGENTA_JEWEL);
                generateJewel(JewelTypes.WHITE_JEWEL);
                }
                
                Thread.sleep(timeInMinutes * 60000);

            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1);
            }

        }

    }

    /**
     * Knuth's algorithm to generate random Poisson distributed numbers
     * @param lambda average rate of success in a Poisson distribution
     * @return random number
     */
    public static int getPoissonRandomNumber(double lambda) {
        int k = 1;
        double p = 1.0;
        Random rd = new Random();

        do {
            k += 1;
            p *= rd.nextDouble();
        } while (p > Math.exp((double) -lambda));
        return k - 1;
    }

    private void generateFood(FoodTypes type) {
        int number = 1;
        Random rdX = new Random();
        Random rdY = new Random();
        double cX, cY;

        switch (type) {
            //perishable
            case PFOOD:
                number = getPoissonRandomNumber(Constants.pFoodLAMBDA);
                break;
            case NPFOOD:
                //non-perishable
                number = getPoissonRandomNumber(Constants.npFoodLAMBDA);
                break;
        }
        for (int i = 0; i < number; i++) {
            do {
                cX = rdX.nextDouble()* (width-0.05f) + 0.05f;
                cY = rdY.nextDouble() * (height-0.05f) + 0.05f;

            } while (world.isOccupied(cX, cY));

            world.createThing(type, cX, cY);
        }

    }

    private void generateJewel(JewelTypes type) {
        int number = 1;
        Random rdX = new Random();
        Random rdY = new Random();
        double cX, cY;
        String pointListStr = "" ;

        switch (type) {
            case RED_JEWEL:
                number = getPoissonRandomNumber(Constants.redLAMBDA);
                break;
            case GREEN_JEWEL:
                number = getPoissonRandomNumber(Constants.greenLAMBDA);
                break;
            case BLUE_JEWEL:
                number = getPoissonRandomNumber(Constants.blueLAMBDA);
                break;
            case YELLOW_JEWEL:
                number = getPoissonRandomNumber(Constants.yellowLAMBDA);
                break;
            case MAGENTA_JEWEL:
                number = getPoissonRandomNumber(Constants.magentaLAMBDA);
                break;
            case WHITE_JEWEL:
                number = getPoissonRandomNumber(Constants.whiteLAMBDA);
                break;
        }
        for (int i = 0; i < number; i++) {
            do {
                cX = rdX.nextDouble()* width;
                cY = rdY.nextDouble() * height;

            } while (world.isOccupied(cX, cY));

                world.createThing(type, cX, cY);
            }

    }

    public void eneableFood() {
        this.createFood = true;
    }

    public void eneableJewel() {
        this.createJewel = true;
    }
    
    public void disableFood() {
        this.createFood = false;
    }

    public void disableJewel() {
        this.createJewel = false;
    }
}
