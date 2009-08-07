<?php
error_reporting(E_ALL);
ini_set('display_errors', '1');
include 'xmlrpc.inc';
include 'xmlrpcs.inc';

//example from tutorial
function sumAndDifference ($params) {

    // Parse our parameters.
    $xval = $params->getParam(0);
    $x = $xval->scalarval();
    $yval = $params->getParam(1);
    $y = $yval->scalarval();

    // Build our response.
    $struct = array('sum' => new xmlrpcval($x + $y, 'int'),
                    'difference' => new xmlrpcval($x - $y, 'int'));
    return new xmlrpcresp(new xmlrpcval($struct, 'struct'));
}

//must exist for yabs
function getYWSIVersion () {
    // Build our response.
    return new xmlrpcresp(new xmlrpcval(1, 'int'));
}


//proof of concept: multidimensional arrays in xml-rpc
//Yabs Java code to test this rp call:
//
//        try {
//            boolean f = new WSConnectionClient(new URL("http://localhost/server.php")).getClient().
//            invokeSetCommand(WSConnectionClient.COMMANDS.ADD_NEW_CONTACTS.toString(),
//                    new Object[][]{
//                        {"1", "cname", "mustermann1"}, {"1","city", "mustermannhausen"},
//                        {"2", "cname", "mustermann2"}, {"2","city", "mustermannhausen2"}
//                        });
//        } catch (Exception malformedURLException) {
//            malformedURLException.printStackTrace();
//        } finally {
//            System.exit(0);
//        }
//
function addNewContacts ($params) {

    //Parse our parameters. *should be a loop through $params instead*
    //Writing to a file as echoing will break the operation
    if (!$handle = fopen("test.txt", 'w+')){ 
            exit; 
        } 

	//getParam(0) returns a 'xmlrpcval'

	//serialized it looks like this:
	//<value><array>
	//<data>
	//<value><string>1</string></value>
	//<value><string>cname</string></value>
	//<value><string>mustermann1</string></value>
	//</data>
	//</array></value>

        //getParam(1) returns a 'xmlrpcval'
	//<value><array>
	//<data>
	//<value><string>1</string></value>
	//<value><string>city</string></value>
	//<value><string>mustermannhausen</string></value>
	//</data>
	//</array></value>

        fwrite($handle, $params->getParam(0)->serialize());
	fwrite($handle, $params->getParam(1)->serialize());
        fwrite($handle, $params->getParam(2)->serialize());
	fwrite($handle, $params->getParam(3)->serialize());

        fclose($handle); 

    // Build our response.
    return new xmlrpcresp(new xmlrpcval(true, 'boolean'));
}



// Declare our signature and provide some documentation.
// (The PHP server supports remote introspection. Nifty!)
$sumAndDifference_sig = array(array('struct', 'int', 'int'));
$sumAndDifference_doc = 'Get script version';

new xmlrpc_server(array('sumAndDifference' =>
                        array('function' => 'sumAndDifference',
                              'signature' => $sumAndDifference_sig,
                              'docstring' => $sumAndDifference_doc),
			'getYWSIVersion' =>
                        array('function' => 'getYWSIVersion')
			,
			'addNewContacts' =>
                        array('function' => 'addNewContacts')
			)
		);
?>