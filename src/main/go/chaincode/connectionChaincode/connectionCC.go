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
	
	if args[0] == "addMember" {
		return t.addMember(stub, args)
	}
	
	return shim.Error("unknown action, must be of add,modify,query or delete")
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

func (t *SimpleChaincode) query (stub shim.ChaincodeStubInterface, args []string) pb.Response {
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
	var connection string
	var result []byte;
	var i int
	for i = 0; companyIterator.HasNext(); i++ {
		responseRange, err := companyIterator.Next()
		if err != nil {	
			fmt.Println("Error getting response from iterator")
			return shim.Error("Problem getting the response from iterator")
		}	
		fmt.Println(responseRange.Value);
		result = responseRange.Value
		json.Unmarshal(responseRange.Value, &connection)
		fmt.Println("Connection: " + connection);
	}
	fmt.Println("Finishing query connections");	
	return shim.Success(result)
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
func main(){
	err := shim.Start(new(SimpleChaincode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}









