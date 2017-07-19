GETTING THE PROJECTS

1. Clone the project from:
<insert our new github link here>
2. Open up your preferred Java IDE (e.g, eclipse, IntelliJ,)
3. Go to File --> Import --> Maven --> Existing Maven Projects, and select the hyperledger folder
4. Either from the command line or the IDE, run the command:
mvn clean install -DkipITs=false -Dmaven.test.failure.ignore=false

SETTING UP DOCKER

1. Download docker from: 
https://www.docker.com/community-edition
2. In git bash, run:
curl -sSL https://goo.gl/iX9dek | bash
to download all the neccessary images
3. *FIRST TIME DOCKER SETUP*
FOR MAC OSX
Docker >> Preferences >> File Sharing
add /code to the list

FOR Windows
Right Click Docker from the bottom right side of the startup bar, click on settings >> Shared Drives and turn on C (or your preferred network drive)


RUNNING FABRIC FROM THE UI

1. Open git bash
2. cd to the scripts folder
3. Run ./fabric.sh up
4. In eclipse, find FabricApplication under src/main/java/org/non/api/web/app, and run it as Java Application
    4a. If it is setting up the chaincode containers, (first time setup or after a fabric restart), it will take a while to start
    4b. If you see the message: INFO  [2017-07-06 15:06:43,195] org.eclipse.jetty.server.Server: Started @9030ms, it has worked
5. Open up your preferred browser and go to localhost:8080/static/sessionProfile.html
    a. Current settings are: 
        i. Organization must be:       		 GTN
        ii. User Name must be:           	 User1
        iii. Channel Name must be:       	 ch1
6. Start by creating trading partners
    a. Name must be not be blank!
7. Query trading partners by name

TROUBLESHOOTING

1. When running the code via eclipse, if you get an "unable to enroll admin" error, the server has not started properly. Restart the server in bash with the command 
./fabric.sh restart

2. When running the code in eclipse you get a "Error while registering the user org.hyperledger.fabric.sdkintegration.SampleUser" restart the server with the command ./fabric.sh restart

3. Changes were made to the chaincode and are not being shown as you run the application. Run the command
./fabric.sh restart
