// DOM Ready =============================================================
$(document).ready(function () {
    videoConference.initListener();
});

var videoConference = (function () {

    var ebVideo;
    var videoReady = false;
    var stopRecording =false;

    function registerHandlers(eb) {
        ebVideo = eb;
        ebVideo.registerHandler("video.user.client", function (data) {
            processImg(data);
        });
    }

    function startRecord() {
        window.URL = window.URL || window.webkitURL;
        navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia;
        navigator.getUserMedia({audio: false, video: true}, function (vid) {
            videoReady = true;
            document.querySelector('video').src = window.URL.createObjectURL(vid);
        }, function(){
            alert("Error on video recording")
        });
        window.requestAnimFrame = (function (callback) {
            return window.requestAnimationFrame ||
                window.webkitRequestAnimationFrame ||
                window.mozRequestAnimationFrame ||
                window.oRequestAnimationFrame ||
                window.msRequestAnimationFrame ||
                function (callback) {
                    window.setTimeout(callback, 1000 / 100);
                };
        })();
    }

    function dFrame(ctx, video, canvas) {
        ctx.drawImage(video, 0, 0);
        var dataURL = canvas.toDataURL('image/jpeg', 0.2);
        if (videoReady && stopRecording===false) {
            ebVideo.send("video.user.server", dataURL);
        }
        requestAnimFrame(function () {
            setTimeout(function () {
                dFrame(ctx, video, canvas);
            }, 200)
        });
    }

    function init() {
        var canvas = document.querySelector('#myCanvas');
        var video = document.querySelector('video');
        var ctx = canvas.getContext('2d');
        dFrame(ctx, video, canvas);
    }

    function processImg(img) {
        document.querySelector('#incomingVideo').src = img;
    };


    function initListener() {
        var startVideoButton = $("#startVideoButton");
        startVideoButton.unbind('click');
        startVideoButton.on('click', function () {
            stopRecording =false;
            startRecord();
            init();
        });
        var stopVideoButton = $("#stopVideoButton");
        stopVideoButton.unbind('click');
        stopVideoButton.on('click', function () {
            stopRecording=true;
        });

    }
// Functions =============================================================


    return {
        registerHandlers: registerHandlers,
        initListener:initListener
    };
}());
