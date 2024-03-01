/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.util;

import com.coppeliarobotics.remoteapi.zmq.RemoteAPIObjects;
import org.checkerframework.checker.units.qual.C;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author bruno
 */
public class Constants {

    public static final List<Double> THING_SIZE = Arrays.asList( 0.1,  0.1,  0.1);
    public static final double BRICK_HEIGTH =  0.5;
    public static final Double DELIVERY_SPOT_HEIGTH = 0.6;

    public enum Color {
        RED(Arrays.asList( 0.95,  0.25,  0.25), "Red"),
        GREEN(Arrays.asList( 0.25,  0.95,  0.25), "Green"),
        BLUE(Arrays.asList( 0.25,  0.25,  0.95), "Blue"),
        YELLOW(Arrays.asList( 0.95,  0.95,  0.25), "Yellow"),
        MAGENTA(Arrays.asList( 0.95,  0.25,  0.95), "Magenta"),
        WHITE(Arrays.asList( 0.95,  0.95,  0.95), "White"),
        ORANGE(Arrays.asList( 0.95,  0.65,  0.25), "Orange"),
        GREY(Arrays.asList( 0.5,  0.5,  0.5), "Grey"),
        BROWN(Arrays.asList( 0.6,  0.4,  0.25), "Brown"),
        AGENT_YELLOW(Arrays.asList( 1.0,  0.8,  0.25), "Yellow"),
        AGENT_GREEN(Arrays.asList( 0.55,  0.8,  0.15), "Green"),
        AGENT_MAGENTA(Arrays.asList( 0.4,  0.3,  0.57), "Magenta"),
        AGENT_RED(Arrays.asList( 0.98,  0.25,  0.27), "Red"),
        DS_YELLOW(Arrays.asList( 0.90,  0.80,  0.15), "Yellow");

        private final List<Double> rgb;
        private final List<Double> hls;
        private final String name;
        Color(List<Double> rgb, String name){
            this.name = name;
            this.rgb = rgb;
            //Code from https://github.com/tips4java/tips4java
            double r = rgb.get(0);
            double g = rgb.get(1);
            double b = rgb.get(2);

            //	Minimum and Maximum RGB values are used in the HSL calculations
            double min = Math.min(r, Math.min(g, b));
            double max = Math.max(r, Math.max(g, b));

            //  Calculate the Hue
            double h = 0;

            if (max == min)
                h = 0;
            else if (max == r)
                h = ((60 * (g - b) / (max - min)) + 360) % 360;
            else if (max == g)
                h = (60 * (b - r) / (max - min)) + 120;
            else if (max == b)
                h = (60 * (r - g) / (max - min)) + 240;

            //  Calculate the Luminance
            double l = (max + min) / 2;

            //  Calculate the Saturation
            double s = 0;

            if (max == min)
                s = 0;
            else if (l <= .5f)
                s = (max - min) / (max + min);
            else
                s = (max - min) / (2 - max - min);

            hls = Arrays.asList(h/360, l, s);
        }
        public List<Double> rgb() { return rgb;}
        public List<Double> hls() { return hls;}
        public String getName(){ return name;}
    }

    public static String BASE_SCRIPT = "#python\n"
            + "\n"
            + "from math import sqrt, floor\n"
            + "import colorsys\n"
            + "\n"
            + "agent_handle = %d\n"
            + "target_handle = %d\n"
            + "fuel_id = 'fuel_%d'\n"
            + "is_npc= %s\n"
            + "ui = '%s'\n"
            + "\n"
            + "def sysCall_init():\n"
            + "    # do some initialization here\n"
            + "    sim = require('sim')\n"
            + "    simUI = require('simUI')\n"
            + "    sim.setFloatSignal(fuel_id, 1000.1)\n" //.1 so the fuel value returned by NPCs is identified as Float
            + "    sim.setFloatSignal('lin_vel', 0.02)\n"
            + "    sim.setFloatSignal('ang_vel', 0.02)\n"
            + "    sim.setFloatSignal('started', -1)\n"
            + "\n"
            + "def sysCall_actuation():\n"
            + "    # put your actuation code here\n"
            + "    #agent_handle = sim.getObjectFromUid(agent_uid)\n"
            + "    #target_handle = sim.getObjectFromUid(target_uid)\n"
            + "    \n"
            + "    if sim.getFloatSignal('started') > 0:\n"
            + "        agent_err_pos = sim.getObjectPosition(agent_handle, target_handle)#[:-1] # only x, y position\n"
            + "        agent_err_ori = sim.getObjectOrientation(agent_handle, target_handle)\n"
            + "        \n"
            + "        fuel = sim.getFloatSignal(fuel_id)\n"
            + "        lin_vel = sim.getFloatSignal('lin_vel')\n"
            + "        ang_vel = sim.getFloatSignal('ang_vel')\n"
            + "        print(f\"{lin_vel} {ang_vel}\")\n"
            + "        if fuel > 0:\n"
            + "            if abs(agent_err_ori[-1]) > ang_vel: # only angle in relation to z-axis\n"
            + "                ang_dir = 1 - 2 * (agent_err_ori[-1] >= 0)\n"
            + "                agent_err_ori[-1] = agent_err_ori[-1] + ang_dir * ang_vel\n"
            + "                sim.setObjectOrientation(agent_handle, target_handle, agent_err_ori)\n"
            + "            else:\n"
            + "                dist = sqrt(agent_err_pos[0]**2 + agent_err_pos[1]**2)\n"
            + "                if dist > lin_vel:\n"
            + "                    dir_vec = [lin_vel * i / dist for i in agent_err_pos[:-1]]\n"
            + "                    agent_err_pos[0] = agent_err_pos[0] - dir_vec[0]\n"
            + "                    agent_err_pos[1] = agent_err_pos[1] - dir_vec[1]\n"
            + "                    sim.setObjectPosition(agent_handle, target_handle, agent_err_pos)\n"
            + "                elif dist > 0:\n"
            + "                    sim.setObjectPosition(agent_handle, target_handle, [0.0,0.0,agent_err_pos[-1]])\n"
            + "                \n"
            + "        \n"
            + "        if not is_npc:\n"
            + "            fuel = max(0, fuel - 0.1)\n"
            + "            sim.setFloatSignal(fuel_id, fuel)\n"
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
            + "    fuel = 1000.1\n"
            + "    list = []\n"
            + "    new_color = baseColor\n"
            + "    visionHandle = sim.getObjectChild(agent_handle, 1)\n"
            + "    list = sim.readVisionSensor(visionHandle)[1]\n"
            + "    list = [floor(k) for k in list]\n"
            + "    if not is_npc:\n"
            + "        fuel = sim.getFloatSignal(fuel_id)\n"
            + "        new_color = update_color(baseColor, fuel)\n"
            + "        simUI.setLabelText(ui, 100, 'x: {:.2f} - y:{:.2f}'.format(pos[0],pos[1]))\n"
            + "        simUI.setLabelText(ui, 110, '{:.3f}'.format(ori[2]))\n"
            + "        simUI.setLabelText(ui, 120, '{:.2f}'.format(fuel))\n"
            + "        simUI.setLabelText(ui, 130, '{}'.format(score))\n"
            + "        for i in range(len(leaflet_table)):\n"
            + "            for j in range(len(leaflet_table[i])):\n"
            + "                simUI.setItem(ui, 200, j, i, str(leaflet_table[i][j]))\n"
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
            + "def move_agent(targetPos, targetOri, vel):\n"
            + "    sim.setFloatSignal('ang_vel', 0.02)\n"
            + "    sim.setFloatSignal('lin_vel', vel)\n"
            + "    sim.setObjectOrientation(target_handle, sim.handle_world, targetOri)\n"
            + "    sim.setObjectPosition(target_handle, sim.handle_world, targetPos)\n"
            + "    return 1\n"
            + "\n"
            + "def move_forward(vel):\n"
            + "    sim.setFloatSignal('lin_vel', vel)\n"
            + "    sim.setObjectOrientation(target_handle, agent_handle, [0.0,0.0,0.0])\n"
            + "    sim.setObjectPosition(target_handle, agent_handle, [1.0,0.0,0.0])\n"
            + "    return 1\n"
            + "\n"
            + "def rotate_agent(dir, vel):\n"
            + "    sim.setFloatSignal('ang_vel', vel)\n"
            + "    targetOri = sim.getObjectOrientation(target_handle, agent_handle)\n"
            + "    if dir == 1:\n"
            + "        targetOri[2] = -3\n"
            + "    else:\n"
            + "        targetOri[2] = 3\n"
            + "    sim.setObjectOrientation(target_handle, agent_handle, targetOri)\n"
            + "    targetPos = [0,0,0]\n"
            + "    sim.setObjectPosition(target_handle, agent_handle, targetPos)\n"
            + "    return 1\n"
            + "\n"
            + "def start_agent():\n"
            + "    sim.setFloatSignal('started', 1)\n"
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
            + "    new_fuel = 1000.1 if (fuel + energy) > 1000 else (fuel + energy)\n"
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
        public int socketCategory();
    }

    public enum DeliverySpotType implements ThingsType{
        DELIVERY_SPOT();

        @Override
        public int shape() { return RemoteAPIObjects._sim.primitiveshape_capsule;}

        @Override
        public Color color() { return Color.DS_YELLOW;}

        @Override
        public String typeName() { return "DeliverySpot";}

        @Override
        public int socketCategory() { return 4;}
    }

    public enum FoodTypes implements ThingsType{
        /**
         * Perishable food. Red sphere.
         */
        PFOOD(RemoteAPIObjects._sim.primitiveshape_spheroid, Color.RED, 300, "PFood", 21),
        /**
         * Non-perishable food. Brown sphere.
         */
        NPFOOD(RemoteAPIObjects._sim.primitiveshape_spheroid, Color.ORANGE, 150, "NPFood", 22);

        private final int shape;
        private final Color color;
        private final String type_name;
        private final double energy;
        private final int socketCategory;

        FoodTypes(int shape, Color color, double energy, String name, int socketCategory){
            this.shape = shape;
            this.color = color;
            this.type_name = name;
            this.energy = energy;
            this.socketCategory = socketCategory;
        }

        @Override
        public int shape() { return shape; }
        @Override
        public Color color() { return color; }
        @Override
        public String typeName() { return type_name; }
        public double energy() { return energy; }
        public int socketCategory() { return socketCategory; }
    }

    public enum JewelTypes implements ThingsType{
        RED_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, Color.RED),
        GREEN_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, Color.GREEN),
        BLUE_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, Color.BLUE),
        YELLOW_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, Color.YELLOW),
        MAGENTA_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, Color.MAGENTA),
        WHITE_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, Color.WHITE);

        private final int shape;
        private final Color color;

        JewelTypes(int shape, Color color){
            this.shape = shape;
            this.color = color;
        }

        @Override
        public int shape() { return shape; }
        @Override
        public Color color() { return color; }
        @Override
        public String typeName() { return "Jewel"; }
        @Override
        public int socketCategory() { return 3; }
    }

    public enum BrickTypes implements ThingsType{
        RED_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.RED),
        BLUE_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.BLUE),
        GREEN_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.GREEN),
        YELLOW_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.YELLOW),
        MAGENTA_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.MAGENTA),
        WHITE_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.WHITE),
        ORANGE_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.ORANGE),
        GREY_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.GREY),
        BROWN_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, Color.BROWN);

        private final int shape;
        private final Color color;

        BrickTypes(int shape, Color color){
            this.shape = shape;
            this.color = color;
        }

        @Override
        public int shape() { return shape; }
        @Override
        public Color color() { return color; }
        @Override
        public String typeName() { return "Brick"; }
        @Override
        public int socketCategory() { return 1; }
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

    public static final int PORT = 4011;
    public static final String ERROR_CODE = "@@@";
}
