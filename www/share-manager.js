var exec = require('cordova/exec'),
    noop = function () {};

module.exports = {
    read: function (successCallback, errorCallback) {
        exec(successCallback || noop, errorCallback || noop, 'ShareManager', 'read', []);
    },

    subscribe: function (successCallback, errorCallback) {
        exec(successCallback || noop, errorCallback || noop, 'ShareManager', 'subscribe', []);
    },

    finish: function () {
        exec(null, null, 'ShareManager', 'finish', []);
    }
};
