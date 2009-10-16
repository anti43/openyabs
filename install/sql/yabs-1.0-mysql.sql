CREATE TABLE accounts  (
	ids             	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	intaccountclass 	SMALLINT DEFAULT 0,
	cname           	VARCHAR(250) NOT NULL,
	description     	VARCHAR(250) NOT NULL,
	taxvalue        	DOUBLE NOT NULL DEFAULT 0,
	dateadded       	DATE NOT NULL,
	intaddedby      	BIGINT(20) DEFAULT 0,
	intparentaccount	BIGINT(20) DEFAULT 0,
	groupsids       	BIGINT(20) UNSIGNED NOT NULL  DEFAULT 1,
	invisible       	SMALLINT DEFAULT 0,
	intaccounttype  	SMALLINT NOT NULL,
	intprofitfid    	SMALLINT NOT NULL,
	inttaxfid       	SMALLINT NOT NULL,
	inttaxuid       	SMALLINT NOT NULL,
	frame           	VARCHAR(25) NOT NULL,
	hierarchypath   	VARCHAR(250) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE addresses  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	groupsids  	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	contactsids	BIGINT(20) UNSIGNED NOT NULL ,
	title      	VARCHAR(250) DEFAULT NULL,
	taxnumber  	VARCHAR(250) DEFAULT NULL,
	prename    	VARCHAR(250) DEFAULT NULL,
	cname      	VARCHAR(250) DEFAULT NULL,
	street     	VARCHAR(250) DEFAULT NULL,
	zip        	VARCHAR(50) DEFAULT NULL,
	city       	VARCHAR(300) DEFAULT NULL,
	company    	VARCHAR(250) DEFAULT NULL,
	department 	VARCHAR(250) DEFAULT NULL,
	country    	VARCHAR(50) DEFAULT NULL,
	ismale     	SMALLINT DEFAULT 0,
	intaddedby 	BIGINT(20) DEFAULT 0,
	dateadded  	DATE DEFAULT NULL,
	inttype    	SMALLINT DEFAULT 0,
	reserve1   	VARCHAR(500) DEFAULT NULL,
	reserve2   	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE comps  (
	ids          	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	groupsids    	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	cname        	VARCHAR(250) DEFAULT NULL,
	state        	VARCHAR(250) DEFAULT NULL,
	phoneprefix  	VARCHAR(250) DEFAULT NULL,
	business     	VARCHAR(250) DEFAULT NULL,
	taxadvisor   	VARCHAR(250) DEFAULT NULL,
	city         	VARCHAR(250) DEFAULT NULL,
	taxadvjob    	VARCHAR(250) DEFAULT NULL,
	street       	VARCHAR(250) DEFAULT NULL,
	stb          	VARCHAR(250) DEFAULT NULL,
	email        	VARCHAR(250) DEFAULT NULL,
	name         	VARCHAR(250) DEFAULT NULL,
	zipcode      	VARCHAR(250) DEFAULT NULL,
	phone        	VARCHAR(250) DEFAULT NULL,
	firstname    	VARCHAR(250) DEFAULT NULL,
	taxauthority 	VARCHAR(250) DEFAULT NULL,
	taxnumber    	VARCHAR(250) DEFAULT NULL,
	taxadvmandant	VARCHAR(250) DEFAULT NULL,
	dateadded    	DATE NOT NULL,
	ismale       	SMALLINT DEFAULT 0,
	isenabled    	SMALLINT DEFAULT 1,
	intaddedby   	BIGINT(20) DEFAULT 0,
	invisible    	SMALLINT DEFAULT 0,
	reserve1     	VARCHAR(500) DEFAULT NULL,
	reserve2     	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE contacts  (
	ids           	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cnumber       	VARCHAR(250) DEFAULT NULL,
	taxnumber     	VARCHAR(250),
	title         	VARCHAR(250) DEFAULT NULL,
	groupsids     	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	country       	VARCHAR(50) DEFAULT NULL,
	prename       	VARCHAR(250) DEFAULT NULL,
	cname         	VARCHAR(250) DEFAULT NULL,
	street        	VARCHAR(250) DEFAULT NULL,
	zip           	VARCHAR(50) DEFAULT NULL,
	city          	VARCHAR(300) DEFAULT NULL,
	mainphone     	VARCHAR(250) DEFAULT NULL,
	fax           	VARCHAR(250) DEFAULT NULL,
	mobilephone   	VARCHAR(250) DEFAULT NULL,
	workphone     	VARCHAR(250) DEFAULT NULL,
	mailaddress   	VARCHAR(350) DEFAULT NULL,
	company       	VARCHAR(250) DEFAULT NULL,
	department    	VARCHAR(250) DEFAULT NULL,
	website       	VARCHAR(350) DEFAULT NULL,
	notes         	VARCHAR(10000),
	dateadded     	DATE NOT NULL,
	isactive      	SMALLINT DEFAULT 0,
	iscustomer    	SMALLINT DEFAULT 0,
	ismanufacturer	SMALLINT DEFAULT 0,
	issupplier    	SMALLINT DEFAULT 0,
	iscompany     	SMALLINT DEFAULT 0,
	ismale        	SMALLINT DEFAULT 0,
	isenabled     	SMALLINT DEFAULT 1,
	intaddedby    	BIGINT(20) DEFAULT 0,
	invisible     	SMALLINT DEFAULT 0,
	reserve1      	VARCHAR(500) DEFAULT NULL,
	reserve2      	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE countries  (
	ids      	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname    	VARCHAR(250) NOT NULL,
	iso      	SMALLINT NOT NULL,
	groupsids	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	reserve1 	VARCHAR(500) DEFAULT NULL,
	reserve2 	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE expenses  (
	ids            	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	groupsids      	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	cnumber        	VARCHAR(250) DEFAULT NULL,
	cname          	VARCHAR(250) DEFAULT NULL,
	accountsids    	BIGINT(20) UNSIGNED NOT NULL ,
	netvalue       	DOUBLE DEFAULT 0,
	taxpercentvalue	DOUBLE DEFAULT 0,
	brutvalue      	DOUBLE DEFAULT 0,
	description    	VARCHAR(5000) DEFAULT NULL,
	dateadded      	DATE NOT NULL,
	intaddedby     	BIGINT(20) DEFAULT 0,
	invisible      	SMALLINT DEFAULT 0,
	reserve1       	VARCHAR(500) DEFAULT NULL,
	reserve2       	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE favourites  (
	ids       	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname     	VARCHAR(250) NOT NULL,
	usersids  	BIGINT(20) UNSIGNED NOT NULL ,
	groupsids 	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	itemsids  	BIGINT(20) UNSIGNED NOT NULL NOT NULL,
	dateadded 	DATE NOT NULL,
	intaddedby	BIGINT(20) DEFAULT 0,
	invisible 	SMALLINT DEFAULT 0,
	reserve1  	VARCHAR(500) DEFAULT NULL,
	reserve2  	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE files  (
	ids       	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname     	VARCHAR(25) NOT NULL,
	groupsids 	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	dateadded 	DATE NOT NULL,
	data      	BLOB(5242880) NOT NULL,
	filesize  	BIGINT(20) NOT NULL,
	intaddedby	BIGINT(20) DEFAULT 0,
	invisible 	SMALLINT DEFAULT 0
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE filestocontacts  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname      	VARCHAR(250) NOT NULL,
	description	VARCHAR(550) DEFAULT NULL,
	contactsids	BIGINT(20) UNSIGNED NOT NULL NOT NULL,
	filename   	VARCHAR(25) NOT NULL,
	intsize    	BIGINT(20) DEFAULT 0,
	mimetype   	VARCHAR(25) DEFAULT NULL,
	intaddedby 	BIGINT(20) DEFAULT 0,
	dateadded  	DATE NOT NULL,
	groupsids  	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE filestoitems  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname      	VARCHAR(250) NOT NULL,
	description	VARCHAR(550) DEFAULT NULL,
	itemsids   	BIGINT(20) UNSIGNED NOT NULL NOT NULL,
	filename   	VARCHAR(25) NOT NULL,
	intsize    	BIGINT(20) DEFAULT 0,
	mimetype   	VARCHAR(25) DEFAULT NULL,
	intaddedby 	BIGINT(20) DEFAULT 0,
	dateadded  	DATE NOT NULL,
	groupsids  	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE filestoproducts  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname      	VARCHAR(250) NOT NULL,
	description	VARCHAR(550) DEFAULT NULL,
	productsids	BIGINT(20) UNSIGNED NOT NULL NOT NULL,
	filename   	VARCHAR(25) NOT NULL,
	intsize    	BIGINT(20) DEFAULT 0,
	mimetype   	VARCHAR(25) DEFAULT NULL,
	intaddedby 	BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
	dateadded  	DATE NOT NULL,
	groupsids  	BIGINT(20)  UNSIGNED NOT NULL DEFAULT 1
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE formatstousers  (
	ids     	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname   	VARCHAR(250) NOT NULL,
	usersids	BIGINT(20) UNSIGNED NOT NULL ,
	inttype 	SMALLINT DEFAULT 0
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE globalsettings  (
	ids      	BIGINT(20) DEFAULT 1,
	cname    	VARCHAR(250) NOT NULL,
	groupsids	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	value    	VARCHAR(250) NOT NULL
	)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE groups  (
	ids          	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname        	VARCHAR(250) NOT NULL,
	description  	VARCHAR(750) DEFAULT NULL,
	defaults     	VARCHAR(250) DEFAULT NULL,
	groupsids    	BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
	dateadded    	DATE NOT NULL,
	reserve1     	VARCHAR(500) DEFAULT NULL,
	intaddedby   	BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
	hierarchypath	VARCHAR(500) DEFAULT NULL,
	reserve2     	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE history  (
	ids       	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname     	VARCHAR(250) NOT NULL,
	username  	VARCHAR(250) NOT NULL,
	dbidentity	VARCHAR(25) NOT NULL,
	intitem   	SMALLINT NOT NULL,
	groupsids 	BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
	dateadded 	DATE NOT NULL,
	intaddedby	BIGINT(20) DEFAULT 0,
	reserve1  	VARCHAR(500) DEFAULT NULL,
	reserve2  	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE items  (
	ids          	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname        	VARCHAR(250) NOT NULL,
	cnumber      	VARCHAR(250) NOT NULL,
	description  	VARCHAR(2500) DEFAULT NULL,
	groupsids    	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	accountsids  	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	contactsids  	BIGINT(20) UNSIGNED NOT NULL ,
	netvalue     	DOUBLE DEFAULT 0,
	taxvalue     	DOUBLE DEFAULT 0,
	discountvalue	DOUBLE DEFAULT 0,
	shippingvalue	DOUBLE DEFAULT 0,
	datetodo     	DATE DEFAULT NULL,
	dateend      	DATE DEFAULT NULL,
	intreminders 	INTEGER DEFAULT 0,
	inttype      	SMALLINT DEFAULT 0,
	dateadded    	DATE NOT NULL,
	intaddedby   	BIGINT(20) DEFAULT 0,
	invisible    	SMALLINT DEFAULT 0,
	intstatus    	SMALLINT DEFAULT 0,
	hierarchypath	VARCHAR(500) DEFAULT NULL,
	reserve1     	VARCHAR(500) DEFAULT NULL,
	reserve2     	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE itemstoaccounts  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	itemsids   	BIGINT(20) UNSIGNED NOT NULL NOT NULL,
	accountsids	BIGINT(20) UNSIGNED NOT NULL ,
	reserve1   	VARCHAR(500) DEFAULT NULL,
	reserve2   	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE languages  (
	ids       	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname     	VARCHAR(250) NOT NULL,
	groupsids 	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	longname  	VARCHAR(250) NOT NULL,
	filename  	VARCHAR(25) NOT NULL,
	dateadded 	DATE NOT NULL,
	intaddedby	BIGINT(20) DEFAULT 0,
	invisible 	SMALLINT DEFAULT 0,
	reserve1  	VARCHAR(500) DEFAULT NULL,
	reserve2  	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE mails  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname      	VARCHAR(1000) NOT NULL,
	dateadded  	DATE NOT NULL,
	intaddedby 	BIGINT(20) DEFAULT 0,
	groupsids  	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	invisible  	SMALLINT DEFAULT 0,
	usersids   	BIGINT(20) UNSIGNED NOT NULL ,
	description	VARCHAR(5000) NOT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE plugins  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname      	VARCHAR(250) NOT NULL,
	description	VARCHAR(550) DEFAULT NULL,
	filename   	VARCHAR(25) NOT NULL,
	intaddedby 	BIGINT(20) DEFAULT 0,
	dateadded  	DATE NOT NULL,
	groupsids  	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE pluginstousers  (
	ids       	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname     	VARCHAR(250) NOT NULL,
	usersids  	BIGINT(20) UNSIGNED NOT NULL NOT NULL,
	pluginsids	BIGINT(20) UNSIGNED NOT NULL NOT NULL,
	intaddedby	BIGINT(20) DEFAULT 0,
	dateadded 	DATE NOT NULL,
	groupsids 	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE productgroups  (
	ids             	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname           	VARCHAR(250) NOT NULL,
	description     	VARCHAR(750) DEFAULT NULL,
	defaults        	VARCHAR(250) DEFAULT NULL,
	groupsids       	BIGINT(20) UNSIGNED NOT NULL  DEFAULT 0,
	productgroupsids	BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
	dateadded       	DATE NOT NULL,
	hierarchypath   	VARCHAR(500) DEFAULT NULL,
	reserve1        	VARCHAR(500) DEFAULT NULL,
	intaddedby      	BIGINT(20) DEFAULT 0,
	reserve2        	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE productlistitems  (
	ids                	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname              	VARCHAR(2500) DEFAULT NULL,
	linkurl            	VARCHAR(250) DEFAULT NULL,
	groupsids          	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	productslistsids    	BIGINT(20) UNSIGNED NOT NULL ,
	originalproductsids	BIGINT(20) UNSIGNED NOT NULL ,
	countvalue         	DOUBLE NOT NULL DEFAULT 0,
	quantityvalue      	DOUBLE NOT NULL DEFAULT 0,
	measure            	VARCHAR(250) NOT NULL,
	description        	VARCHAR(1000) DEFAULT NULL,
	internalvalue      	DOUBLE DEFAULT 0,
	totalnetvalue      	DOUBLE DEFAULT 0,
	totalbrutvalue     	DOUBLE DEFAULT 0,
	externalvalue      	DOUBLE DEFAULT 0,
	taxpercentvalue    	DOUBLE NOT NULL DEFAULT 0,
	dateadded          	DATE NOT NULL,
	intaddedby         	BIGINT(20) DEFAULT 0,
	invisible          	SMALLINT DEFAULT 0,
	reserve1           	VARCHAR(500) DEFAULT NULL,
	reserve2           	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE productslists  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname      	VARCHAR(2500) DEFAULT NULL,
	groupsids  	BIGINT(20) UNSIGNED NOT NULL  DEFAULT 1,
	description	VARCHAR(1000) DEFAULT NULL,
	dateadded  	DATE NOT NULL,
	intaddedby 	BIGINT(20) DEFAULT 0,
	invisible  	SMALLINT DEFAULT 0,
	reserve1   	VARCHAR(500) DEFAULT NULL,
	reserve2   	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE products  (
	ids             	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname           	VARCHAR(500) NOT NULL,
	cnumber         	VARCHAR(250) NOT NULL,
	description     	VARCHAR(5000),
	externalnetvalue	DOUBLE DEFAULT 0,
	internalnetvalue	DOUBLE DEFAULT 0,
	measure         	VARCHAR(250) NOT NULL,
	taxids          	BIGINT(20) UNSIGNED NOT NULL ,
	manufacturesids	BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
	suppliersids    	BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
	groupsids       	BIGINT(20) UNSIGNED NOT NULL  DEFAULT 1,
	productgroupsids	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	url             	VARCHAR(250) DEFAULT NULL,
	ean             	VARCHAR(25) DEFAULT NULL,
	reference       	VARCHAR(50) DEFAULT NULL,
	dateadded       	DATE NOT NULL,
	intaddedby      	BIGINT(20) DEFAULT 0,
	invisible       	SMALLINT DEFAULT 0,
	inttype         	SMALLINT NOT NULL,
	defaultimage    	VARCHAR(30) DEFAULT NULL,
	reserve1        	VARCHAR(500) DEFAULT NULL,
	reserve2        	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE reminders  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname      	VARCHAR(250) NOT NULL,
	description	VARCHAR(550) DEFAULT NULL,
	stagesids  	BIGINT(20) UNSIGNED NOT NULL,
	itemsids   	BIGINT(20) UNSIGNED NOT NULL,
	extravalue 	DOUBLE DEFAULT 0,
	intaddedby 	BIGINT(20) DEFAULT 0,
	dateadded  	DATE NOT NULL,
	groupsids  	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE revenues  (
	ids            	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	groupsids      	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	cnumber        	VARCHAR(250) DEFAULT NULL,
	cname          	VARCHAR(250) DEFAULT NULL,
	accountsids    	BIGINT(20) UNSIGNED NOT NULL,
	netvalue       	DOUBLE DEFAULT 0,
	taxpercentvalue	DOUBLE DEFAULT 0,
	brutvalue      	DOUBLE DEFAULT 0,
	description    	VARCHAR(5000) DEFAULT NULL,
	dateadded      	DATE NOT NULL,
	intaddedby     	BIGINT(20) DEFAULT 0,
	invisible      	SMALLINT DEFAULT 0,
	reserve1       	VARCHAR(500) DEFAULT NULL,
	reserve2       	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE schedule  (
	ids          	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname        	VARCHAR(250) NOT NULL,
	groupsids    	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	usersids     	BIGINT(20) UNSIGNED NOT NULL ,
	itemsids     	BIGINT(20) UNSIGNED NOT NULL ,
	stopdate     	DATE NOT NULL,
	startdate    	DATE NOT NULL,
	nextdate     	DATE NOT NULL,
	isdone       	SMALLINT DEFAULT 0,
	intervalmonth	SMALLINT NOT NULL,
	dateadded    	DATE NOT NULL,
	intaddedby   	BIGINT(20) DEFAULT 0,
	invisible    	SMALLINT DEFAULT 0,
	reserve1     	VARCHAR(500) DEFAULT NULL,
	reserve2     	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE searchindex  (
	ids       	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	dbidentity	VARCHAR(25) NOT NULL,
	groupsids 	BIGINT(20) UNSIGNED NOT NULL ,
	rowid     	BIGINT(20) NOT NULL,
	text      	VARCHAR(5000) DEFAULT NULL
	)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE stages  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname      	VARCHAR(250) NOT NULL,
	description	VARCHAR(550) DEFAULT NULL,
	extravalue 	DOUBLE DEFAULT 0,
	intaddedby 	BIGINT(20) DEFAULT 0,
	dateadded  	DATE NOT NULL,
	groupsids  	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE subitems  (
	ids                	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname              	VARCHAR(5000) DEFAULT NULL,
	itemsids           	BIGINT(20) UNSIGNED NOT NULL ,
	groupsids          	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	originalproductsids	BIGINT(20) UNSIGNED DEFAULT NULL,
	countvalue         	DOUBLE NOT NULL DEFAULT 0,
	quantityvalue      	DOUBLE NOT NULL DEFAULT 0,
	measure            	VARCHAR(250) NOT NULL,
	linkurl            	VARCHAR(1000) DEFAULT NULL,
	description        	VARCHAR(1000) DEFAULT NULL,
	internalvalue      	DOUBLE DEFAULT 0,
	totalnetvalue      	DOUBLE DEFAULT 0,
	totalbrutvalue     	DOUBLE DEFAULT 0,
	externalvalue      	DOUBLE DEFAULT 0,
	taxpercentvalue    	DOUBLE NOT NULL DEFAULT 0,
	datedelivery       	DATE DEFAULT NULL,
	dateadded          	DATE NOT NULL,
	intaddedby         	BIGINT(20) DEFAULT 0,
	invisible          	SMALLINT DEFAULT 0,
	reserve1           	VARCHAR(500) DEFAULT NULL,
	reserve2           	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE tablelock  (
	ids     	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname   	VARCHAR(250),
	rowid   	BIGINT(20) NOT NULL,
	usersids	BIGINT(20) UNSIGNED NOT NULL ,
	reserve1	VARCHAR(500) DEFAULT NULL,
	reserve2	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE tax  (
	ids       	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname     	VARCHAR(250),
	taxvalue  	DOUBLE DEFAULT 0,
	identifier	VARCHAR(250) DEFAULT NULL,
	groupsids 	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	country   	VARCHAR(50) DEFAULT NULL,
	dateadded 	DATE NOT NULL,
	intaddedby	BIGINT(20) DEFAULT 0,
	invisible 	SMALLINT DEFAULT 0,
	reserve1  	VARCHAR(500) DEFAULT NULL,
	reserve2  	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE templates  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname      	VARCHAR(250) NOT NULL,
	format     	VARCHAR(55) DEFAULT NULL,
	description	VARCHAR(550) DEFAULT NULL,
	intsize    	BIGINT(20) DEFAULT 0,
	mimetype   	VARCHAR(25) DEFAULT NULL,
	filename   	VARCHAR(25) NOT NULL,
	intaddedby 	BIGINT(20) DEFAULT 0,
	dateadded  	DATE NOT NULL,
	groupsids  	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE templatestousers  (
	ids         	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname       	VARCHAR(250) NOT NULL,
	usersids    	BIGINT(20) UNSIGNED NOT NULL ,
	templatesids	BIGINT(20) UNSIGNED NOT NULL NOT NULL,
	groupsids   	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE trashbin  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname      	VARCHAR(500),
	rowid      	BIGINT(20) NOT NULL,
	description	VARCHAR(2500),
	deleteme   	SMALLINT DEFAULT 1,
	reserve1   	VARCHAR(500) DEFAULT NULL,
	reserve2   	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE userproperties  (
	ids       	BIGINT(20) DEFAULT 1,
	cname     	VARCHAR(250) NOT NULL,
	value     	VARCHAR(250) NOT NULL,
	usersids  	BIGINT(20) UNSIGNED NOT NULL NOT NULL,
	dateadded 	DATE NOT NULL,
	intaddedby	BIGINT(20) DEFAULT 0,
	groupsids 	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	invisible 	SMALLINT DEFAULT 0
	)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE users  (
	ids              	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname            	VARCHAR(250) NOT NULL,
	groupsids        	BIGINT(20) UNSIGNED NOT NULL DEFAULT 1,
	intdefaultaccount	BIGINT(20) DEFAULT 1,
	compsids         	BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
	intdefaultstatus 	BIGINT(20) DEFAULT 1,
	fullname         	VARCHAR(250) NOT NULL,
	password         	VARCHAR(250) NOT NULL,
	laf              	VARCHAR(50) DEFAULT NULL,
	locale           	VARCHAR(50) DEFAULT NULL,
	defcountry       	VARCHAR(50) DEFAULT NULL,
	mail             	VARCHAR(50) DEFAULT NULL,
	language         	VARCHAR(50) DEFAULT NULL,
	inthighestright  	SMALLINT DEFAULT 3,
	isenabled        	SMALLINT DEFAULT 1,
	isrgrouped       	SMALLINT DEFAULT 0,
	isloggedin       	SMALLINT DEFAULT 0,
	datelastlog      	DATE DEFAULT NULL,
	dateadded        	DATE NOT NULL,
	intaddedby       	BIGINT(20) DEFAULT 0,
	invisible        	SMALLINT DEFAULT 0,
	reserve1         	VARCHAR(500) DEFAULT NULL,
	reserve2         	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE webshops  (
	ids                 	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	cname               	VARCHAR(250) NOT NULL,
	description         	VARCHAR(750) DEFAULT NULL,
	groupsids           	BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
	dateadded           	DATE NOT NULL,
	isrequestcompression	SMALLINT DEFAULT 0,
	isauthenticated     	SMALLINT DEFAULT 0,
	username            	VARCHAR(50) DEFAULT NULL,
	passw               	VARCHAR(50) DEFAULT NULL,
	reserve1            	VARCHAR(500) DEFAULT NULL,
	intaddedby          	BIGINT(20) DEFAULT 0,
	interv               	BIGINT(20) DEFAULT 0,
	url                 	VARCHAR(500) DEFAULT NULL,
	reserve2            	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE wscontactsmapping  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	webshopsids	BIGINT(20) UNSIGNED NOT NULL ,
	cname      	VARCHAR(250) NOT NULL,
	groupsids  	BIGINT(20) UNSIGNED NOT NULL ,
	contactsids	BIGINT(20) UNSIGNED NOT NULL ,
	wscontact  	VARCHAR(250) NOT NULL,
	dateadded  	DATE DEFAULT NULL,
	intaddedby 	BIGINT(20) DEFAULT 0,
	invisible  	SMALLINT DEFAULT 0,
	reserve1   	VARCHAR(500) DEFAULT NULL,
	reserve2   	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE TABLE wsitemsmapping  (
	ids        	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	webshopsids	BIGINT(20) UNSIGNED NOT NULL ,
	cname      	VARCHAR(250) NOT NULL,
	groupsids  	BIGINT(20) UNSIGNED NOT NULL ,
	itemsids   	BIGINT(20) UNSIGNED NOT NULL ,
	wsitem     	VARCHAR(250) NOT NULL,
	dateadded  	DATE DEFAULT NULL,
	intaddedby 	BIGINT(20) DEFAULT 0,
	invisible  	SMALLINT DEFAULT 0,
	reserve1   	VARCHAR(500) DEFAULT NULL,
	reserve2   	VARCHAR(500) DEFAULT NULL
)ENGINE=MyISAM  DEFAULT CHARSET=utf8;

CREATE UNIQUE INDEX SQL091006084310810
	ON groups(cname);

CREATE UNIQUE INDEX SQL091006084310820
	ON groups(ids);

CREATE UNIQUE INDEX SQL091006084311391
	ON productgroups(cname, groupsids);

CREATE UNIQUE INDEX SQL091006084311390
	ON productgroups(ids);

CREATE UNIQUE INDEX SQL091006084311800
	ON history(ids);

CREATE UNIQUE INDEX SQL091006084312110
	ON countries(iso);

CREATE UNIQUE INDEX SQL091006084312111
	ON countries(ids);

CREATE UNIQUE INDEX SQL091006084312620
	ON globalsettings(cname, groupsids);

CREATE UNIQUE INDEX SQL091006084312900
	ON tax(ids);

CREATE INDEX SQL091006084312890
	ON tax(groupsids);

CREATE UNIQUE INDEX SQL091006084313452
	ON contacts(cnumber, groupsids);

CREATE UNIQUE INDEX SQL091006084313451
	ON contacts(ids);

CREATE INDEX SQL091006084313450
	ON contacts(groupsids);

CREATE UNIQUE INDEX SQL091006084313971
	ON users(cname, groupsids);

CREATE UNIQUE INDEX SQL091006084313970
	ON users(ids);

CREATE INDEX SQL091006084313960
	ON users(groupsids);

CREATE UNIQUE INDEX SQL091006084314570
	ON files(cname);

CREATE UNIQUE INDEX SQL091006084314572
	ON files(ids);

CREATE INDEX SQL091006084314571
	ON files(groupsids);

CREATE UNIQUE INDEX SQL091006084315174
	ON languages(cname, groupsids);

CREATE UNIQUE INDEX SQL091006084315171
	ON languages(longname);

CREATE UNIQUE INDEX SQL091006084315173
	ON languages(ids);

CREATE INDEX SQL091006084315170
	ON languages(groupsids);

CREATE INDEX SQL091006084315172
	ON languages(filename);

CREATE UNIQUE INDEX SQL091006084315882
	ON favourites(ids);

CREATE INDEX SQL091006084315880
	ON favourites(usersids);

CREATE INDEX SQL091006084315881
	ON favourites(groupsids);

CREATE UNIQUE INDEX SQL091006084316451
	ON accounts(ids);

CREATE INDEX SQL091006084316450
	ON accounts(groupsids);

CREATE UNIQUE INDEX SQL091006084316862
	ON items(cnumber, inttype);

CREATE UNIQUE INDEX SQL091006084316861
	ON items(ids);

CREATE INDEX SQL091006084316850
	ON items(groupsids);

CREATE INDEX SQL091006084316851
	ON items(accountsids);

CREATE INDEX SQL091006084316860
	ON items(contactsids);

CREATE UNIQUE INDEX SQL091006084317902
	ON subitems(ids);

CREATE INDEX SQL091006084317900
	ON subitems(itemsids);

CREATE INDEX SQL091006084317901
	ON subitems(groupsids);

CREATE UNIQUE INDEX SQL091006084318483
	ON schedule(ids);

CREATE INDEX SQL091006084318480
	ON schedule(groupsids);

CREATE INDEX SQL091006084318481
	ON schedule(usersids);

CREATE INDEX SQL091006084318482
	ON schedule(itemsids);

CREATE UNIQUE INDEX SQL091006084319621
	ON products(cnumber, groupsids);

CREATE UNIQUE INDEX SQL091006084319620
	ON products(ids);

CREATE INDEX SQL091006084319610
	ON products(taxids);

CREATE INDEX SQL091006084319611
	ON products(groupsids);

CREATE INDEX SQL091006084319612
	ON products(productgroupsids);

CREATE UNIQUE INDEX SQL091006084320591
	ON productslists(ids);

CREATE INDEX SQL091006084320590
	ON productslists(groupsids);

CREATE UNIQUE INDEX SQL091006084321141
	ON productlistitems(ids);

CREATE INDEX SQL091006084321130
	ON productlistitems(groupsids);

CREATE INDEX SQL091006084321131
	ON productlistitems(productslistsids);

CREATE INDEX SQL091006084321140
	ON productlistitems(originalproductsids);

CREATE INDEX SQL091006084322190
	ON userproperties(groupsids);

CREATE UNIQUE INDEX SQL091006084322552
	ON mails(ids);

CREATE INDEX SQL091006084322550
	ON mails(groupsids);

CREATE INDEX SQL091006084322551
	ON mails(usersids);

CREATE UNIQUE INDEX SQL091006084323381
	ON comps(ids);

CREATE INDEX SQL091006084323380
	ON comps(groupsids);

CREATE UNIQUE INDEX SQL091006084324160
	ON expenses(ids);

CREATE INDEX SQL091006084324140
	ON expenses(groupsids);

CREATE INDEX SQL091006084324141
	ON expenses(accountsids);

CREATE UNIQUE INDEX SQL091006084324810
	ON revenues(ids);

CREATE INDEX SQL091006084324790
	ON revenues(groupsids);

CREATE INDEX SQL091006084324800
	ON revenues(accountsids);

CREATE UNIQUE INDEX SQL091006084325551
	ON webshops(cname, groupsids);

CREATE UNIQUE INDEX SQL091006084325550
	ON webshops(ids);

CREATE UNIQUE INDEX SQL091006084326054
	ON wscontactsmapping(wscontact, groupsids, webshopsids);

CREATE UNIQUE INDEX SQL091006084326053
	ON wscontactsmapping(ids);

CREATE INDEX SQL091006084326050
	ON wscontactsmapping(webshopsids);

CREATE INDEX SQL091006084326051
	ON wscontactsmapping(groupsids);

CREATE INDEX SQL091006084326052
	ON wscontactsmapping(contactsids);

CREATE UNIQUE INDEX SQL091006084326881
	ON wsitemsmapping(wsitem, groupsids, webshopsids);

CREATE UNIQUE INDEX SQL091006084326880
	ON wsitemsmapping(ids);

CREATE INDEX SQL091006084326870
	ON wsitemsmapping(webshopsids);

CREATE INDEX SQL091006084326871
	ON wsitemsmapping(groupsids);

CREATE INDEX SQL091006084326872
	ON wsitemsmapping(itemsids);

CREATE UNIQUE INDEX SQL091006084327910
	ON tablelock(ids);

CREATE UNIQUE INDEX SQL091006084327911
	ON tablelock(cname, rowid);

CREATE INDEX SQL091006084327890
	ON tablelock(usersids);

CREATE UNIQUE INDEX SQL091006084328480
	ON itemstoaccounts(ids);

CREATE INDEX SQL091006084328460
	ON itemstoaccounts(itemsids);

CREATE INDEX SQL091006084328461
	ON itemstoaccounts(accountsids);

CREATE UNIQUE INDEX SQL091006084329120
	ON trashbin(ids);

CREATE UNIQUE INDEX SQL091006084329671
	ON filestocontacts(ids);

CREATE INDEX SQL091006084329650
	ON filestocontacts(contactsids);

CREATE INDEX SQL091006084329651
	ON filestocontacts(filename);

CREATE INDEX SQL091006084329670
	ON filestocontacts(groupsids);

CREATE UNIQUE INDEX SQL091006084330492
	ON templates(ids);

CREATE INDEX SQL091006084330490
	ON templates(filename);

CREATE INDEX SQL091006084330491
	ON templates(groupsids);

CREATE UNIQUE INDEX SQL091006084331263
	ON filestoitems(ids);

CREATE INDEX SQL091006084331260
	ON filestoitems(itemsids);

CREATE INDEX SQL091006084331261
	ON filestoitems(filename);

CREATE INDEX SQL091006084331262
	ON filestoitems(groupsids);

CREATE UNIQUE INDEX SQL091006084332163
	ON filestoproducts(ids);

CREATE INDEX SQL091006084332160
	ON filestoproducts(productsids);

CREATE INDEX SQL091006084332161
	ON filestoproducts(filename);

CREATE INDEX SQL091006084332162
	ON filestoproducts(groupsids);

CREATE UNIQUE INDEX SQL091006084333560
	ON plugins(ids);

CREATE INDEX SQL091006084333550
	ON plugins(filename);

CREATE INDEX SQL091006084333551
	ON plugins(groupsids);

CREATE UNIQUE INDEX SQL091006084334241
	ON pluginstousers(ids);

CREATE INDEX SQL091006084334230
	ON pluginstousers(pluginsids);

CREATE INDEX SQL091006084334240
	ON pluginstousers(groupsids);

CREATE UNIQUE INDEX SQL091006084334891
	ON formatstousers(ids);

CREATE INDEX SQL091006084334890
	ON formatstousers(usersids);

CREATE UNIQUE INDEX SQL091006084335461
	ON templatestousers(ids);

CREATE INDEX SQL091006084335450
	ON templatestousers(usersids);

CREATE INDEX SQL091006084335451
	ON templatestousers(templatesids);

CREATE INDEX SQL091006084335460
	ON templatestousers(groupsids);

CREATE UNIQUE INDEX SQL091006084336332
	ON addresses(ids);

CREATE INDEX SQL091006084336330
	ON addresses(groupsids);

CREATE INDEX SQL091006084336331
	ON addresses(contactsids);

CREATE UNIQUE INDEX SQL091006084336882
	ON reminders(ids);

CREATE INDEX SQL091006084336880
	ON reminders(itemsids);

CREATE INDEX SQL091006084336881
	ON reminders(groupsids);

CREATE UNIQUE INDEX SQL091006084337581
	ON stages(ids);

CREATE INDEX SQL091006084337580
	ON stages(groupsids);

ALTER TABLE contacts
	ADD CONSTRAINT CONST3
	 UNIQUE (cnumber, groupsids) ;

ALTER TABLE countries
	ADD CONSTRAINT SQL0910060843121101
	 UNIQUE (iso) ;

ALTER TABLE files
	ADD CONSTRAINT SQL0910060843145701
	 UNIQUE (cname) ;

ALTER TABLE globalsettings
	ADD CONSTRAINT CONST2
	 UNIQUE (cname, groupsids) ;

ALTER TABLE groups
	ADD CONSTRAINT SQL0910060843108101
	 UNIQUE (cname) ;

ALTER TABLE items
	ADD CONSTRAINT CONST7
	 UNIQUE (cnumber, inttype) ;

ALTER TABLE languages
	ADD CONSTRAINT SQL0910060843151711
	 UNIQUE (longname) ;

ALTER TABLE languages
	ADD CONSTRAINT CONST6
	 UNIQUE (cname, groupsids) ;

ALTER TABLE productgroups
	ADD CONSTRAINT CONST1
	 UNIQUE (cname, groupsids) ;

ALTER TABLE products
	ADD CONSTRAINT CONST8
	 UNIQUE (cnumber, groupsids) ;

ALTER TABLE tablelock
	ADD CONSTRAINT ONE_LOCK
	 UNIQUE (cname, rowid) ;

ALTER TABLE users
	ADD CONSTRAINT CONST4
	 UNIQUE (cname, groupsids) ;

ALTER TABLE webshops
	ADD CONSTRAINT CONST9
	 UNIQUE (cname, groupsids) ;

ALTER TABLE wscontactsmapping
	ADD CONSTRAINT CONST10
	 UNIQUE (wscontact, groupsids, webshopsids) ;

ALTER TABLE wsitemsmapping
	ADD CONSTRAINT CONST11
	 UNIQUE (wsitem, groupsids, webshopsids) ;

ALTER TABLE accounts
	ADD CONSTRAINT SQL0910060843164501
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE addresses
	ADD CONSTRAINT SQL0910060843363311
	FOREIGN KEY(contactsids)
	REFERENCES contacts(ids);

ALTER TABLE addresses
	ADD CONSTRAINT SQL0910060843363301
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE comps
	ADD CONSTRAINT SQL0910060843233801
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE contacts
	ADD CONSTRAINT SQL0910060843134501
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE expenses
	ADD CONSTRAINT SQL0910060843241411
	FOREIGN KEY(accountsids)
	REFERENCES accounts(ids);

ALTER TABLE expenses
	ADD CONSTRAINT SQL0910060843241401
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE favourites
	ADD CONSTRAINT SQL0910060843158811
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE favourites
	ADD CONSTRAINT SQL0910060843158801
	FOREIGN KEY(usersids)
	REFERENCES users(ids)
	ON DELETE CASCADE ;

ALTER TABLE files
	ADD CONSTRAINT SQL0910060843145711
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE filestocontacts
	ADD CONSTRAINT SQL0910060843296701
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE filestocontacts
	ADD CONSTRAINT SQL0910060843296511
	FOREIGN KEY(filename)
	REFERENCES files(cname)
	ON DELETE CASCADE ;

ALTER TABLE filestocontacts
	ADD CONSTRAINT SQL0910060843296501
	FOREIGN KEY(contactsids)
	REFERENCES contacts(ids)
	ON DELETE CASCADE ;

ALTER TABLE filestoitems
	ADD CONSTRAINT SQL0910060843312621
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE filestoitems
	ADD CONSTRAINT SQL0910060843312611
	FOREIGN KEY(filename)
	REFERENCES files(cname)
	ON DELETE CASCADE ;

ALTER TABLE filestoitems
	ADD CONSTRAINT SQL0910060843312601
	FOREIGN KEY(itemsids)
	REFERENCES items(ids)
	ON DELETE CASCADE ;

ALTER TABLE filestoproducts
	ADD CONSTRAINT SQL0910060843321621
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE filestoproducts
	ADD CONSTRAINT SQL0910060843321611
	FOREIGN KEY(filename)
	REFERENCES files(cname)
	ON DELETE CASCADE ;

ALTER TABLE filestoproducts
	ADD CONSTRAINT SQL0910060843321601
	FOREIGN KEY(productsids)
	REFERENCES products(ids)
	ON DELETE CASCADE ;

ALTER TABLE formatstousers
	ADD CONSTRAINT SQL0910060843348901
	FOREIGN KEY(usersids)
	REFERENCES users(ids)
	ON DELETE CASCADE ;

ALTER TABLE items
	ADD CONSTRAINT SQL0910060843168601
	FOREIGN KEY(contactsids)
	REFERENCES contacts(ids)
	ON DELETE CASCADE ;

ALTER TABLE items
	ADD CONSTRAINT SQL0910060843168511
	FOREIGN KEY(accountsids)
	REFERENCES accounts(ids);

ALTER TABLE items
	ADD CONSTRAINT SQL0910060843168501
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE itemstoaccounts
	ADD CONSTRAINT SQL0910060843284611
	FOREIGN KEY(accountsids)
	REFERENCES accounts(ids)
	ON DELETE CASCADE ;

ALTER TABLE itemstoaccounts
	ADD CONSTRAINT SQL0910060843284601
	FOREIGN KEY(itemsids)
	REFERENCES items(ids)
	ON DELETE CASCADE ;

ALTER TABLE languages
	ADD CONSTRAINT SQL0910060843151721
	FOREIGN KEY(filename)
	REFERENCES files(cname)
	ON DELETE CASCADE ;

ALTER TABLE languages
	ADD CONSTRAINT SQL0910060843151701
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE mails
	ADD CONSTRAINT SQL0910060843225511
	FOREIGN KEY(usersids)
	REFERENCES users(ids)
	ON DELETE CASCADE ;

ALTER TABLE mails
	ADD CONSTRAINT SQL0910060843225501
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE plugins
	ADD CONSTRAINT SQL0910060843335511
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE plugins
	ADD CONSTRAINT SQL0910060843335501
	FOREIGN KEY(filename)
	REFERENCES files(cname)
	ON DELETE CASCADE ;

ALTER TABLE pluginstousers
	ADD CONSTRAINT SQL0910060843342401
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE pluginstousers
	ADD CONSTRAINT SQL0910060843342301
	FOREIGN KEY(pluginsids)
	REFERENCES plugins(ids)
	ON DELETE CASCADE ;

ALTER TABLE productlistitems
	ADD CONSTRAINT SQL0910060843211401
	FOREIGN KEY(originalproductsids)
	REFERENCES products(ids)
	ON DELETE CASCADE ;

ALTER TABLE productlistitems
	ADD CONSTRAINT SQL0910060843211311
	FOREIGN KEY(productslistsids)
	REFERENCES productslists(ids)
	ON DELETE CASCADE ;

ALTER TABLE productlistitems
	ADD CONSTRAINT SQL0910060843211301
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE productslists
	ADD CONSTRAINT SQL0910060843205901
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE products
	ADD CONSTRAINT SQL0910060843196121
	FOREIGN KEY(productgroupsids)
	REFERENCES productsgroups(ids);

ALTER TABLE products
	ADD CONSTRAINT SQL0910060843196111
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE products
	ADD CONSTRAINT SQL0910060843196101
	FOREIGN KEY(taxids)
	REFERENCES tax(ids);

ALTER TABLE reminders
	ADD CONSTRAINT SQL0910060843368811
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE reminders
	ADD CONSTRAINT SQL0910060843368801
	FOREIGN KEY(itemsids)
	REFERENCES items(ids)
	ON DELETE CASCADE ;

ALTER TABLE revenues
	ADD CONSTRAINT SQL0910060843248001
	FOREIGN KEY(accountsids)
	REFERENCES accounts(ids);

ALTER TABLE revenues
	ADD CONSTRAINT SQL0910060843247901
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE schedule
	ADD CONSTRAINT SQL0910060843184821
	FOREIGN KEY(itemsids)
	REFERENCES items(ids)
	ON DELETE CASCADE ;

ALTER TABLE schedule
	ADD CONSTRAINT SQL0910060843184811
	FOREIGN KEY(usersids)
	REFERENCES users(ids)
	ON DELETE CASCADE ;

ALTER TABLE schedule
	ADD CONSTRAINT SQL0910060843184801
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE stages
	ADD CONSTRAINT SQL0910060843375801
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE subitems
	ADD CONSTRAINT SQL0910060843179011
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE subitems
	ADD CONSTRAINT SQL0910060843179001
	FOREIGN KEY(itemsids)
	REFERENCES items(ids)
	ON DELETE CASCADE ;

ALTER TABLE tablelock
	ADD CONSTRAINT SQL0910060843278901
	FOREIGN KEY(usersids)
	REFERENCES users(ids)
	ON DELETE CASCADE ;

ALTER TABLE tax
	ADD CONSTRAINT SQL0910060843128901
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE templates
	ADD CONSTRAINT SQL0910060843304911
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE templates
	ADD CONSTRAINT SQL0910060843304901
	FOREIGN KEY(filename)
	REFERENCES files(cname)
	ON DELETE CASCADE ;

ALTER TABLE templatestousers
	ADD CONSTRAINT SQL0910060843354601
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE templatestousers
	ADD CONSTRAINT SQL0910060843354511
	FOREIGN KEY(templatesids)
	REFERENCES templates(ids)
	ON DELETE CASCADE ;

ALTER TABLE templatestousers
	ADD CONSTRAINT SQL0910060843354501
	FOREIGN KEY(usersids)
	REFERENCES users(ids)
	ON DELETE CASCADE ;

ALTER TABLE userproperties
	ADD CONSTRAINT SQL0910060843221901
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE users
	ADD CONSTRAINT SQL0910060843139601
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids);

ALTER TABLE wscontactsmapping
	ADD CONSTRAINT SQL0910060843260521
	FOREIGN KEY(contactsids)
	REFERENCES contacts(ids)
	ON DELETE CASCADE ;

ALTER TABLE wscontactsmapping
	ADD CONSTRAINT SQL0910060843260511
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids)
	ON DELETE CASCADE ;

ALTER TABLE wscontactsmapping
	ADD CONSTRAINT SQL0910060843260501
	FOREIGN KEY(webshopsids)
	REFERENCES webshops(ids)
	ON DELETE CASCADE ;

ALTER TABLE wsitemsmapping
	ADD CONSTRAINT SQL0910060843268721
	FOREIGN KEY(itemsids)
	REFERENCES contacts(ids)
	ON DELETE CASCADE ;

ALTER TABLE wsitemsmapping
	ADD CONSTRAINT SQL0910060843268711
	FOREIGN KEY(groupsids)
	REFERENCES groups(ids)
	ON DELETE CASCADE ;

ALTER TABLE wsitemsmapping
	ADD CONSTRAINT SQL0910060843268701
	FOREIGN KEY(webshopsids)
	REFERENCES webshops(ids)
	ON DELETE CASCADE ;



DELIMITER %

CREATE TRIGGER contacts_INDEXER1
	AFTER INSERT ON contacts

	FOR EACH ROW BEGIN
	INSERT INTO searchindex (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids, 'contacts',NEW.ids,NEW.cnumber||' '||NEW.taxnumber||' '||NEW.title||' '||NEW.country||' '|| NEW.prename||' '|| NEW.cname||' '|| NEW.street||' '||NEW.zip||' '|| NEW.city ||' '||NEW.mainphone||' '||NEW.fax||' '||NEW.mobilephone||' '||NEW.workphone||' '||NEW.mailaddress||' '||NEW.company||' '|| NEW.department||' '||NEW.website||' '||NEW.notes);
END;
	%

CREATE TRIGGER contacts_INDEXER2
	AFTER UPDATE ON contacts

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'contacts' AND  rowid = NEW.ids;
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'contacts',NEW.ids,NEW.cnumber||' '||NEW.taxnumber||' '||NEW.title||' '||NEW.country||' '|| NEW.prename||' '|| NEW.cname||' '|| NEW.street||' '||NEW.zip||' '|| NEW.city ||' '||NEW.mainphone||' '||NEW.fax||' '||NEW.mobilephone||' '||NEW.workphone||' '||NEW.mailaddress||' '||NEW.company||' '|| NEW.department||' '||NEW.website||' '||NEW.notes);
	INSERT INTO trashbin (deleteme, cname, rowid, description) VALUES (NEW.invisible,'contacts',NEW.ids,NEW.cnumber||' ('|| NEW.cname||')');
END;
%

CREATE TRIGGER contacts_INDEXER4
	AFTER DELETE ON contacts

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'contacts' AND  rowid = OLD.ids;
	DELETE FROM trashbin WHERE cname = 'contacts' AND  rowid = OLD.ids;
END;
%

CREATE TRIGGER expenses_INDEXER1
	AFTER INSERT ON expenses

	FOR EACH ROW BEGIN
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'expenses',NEW.ids,NEW.cname||' '||NEW.dateadded);
END;
	%
CREATE TRIGGER expenses_INDEXER2
	AFTER UPDATE ON expenses

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'expenses' AND  rowid = NEW.ids;
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'expenses',NEW.ids,NEW.cname||' '||NEW.dateadded);
	INSERT INTO trashbin (deleteme, cname, rowid, description) VALUES (NEW.invisible,'expenses',NEW.ids,NEW.cname);
END;
%

CREATE TRIGGER expenses_INDEXER4
	AFTER DELETE ON expenses

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'expenses' AND  rowid = OLD.ids;
	DELETE FROM trashbin WHERE cname = 'expenses' AND  rowid = OLD.ids;
END;
%

CREATE TRIGGER filestocontacts_INDEXER1
	AFTER INSERT ON filestocontacts

	FOR EACH ROW BEGIN
	INSERT INTO searchindex (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'filestocontacts',NEW.ids,NEW.cname||' '||NEW.description||' '||NEW.filename);
END;
	%
CREATE TRIGGER filestocontacts_INDEXER2
	AFTER UPDATE ON filestocontacts

	FOR EACH ROW BEGIN
  DELETE FROM searchindex WHERE dbidentity = 'filestocontacts' AND  rowid = NEW.ids;
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'filestocontacts',NEW.ids,NEW.cname||' '||NEW.description||' '||NEW.filename);
END;
	%

CREATE TRIGGER filestocontacts_INDEXER4
	AFTER DELETE ON filestocontacts

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'filestocontacts' AND  rowid = OLD.ids;

	END;
	%
CREATE TRIGGER groups_INDEXER1
	AFTER INSERT ON groups

	FOR EACH ROW BEGIN
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'groups',NEW.ids,NEW.cname||' '||NEW.description||' '||NEW.dateadded);
END;
	%
CREATE TRIGGER groups_INDEXER2
	AFTER UPDATE ON groups

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'groups' AND  rowid = NEW.ids;
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'groups',NEW.ids,NEW.cname||' '||NEW.description||' '||NEW.dateadded);
END;
%
CREATE TRIGGER groups_INDEXER4
	AFTER DELETE ON groups

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'groups' AND  rowid = OLD.ids;
END;
	%
CREATE TRIGGER items_INDEXER1
	AFTER INSERT ON items

	FOR EACH ROW BEGIN
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'items',NEW.ids,NEW.cname||' '||NEW.dateadded);
END;
	%
CREATE TRIGGER items_INDEXER2
	AFTER UPDATE ON items

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'items' AND  rowid = NEW.ids;
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'items',NEW.ids,NEW.cname||' '||NEW.dateadded);
	INSERT INTO trashbin (deleteme, cname, rowid, description) VALUES (NEW.invisible,'items',NEW.ids,NEW.cname);
END;
%

CREATE TRIGGER items_INDEXER4
	AFTER DELETE ON items

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'items' AND  rowid = OLD.ids;
  DELETE FROM trashbin WHERE cname = 'items' AND  rowid = OLD.ids;
END;
	%

CREATE TRIGGER products_INDEXER1
	AFTER INSERT ON products

	FOR EACH ROW BEGIN
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'products',NEW.ids,NEW.cname||' '||NEW.cnumber||' '||NEW.description||' '||NEW.dateadded);
END;
	%
CREATE TRIGGER products_INDEXER2
	AFTER UPDATE ON products

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'products' AND  rowid = NEW.ids;
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'products',NEW.ids,NEW.cname||' '||NEW.cnumber||' '||NEW.description||' '||NEW.dateadded);
	INSERT INTO trashbin (deleteme, cname, rowid, description) VALUES (NEW.invisible, 'products',NEW.ids,NEW.cnumber||' ('|| NEW.cname||')');
END;
%

CREATE TRIGGER products_INDEXER4
	AFTER DELETE ON products

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'products' AND  rowid = OLD.ids;
	DELETE FROM trashbin WHERE cname = 'products' AND  rowid = OLD.ids;
END;
	%


CREATE TRIGGER revenues_INDEXER1
	AFTER INSERT ON revenues

	FOR EACH ROW BEGIN
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'revenues',NEW.ids,NEW.cname||' '||NEW.dateadded);
END;
	%
CREATE TRIGGER revenues_INDEXER2
	AFTER UPDATE ON revenues

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'revenues' AND  rowid = NEW.ids;
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'revenues',NEW.ids,NEW.cname||' '||NEW.dateadded);
	INSERT INTO trashbin (deleteme, cname, rowid, description) VALUES (NEW.invisible,'revenues',NEW.ids,NEW.cname);
END;
	%
CREATE TRIGGER revenues_INDEXER4
	AFTER DELETE ON revenues

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'revenues' AND  rowid = OLD.ids;
	DELETE FROM trashbin WHERE cname = 'revenues' AND  rowid = OLD.ids;
END;
%
CREATE TRIGGER subitems_INDEXER1
	AFTER INSERT ON subitems

	FOR EACH ROW BEGIN
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'subitems',NEW.ids,NEW.cname||' '||NEW.description||' '||NEW.dateadded);
END;
	%
CREATE TRIGGER subitems_INDEXER2
	AFTER UPDATE ON subitems

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'subitems' AND  rowid = NEW.ids;
	INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (NEW.groupsids,'subitems',NEW.ids,NEW.cname||' '||NEW.description||' '||NEW.dateadded);
END;
	%

CREATE TRIGGER subitems_INDEXER4
	AFTER DELETE ON subitems

	FOR EACH ROW BEGIN
	DELETE FROM searchindex WHERE dbidentity = 'subitems' AND  rowid = OLD.ids;
END;
	%
#Does not work in mysql!
#CREATE TRIGGER THRASH_HANDLER1
#	AFTER INSERT ON trashbin
#	FOR EACH ROW BEGIN
#	DELETE FROM trashbin WHERE deleteme = 0;
#	DELETE FROM trashbin WHERE ids IN (SELECT ids FROM trashbin WHERE EXISTS( SELECT ids FROM trashbin AS tmptable WHERE trashbin.cname = tmptable.cname AND trashbin.rowid = tmptable.rowid HAVING trashbin.ids < MAX(tmptable.ids) ) );
#END;
#%
DELIMITER ;


INSERT INTO groups(cname, description, defaults, groupsids, dateadded, reserve1, intaddedby, hierarchypath, reserve2)
  VALUES( 'All Groups', 'This group is visible to everyone.', NULL, 0, '2009-04-03', NULL, 0, NULL, NULL);
INSERT INTO accounts(intaccountclass, cname, description, taxvalue, dateadded, intaddedby, intparentaccount, groupsids, invisible, intaccounttype, intprofitfid, inttaxfid, inttaxuid, frame, hierarchypath)
  VALUES( 0, 'All Accounts', 'This account is the parent account of all account frames.', 0.0, '2009-04-03', 0, 0, 1, 0, 0, 0, 0, 0, 'builtin', NULL);
INSERT INTO accounts(intaccountclass, cname, description, taxvalue, dateadded, intaddedby, intparentaccount, groupsids, invisible, intaccounttype, intprofitfid, inttaxfid, inttaxuid, frame, hierarchypath)
  VALUES( 0, 'Expenses', '', 0.0, '2009-04-03', 0, 1, 1, 0, 2, 0, 0, 0, 'builtin', NULL);
INSERT INTO accounts(intaccountclass, cname, description, taxvalue, dateadded, intaddedby, intparentaccount, groupsids, invisible, intaccounttype, intprofitfid, inttaxfid, inttaxuid, frame, hierarchypath)
  VALUES( 0, 'Income', '', 0.0, '2009-04-03', 0, 1, 1, 0, 3, 0, 0, 0, 'builtin', NULL);
INSERT INTO countries(cname, iso, groupsids, reserve1, reserve2)
  VALUES( 'Deutschland', 276, 1, NULL, NULL);
INSERT INTO globalsettings(cname, groupsids, VALUE)
  VALUES( 'yabs_dbversion', 1, '1.0');
INSERT INTO productgroups(cname, description, defaults, groupsids, productgroupsids, dateadded, hierarchypath, reserve1, intaddedby, reserve2)
  VALUES( 'All Products', 'This product group is visible to everyone.', NULL, 0, 0, '2009-04-03', NULL, NULL, 0, NULL);
INSERT INTO searchindex(dbidentity, groupsids, rowid, text)
  VALUES( 'groups', 0, 1, 'All Groups This group is visible to everyone. 2009-04-03');
INSERT INTO tax(cname, taxvalue, identifier, groupsids, country, dateadded, intaddedby, invisible, reserve1, reserve2)
  VALUES( 'Default 0%', 0.0, 'Default 0%', 1, NULL, '2009-04-03', 0, 0, NULL, NULL);
INSERT INTO tax(cname, taxvalue, identifier, groupsids, country, dateadded, intaddedby, invisible, reserve1, reserve2)
  VALUES( 'Default 19%', 19.0, 'Default 19%', 1, NULL, '2009-04-03', 0, 0, NULL, NULL);
INSERT INTO tax( cname, taxvalue, identifier, groupsids, country, dateadded, intaddedby, invisible, reserve1, reserve2)
  VALUES( 'Default 7%', 7.0, 'Default 7%', 1, NULL, '2009-04-03', 0, 0, NULL, NULL);
INSERT INTO users(cname, groupsids, intdefaultaccount, compsids, intdefaultstatus, fullname, password, laf, locale, defcountry, mail, language, inthighestright, isenabled, isrgrouped, isloggedin, datelastlog, dateadded, intaddedby, invisible, reserve1, reserve2)
  VALUES( 'admin', 1, 1, 0, 1, 'Administrator', '5f4dcc3b5aa765d61d8327deb882cf99', 'com.sun.java.swing.plaf.windows.WindowsLookAndFeel', 'en_GB', '', '', 'buildin_en', 0, 1, 0, 0, '2009-10-06', '2009-04-03', 0, 0, NULL, NULL);