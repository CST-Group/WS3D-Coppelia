# WS3D-Coppelia
Version of WS3D to run with CoppeliaSim robotics simulator

## Installation

### Gradle

- Step 1. Add the JitPack repository to your build file. Add it in your root build.gradle at the end of repositories:

```
	repositories {
			...
			maven { url 'https://jitpack.io' }
	}
```

- Step 2. Add the dependency

```
	dependencies {
            ...
            implementation 'com.github.CST-Group:WS3D-Coppelia:0.5'
	}
```