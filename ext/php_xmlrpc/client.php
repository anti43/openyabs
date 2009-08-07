<html>

<body>
<h1>XML-RPC PHP Demo</h1>

<?php
error_reporting(E_ALL);
ini_set('display_errors', '1');
include 'xmlrpc.inc';

// Make an object to represent our server.
$server = new xmlrpc_client('/server.php',
                            'localhost', 80);

// Send a message to the server.
$message = new xmlrpcmsg('sumAndDifference',
                         array(new xmlrpcval(5, 'int'),
                               new xmlrpcval(3, 'int')));
$result = $server->send($message);

// Process the response.
if (!$result) {
    print "<p>Could not connect to HTTP server.</p>";
} elseif ($result->faultCode()) {
    print "<p>XML-RPC Fault #" . $result->faultCode() . ": " .
        $result->faultString();
} else {
    $struct = $result->value();
    $sumval = $struct->structmem('sum');
    $sum = $sumval->scalarval();
    $differenceval = $struct->structmem('difference');
    $difference = $differenceval->scalarval();
    print "<p>Sum: " . htmlentities($sum) .
        ", Difference: " . htmlentities($difference) . "</p>";
}
?>

</body></html>