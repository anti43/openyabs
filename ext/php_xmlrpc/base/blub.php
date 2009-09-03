<?php
include 'dbconnector.php';
/**
 * alle eigenschaften eines webshopabgleichs
 */
class Webshop extends DbConnector {

    private $operator = array('<','>','=');

    public function __construct() {
    //$operator = array('<','>','=');//$this->operator;
    }
    /**
     *
     * @param int $ID
     * @param array $operator
     * @return array of Contacts
     */
    public function getCustomerContactOutput($operand,$ID) {
        $gesamteContacts = array();
        $i; //pos. memo
        try {
            $pdo = new DbConnector();
            $newContacts = $pdo->prepare("SELECT * FROM customers_info LEFT JOIN
                            ( customers, address_book, countries ) ON ( customers_info.customers_info_id =
                              customers.customers_id AND customers.customers_id = address_book_id AND
                              address_book.entry_country_id = countries.countries_id ) WHERE
                              customers_info_id ? ?");
            $newContacts->execute(array($operand,$ID));
            $tmp = $newContacts->fetchAll();

            foreach ($tmp as $row) {
                if($row['customers_gender'] == 'm') {
                    $gender = true;
                }else $gender = false;

                if($row['customers_vat_id_status'] != null && $row['customers_vat_id_status'] == 1) {
                    $iscompany = true;
                    $vatid = $row['customers_vat_id'];
                }else {
                    $iscompany = false;
                    $vatid = '';
                }

                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('ids', 'string'),
                    'value' => new xmlrpcval($row['customers_id'], 'int')), 'struct');

                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('cname', 'string'),
                    'value' => new xmlrpcval($row['customers_lastname'], 'string')), 'struct');

                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('taxnumber', 'string'),
                    'value' => new xmlrpcval($vatid, 'int')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('prename', 'string'),
                    'value' => new xmlrpcval($row['customers_firstname'], 'string')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('street', 'string'),
                    'value' => new xmlrpcval($row['entry_street_address'], 'string')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('zip', 'string'),
                    'value' => new xmlrpcval($row['entry_postcode'], 'int')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('city', 'string'),
                    'value' => new xmlrpcval($row['entry_city'], 'string')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('mainphone', 'string'),
                    'value' => new xmlrpcval($row['customers_telephone'], 'int')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('fax', 'string'),
                    'value' => new xmlrpcval($row['customers_fax'], 'int')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('mailaddress', 'string'),
                    'value' => new xmlrpcval($row['customers_email_address'], 'string')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('company', 'string'),
                    'value' => new xmlrpcval($row['entry_company'], 'string')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('country', 'string'),
                    'value' => new xmlrpcval($row['countries_name'], 'string')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('ismale', 'string'),
                    'value' => new xmlrpcval($gender, 'boolean')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('iscompany', 'string'),
                    'value' => new xmlrpcval($iscompany, 'boolean')), 'struct');

                $i++;
            }
            return $gesamteContacts;

        } catch (PDOException $e)  {
            throw new exception($e->getMessage());
        }

    }

    /**
     * Gibt die neuen Kontakte im Array zurück
     * @param $lastContactID
     * @return Array
     */
    public function getNewContacts($lastContactID){
        $this->getCustomerContactOutput($lastContactID, $this->operator[0]);
    }

    public function getContact($cID,$CorG) {
		/*
		 * $CorG Customer or Guest
		 * Wenn $CorG true, dann New Customer
		 * sonst Guest == orderID
		 */
        if ($CorG == true) { // New Customer
            /*
             * wenn aktuelle Customeranzahl kleiner dann bei getCustomerConactOutput =
             */
            if($this->getactualCustomerCount() >= $cID) {

                //abruf existierender Customer
                $sqlstatement = 'SELECT * FROM customers_info LEFT JOIN
                            ( customers, address_book, countries ) ON ( customers_info.customers_info_id =
                              customers.customers_id AND customers.customers_id = address_book_id AND
                              address_book.entry_country_id = countries.countries_id ) WHERE
                              customers_info_id > ?';
                $this->getCustomerContactOutput($cID, $sqlstatement);
            }
        }else {
            //New Guest
            $this->getNewGuestContact($cID);
        }
    }



    /**
     * all Subfunctions are protected
     */
        /**
         *  gibt die aktuelle Anzahl der Customers zurück
         * @return int or null
         */

     	public function getactualCustomerCount(){
			try{
			$pdo = new DbConnector();
			$tmp = $pdo->prepare("SELECT COUNT(*) FROM customers");
			$tmp->execute();
			return $tmp->fetchColumn();
		} catch (PDOException $e) {
			throw new exception($e->getMessage());
		}
	}

    /**
     *
     * @param int $ID
     * @return array
     */
	protected function getNewGuestContact($ID){

            try{
			$pdo = new DbConnector();
			$newGuestContact = $pdo->prepare("SELECT * FROM `orders` where orders_id = ?");
			$newGuestContact->execute(array($ID));
                        $tmp = $newGuestContact->fetchAll();
                        var_dump($tmp);
            foreach ($tmp as $row) {
                if($row['customers_gender'] == 'm') {
                    $gender = true;
                }else $gender = false;

                if($row['customers_vat_id_status'] != null && $row['customers_vat_id_status'] == 1) {
                    $iscompany = true;
                    $vatid = $row['customers_vat_id'];
                    $company = $row['customers_company'];
                }else {
                    $iscompany = false;
                    $vatid = '';
                    $company = '';
                }

                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('ids', 'string'),
                    'value' => new xmlrpcval($row['customers_id'], 'int')), 'struct');


                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('cname', 'string'),
                    'value' => new xmlrpcval($row['customers_name'], 'string')), 'struct');

                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('taxnumber', 'string'),
                    'value' => new xmlrpcval($vatid, 'int')), 'struct');

                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('street', 'string'),
                    'value' => new xmlrpcval($row['customers_street_address'], 'string')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('zip', 'string'),
                    'value' => new xmlrpcval($row['customers_postcode'], 'int')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('city', 'string'),
                    'value' => new xmlrpcval($row['customers_city'], 'string')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('mainphone', 'string'),
                    'value' => new xmlrpcval($row['customers_telephone'], 'int')), 'struct');

                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('mailaddress', 'string'),
                    'value' => new xmlrpcval($row['customers_email_address'], 'string')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('company', 'string'),
                    'value' => new xmlrpcval($company, 'string')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('country', 'string'),
                    'value' => new xmlrpcval($row['customers_country'], 'string')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('ismale', 'string'),
                    'value' => new xmlrpcval($gender, 'boolean')), 'struct');
                $gesamteContacts[] =    new xmlrpcval(array(
                    'id' => new xmlrpcval($i, 'int'),
                    'key' => new xmlrpcval('iscompany', 'string'),
                    'value' => new xmlrpcval($iscompany, 'boolean')), 'struct');

                $i++;
            }
            return $gesamteContacts;






		} catch (PDOException $e) {
			throw new exception($e->getMessage());
		}
	}
}
?>