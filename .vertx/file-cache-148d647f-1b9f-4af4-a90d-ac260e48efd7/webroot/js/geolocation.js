//DOM Ready =============================================================
$(document).ready(function () {
    geolocation.initListetener();
});

var geolocation = (function () {

    var x = document.getElementById("demo");
    var stop = false;
    var geoEb;

    function registerHandlers(eb) {
        geoEb = eb;
        geoEb.registerHandler("track.user.client", function (data) {
            debugger;
            var otherUser = jQuery.parseJSON(data);
            var currentUser = index.getUserData();
            console.log("New position for user " + otherUser.username);
            //if (currentUser.username !== otherUser.username) {
            x.innerHTML = "Distance between User: " +
                currentUser.username + " and User:" + otherUser.username +
                " is " + calculateDistance(currentUser.position, otherUser.position);
            //}
        });
    }

    function initListetener() {
        var startTraking = $("#startTraking");
        startTraking.unbind('click');
        startTraking.on('click', function () {
            startTraking();
        });
        $("#stopTraking").on('click', function () {
            stopTracking();
        });
    }

    function stopTracking() {
        stop = true;
    }

    function startTraking() {
        if ($('#inputUserId').val() === '') {
            alert("you need to select a user first. Click on search by bus")
        } else {
            getDistance();
        }
    }

    function getDistance() {
        //setInterval(function () {
        if (stop) {
            return;
        }
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(sendPosition);
        } else {
            x.innerHTML = "Geolocation is not supported by this browser!.";
        }
        //}, 5);
    }

    function sendPosition(position) {
        var currentPosition = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude
        };
        var userData = index.getUserData();
        userData._id = $('#inputUserId').val();
        userData.position = currentPosition;
        geoEb.send("track.user.server", userData);
    }

    /*Function to calculate distance between two coordinates on earth surface*/
    function calculateDistance(userPosition, otherUserPosition) {
        debugger;
        var startLatRads = degreesToRadians(userPosition.latitude);
        var startLongRads = degreesToRadians(userPosition.longitude);
        var destLatRads = degreesToRadians(otherUserPosition.latitude);
        var destLongRads = degreesToRadians(otherUserPosition.longitude);
        var Radius = 6371; // radius of the Earth in metres
        var distance = Math.acos(Math.sin(startLatRads) * Math.sin(destLatRads) +
                Math.cos(startLatRads) * Math.cos(destLatRads) *
                Math.cos(startLongRads - destLongRads)) * Radius;
        return distance;
    }

    /*Converts degree to radians*/
    function degreesToRadians(degrees) {
        var radians = (degrees * Math.PI) / 180;
        return radians;
    }

    return {
        initListetener: initListetener,
        registerHandlers: registerHandlers
    };
}());