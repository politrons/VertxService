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
            var otherUser = jQuery.parseJSON(data);
            var currentUser = index.getUserData();
            if (currentUser.username !== otherUser.username) {
                console.log("New position for user " + otherUser.username);
                console.log(currentUser.fullname + " position "+ currentUser.position.longitude + " " + currentUser.position.latitude);
                console.log(otherUser.fullname + " position "+ otherUser.position.longitude + " " + otherUser.position.latitude);

                x.innerHTML = "Distance between " +
                currentUser.fullname + " and " + otherUser.fullname +
                " is " + calculateDistance(currentUser.position, otherUser.position);
            }
        });
    }

    function initListetener() {
        var startTrakingButton = $("#startTrakingButton");
        startTrakingButton.unbind('click');
        startTrakingButton.on('click', function () {
            stop=false;
            $("#startTrakingButton").attr("disabled", "disabled");
            startTraking();
        });
        $("#stopTrakingButton").on('click', function () {
            stopTracking();
            $("#startTrakingButton").removeAttr("disabled");
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
        setInterval(function () {
        if (stop) {
            return;
        }
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(sendPosition);
        } else {
            x.innerHTML = "Geolocation is not supported by this browser!.";
        }
        }, 5000);
    }

    function sendPosition(position) {
        var currentPosition = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude
        };
        var userData = index.getUserData();
        userData._id = $('#inputUserId').val();
        userData.position = currentPosition;
        $("#inputUserLongitude").val(currentPosition.longitude);
        $("#inputUserLatitude").val(currentPosition.latitude);
        geoEb.send("track.user.server", userData);
    }

    /*Function to calculate distance between two coordinates on earth surface*/
    function calculateDistance(userPosition, otherUserPosition) {
        var startLatRads = degreesToRadians(userPosition.latitude);
        var startLongRads = degreesToRadians(userPosition.longitude);
        var destLatRads = degreesToRadians(otherUserPosition.latitude);
        var destLongRads = degreesToRadians(otherUserPosition.longitude);
        var Radius = 6371; // radius of the Earth in kilometres
        var distance = Math.acos(Math.sin(startLatRads) * Math.sin(destLatRads) +
                Math.cos(startLatRads) * Math.cos(destLatRads) *
                Math.cos(startLongRads - destLongRads)) * Radius;
        distance = parseFloat(distance * 1000);
        return distance.toFixed(2);
    }

    /*Converts degree to radians*/
    function degreesToRadians(degrees) {
        degrees = parseFloat(degrees);
        degrees.toFixed(2);
        var radians = (degrees * Math.PI) / 180;
        return radians;
    }

    return {
        initListetener: initListetener,
        registerHandlers: registerHandlers
    };
}());