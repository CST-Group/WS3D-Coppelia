/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.util;

import com.coppeliarobotics.remoteapi.zmq.RemoteAPIObjects;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author bruno
 */
public class Constants {

    public static final List<Float> THING_SIZE = Arrays.asList((float) 0.1, (float) 0.1, (float) 0.1);
    public static final float BRICK_HEIGTH = (float) 0.5;
    
    public enum Color {
        RED(Arrays.asList((float) 0.95, (float) 0.25, (float) 0.25)),
        GREEN(Arrays.asList((float) 0.25, (float) 0.95, (float) 0.25)),
        BLUE(Arrays.asList((float) 0.25, (float) 0.25, (float) 0.95)),
        YELLOW(Arrays.asList((float) 0.95, (float) 0.95, (float) 0.25)),
        MAGENTA(Arrays.asList((float) 0.95, (float) 0.25, (float) 0.95)),
        WHITE(Arrays.asList((float) 0.95, (float) 0.95, (float) 0.95)),
        ORANGE(Arrays.asList((float) 0.95, (float) 0.65, (float) 0.25)),
        GREY(Arrays.asList((float) 0.5, (float) 0.5, (float) 0.5)),
        BROWN(Arrays.asList((float) 0.6, (float) 0.4, (float) 0.25)),
        AGENT_YELLOW(Arrays.asList((float) 1, (float) 0.8, (float) 0.25)),
        AGENT_GREEN(Arrays.asList((float) 0.55, (float) 0.8, (float) 0.15)),
        AGENT_MAGENTA(Arrays.asList((float) 0.4, (float) 0.3, (float) 0.57)),
        AGENT_RED(Arrays.asList((float) 0.98, (float) 0.25, (float) 0.27));

        private final List<Float> rgb;
        private final List<Float> hls;
        Color(List<Float> rgb){
            this.rgb = rgb;
            //Code from https://github.com/tips4java/tips4java
            float r = rgb.get(0);
            float g = rgb.get(1);
            float b = rgb.get(2);

            //	Minimum and Maximum RGB values are used in the HSL calculations
            float min = Math.min(r, Math.min(g, b));
            float max = Math.max(r, Math.max(g, b));

            //  Calculate the Hue
            float h = 0;

            if (max == min)
                h = 0;
            else if (max == r)
                h = ((60 * (g - b) / (max - min)) + 360) % 360;
            else if (max == g)
                h = (60 * (b - r) / (max - min)) + 120;
            else if (max == b)
                h = (60 * (r - g) / (max - min)) + 240;

            //  Calculate the Luminance
            float l = (max + min) / 2;

            //  Calculate the Saturation
            float s = 0;

            if (max == min)
                s = 0;
            else if (l <= .5f)
                s = (max - min) / (max + min);
            else
                s = (max - min) / (2 - max - min);

            hls = Arrays.asList(h/360, l, s);
        }
        public List<Float> rgb() { return rgb;}
        public List<Float> hls() { return hls;}
    }
    
    public static String BASE_SCRIPT = "#python\n"
            + "\n"
            + "from math import sqrt, floor\n"
            + "import colorsys\n"
            + "\n"
            + "lin_vel = 0.02 # m/s\n"
            + "ang_vel = 0.02  # rad/s\n"
            + "agent_handle = %d\n"
            + "target_handle = %d\n"
            + "fuel_id = 'fuel_%d'\n"
            + "ui = '%s'\n"
            + "\n"
            + "def sysCall_init():\n"
            + "    # do some initialization here\n"
            + "    sim.setFloatSignal(fuel_id, 1000)\n"
            + "\n"
            + "def sysCall_actuation():\n"
            + "    # put your actuation code here\n"
            + "    #agent_handle = sim.getObjectFromUid(agent_uid)\n"
            + "    #target_handle = sim.getObjectFromUid(target_uid)\n"
            + "    \n"
            + "    agent_err_pos = sim.getObjectPosition(agent_handle, target_handle)#[:-1] # only x, y position\n"
            + "    agent_err_ori = sim.getObjectOrientation(agent_handle, target_handle)\n"
            + "    \n"
            + "    fuel = sim.getFloatSignal(fuel_id)\n"
            + "    \n"
            + "    if fuel > 0:\n"
            + "        if abs(agent_err_ori[-1]) > ang_vel: # only angle in relation to z-axis\n"
            + "            ang_dir = 1 - 2 * (agent_err_ori[-1] >= 0)\n"
            + "            agent_err_ori[-1] = agent_err_ori[-1] + ang_dir * ang_vel\n"
            + "            sim.setObjectOrientation(agent_handle, target_handle, agent_err_ori)\n"
            + "        else:\n"
            + "            dist = sqrt(agent_err_pos[0]**2 + agent_err_pos[1]**2)\n"
            + "            if dist > lin_vel:\n"
            + "                dir_vec = [lin_vel * i / dist for i in agent_err_pos[:-1]]\n"
            + "                agent_err_pos[0] = agent_err_pos[0] - dir_vec[0]\n"
            + "                agent_err_pos[1] = agent_err_pos[1] - dir_vec[1]\n"
            + "                sim.setObjectPosition(agent_handle, target_handle, agent_err_pos)\n"
            + "            \n"
            + "    \n"
            + "    sim.setFloatSignal(fuel_id, fuel - 0.1)\n"
            + "\n"
            + "def sysCall_sensing():\n"
            + "    # put your sensing code here\n"
            + "    pass\n"
            + "\n"
            + "def sysCall_cleanup():\n"
            + "    # do some clean-up here\n"
            + "    pass\n"
            + "\n"
            + "def status(score, leaflet_table, baseColor):\n"
            + "    #agent_handle = sim.getObjectFromUid(agent_uid)\n"
            + "    pos = sim.getObjectPosition(agent_handle, sim.handle_world)\n"
            + "    ori = sim.getObjectOrientation(agent_handle, sim.handle_world)\n"
            + "    fuel = sim.getFloatSignal(fuel_id)\n"
            + "    new_color = update_color(baseColor, fuel)\n"
            + "    visionHandle = sim.getObjectChild(agent_handle, 1)\n"
            + "    list = sim.readVisionSensor(visionHandle)[1]\n"
            + "    list = [floor(k) for k in list]\n"
            + "    simUI.setLabelText(ui, 100, 'x: {:.2f} - y:{:.2f}'.format(pos[0],pos[1]))\n"
            + "    simUI.setLabelText(ui, 110, '{:.3f}'.format(ori[2]))\n"
            + "    simUI.setLabelText(ui, 120, '{:.2f}'.format(fuel))\n"
            + "    simUI.setLabelText(ui, 130, '{}'.format(score))\n"
            + "    for i in range(len(leaflet_table)):\n"
            + "        for j in range(len(leaflet_table[i])):\n"
            + "            simUI.setItem(ui, 200, j, i, str(leaflet_table[i][j]))\n"
            + "    \n"
            + "    \n"
            + "    return pos, ori, fuel, list, new_color\n"
            + "    \n"
            + "def update_color(baseColor, fuel):\n"
            + "    updateColor = baseColor\n"
            + "    updateColor[1] = baseColor[1] * fuel/1000.0\n"
            + "    updateColor = list(colorsys.hls_to_rgb(*updateColor))\n"
            + "    renderHandle = sim.getObjectsInTree(agent_handle, sim.handle_all, 1)\n"
            + "    sim.setObjectColor(renderHandle[0], 0, sim.colorcomponent_ambient_diffuse, updateColor) \n"
            + "    return updateColor\n"
            + "    \n"
            + "def move_agent(targetPos, targetOri):\n"
            + "    sim.setObjectOrientation(target_handle, sim.handle_world, targetOri)\n"
            + "    #sim.setObjectOrientation(agent_handle, sim.handle_world, targetOri)\n"
            + "    sim.setObjectPosition(target_handle, sim.handle_world, targetPos)\n"
            + "    return 1\n"
            + "\n"
            + "def rotate_agent():\n"
            + "    targetOri = sim.getObjectOrientation(target_handle, agent_handle)\n"
            + "    targetOri[2] = 3\n"
            + "    sim.setObjectOrientation(target_handle, agent_handle, targetOri)\n"
            + "    targetPos = [0,0,0]\n"
            + "    sim.setObjectPosition(target_handle, agent_handle, targetPos)\n"
            + "    return 1\n"
            + "\n"
            + "def stop_agent():\n"
            + "    targetPos = [0,0,0]\n"
            + "    sim.setObjectPosition(target_handle, agent_handle, targetPos)\n"
            + "    euler = sim.getObjectOrientation(target_handle, agent_handle)\n"
            + "    euler[2] = 0\n"
            + "    sim.setObjectOrientation(target_handle, agent_handle, euler)\n"
            + "    return 1\n"
            + "\n"
            + "def increase_fuel(energy):\n"
            + "    fuel = sim.getFloatSignal(fuel_id)\n"
            + "    new_fuel = 1000 if (fuel + energy) > 1000 else (fuel + energy)\n"
            + "    sim.setFloatSignal(fuel_id, new_fuel)\n"
            + "    return 1\n";
   
    public static double THING_OCCUPANCY_RADIUS = 0.15;
    public static double AGENT_OCCUPANCY_RADIUS = 0.25;
    
    /**
     * Resources Generator package constants
     */
    public static final int TIMEFRAME = 3; //default in minutes
    ////////Poisson distribution
    //the average rate of generation of each kind of crystal:
    public static final double redLAMBDA = 1;
    public static final double greenLAMBDA = 0.4;
    public static final double blueLAMBDA = 0.5;
    public static final double yellowLAMBDA = 0.7;
    public static final double magentaLAMBDA = 0.3;
    public static final double whiteLAMBDA = 0.2;
    public static final double pFoodLAMBDA = 1;
    public static final double npFoodLAMBDA = 0.7;
    public static double SECURITY = 30; //empiric
    
    //Leaflet
    public static int LEAFLET_NUMBER_OF_ITEMS = 3;
    public static int MAX_NUMBER_ITEMS_PER_COLOR = 3;
    public static int NUM_LEAFLET_PER_AGENTS = 3;

    /**
     * Interface to define an object type. It requires that each object type has defined a
     * shape (as a primitive form CoppeliaSim), a color (as a RGB values) and a name.
     *
     * @see FoodTypes
     * @see BrickTypes
     * @see JewelTypes
     */
    public interface ThingsType {
        public int shape();
        public Color color();
        public String typeName();
    }


    public enum FoodTypes implements ThingsType{
        /**
         * Perishable food. Red sphere.
         */
        PFOOD(RemoteAPIObjects._sim.primitiveshape_spheroid, Color.RED, 300, "P_Food"),
        /**
         * Non-perishable food. Brown sphere.
         */
        NPFOOD(RemoteAPIObjects._sim.primitiveshape_spheroid, Color.ORANGE, 150, "NP_Food");
        
        private final int shape;
        private final Color color;
        private final String type_name;
        private final float energy;
        
        FoodTypes(int shape, Color color, float energy, String name){
            this.shape = shape;
            this.color = color;
            this.type_name = name;
            this.energy = energy;
        }
        
        @Override
        public int shape() { return shape; }
        @Override
        public Color color() { return color; }
        @Override
        public String typeName() { return type_name; }
        public float energy() { return energy; }
    }
    
    public enum JewelTypes implements ThingsType{
        RED_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, Color.RED, "Red_Jewel"),
        GREEN_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, Color.GREEN, "Green_Jewel"),
        BLUE_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, Color.BLUE, "Blue_Jewel"),
        YELLOW_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, Color.YELLOW, "Yellow_Jewel"),
        MAGENTA_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, Color.MAGENTA, "Magenta_Jewel"),
        WHITE_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, Color.WHITE, "White_Jewel");
        
        private final int shape;
        private final Color color;
        private final String type_name;
        
        JewelTypes(int shape, Color color, String name){
            this.shape = shape;
            this.color = color;
            this.type_name = name;
        }
        
        @Override
        public int shape() { return shape; }
        @Override
        public Color color() { return color; }
        @Override
        public String typeName() { return type_name; }
    }
    
    public enum BrickTypes implements ThingsType{
        RED_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.RED, "Red_Brick"),
        BLUE_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.BLUE, "Blue_Brick"),
        GREEN_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.GREEN, "Green_Brick"),
        YELLOW_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.YELLOW, "Yellow_Brick"),
        MAGENTA_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.MAGENTA, "Magenta_Brick"),
        WHITE_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.WHITE, "White_Brick"),
        ORANGE_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.ORANGE, "Orange_Brick"),
        GREY_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.GREY, "Grey_Brick"),
        BROWN_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.BROWN, "Brown_Brick");

        private final int shape;
        private final Color color;
        private final String type_name;
        
        BrickTypes(int shape, Color color, String name){
            this.shape = shape;
            this.color = color;
            this.type_name = name;
        }
        
        @Override
        public int shape() { return shape; }
        @Override
        public Color color() { return color; }
        @Override
        public String typeName() { return type_name; }
    }
    
    public static int getPaymentColor(JewelTypes type){
        switch(type) {
            case RED_JEWEL:
                return 10;
            case GREEN_JEWEL:
                return 8;
            case BLUE_JEWEL:
                return 6;
            case MAGENTA_JEWEL:
                return 4;
            case YELLOW_JEWEL:
                return 2;
            case WHITE_JEWEL:
                return 1;
            default:
                return 0;
        }
    }
}
