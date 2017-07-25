# NETWORK OF NETWORKS v1.0
## GETTING STARTED WITH HYPERLEDGER FABRIC 

### INSTALL PREREQUISITES
***

1. Install latest version of cURL.
2. Install Docker and Docker-compose  
__MacOSX, \*nix, or Windows 10 :__ Docker v1.12 or greater is required.  
__Older versions of Windows :__ Docker Toolbox - again, Docker version v1.12 or greater is required.

	_Docker-compose_ will automatically be installed with Docker or Docker-Toolbox. You should check that you have Docker Compose version 1.8 or greater installed. If not, we recommend that you install a more recent version of Docker.You can check the version of Docker Compose you have installed with the following command from a terminal prompt:  
	`docker-compose --version`	
3.	Install Go Programming Language  
Hyperledger Fabric uses the Go programming language 1.7.x for many of its components.Given that we are writing a Go chaincode program, we need to be sure that the source code is located somewhere within the `$GOPATH` tree. First, you will need to check that you have set your `$GOPATH` environment variable.If nothing is displayed when you echo `$GOPATH`, you will need to set it. Typically, the value will be a directory tree child of your development workspace, if you have one, or as a child of your `$HOME` directory. Since we’ll be doing a bunch of coding in Go, you might want to add the following to your ~/.bashrc :  

	`export GOPATH=$HOME/go`  

	`export PATH=$PATH:$GOPATH/bin`

4. If you are developing on Windows , you may need Git bash.
5. Install your preferred Java IDE ( [eclipse]( https://www.eclipse.org/downloads/ ) , [IntelliJ]( https://www.jetbrains.com/idea/download/ ))		

### GETTING THE PROJECT
***
1.  Clone the project from:
https://github.com/infor-cloud/supply-chain-insights
2.	Open up your preferred Java IDE (e.g, eclipse, IntelliJ,)
3.	Go to File --> Import --> Maven --> Existing Maven Projects, and select the hyperledger folder
4.	Either from the command line or the IDE, run the command:  
    `mvn clean install -DkipITs=false -Dmaven.test.failure.ignore=false`

### SETTING UP DOCKER
***
1. Download Docker from:  
https://www.docker.com/community-edition
2. *FIRST TIME DOCKER SETUP*  
    **For MAC OSX**  
	Docker >> Preferences >> File Sharing  
	Add the hyperledger folder to the list  
    **For Windows**  
    Right Click Docker from the bottom right side of the startup bar, click on settings >> Shared Drives and turn on C (or your preferred network drive)
3.  In git bash, run:  
    `curl -sSL https://goo.gl/iX9dek | bash`  
    to download all the neccessary images. The curl command above downloads and executes a bash script that will download and extract all of the platform-specific binaries you will need to set up your network and place them into the cloned repo you created above. It retrieves four platform-specific binaries:

	    cryptogen,
		configtxgen,
		configtxlator and
		peer
    and places them in the bin sub-directory of the current working directory.
You may want to add that to your *PATH* environment variable so that these can be picked up without fully qualifying the path to each binary. e.g.:  
    `export PATH=<path to download location>/bin:$PATH`  
	Finally, the script will download the Hyperledger Fabric docker images from Docker Hub into your local Docker registry and tag them as *latest*. You can see all images by using  
    `docker images`  
	These are the components that will ultimately comprise our Hyperledger Fabric network. You will also notice that you have two instances of the same image ID - one tagged as *x86_64-1.0.0*(or depending upon your architecture) and one tagged as *latest*.

### CONFIGURATIONS
***

>Note : Our default configuration creates a network of 1 orderer and 3 orgs(GTN, Elemica and Dun&BradStreet) each of which has CA and 2 peers. Test Certificates and channel artifacts are provided. Follow **RUNNING FABRIC FROM THE UI** directly if you want to use the default certificates and configuration.


##### GENERATING CERTIFICATES AND CHANNEL ARTIFACTS  
1. Edit scripts/config/crypto-config.yaml file to add additional orgs and peers
2. Edit scripts/config/configtx.yaml file to generate channel artifacts related to orgs. 
3.	Use the platform-specefic-binaries , 
* *<your-platform>/bin/cryptogen* tool to generate the certificates. That will create *crypto-config* directory under <your-platform>. Copy all files 
from <your-platform>/crypto-config directory to ./crypto-config  
Command :  
`<your-platform>/cryptogen generate --config=./scripts/config/crypto-config.yaml`

    >Note : Certificates and keys generated in ./crypto-config are 
	for test environment only. You may replace them with your own 
	certificates and keys in real environment.

* Use the *<your-platform>/bin/configtxgen* tool to create Channel configuration and orderer bootstrap files which will be needed to create a new channel :
	   
    - To tell the configtxgen tool where to look for the configtx.yaml file that it needs to ingest :  
	`export FABRIC_CFG_PATH=$PWD` (Current directory in this case)
	   
    - Create the orderer block:  
`./<your-platform>/configtxgen -profile ThreeOrgsOrdererGenesis -outputBlock ./scripts/config/orderer.block`
	   
    - Create the channel transaction artifact:  
	   **make sure to set the \<channel-ID\>.**  
`./<your-platform>/configtxgen -profile ThreeOrgsChannel -outputCreateChannelTx ./scripts/config/<channel-ID>.tx -channelID <channel-ID>`


##### CONFIGURING DOCKER-COMPOSE 
>Note - we have provided docker-compose.yaml which will run a network of 1 orderer and 3 orgs each of which has CA and 2 peers. Also you can use separate docker compose files for running network of single org using command :  
`docker-compose -f <fileName> up`   
and if you want to run orderer with this org , use :   
`docker-compose -f orderer-docker-compose.yaml -f <fileName> up`

To configure docker-compose for your org

1.	Assuming you have all certificates and channel artifacts of your org , you can configure the path location of your certificates and keys, volume 	 and port mapping , peer names and msp of organization.

2.	As peer extends *./peer-base/peer-base.yaml* , make sure you have correct settings for starting chaincode containers on same bridge network as peers.
As currently docker-compose.yaml is in scripts folder , `CORE_VM_DOCKER_HOSTCONFIG_NETWORKMODE=scripts_default`. Change this variable according to your folder name. 

For more information , read <https://docs.docker.com/compose/networking/>

### RUNNING FABRIC FROM THE UI
***
1.	Open git bash
2.	cd to the scripts folder
3.	Run `./fabric.sh up` ( Currently it runs default *docker-compose.yaml* , you can edit it to run compose file of your org).
4.	Check *./scripts/config/networkConfig_properties.yaml* and make sure you have right details of your org , peers (same as in docker-compose) and channel.
5.	In your Java Project , find **FabricApplication** under *src/main/java/org/non/api/web/app*, and run it as Java Application, which bootstraps Jetty
    - If it is setting up the chaincode containers, (first time setup or after a fabric restart), it will take a while to start
    - If you see the message: INFO  [2017-07-06 15:06:43,195] org.eclipse.jetty.server.Server: Started @9030ms, it has worked
6.	Open up your preferred browser and go to `localhost:8080/static/sessionProfile.html`	    
7.	Start by creating trading partners
    - Name must be not be blank!
8.	Query trading partners by name to get the information entered when creating a partner

##### TROUBLESHOOTING

1. When running the code via eclipse, if you get an *"unable to enroll admin"* error, the server has not started properly. Restart the server in bash  
with the command  
`./fabric.sh restart`

2. When running the code in eclipse you get a *"Error while registering the user org.hyperledger.fabric.sdkintegration.SampleUser" restart the server 	  with the command   
`./fabric.sh restart`

3. Changes were made to the chaincode and are not being shown as you run the application. Run the command  
`./fabric.sh restart`

For more documentation, visit:     
http://hyperledger-fabric.readthedocs.io/en/latest/blockchain.html