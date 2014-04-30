# [Codebits 2014][cb] Digital Signage Client (MQTT Branch)

This is an [MQTT][mqtt] enabled version of our client that aims to:

* Provide a way for bi-directional communication between the app and the `video` tag for error handling (i.e., skip to the next playlist item if an HTTP live stream breaks or when a video finishes untimely).
* Implement client status metrics.
* Go back to the original 2012 design (only send playlists when changed, do playlist randomness on the client side, etc.)
* Use MQTT instead of HTTP polling for real-time sync of multiple displays into a "signage wall".

## Program Flow

1. Client registers on [MQTT][mqtt] broker and sends out a "new client" message to the "signage-server" topic.
2. Client then subscribes a private "signage-client-<MAC Address>" topic and awaits further instructions
3. The server then sends out one of the following:

* New playlist/announcement message
* Join group message (indicating a group topic to which the client MUST subscribe to)

The client will then listen on its private topic and any group topics for more messages, and broadcast to the "signage-status" topic the URLs of any assets it is currently playing.

## Messages

There are two main kinds of messages:

1. A "new playlist" message that contains JSON data specifying what the client should do
2. An "announcement" message that contains a URL to be played immediately (with an optional duration)

## Playlist Specs

A playlist is a JSON structure like so:

    {
        "playlist": [
            {"uri": "http://...", "duration": 30},
            {"shuffle": "_random", "count": 3},
            {"uri": "http://...", "duration": 30},
            ...
        ],
        "_random": [
            {"uri": "http://...", "duration": 30},
            ...
        ]
    }

The `shuffle`: `key` construct allows us to build sub-playlists on the server side and serialize them inside a playlist update.

## Target Hardware

The target hardware we used was the [Minix Neo X5 mini][minix], which runs Android 4.2.2 out of the box and retails for ~€70, providing a nice bang for the buck (mind you, if you really need 1080p video you'll likely be better off with the X7 mini -- but we haven't tested that).

[Here's a short video of our early stress testing][flickr] -- the set includes screen recordings (from a Mac) of some of the displays we created, and there's [an insane amount of photography][fotos] and [video][videos] from the event for your perusal if you've never actually been to [Codebits][cb].

There was no rooting or custom firmware involved, and the only serious issue we had was the infamous [Android Webview memory leak bug][gc], which was heavily apparent if you used `canvas` for rendering assets (which we did, alas).

## Architecture

This app consists of an `InvisibleActivity` that provides a launcher icon and sets up a `PollingService` to run on boot. That in turn contacts the server, grabs playlist updates (basically a sequence of URLs) and hands them over to a `PlayerService` which actually asks a `FullScreenWebviewActivity` to render the signage displays.

All the signage displays we built were HTML5 pages that used the [Codebits API][api], residing on a [MEO Cloud][mc] shared folder, and none come with the client.

To play video, we just stick a `video` tag inside the WebView and run with it -- it was simpler than fiddling with a VideoView in the time we had, and has the additional benefit that we can place SVG overlays atop the `video` tag with program info. It does, however, have the drawback that we can't respond well to breaks in HLS streaming, etc.

## Stuff That Needs Improving

* Due to issues with passing some data in intent extras, we hand over some stuff between services using static class members. This is a hack, and needs to be expunged from the code.

## Building

First off, edit `strings.xml.dist` with the appropriate endpoints and rename it to `strings.xml`.

Without an IDE, just set your PATH to the Android tools and use `ant`:

    export PATH=$PATH:$HOME/Developer/Android/sdk/tools
    ant release

If you must use an IDE, this repo includes NetBeans project files. We recommend you use those.

## Running

    # set up emulator
    android avd
    # start emulator
    emulator -avd ARM720 &
    ant release install
    # check logs
    monitor

## Recovering from Eclipse

In case you end up importing the project there and wish to return to a saner environment:

    android list targets
    android update project -p . -t 17
    
    
[dsc]: https://github.com/sapo/digital-signage-client
[dss]: https://github.com/sapo/digital-signage-server
[api]: https://codebits.eu/s/api
[mc]: https://meocloud.pt/
[cbb]: https://codebits.eu/s/blog/bee64deeb27071c592b0adcac7243e0a
[gc]: https://code.google.com/p/android/issues/detail?id=9375
[minix]: http://www.minix.com.hk/Products/MINIX-NEO-X5mini.html
[flickr]: https://www.flickr.com/photos/ruicarmo/13842749675/in/set-72157643937892615
[fotos]: http://fotos.sapo.pt/pesquisa/?termos=codebits&listar=muitas&ordenar=maisrecentes
[videos]: http://videos.sapo.pt/search.html?word=codebits&order=news&page=1
[cb]: https://codebits.eu
[mqtt]: http://mqtt.org
