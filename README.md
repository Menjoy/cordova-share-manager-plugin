# Cordova Share Manager

Simple way to add your application to share widget

## Platforms support

Android

## Install

```cordova plugin add https://github.com/Menjoy/cordova-share-manager-plugin.git --save```

## Dependencies

```cordova plugin add https://github.com/Menjoy/cordova-custom-config.git --save```

!Important: basic version of cordova-custom-config plugin doesn't support adding new nodes to xml.

## Android manifest changes

You should allow your application to receive data from another application, by adding follows strings to config.xml
Cordova-custom-plugin will set these fields to AndroidManifest.xml

```xml
    <config-file target="AndroidManifest.xml" parent="./application/activity/[@android:name='MainActivity']" add="true">
        <intent-filter android:label="@string/launcher_name">
            <action android:name="android.intent.action.SEND" />
            <action android:name="android.intent.action.SEND_MULTIPLE" />
            <category android:name="android.intent.category.DEFAULT" />
            <data android:mimeType="image/*" />
        </intent-filter>
    </config-file>
```

Also we should add fields for run only one instance of application (to config.xml too):

```xml
<preference name="AndroidLaunchMode" value="singleTask"/>
```

You can read official android documentation abouth these changes in manifest

https://developer.android.com/training/sharing/receive.html

## Javascript API

After plugin installed you can access share manager from your js app by ```window.shareManager``` object:

### subscribe

Set callback for each new intent passed to application:

```js
window.shareManager.subscribe((data) => {
    /* data = {
        items: [{
            text: 'OMFG! Look at this!'
            uri: 'content://media/external/images/media/5',
            type: 'image/jpeg',
            extension: 'jpeg',
            width: 300,
            height: 500,
            size: 1427 // bytes
        }],
        type: 'image/*'
    };
    */
});
```

### read

Force intent read, it's useless if user shared data to application when it was closed.
First of all you should subscribe, and then force read intent.
As a result you will receive data inside subscribe callback.

```js
window.shareManager.read();
```
