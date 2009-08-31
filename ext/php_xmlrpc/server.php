<?php
//error_reporting(E_ALL);
//ini_set('display_errors', '1');
include 'xmlrpc.inc';
include 'xmlrpcs.inc';
include 'base/webshop.php';


//must exist for yabs
function getYWSIVersion () {
    return new xmlrpcresp(new xmlrpcval(1, 'int'));
}


function addNewContacts ($params) {
//Platzhalter
}

function getNewContacts($cID) {
	$cID = $cID->getParam(0)->scalarval();
	$tmp = new Webshop();
	$arr = $tmp->getNewContacts($cID);
    return new xmlrpcresp(new xmlrpcval($arr, 'struct'));
}

/**
 *
 * @param $cID
 * @return array of orderinfos
 */
function getNewOrders($cID) {
	$cID = $cID->getParam(0)->scalarval();
	$tmp = new Webshop();
	$arr = $tmp->getNewOrders($cID);
    return new xmlrpcresp(new xmlrpcval($arr, 'struct'));
}

new xmlrpc_server(array(
	'addNewContacts' =>
	array('function' => 'addNewContacts'),
    'getNewContacts' =>
    array('function' => 'getNewContacts'),
    'getNewOrders' =>
    array('function' => 'getNewOrders'),
    'getYWSIVersion' =>
    array('function'=> 'getYWSIVersion'))


    );
?>
