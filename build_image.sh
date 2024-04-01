wget -nc --show-progress https://downloads.coppeliarobotics.com/V4_6_0_rev18/CoppeliaSim_Player_V4_6_0_rev18_Ubuntu22_04.tar.xz -P ./coppelia

./gradlew jar

docker build -t brgsil/ws3d-coppelia .
