xhost +

XAUTH=`xauth info | grep file | awk '{print $3}'`

sudo docker run --rm -it --name coppelia-sim \
    -e DISPLAY \
    --net=host \
    --device /dev/snd \
    --privileged \
    -v $XAUTH:/root/.Xauthority \
    -p 4011:4011 \
    brgsil/ws3d-coppelia

xhost -
