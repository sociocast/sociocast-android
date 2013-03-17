![Alt text](/images/logo.png)

#Sociocast Android Library

##Overview

The Sociocast Android Library provides Adroid developers with wrapper functionality for Sociocast REST API methods. The Library provides basic HTTP request/response functionality as well as the general queueing of entity observations when the device does not have network connectivity. Entity Observations are stored in a SQL Lite database, wrapped by a [Content Provider](http://developer.android.com/guide/topics/providers/content-providers.html) class.   

The library utilizes the Android `IntentService` class which handles asynchronous requests (`Intent`) on demand. `IntentService` Clients send requests through `startService(Intent)` calls; the service is started as needed, handles each Intent in turn using a worker thread, and stops itself when it runs out of work. 

Additionally, the library use its `QueuedEventContentProvider` to provide a locally stored queue for entity observations when there is no network connectivity. This class is backed by a [`SQLiteDatabase`](http://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html). 

##Installation

To install the Sociocast Android Library, download and place the `sociocast-android.jar` (located in the [/bin/](https://github.com/sociocast/sociocast-android/tree/master/sociocast-android/bin) directory) into your project's classpath. Also make sure that the `AndroidManifest.xml` file for your application contains the following permissions within the `manifest` tag:

    <!-- ADDED FOR SOCIOCAST LIBRARY USE -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
Additionally, the following application components are required as well within the `application` tag:

    <!-- START: ADDED FOR SOCIOCAST LIBRARY USE -->
    <service android:name="com.sociocast.android.service.SociocastService" />  
    <provider android:authorities="com.sociocast.android.provider.events" android:name="com.sociocast.android.content_provider.QueuedEventContentProvider"/>      
    <receiver android:name="com.sociocast.android.receiver.ConnectivityChangedReceiver" enabled="false">
        <intent-filter>
            <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
            <action android:name="com.sociocast.android.retry_queued_items" />
        </intent-filter>
    </receiver>        
    <!-- END: ADDED FOR SOCIOCAST LIBRARY USE -->

For more information on the AndroidManifest.xml file see [this link](http://developer.android.com/guide/topics/manifest/manifest-intro.html). Additionally, you will find a sample Sociocast Android Library client [here](https://github.com/sociocast/sociocast-android/tree/master/sociocast-android-client).

##Initialization 

You can find usage examples in the [Sociocast Android Library](https://github.com/sociocast/sociocast-android/tree/master/sociocast-android-client). To initialize the Sociocast Android Library, from your app, you can use the following command:

    // Create instance of Sociocast Android Library    
    Sociocast sociocast = Sociocast.newInstance(this, apikey, secret, clid, true);
    
Make sure to pass in your `apikey`, `secret`, `clid` (Client ID), and whether to use Sandbox (true) or Production (false).

##Usage
To send and retrieve data from Sociocast using the Sociocast Android Library you can now use the `Sociocast` object. The `Sociocast` class implements the `SociocastAPI` interface which defines the basic Sociocast REST API methods.

###`sociocast.entityObserve`

