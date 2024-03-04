FROM ubuntu:22.04

RUN apt update -y && apt upgrade -y && \
	export DEBIAN_FRONTEND=noninteractive && \
    apt-get install -y --no-install-recommends \
        vim tar xz-utils \
        libx11-6 libxcb1 libxau6 libgl1-mesa-dev \
        xvfb dbus-x11 x11-utils libxkbcommon-x11-0 \
        libavcodec-dev libavformat-dev libswscale-dev \
        python3.10 python3-pip libraw1394-11 libmpfr6 \
        libusb-1.0-0 \
        && \
    apt-get autoclean -y && apt-get autoremove -y && apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN apt update && apt install openjdk-11-jdk -y

RUN mkdir -p /shared /opt

RUN pip3 install pyzmq cbor

COPY ./coppelia/CoppeliaPlayer_4_6_rev8.tar.xz /opt/
#COPY ./coppelia/usrset.txt /root/.CoppeliaSim/
RUN tar -xf /opt/CoppeliaPlayer_4_6_rev8.tar.xz -C /opt && \
    rm /opt/CoppeliaPlayer_4_6_rev8.tar.xz

ENV COPPELIASIM_ROOT_DIR=/opt/CoppeliaPlayer_4_6
ENV LD_LIBRARY_PATH=$COPPELIASIM_ROOT_DIR:$LD_LIBRARY_PATH
ENV PATH=$COPPELIASIM_ROOT_DIR:$PATH

#RUN echo '#!/bin/bash\ncd $COPPELIASIM_ROOT_DIR\n/usr/bin/xvfb-run --server-args "-ac -screen 0, 1024x1024x24" coppeliaSim "$@"' > /entrypoint && chmod a+x /entrypoint

# Use following instead to open an application window via an X server:
#RUN echo '#!/bin/bash\ncd $COPPELIASIM_ROOT_DIR\n./coppeliaSim "$@"' > /entrypoint && chmod a+x /entrypoint

EXPOSE 23000-23500
#ENTRYPOINT ["/entrypoint"]

WORKDIR /ws3d_coppelia

COPY ./build/libs/WS3D-Coppelia-0.2.jar .
COPY ./coppelia/exec.sh .
COPY ./coppelia/initial_scene.ttt .
RUN chmod +x exec.sh
CMD ./exec.sh
