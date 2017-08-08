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
	Name		string `json:"name"`
	WebsiteURI	string `json:"websiteURI"`
	StreetName	string `json:"streetName"`
	CityName 	string `json:"cityName"`
	PostalZone	string `json:"postalZone"`
	ContactName string `json:"contactName"`
	ContactEmail string `json:"contactEmail"`
	ContactTelephone string `json:"contactTelephone"`
	Ids	[]Identifier `json:"idList"`
	Verified 	bool
}

type Identifier struct {
	SchemeName	string `json:"schemeName"`
	Id 			string `json:"id"`	
}




func ( t *SimpleChaincode) Init (stub shim.ChaincodeStubInterface) pb.Response {
	fmt.Println("##### INIT CC ####");
	fmt.Println("hello world");
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
	
	if args[0] == "addMember" {
		return t.addMember(stub, args)
	}
	
	if args[0] == "queryVerified" {
		return t.queryVerified(stub, args)
	}
	
	if args[0] == "addConnection" {
		return t.addConnection(stub, args)
	}
	
	if args[0] == "queryConnection" {
		return t.queryConnection(stub, args)
	}
	
	if args[0] == "getHistory" {
		return t.getHistory (stub, args)	
	}
	
	return shim.Error("unknown action, must be of add, modify, query or delete")
}


func (t *SimpleChaincode) isVerified (stub shim.ChaincodeStubInterface) bool{
	resp, err := stub.GetCreator()
	key := string (resp)
	if err != nil {
		fmt.Println ("Error getting user cert")
		return	false;
	}
	fmt.Println(key)
	roleBytes,err := stub.GetState(key)
	fmt.Println("Rolebytes: ", roleBytes)
	var role string
	json.Unmarshal(roleBytes, &role)
	fmt.Println("Role :", role)
	if err != nil {
		fmt.Println("Error getting role from cert");
		return false;
	}
	if role == "ntp" {
		return false
	} else if role == "oracle" {
		return true
	} 
	fmt.Println("Role: %s", role)
	return false
}


func (t *SimpleChaincode) addMember (stub shim.ChaincodeStubInterface, args[] string) pb.Response  {
	if len(args) != 2 {
		return shim.Error ("Incorrect Number of arguments. Expecting 2")
	}
	fmt.Println("ROLE: ")
	fmt.Println(args[1])
	var role string = args[1]
	buf, err := json.Marshal(role)		
	resp, err := stub.GetCreator()
	if err != nil {
		fmt.Println("goshdarn it %s\n", err)
		return shim.Error("Getting the creator failed")
	} 
	var msp string = string(resp)
	fmt.Println("buf: ")
	fmt.Println(buf)
	fmt.Println("Key");
	fmt.Println(msp);
	err = stub.PutState(msp, buf)
	if (err != nil) {
		fmt.Println("goshdarn it %s\n", err)
		return shim.Error("Failed putting data in state")
	} 
	fmt.Println("Role saved!");
	return shim.Success(nil)
	
}

func (t *SimpleChaincode) add (stub shim.ChaincodeStubInterface, args[] string) pb.Response{
	if len(args) != 4 {
		return shim.Error ("Incorrect number of arguments. Expecting 4")	
	}
	var key string
	var err error
	
	key = args[1]
	if args[3]=="create"{
		state, err:= stub.GetState(key)
		if state != nil {
			jsonResp := "{\"Error\":\"Failed to get state for Already exist\"}"
			return shim.Error(jsonResp)
		}
		if err != nil {
			return shim.Error(err.Error())
		}
	}
	
	//Initialize the Chaincode
	
	buf, err := json.Marshal(args[2])
	
	
	id := make([]Identifier,0);
		
	data := &TradeData{"","","","","","","","",id, false}
	err = json.Unmarshal([]byte(args[2]),data)
	
	if err!=nil {
		fmt.Println("Error %s", err);
	}
	
	var boolVal string
	fmt.Println("##########DATA############")
	if t.isVerified(stub) {
		fmt.Println ("Verified!")
		data.Verified = true;
		boolVal = "Verified"
		index := "verified~name"
		verifiedNameIndexKey, _ := stub.CreateCompositeKey(index, []string{"Unverified",data.Name})
		stub.DelState(verifiedNameIndexKey)

	} else {
		fmt.Println("Not verified :(")
		data.Verified = false;
		boolVal = "Unverified"
	}

//	buf, err =  json.Marshal(data)
	err = stub.PutState(key, buf)
	if (err != nil){
		return shim.Error(err.Error())
	}	
	
	index := "verified~name"

	verifiedNameIndexKey, err := stub.CreateCompositeKey(index, []string{boolVal,data.Name})
	if err != nil {
		return shim.Error(err.Error())
	}
	value := []byte{0x00}
	fmt.Println(verifiedNameIndexKey)
	stub.PutState(verifiedNameIndexKey, value)
	
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

func (t *SimpleChaincode) addConnection (stub shim.ChaincodeStubInterface, args []string) pb.Response{
	if len(args) != 4 {
		return shim.Error("Incorrect number of args; expecting the name of the two orgs and the connection") 
	}
	comp1 := args[1]
	comp2 := args[2]
	connection := args[3]
	
	
	buf, err := json.Marshal(connection)
	index := "comp1~comp2"

	connectionIndexKey, err := stub.CreateCompositeKey(index, []string{comp1,comp2})
	if err != nil {
		fmt.Println("Problem making composite keys")
		return shim.Error("Problem getting composite key");
	}
	
	stub.PutState(connectionIndexKey, buf);
	
	fmt.Println("##COMP1##");
	fmt.Println(comp1);
	fmt.Println("##COMP2##");
	fmt.Println(comp2);
	fmt.Println("##CONNECTION##");
	fmt.Println(connection);
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

func (t *SimpleChaincode) queryVerified(stub shim.ChaincodeStubInterface, args[]string) pb.Response{
    fmt.Println("queryVerified");
    //Uncomment after updating HLConnection
   // if t.isVerified(stub) == false {
   //     return shim.Error("You're not allowed to be here")
   // } 
    verified := args[1]
    //verified := "Unverified"
    
    verifiedResultsIterator, err := stub.GetStateByPartialCompositeKey("verified~name", []string{verified})
    
    if err != nil {
        fmt.Println("Got Error getting the iterator");
        return shim.Error(err.Error())
    }
    defer verifiedResultsIterator.Close()
    var buffer bytes.Buffer
    var trade []byte
    buffer.WriteString("[")
    bArrayMemberAlreadyWritten := false
    var i int
    for i = 0; verifiedResultsIterator.HasNext(); i++ {
        responseRange, err := verifiedResultsIterator.Next()
        if err != nil {
            return shim.Error(err.Error())
        }

        objectType, compositeKeyParts, err := stub.SplitCompositeKey(responseRange.Key)
        if err != nil {
            return shim.Error(err.Error())
        }
        returnedVerification := compositeKeyParts[0]
        returnedPartnerName := compositeKeyParts[1]
        fmt.Printf("- found a Partner from index:%s verification:%s name:%s\n", objectType, returnedVerification, returnedPartnerName)
        
        trade,err = stub.GetState(returnedPartnerName)
        if bArrayMemberAlreadyWritten == true {
            buffer.WriteString(",")
        }
        // Record is a JSON object, so we write as-is
        buffer.Write(trade)
        bArrayMemberAlreadyWritten = true
    }
    buffer.WriteString("]")

    fmt.Println("Ending queryVerified");
    return shim.Success(buffer.Bytes())
    
}

func (t *SimpleChaincode) queryConnection (stub shim.ChaincodeStubInterface, args []string) pb.Response {
	fmt.Println("####QUERY CONNECTION#####")
	if len(args) != 2 {
		return shim.Error ("Incorrect number of parameters, expecting 2")
	}
	
	company := args[1]
	fmt.Println("Looking for connections with " + company);
	companyIterator, err := stub.GetStateByPartialCompositeKey("comp1~comp2", []string{company})
	if err != nil {
		fmt.Println("Error getting keys from ledger")
		return shim.Error("Error getting keys from the ledger")
	}
	defer companyIterator.Close()
	var i int;
	for i = 0; companyIterator.HasNext(); i++ {
		responseRange, err := companyIterator.Next()
		if err != nil {	
			fmt.Println("Error getting response from iterator")
			return shim.Error("Problem getting the response from iterator")
		}	
		fmt.Println(responseRange.Value);
		var connection string
		json.Unmarshal(responseRange.Value, &connection)
		fmt.Println("Connection: " + connection);
	}
	fmt.Println("Finishing query connections");	
	return shim.Success(nil)
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