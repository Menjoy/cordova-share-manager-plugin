var exec = require('cordova/exec'),
    noop = function () {};

module.exports = {
    read: function (successCallback = 0, errorCallback) {
        exec(successCallback || noop, errorCallback || noop, 'ShareManager', 'read', []);
    },

    subscribe: function (callback = 0) {
        exec(callback || noop, 'ShareManager', 'subscribe', []);
    }
};
