#!/bin/bash
/opt/CoppeliaPlayer_4_6/coppeliaSim.sh initial_scene.ttt -gGUIITEMS_20 &> /dev/null &

sleep 10

java -jar WS3D-Coppelia-0.2.jar
