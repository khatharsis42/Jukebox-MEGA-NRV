/**
Script managing the interface.
It uses JQuery

It is used to load the Youtube iframe, refresh the playlist and suggestion column.
 */

/**
 *
 * @param html: html of the page
 * @param d: dictionary of key values which will replace { key } in html
 * @returns a html, with values replacing keys
 */
function template(html, d) {
    let r = html;
    for (let i in d) {
        r=r.replaceAll("{"+i+"}", d[i]);
    }
    r=r.replace(/{\w+}/g, "");
    return r;
}


/**
 * Called on click of the buttons which switch the recto/verso of song tiles.
 * These buttons are .btn-more and .btn-back.
 */
function toggle_recto_verso() {
    let track_li = $(this).closest("li");
    track_li.find(".verso").toggle();
    track_li.find(".recto").toggle();
}


/**
 * Generate the HTML for a track tile
 *
 * @param t: dictionary representing a serialized Track
 * @returns {jQuery|HTMLElement} : html for the tile
 */
function generate_track_html(t) {
    let track_html = $(template(track_template, t));
    track_html.data("track", t); // we add the serialized object as data to the tile
    track_html.find(".verso").hide();
    track_html.find(".btn-more").click(toggle_recto_verso);
    track_html.find(".btn-back").click(toggle_recto_verso);
    track_html.find(".btn-top").click(function() {
        $.post("/move-track", {"action": "top", "randomid": t["randomid"]});
    });
    track_html.find(".btn-up").click(function() {
        $.post("/move-track", {"action": "up", "randomid": t["randomid"]});
    });
    track_html.find(".btn-down").click(function() {
        $.post("/move-track", {"action": "down", "randomid": t["randomid"]});
    });

    track_html.find(".btn-refresh").click(function() {
        $.post("/refresh-track", {"url": t["url"]});
    });
    return track_html
}

/**
 * Generate the HTML for a track tile in the suggestion column.
 * It deactivate the buttons to move the track, and the one to remove it.
 *
 * @param t : dict, serialized Track
 * @returns {jQuery|HTMLElement} : html for the tile
 */
function generate_track_html_suggest(t) {
    let track_html = generate_track_html(t);
    track_html.find(".btn-add").click(function() {
        $.post("/add", t);
    });
    track_html.find(".btn-remove").remove();
    track_html.find(".btn-up").remove();
    track_html.find(".btn-down").remove();
    track_html.find(".btn-top").remove();
    return track_html;
}


/**
 * Generate the HTML for a track tile in the queue.
 * It deactivate the button to remove the track.
 *
 * @param t : dict, serialized Track
 * @returns {jQuery|HTMLElement} : html for the tile
 */
function generate_track_html_queue(t) {
    let track_html = generate_track_html(t);
    track_html.find(".btn-remove").click(function() {
        console.log("Removing track with randomid: ", t["randomid"]);
        $.post("/remove", t);
    });
    track_html.find(".btn-add").remove();
    return track_html;
}


track_template = `
<li class="list-group-item track" id="{randomid}">
    <div class="row recto">
        <div class="col-4 centered">
            <img class="albumart" src="{albumart_url}">
        </div>
        <div class="col-7 track-info centered">
            <span class="track-title">{title}</span>
            <span class="track-artist">{artist}</span>
            <span class="track-duration">{duration} s.</span>
            <span class="track-user float-right">Added by {user}</span>
        </div>
        <div class="col-1">
            <button class="icon btn-more" alt="More"></button>
            <button class="icon btn-add" alt="Play"></button>
            <button class="icon btn-top" alt="Top"></button>
            <button class="icon btn-up" alt="Up"></button>
            <button class="icon btn-down" alt="Down"></button>
            <button class="icon btn-remove" alt="Enlever"></button>
        </div>
     </div>
     <div class="row verso">
        <div class="col-11">
            <span class="track-title">{title}</span>
            <span class="track-url"><a href="{url}">Link to media</a></span>
            <span class="track-album">Album : {album}</span>
            <span class="track-source">From {source}</span>
        </div>
        <div class="col-1">
            <button class="icon btn-back" alt="Back"></button>
            <button class="icon btn-refresh" alt="Refresh"></button>
            <a href="/statistics/track/{id}">
                <button class="icon btn-stats" alt="Stats"></button>
            </a>
        </div>
    </div>
</li>
`;

// We load the Youtube iframe
const tag = document.createElement('script');
tag.src = "https://www.youtube.com/iframe_api";
const firstScriptTag = document.getElementsByTagName('script')[0];
firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

let pause = false;
let yt = 0;
function onYouTubeIframeAPIReady() {
    yt = new YT.Player('YT', {
        height: '360',
        width: '640',
        events: {
            'onReady': function() {
                console.log("YT ready");
                yt.mute();
                yt.ready = true;
                yt.hasTrack = false;
                $('#pause').on("click", function() {
                    if (pause) {yt.playVideo();} else {yt.pauseVideo();}
                    pause = ! pause;
                    $.post('/pause_play');
                    return true;
                });

                $('#rewind').on("click", function() {
                    $.post('rewind');
                    return true;
                });

                $('#advance').on("click", function() {
                    $.post('advance');
                    return true;
                });
            }
        }

    });
}

function mobileCheck() {
  let check = false;
  (function(a){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4))) check = true;})(navigator.userAgent||navigator.vendor||window.opera);
  return check;
};
const isMobile = mobileCheck();

if (isMobile) {
    var isChromium = window.chrome;
    var winNav = window.navigator;
    var vendorName = winNav.vendor;
    var isOpera = typeof window.opr !== "undefined";
    var isIEedge = winNav.userAgent.indexOf("Edg") > -1;
    var isIOSChrome = winNav.userAgent.match("CriOS");

    if (isIOSChrome) {
       alert("The jukebox is currently bugged on Chromium-based browsers. Please use a better browser, such as Firefox.")
    } else if(
      isChromium !== null &&
      typeof isChromium !== "undefined" &&
      vendorName === "Google Inc." &&
      isOpera === false &&
      isIEedge === false
    ) {
       alert("The jukebox is currently bugged on Chromium-based browsers. Please use a better browser, such as Firefox.")
    } else {
       // not Google Chrome
    }
}

/**
 * Synchronizes the video from Youtube iframe with the sound from mpv.
 *
 * @param mpv_time : float : timestamp of mpv
 */
function syncVideo(mpv_time) {
    if (mpv_time <= 0.01) {
        yt.pauseVideo();
    } else {
        let ytTime = yt.getCurrentTime();
        if (! pause) {yt.playVideo();}
        let delta = ytTime - mpv_time;
        if (Math.abs(delta) > 1) {
            yt.seekTo(mpv_time);
        }
    }
}

/**
 * Updates the playlist
 * Also calls the function updating the Youtube iframe.
 *
 * Careful when editing, this function works fine, but may be a bit messy.
 * @param data: dict given by /sync
 */
function updates_playlist(data) {
    // we first get the current playlistHTML
    let playlist = data.playlist;
    let playlistHTML = $("#playlist");
    // then we must check that all elements in playlist are in playlistHTML
    if (playlist.length === 0) {
        playlistHTML.children().remove();
        return;
    }

    /* so we browse the playlist dict
    If the track in the html corresponds to the current one in the dict, it's fine.
    Else we remove the track in HTML and put the one in the dict.
    We don't "move" the html tiles, we just delete them wherever they are wrong.
    */
    for (let i=0; i<playlist.length; i++) {
        let track = playlist[i];
        if (i === 0 && (playlistHTML.find(".track") === 0 || playlistHTML.find(".track:first").attr("id") != track.randomid)) {
            if (playlistHTML.find(".track").length !== 0) {
                playlistHTML.find(".track:first").remove();
            }
            if (playlistHTML.find("#playlist-playing").length !== 0) {
                playlistHTML.find("#playlist-playing").remove();
            }
            playlistHTML.prepend(generate_track_html_queue(track));
            playlistHTML.find(".track:first .btn-down").hide();
            playlistHTML.find(".track:first .btn-up").hide();
            playlistHTML.find(".track:first .btn-top").hide();

            // then we manage the Youtube iframe
            // We need to do that, because we need to let the yt thingy load
            if (yt.ready && $("#YT").is(":visible") && track["source"] === "youtube") {
                let url = new URL(data.playlist[0]["url"]);
                let videoId = url.searchParams.get("v");
                let t;
                if (url.searchParams.has("t")) {
                    t = parseInt(url.searchParams.get("t"), 10);
                } else {
                    t = 0;
                }
                if (yt.url !== videoId) {
                    console.log("Loading video with id : ", videoId, " at time ", t);
                    yt.cueVideoById(new String(videoId), t);
                    yt.url = videoId;
                    yt.playVideo();
                }
                syncVideo(data.time);
                yt.hasTrack = true;
            }
            if (playlistHTML.find("#playlist-playing").length === 0) { // it doesn't display
                console.log("Adding Lecture en cours");
                playlistHTML.find(".track:first").before("<li id='playlist-playing' class='playlist-title'>Now playing</li>");
            }
        }
        // we do not use !== as the elements are not the same types
        else if (playlistHTML.find(".track:last-child").index < i || playlistHTML.find(".track:eq("+i+")").attr("id") != track.randomid) {
            if (i===1) {
                playlistHTML.find("#playlist-queue").remove();
            }
            playlistHTML.find(".track:eq("+i+")").remove();
            let j = i-1;
            playlistHTML.find(".track:eq("+j+")").after(generate_track_html_queue(track));


            if (playlistHTML.find("#playlist-queue").length === 0) {
                playlistHTML.find(".track:eq(0)").after(
                "<li id='playlist-queue' class='playlist-title'><div>Upcoming...</div><div id='playlist-length' style='font-size:small;letter-spacing: .2em;'></div></li>"
                );
            }
        }
    }

    // we must remove elements in playlistHTML which are too much
    let i = playlist.length -1;
    let track_tile = playlistHTML.find(".track:eq("+i+")");
    while (track_tile.next().length !== 0) {
        track_tile = track_tile.next();
        track_tile.remove();
    }
}
// This is here so that we only reload the page when the user is watching it
var pageStatus=true;

/**
 * This is what happens when we GET /sync
 */
sync = function() {
    if (pageStatus) {
        $.get("/sync", function (data) {
            $('#volume-slider').val(data.volume);
            if ( yt !== 0 ) {
                if ( yt.ready )  updates_playlist(data);
                if ( yt.hasTrack ) syncVideo(data.time);
            }
            s = document.getElementById("playlist-length");
            if (s !== null) s.innerHTML="Time Left:" + data.playlist_length;
        });
        window.setTimeout(arguments.callee, 2000);
        return;
    }
    window.setTimeout(arguments.callee, 100);
    return;
}();

/**
 *
 */
let delay = (function(){
    let timer = 0;
    return function(callback, ms){
        clearTimeout (timer);
        timer = setTimeout(callback, ms);
    };
})();

/**
 * This is so that you can shift add tracks
 */
var shiftKeyDown = false;
document.addEventListener("keyup", function(event) {
    if (event.code === 'ShiftLeft' || event.code === 'ShiftRight') {
        shiftKeyDown = false;
    }
});
document.addEventListener("keydown", function(event) {
    if (event.code === 'ShiftLeft' || event.code === 'ShiftRight') {
        shiftKeyDown = true;
    }
});

/**
 * When pressing enter in the search bar, make a search.
 */
$('#query').keyup(function(e) {
    let code = e.which;
    // We make a search **only** if the enter key has been pressed
    if (code != 13) {
        return;
    }
    let query = $('#query').val().trim();
    if (query == "") {
        $("#search_results").html("");
        $("#search_results").hide();
        return;
    }
    delay(function() {
        console.log("searching "+$('#query').val());
        $.post("/search", {"q": query}, function(data) {
            $("#search_results").html("");
            if (data==="ok") {
                $('#query').val("");
                $('#search_results').hide();
                return;
            }
            for (let i=0; i<data.length; i++) {
                let track = data[i];
                console.log("Track :");
                console.log(track);
                $('#search_results').append(generate_track_html_suggest(track));
                $('#search_results li:last .btn-add').click(function() {
                    $('#query').val("");
                    if (!shiftKeyDown) {
                        $('#search_results').hide();
                    }
                });
            }
            $("#search_results").show()
        }, dataType="json");
        if ($('#query').val() == "") {
            $("#search_results").html("");
            $("#search_results").hide();
            return;
        }
    },250);
});

$('#query').focus(function () {
    if ($('#query').val() != "")
        $('#search_results').show();
});


$('#jump').keyup(function(e) {
    let code = e.which;
    // We make a search **only** if the enter key has been pressed
    if (code != 13) {
        return;
    }
    let jump = $('#jump').val().trim();
    if (jump == "") {
        return;
    }
    delay(function() {
        console.log("jumping to "+$('#jump').val());
        $.post("/jump", {"jump": jump})
    },250);
});


/**
 * Get the suggested tracks
 */
function suggest() {
    $.get("/suggest", function (data) {
        $('#suggestions').html("");
        for (let i =0; i<data.length; i++) {
            let t = data[i];
            $('#suggestions').append(generate_track_html_suggest(t));
        }
    });
}

$("#volume-slider").change(function() {
    $.post("/volume", {"volume": $(this).val()})
});

$("#refresh-suggestions").click(suggest);

$("#toggle-YT").click(function() {
    $("#YT").toggle();
    if (!$("#YT").is(":visible")) {
        yt.stopVideo();
    } else {
        yt.playVideo();
    }
});

// we hide the search results by default
$('#search_results').hide();


$(document).ready(function() {
    suggest();
});
