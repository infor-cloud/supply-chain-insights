package main


import(
	"encoding/json"
	"fmt"
	"bytes"
	"strconv"
	"time"
	
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
	fmt.Println("##### INIT CC ####");
	return shim.Success(nil)
}

func (t *SimpleChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response{
	fmt.Println("########## example_cc Invoke #########")
	function, args := stub.GetFunctionAndParameters()

	if(function != "invoke"){
		return shim.Error("unknown function call");
	}

	if len(args) < 1 {
		return shim.Error("Incorrect number of arguments. Expecting at least 1")
	}
	
	if args[0] == "delete" {
		return t.delete(stub, args)
	}
	
	if args[0] == "query"{
		return t.query(stub, args)
	}
	
	if args[0] == "modify"{
		return t.modify (stub, args)
	}
	
	if args[0] == "add" {
		return t.add (stub, args)	
	}
	
	if args[0] == "getHistory" {
		return t.getHistory (stub, args)	
	}
	
	return shim.Error("unknown action, must be of add, modify, query or delete")
}

func (t *SimpleChaincode) add (stub shim.ChaincodeStubInterface, args[] string) pb.Response{
	if len(args) != 3 {
		return shim.Error ("Incorrect numbeer of arguments. Expecting 3")	
	}
	
	var key string
	var err error
	
	//Initialize the Chaincode
	key = args[1]
	buf, err := json.Marshal(args[2])
	//json.Unmarshal(buf, &trade)
	err = stub.PutState(key, buf)
	if (err != nil){
		return shim.Error(err.Error())
	}	
	
	return shim.Success(nil)
}



func (t *SimpleChaincode) delete (stub shim.ChaincodeStubInterface, args[] string) pb.Response{
	if len(args) != 2 {
		return shim.Error ("Incorrect number of arguments. Expecting 2")
	}
	
	key := args[1]
	
	err := stub.DelState(key)
	if err != nil {
		return shim.Error("Failed to delete state")
	}
	return shim.Success(nil)

}

func (t *SimpleChaincode) query (stub shim.ChaincodeStubInterface, args []string) pb.Response{
	var key string
	trade :=  TradingPartner{}
	var err error
	if  len(args) != 2	 {
		return shim.Error ("Incorrect number of arguments. Expecting name of the person to query")
	}
	key = args[1]
	
	tradingpartner, err := stub.GetState(key)
	fmt.Println("Printing tradingpartner[byte]:")
	fmt.Println(string(tradingpartner))

	if err != nil {
			jsonResp := "{\"Error\":\"Failed to get state for " + key + "\"}"
			return shim.Error(jsonResp)
	}
	json.Unmarshal([]byte(tradingpartner), &trade)
	
	fmt.Println("Printing trade[value]:")
	fmt.Println(trade)
	
	return shim.Success(tradingpartner)
}

func (t *SimpleChaincode) modify (stub shim.ChaincodeStubInterface, args []string) pb.Response{
	var key string
	var err error
	
	if len(args) != 3{
		return shim.Error ("Incorrect number of arguments, expecting name of person and serialized TradingPartner")
	}
	
	key = args[1]
	trade, err := json.Marshal(args[2])
	err = stub.PutState(key, trade)
	if(err != nil) {
		return shim.Error(err.Error())
	}

	return shim.Success(nil);
}

func (t *SimpleChaincode) getHistory (stub shim.ChaincodeStubInterface, args []string) pb.Response{
	var key string
	var err error
	if  len(args) != 2	 {
		return shim.Error ("Incorrect number of arguments. Expecting key to query")
	}
	key = args[1]
	
	fmt.Printf("########### start getHistoryForkey: %s ##########", key)

	resultsIterator, err := stub.GetHistoryForKey(key)
	if err != nil {
		return shim.Error(err.Error())
	}
	defer resultsIterator.Close()

	// buffer is a JSON array containing historic values for the key
	var buffer bytes.Buffer
	buffer.WriteString("[")

	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		response, err := resultsIterator.Next()
		if err != nil {
			return shim.Error(err.Error())
		}
		// Add a comma ahead of array members, skip it for the first array member
		if bArrayMemberAlreadyWritten == true {
			buffer.WriteString(",")
		}
		buffer.WriteString("{\"TxId\":")
		buffer.WriteString("\"")
		buffer.WriteString(response.TxId)
		buffer.WriteString("\"")

		buffer.WriteString(", \"Value\":")
		// if it was a delete operation on given key, then we need to set the
		//corresponding value null. Else, we will write the response.Value 
		//as-is (as the Value itself a JSON marble)
		if response.IsDelete {
			buffer.WriteString("null")
		} else {
			buffer.WriteString(string(response.Value))
		}

		buffer.WriteString(", \"Timestamp\":")
		buffer.WriteString("\"")
		buffer.WriteString(time.Unix(response.Timestamp.Seconds, int64(response.Timestamp.Nanos)).String())
		buffer.WriteString("\"")

		buffer.WriteString(", \"IsDelete\":")
		buffer.WriteString("\"")
		buffer.WriteString(strconv.FormatBool(response.IsDelete))
		buffer.WriteString("\"")

		buffer.WriteString("}")
		bArrayMemberAlreadyWritten = true
	}
	buffer.WriteString("]")

	fmt.Printf("- getHistoryForKey returning:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}
func main(){
	err := shim.Start(new(SimpleChaincode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}









