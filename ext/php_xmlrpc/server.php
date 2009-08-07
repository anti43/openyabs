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
function addNewContacts ($params) {

    //Parse our parameters. *should be a loop through $params instead*
    //Writing to a file as echoing will break the operation
    if (!$handle = fopen("test.txt", 'w+')){ 
            exit; 
        } 

	//getParam(0) returns a 'xmlrpcval' which i have no glue how to handle
        fwrite($handle, $params->getParam(0)->serialize());
	fwrite($handle, $params->getParam(1)->serialize());

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