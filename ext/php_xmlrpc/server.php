<?php
//error_reporting(E_ALL);
//ini_set('display_errors', '1');
include 'xmlrpc.inc';
include 'xmlrpcs.inc';


//must exist for yabs
function getYWSIVersion () {
    return new xmlrpcresp(new xmlrpcval(1, 'int'));
}



function addNewContact ($params) {

//Parse our parameters. *should be a loop through $params instead*
//Writing to a file as echoing will break the operation
    if (!$handle = fopen("test.txt", 'w+')) {
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
    return new xmlrpcresp(new xmlrpcval(rand(), 'int'));
}



function getNewContacts($zeitraum) {

    $arr =
        array(

        new xmlrpcval(array('id' => new xmlrpcval(0, 'int'),
        'key' => new xmlrpcval('ids', 'int'),
        'value' => new xmlrpcval(2000, 'int')), 'struct'),

        new xmlrpcval(array('id' => new xmlrpcval(1, 'int'),
        'key' => new xmlrpcval('ids', 'int'),
        'value' => new xmlrpcval(2001, 'int')), 'struct'),

        new xmlrpcval(array('id' => new xmlrpcval(0, 'int'),
        'key' => new xmlrpcval('contactsids', 'int'),
        'value' => new xmlrpcval(2, 'int')), 'struct'),

        new xmlrpcval(array('id' => new xmlrpcval(0, 'int'),
        'key' => new xmlrpcval('cname', 'string'),
        'value' => new xmlrpcval('spaci76', 'string')), 'struct'),

        new xmlrpcval(array('id' => new xmlrpcval(1, 'int'),
        'key' => new xmlrpcval('contactsids', 'int'),
        'value' => new xmlrpcval(3, 'int')), 'struct'),

        new xmlrpcval(array('id' => new xmlrpcval(1, 'int'),
        'key' => new xmlrpcval('cname', 'string'),
        'value' => new xmlrpcval('spaci2009', 'string')), 'struct')
    );

    return new xmlrpcresp(new xmlrpcval($arr, 'struct'));
}


new xmlrpc_server(array('addNewContact' =>
    array('function' => 'addNewContact')
    ,
    'getNewContacts' =>
    array('function' => 'getNewContacts')
    ,
    'getYWSIVersion' =>
    array('function'=> 'getYWSIVersion'))
);
?>
