<?php
class DbConnector extends PDO {
	private $settings = array ();
	private $persit = array (PDO :: ATTR_PERSISTENT => true);
	public function __construct($file = 'settings.ini') {
		
	
                if (!$settings = parse_ini_file($file, TRUE))
			throw new exception('Unable to open ' . $file . '.');

		$dns = $settings['database']['driver'] . ':host=' . $settings['database']['host'] .
		 ((!empty ($settings['database']['port'])) ? (';port=' . $settings['database']['port']) : '') .
		';dbname=' . $settings['database']['schema'];

		parent :: __construct($dns, $settings['database']['username'], $settings['database']['password'], $persit);
	}
}

?>