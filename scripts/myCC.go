package main


import(
	"encoding/json"
	"fmt"
	//"strconv"
	
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"

)

// Chaincode Implementation
type SimpleChaincode struct{
}

type TradingPartner struct{
	Trade TradeData `json:"trade"`
}

type TradeData struct{
	name		string `json:"fname"`
	uid			string `json:"lname"`
	websiteURI	string `json:"websiteURI"`
	address		Address `json:"address"`
	contact		Contact	`json:"contact"`
	identifier	Identifier `json:"identifier"`
}

type Address struct {
	streetName	string `json:"streetName"`
	cityName 	string `json:"cityName"`
	postalZone	string `json:"postalZone"`
}

type Contact struct {
	name 	string `json:"name"`
	email	string `json:"email"`
	telephone	string `json:telephone"`
}

type Identifier struct {
	idData identifierData
}

type identifierData struct{
	schemeName	string `json:"schemeName"`
	id 			string `json:"id"`	

}



func ( t *SimpleChaincode) Init (stub shim.ChaincodeStubInterface) pb.Response {
	fmt.Println("##### INIT CC #####")
	
	
	return shim.Success(nil)
}

func (t *SimpleChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response{
	fmt.Println("########## example_cc Invoke #########")
	function, args := stub.GetFunctionAndParameters()

	if len(args) < 1 {
		return shim.Error("Incorrect number of arguments. Expecting at least 1")
	}
	
	if function == "delete" {
		return t.delete(stub, args)
	}
	
	if function == "query"{
		return t.query(stub, args)
	}
	
	if function == "modify"{
		return t.modify (stub, args)
	}
	
	if function == "add" {
		return t.add (stub, args)	
	}
	
	return shim.Error(function)
}

func (t *SimpleChaincode) add (stub shim.ChaincodeStubInterface, args[] string) pb.Response{
	if len(args) != 2 {
		return shim.Error ("Incorrect numbeer of arguments. Expecting 2")	
	}
	
	//var tradingpartner TradingPartner
	var key string
	//var trade string
	var err error
	
	//Initialize the Chaincode
	key = args[0]
	//json.Unmarshal([]byte(args[1]), &tradingpartner)
	buf, err := json.Marshal(args[1])
	//json.Unmarshal(buf, &trade)
	
	fmt.Printf(args[1])
	err = stub.PutState(key, buf)
	if (err != nil){
		return shim.Error(err.Error())
	}	
	
	return shim.Success(buf)
}



func (t *SimpleChaincode) delete (stub shim.ChaincodeStubInterface, args[] string) pb.Response{
	if len(args) != 1 {
		return shim.Error ("Incorrect number of arguments. Expecting 1")
	}
	
	key := args[0]
	
	err := stub.DelState(key)
	if err != nil {
		return shim.Error("Failed to delete state")
	}
	return shim.Success(nil)

}

func (t *SimpleChaincode) query (stub shim.ChaincodeStubInterface, args []string) pb.Response{
	var key string
	//trade :=  TradingPartner{}
	var err error
	if  len(args) != 1	 {
		return shim.Error ("Incorrect number of arguments. Expecting name of the person to query")
	}
	key = args[0]
	
	tradingpartner, err := stub.GetState(key)
	if err != nil {
			jsonResp := "{\"Error\":\"Failed to get state for " + key + "\"}"
			return shim.Error(jsonResp)
	}
	if tradingpartner == nil {
		jsonResp := "{\"Error\":\"Nil amount for " + key + "\"}"
		return shim.Error(jsonResp)
	}
	json.Unmarshal([]byte(tradingpartner), &t)
	
	
	//fmt.Printf("%+v", tradingpartner)
	return shim.Success(tradingpartner)
}

func (t *SimpleChaincode) modify (stub shim.ChaincodeStubInterface, args []string) pb.Response{
	var key string
	
	//var tradingpartner TradingPartner
	var err error
	
	if len(args) != 2{
		return shim.Error ("Incorrect number of arguments, expecting name of person and serialized TradingPartner")
	}
	
	key = args[0]
	trade, err := json.Marshal(args[1])
	//tradingpartner = trade
	
	err = stub.PutState(key, trade)
	if(err != nil) {
		return shim.Error(err.Error())
	}

	return shim.Success(nil);
}
func main(){
	err := shim.Start(new(SimpleChaincode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}









