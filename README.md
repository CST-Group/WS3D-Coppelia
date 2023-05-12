# WS3D-Coppelia
Version of WS3D to run with CoppeliaSim robotics simulator

---
## Installation

### Dependencies

#### 1. CoppeliaSim

The WS3D-Coppelia library uses CoppeliaSim v4.4 for running the environment simulation. If you do not have CoppeliaSim installed in your computer, it is possible to get the appropriate file for your operational system on the [official website](https://www.coppeliarobotics.com/previousVersions) and follow the steps 1 through 3 on this [link](https://gist.github.com/h3ct0r/fa5b85eb0ed2c02132734e19128e4218). 
> ⚠️ **Make sure to install version 4.4.0** as newer version may not be compatible with the packaged CoppeliaSim API.

#### 2. Python

The simulation scripts inside CoppeliaSim utilizes python3 and the libraries `cbor`, `zmq` and `lark`. Make sure to have python3 and the libraries installed in your machine.
```
$ sudo apt install python3
$ pip install cbor zmq lark
```

### Gradle

1. Add the JitPack repository to your build file. Add it in your root build.gradle at the end of repositories:

```
	repositories {
			...
			maven { url 'https://jitpack.io' }
	}
```

2. Add the dependency

```
	dependencies {
            ...
            implementation 'com.github.CST-Group:WS3D-Coppelia:0.5'
	}
```

## Test Demo

The WS3D-Coppelia source code comes with a demo simulation built to demonstrate its use with the [Cognitive Systems Toolkit (CST)](https://cst.fee.unicamp.br/).

### Running the demo

1. Open Coppelia Simulator.
2.  Navigate to WS3D-Coppelia source folder
```
$ cd WS3D-Coppelia
```
3. Run the simulation with gradle
```
$ ./gradlew run
```
