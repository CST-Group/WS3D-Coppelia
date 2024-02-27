# WS3D with CoppeliaSim
Version of WS3D to run with CoppeliaSim robotics simulator

## 🚀 Pre-requirements

To run the simulator, Docker Engine must be installed on your system. If you haven't yet installed the Docker Engine, follow the installation instructions below:

### Installing the Docker Engine

#### **Linux**

To run the container, you need to install the Docker Engine. Follow the steps on the official [Docker] website (https://docs.docker.com/engine/install/ubuntu/#install-using-the-repository).

> ⚠️ Make sure you follow the instructions for installing Docker Engine by apt. Docker Desktop for Linux will not allow you to use the graphical interfaces in the examples.

#### **Windows**

Download the installer and follow the installation steps on the [Docker] website (https://docs.docker.com/desktop/install/windows-install/).

> ⚠️ Make sure that WSL 2 is installed according to the instructions in the [System Requirements](https://docs.docker.com/desktop/install/windows-install/#system-requirements).

For use on Windows, you will also need to install [VcXsrv](https://sourceforge.net/projects/vcxsrv/)

#### **MacOS**

Download the installer for your hardware and follow the installation steps on the [Docker] website(https://docs.docker.com/desktop/install/mac-install/)

For use on Mac you will also need to install [XQuatz](https://www.xquartz.org/)

## 🏃‍♀️ Running the Docker Container

### **Linux**

Once you have the Docker Engine installed, the Docker container for CoppeliaSim can be executed with the script provided in this repository:

```bash
./docker.sh
```

The `docker.sh` script will start the container and initialize CoppeliaSim with XServer and the Java controler application with socket communication for [WS3DProxy](https://github.com/CST-Group/WS3DProxy).

### **Windows**

1. Start XServer in Windows, following the steps below:
    1. Run the XLaunch.exe application
    2. Select the 'Multiple windows' option, set the 'Display number' to 0 and click 'Next'
    3. Select the 'Start no client' option and click 'Next'
    4. Check the 'Disable access control' option, click 'Next' and start XServer by clicking 'Finish'.
3. Start Docker Desktop
4. Find the WSL connection ip, according to the steps below:
    1. Start the Command Prompt and run the `ipconfig` command
    2. Find the connection adapter with reference to the WSL. This is usually the Ethernet adapter and will indicate `(WSL)` in the title.
    3. Copy the IPv4 address
5. Still at the Command Prompt, start the Docker container with the following command, inserting the ip you copied earlier into the `<MY-WSL-IP>` tag
```
docker run --rm -it --privileged -v \\wsl$\Ubuntu\mnt\wslg:/mnt/wslg/ -e DISPLAY=<MY-WSL-IP>:0 -p 4011:4011 brgsil/ws3d-coppelia
```
