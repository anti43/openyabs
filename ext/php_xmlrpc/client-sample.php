<html>

<body>

<?php
error_reporting(E_ALL);
ini_set('display_errors', '1');
include 'xmlrpc.inc';

// Make an object to represent our server.
$client = new xmlrpc_client('/~anti/PhpProject1/server.php',
                            'localhost', 80);
$client->setdebug(1);

// Send a message to the server.
$message = new xmlrpcmsg('getNewContacts',
                         array(new xmlrpcval("20010716T18:16:18", 'dateTime.iso8601'),
                               new xmlrpcval("20010716T18:16:18", 'dateTime.iso8601')));
$result = $client->send($message);

// Process the response.
if (!$result) {
    print "<p>Could not connect to HTTP server.</p>";
} elseif ($result->faultCode()) {
    print "<p>XML-RPC Fault #" . $result->faultCode() . ": " .
        $result->faultString();
} else {

   print "<p>XML-RPC Response : " .$result-> serialize()."</p>";

}
?>

</body></html>