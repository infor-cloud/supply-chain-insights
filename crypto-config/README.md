This folder has all the certificates and private keys.

How to generate the certificates for test org, 

1) Edit ../scripts/config/crypto-config.yaml file to add additional orgs and peers
2) Edit ../scripts/config/configtx.yaml file to generate channel artifacts related to orgs. 
3) Use the release/<your-platform>/cryptogen tool to generate the certificates. That will create crypto-config directory under <your-platform>. Copy all files from release/<platform>/crypto-config directory to /code/gtnexus/devl/blockchain/src/test/resources/crypto-config 

		Command : <your-platform>/cryptogen generate --config= ./scripts/config/crypto-config.yaml


4) Use the release/<your-platform>/configtxgen tool to create Channel configuration and orderer bootstrap files which will be needed to create a new channel :
   
   To tell the configtxgen tool where to look for the configtx.yaml file that it needs to ingest.
		eg. export FABRIC_CFG_PATH=$PWD (Current directory in this case)
   
   Create the orderer block:
		./<your-platform>/configtxgen -profile ThreeOrgsOrdererGenesis -outputBlock /code/gtnexus/devl/blockchain/src/test/resources/scripts/config/orderer.block
   
   Create the channel transaction artifact:
   
	    # make sure to set the <channel-ID> parm

		./<your-platform>/configtxgen -profile ThreeOrgsChannel -outputCreateChannelTx /code/gtnexus/devl/blockchain/src/test/resources/scripts/config/<channel-ID>.tx -channelID <channel-ID>

	
5) Edit the /code/gtnexus/devl/blockchain/src/test/resources/scripts/docker-compose.yaml file to spin up the network. 