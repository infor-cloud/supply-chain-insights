package main


import(
	"encoding/json"
	"fmt"
	"bytes"
	"strconv"
	"time"
	"strings"
	"math/rand"

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

type Profile struct {
	Uuid string
}

type Metadata struct {
	ProposalUpdateKey string
	
}
type Identifier struct {
	SchemeName	string `json:"schemeName"`
	Id 			string `json:"id"`	
}



// Init is called once in the  beginning to define any neccessary variables.
// Here, we don't need it, and just return a nil string
func ( t *SimpleChaincode) Init (stub shim.ChaincodeStubInterface) pb.Response {
	fmt.Println("##### INIT CC ####");
	return shim.Success(nil)
}

// All methods will need a function type of invoke and then the method they want to call
// as the first parameter of the args
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
	
  if args[0] == "queryByRange" {
		return t.queryByRange(stub, args)
	}
	if args[0] == "getHistory" {
		return t.getHistory (stub, args)	
	}
	
	fmt.Println("Cant find method of" + args[0])
	return shim.Error("unknown action, must be of add, addMember, modify, query, queryVerified, queryByRange, getHistory or delete")
}

// Any member of the network can make a transaction, however, all data must be
// verified by an oracle, so here we check the creator of the transaction and
// if the user's certificate is in the ledger as an oracle, their transaction will
// be considered VERIFIED
func (t *SimpleChaincode) isVerified (stub shim.ChaincodeStubInterface) bool{
	// resp will be the certificate and mspID of the person who made this transaction
	resp, err := stub.GetCreator()
	key := string (resp)
	if err != nil {
		fmt.Println ("Error getting user cert")
		return	false;
	}
	fmt.Println(key)
	
	// Certs should have been enrolled in the ledger in the addMember at the beginning of the channel creation
	roleBytes,err := stub.GetState(key)
	fmt.Println("Rolebytes: ", roleBytes)
	var role string
	json.Unmarshal(roleBytes, &role)
	fmt.Println("Role :", role)
	if err != nil {
		fmt.Println("Error getting role from cert");
		return false;
	}
	
	// Check the value from the user cert, and verify if oracle, and do not verify otherwise
	if role == "ntp" {
		return false
	} else if role == "oracle" {
		return true
	} 
	fmt.Println("Role: %s", role)
	return false
}


func (t *SimpleChaincode) pseudo_uuid() []byte {

    b := make([]byte, 16)
    var i int
	for i = 0; i < len(b); i++ {
		//UUID will be a random 16 digit byte array, with each digit being a number from 0-9
		// On a side note, math/rand is used because crypto/rand gave us huge problems
		// when putting into the ledger. Very sad :(
		b[i] = byte(rand.Intn(10)+48)
	}
    return b
}


// addMember will take in one parameter as the role of this user, and the certificate is
// obtained from the method stub.GetCreator(), which is mspID + certificate of the User
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
	// GetCreator() returns a byte array so it must be parsed to a string in order to be a key
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
	var uuid []byte
	
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
	
	key = strings.ToLower(args[1])
	
	//Marshal takes in a string version of a trading partner and converts it into a byte array
	//buf, err := json.Marshal(args[2])
	if err != nil {
		fmt.Println(err.Error());
		return shim.Error( err.Error())
	}
	
	// UUID is a random numeric 16 digit string  in byte array format
	uuid = t.pseudo_uuid()
	fmt.Println(uuid)

	var uid int;
	err = json.Unmarshal(uuid, &uid)
	
	prof := &Profile{strconv.Itoa(uid)}
	
	if err != nil {
		fmt.Println("UID to string: " +  err.Error());
		return shim.Error( err.Error())
	}
	
	profBytes, err := json.Marshal(prof)
	err = stub.PutState(key, profBytes)
	if err != nil {
		fmt.Println("Putting name to UUID into ledger error: ") 
		fmt.Println(err.Error())
		return shim.Error(err.Error())
	}

	fmt.Println(args[2]);
	
	// Create an empty TradeData to parse the trading partner passed in
	id := make([]Identifier,0);	
	data := &TradeData{"","","","","","","","",id, false}
	
	// Unmarshal takes the trading partner string into a TradingPartner class
	err = json.Unmarshal([]byte(args[2]),data)
	
	if err!=nil {
		fmt.Println("Error %s", err);
		return shim.Error(err.Error())
	}
	
	var boolVal string
	// Check the role of the person making this transaction
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

	
	//buf, err =  json.Marshal(data)
	/*
//	buf, err =  json.Marshal(data)
	err = stub.PutState(key, buf)
	if (err != nil){
		return shim.Error(err.Error())
	}	
*/
	
	
	// Creating a composite key:
	// The key will have a format similar to verified~uuidVerified1234ImARealNumber5
	// This format allows us to search the composites for the index and then
	// The value of the prefix of the key. 
	index := "verified~uuid"
	verifiedNameIndexKey, err := stub.CreateCompositeKey(index, []string{boolVal,prof.Uuid})
	if err != nil {
		return shim.Error(err.Error())
	}
	fmt.Println(verifiedNameIndexKey)
	stub.PutState(verifiedNameIndexKey, buf)
	
	index = "metadata~uuid"
	metadataUUIDIndexKey, err := stub.CreateCompositeKey(index, []string{"metadata",prof.Uuid})
	if err != nil {
		return shim.Error(err.Error())
	}
	metaKey,err := stub.CreateCompositeKey(index, []string{"Unverified",prof.Uuid})
	metadata := Metadata{metaKey}
	fmt.Println(metadataUUIDIndexKey)
	metaBytes,err := json.Marshal(metadata)
	stub.PutState(metadataUUIDIndexKey, metaBytes )
	
	
	fmt.Println("Add finished");
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
	fmt.Println ("#### QUERY BEGIN ####")
	var key string
	//trade :=  TradingPartner{}
	var err error
	if  len(args) != 2	 {
		return shim.Error ("Incorrect number of arguments. Expecting name of the person to query")
	}
	key = strings.ToLower(args[1])
	
	// Get the UUID from the ledger by the name of the company
	tradingpartner, err := stub.GetState(key)
	fmt.Println("Printing tradingpartner[byte]:")
	fmt.Println("String data: " + string(tradingpartner))

	if err != nil {
			jsonResp := "{\"Error\":\"Failed to get state for " + key + "\"}"
			return shim.Error(jsonResp)
	}
	
	prof := &Profile{""}
	err = json.Unmarshal(tradingpartner, &prof)
	
	// Searching only the verified states in the query method for the UUID provided by the name
	index := "verified~uuid"
	verifiedNameIndexKey, err := stub.CreateCompositeKey(index, []string{"Verified",prof.Uuid})
	fmt.Println("Index Key: " + verifiedNameIndexKey);
	data, err := stub.GetState(verifiedNameIndexKey)
	
	fmt.Println("Data: " +  string(data));
	//json.Unmarshal([]byte(tradingpartner), &trade)
	fmt.Println("#### Query End ####");
	return shim.Success(data)
}


// Query Verified will return a list of trading partners that will either be of type 
// Verified or Unverified
func (t *SimpleChaincode) queryVerified(stub shim.ChaincodeStubInterface, args[]string) pb.Response{
    fmt.Println("queryVerified");
    
    // In order to look at the verified data, the user must of role Oracle
    if t.isVerified(stub) == false {
       return shim.Error("You're not allowed to be here")
    } 
    
    // Create an iterator for a list of keys that have the field of what we want to find
    verified := args[1]
    verifiedResultsIterator, err := stub.GetStateByPartialCompositeKey("verified~uuid", []string{verified})
    
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
		/*
        objectType, compositeKeyParts, err := stub.SplitCompositeKey(responseRange.Key)
        if err != nil {
            return shim.Error(err.Error())
        }
        returnedVerification := compositeKeyParts[0]
        returnedPartnerName := compositeKeyParts[1]
        fmt.Printf("- found a Partner from index:%s verification:%s name:%s\n", objectType, returnedVerification, returnedPartnerName)
        
        trade,err = stub.GetState(returnedPartnerName)
        */
		trade, err = stub.GetState(responseRange.Key)
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

// **** CURRENTLY UNUSED *****
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


// Searches the ledger by prefix, for any key that are between the startkey and endkey passed in by the user
func (t *SimpleChaincode) queryByRange (stub shim.ChaincodeStubInterface, args []string) pb.Response {
	var startKey string
	var endKey string
	
	fmt.Println("query by range start");
	if len(args) != 3 {
		return shim.Error("Incorrect number of arguments, expecting 3") 
	}
	
	
	startKey = args[1]
	endKey = args[2]
	
	tradeList,err := stub.GetStateByRange(startKey, endKey) 
	if err != nil {
		fmt.Println("Error getting state")
		return shim.Error(err.Error())
	}
	
	var buffer bytes.Buffer
	buffer.WriteString("[")
	
	bArrayMemberAlreadyWritten := false
	for tradeList.HasNext() {
		queryResponse, err := tradeList.Next()
		if err != nil {
			fmt.Println(err.Error())
			return shim.Error(err.Error())
		}
		// Add a comma before array members, suppress it for the first array member
		if bArrayMemberAlreadyWritten == true {
			buffer.WriteString(",")
		}
		buffer.WriteString("{\"Key\":")
		buffer.WriteString("\"")
		buffer.WriteString(queryResponse.Key)
		buffer.WriteString("\"")

		buffer.WriteString(", \"Record\":")
		// Record is a JSON object, so we write as-is
		buffer.WriteString(string(queryResponse.Value))
		
		buffer.WriteString("}")
		bArrayMemberAlreadyWritten = true
	}
	buffer.WriteString("]")
	
	fmt.Printf("- queryByRange queryResult:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}


// Gets all of the changes to this key
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

	return shim.Success(buffer.Bytes())
}

func main(){
	err := shim.Start(new(SimpleChaincode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}