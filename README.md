
### SheetMusic Consortium Harvester digidev.library.ucla.edu lit347v

#### Make a copy of web.xml on digidev tomcat7 under oai/, use this when deploying on digidev 

#### More customization done for sheetmusic harvester 
https://github.com/UCLALibrary/sheetmusic-oai/blob/master/dlese-tools-project/src/org/dlese/dpc/oai/harvester/Harvester.java
line 1219

For data normalization and mods output

Instructions for building the jOAI software v@VERSION@ from source.

Quick instructions: 
--------------------------------------------

This software may be built easily using Ant. If you are already using Ant
and Tomcat, building the software is a three step process:
1. Obtain the two code directories joai-project and dlese-tools-project from SourceForge CVS.
2. Set the ant property 'catalina.home' to point to your Tomcat installation.
3. From the joai-project directory, execute the 'deploy' target.

This will build the software and place it in a directory named 'oai'
inside your Tomcat webapps directory.


Detailed instructions:
--------------------------------------------

To build the jOAI software from source you must have the following:
  
1. joai-project - the jOAI software source code module
2. dlese-tools-project - the Java libraries source code module
3. Tomcat version 7.x (available at http://tomcat.apache.org/)
4. Ant (available at http://ant.apache.org/)
5. Java Platform, Standard Edition v6


 
Build instructions:

These instructions assume you will be working in a UNIX command-line environment.
The software can also be built on Windows but specific instructions are not 
provided here.

1. Clone this repo.

2. Obtain and install all the required tools (Java, Ant, Tomcat).

3. Place the joai-project and dlese-tools-project directories into a single directory.
For example:
 # cd ~/sheetmusic-oai
 # ls 
   dlese-tools-project joai-project
	  
4. cd into the joai-project directory.
  # cd joai-project

5. Create a file named build.properties in the joai-project directory 
(alternatively this can be placed in your home directory).

6. Edit the build.properties file and set the poperty catalina.home to
point to your intallation of Tomcat, for example:
catalina.home = /home/username/dev/apache-tomcat-7.x

See the build.xml file located in joai-project for information on the 
properties settings.

7. Execute the Ant deploy command.
  # ant deploy
You should see a number of messages in your terminal. Once complete
you should see a message like:
    BUILD SUCCESSFUL
    Total time: 36 seconds

8. The software will be built into a directory named oai inside your
Tomcat webapps directory. After (re)starting Tomcat you will be able to
access the software at the URL http://localhost:8080/oai/
(substitute localhost with your domain name if appropriate).

