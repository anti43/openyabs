<?php
include 'xmlrpc.inc';
include 'xmlrpcs.inc';

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

function getYWSIVersion () {
    // Build our response.
    return new xmlrpcresp(new xmlrpcval(1, 'int'));
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
			)
		);
?>