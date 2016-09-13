# OneSpace
OneSpace is our research prototype for the creation of Cyber-Physical Social Networks (CPSN). We motivate the idea behind CPSN and describe our prototype in an upcoming journal article for the  the Special Issue "Advances in Social Computing" of the ACM Transactions on Internet Technology. A pre-print of the article is available [here](http://christianvonderweth.org/publications/docs/vdw-CyberPhysicalSocialNetworks.pdf)

### Abstract 
In the offline world, getting to know new people is heavily influenced by people's physical context, i.e., their current geolocation. People meet in classes, bars, clubs, public transport, etc. In contrast, first-generation online social networks such as Facebook or Google+ do not consider users' context and thus mainly reflect real-world relationships (e.g., family, friends, colleagues). Location-based social networks, or second-generation social networks, such as Foursquare or Facebook Places take the physical location of users into account to find new friends. However, with the increasing number and wide range of popular platforms and services on the Web, people spend a considerable time moving through the online world. In this paper, we introduce cyber-physical social networks (CPSN) as the third generation of online social networks. Beside their physical locations, CPSN consider also users' *virtual locations* for connecting to new friends. In a nutshell, we regard a web page as a place where people can meet and interact. The intuition is that a web page is a good indicator for a user's current interest, likings or information needs. Moreover, we link virtual and physical locations allowing for users to socialize across the online and offline world. Our main contributions focus on the two fundamental tasks of creating meaningful virtual locations as well as creating meaningful links between virtual and physical locations, where "meaningful" depends on the application scenario. To this end, we present OneSpace, our prototypical implementation of a cyber-physical social network. OneSpace provides a live and social recommendation service for touristic venues (e.g., hotels, restaurants, attractions). It allows mobile users close to a venue and web users browsing online content about the venue to connect and interact in an ad-hoc manner. Connecting users based on their shared virtual and physical locations gives way to a plethora of novel use cases for social computing, as we will illustrate. We evaluate our proposed methods for constructing and linking locations, and present the results of a first user study investigating the potential impact of cyber-physical social networks.

## Requirements
In the following, we outline the required configurations to set up OneSpace.

**IMPORTANT:** Please note that this is research prototype and subject to constant modifications and updates. The code lacks proper documentation and the data is tailored to our research purposes. Furthermore, some features require access to public but third-party APIs which in turn require correct credentials. We therefore strongly recommend you to contact us for help; see contact information in the [journal article](http://christianvonderweth.org/publications/docs/vdw-CyberPhysicalSocialNetworks.pdf).

### MySQL
The backend data is stored in a [MySQL](https://www.mysql.com/) database. Please check the MySQL website for the latest version and documentations.

The latest snapshot of our database can be downloaded as MySQL dump [here](http://christianvonderweth.org/projects/onespace/data/).

### Apache
The sole purpose [Apache Web server](https://www.apache.org/) is to enable the BOSH-based access to the XMPP server, see below, as well as accessing the configuration Web interface of Openfire, the XMPP server we currently deploy. Please check the Apache website for the latest version and documentations.

### Node.js
Node.js serves as the access point to the backend data, as an alternative to an Apache-PHP-MySQL stack. The Node.js server does no rendering but only provides various API call to get and put data.
```
> sudo apt-get install nodejs
```

The following packages are required:
```
> npm install http
> npm install express
> npm install step
> npm install url
> npm install body-parser
> npm install connect-multiparty
> npm install fs
> npm install path
> npm install mime-types
> npm install mkdirp
> npm install lwip
> npm install crypto
> npm install unshort
> npm install ajax-request
> npm install mysql
> npm install node-uuid
```

### XMPP Server
The XMPP server handles the instant messaging service. In principle, any XMPP server with multi-user chat support should do just fine. We use [Openfire](https://www.igniterealtime.org/projects/openfire/) in for our prototype. Please check the Openfire website for the latest version and documentations.

#### Enabling BOSH
Bidirectional-streams Over Synchronous HTTP (BOSH) is required to overcome the stateless nature of the basic HTTP protocol and to allow pushing data from the server to a client. By default, Openfire listens on port 7070, with “/http-bind/” (the trailing slash is important).

Since the browser add-on is Javascript-based, all Javascript restrictions enforced by the browser apply. One of these restrictions is "same origin policy". It means that Javascript can only send HTTP requests to the domain (and port) it was loaded from and since it is served on HTTP (port 80) it can’t make requests to port 7070. Solution: Javascript client will make requests to the same domain and port. The requests will be forwarded locally to port 7070 by Apache. This requires the the configuration of Apache. 

(1) Enable required modules
```
> sudo a2enmod proxy
> sudo a2enmod proxy_http
```

(2) Add the following the the Apache configuration file:
```
  ...
  ProxyRequests Off
  ProxyPass /http-bind http://localhost:7070/http-bind/
  ProxyPassReverse /http-bind http://localhost:7070/http-bind/ 
  ...
```
Restart Apache.
