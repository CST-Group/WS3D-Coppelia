#!/bin/bash
java -jar WS3D-Coppelia-0.2.jar &

pid=$!

/opt/CoppeliaPlayer_4_6/coppeliaSim.sh initial_scene.ttt -gGUIITEMS_20 &> /dev/null

sudo kill $pid
