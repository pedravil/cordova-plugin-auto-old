var Auto = {
    init: function (success, error) {
        cordova.exec(success,error,'Auto','init',[]);
    }
};

module.exports = Auto;
