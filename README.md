[![Stories in Ready](https://badge.waffle.io/rcarmo/android-signage-client.png?label=ready&title=Ready)](https://waffle.io/rcarmo/android-signage-client)
# [Pixels Camp][pc] Digital Signage Client

This is a fork of the [Codebits 2014 digital signage client][cb], which in itself started out as a barebones webview to test browser behavior and grew organically from there. It targets Android 4.2 (platform 17) because that is what the hardware we had then ran, and on top of "normal" webview behavior includes a few tweaks:

* Enable the back button (for testing)
* JS extensions for exposing the device IP address, location, manual APK downloads, SSL certificate validation overrides (because this had to talk to test machines with self-signed certificates)
* ...and other things you generally _shouldn't_ do in an Android application, but might be useful to somebody.

## Dependencies

Unlike [the Raspberry Pi client from 2012][dsc], this won't be very useful without a server to go with it. Or, in this year's edition, static files in a blob store someplace.

## Target Hardware

The target hardware we used was the [Minix Neo X5 mini][minix], which runs Android 4.2.2 out of the box and retailed for ~€70, providing a nice bang for the buck (although it was not able to do 1080p video properly with the stock firmware). It's kind of sad to see that, at least for digital signage, nothing at least as good has been able to replace it at this point in 2016, really, but 

[Here's a short video of our early stress testing][flickr] -- the set includes screen recordings (from a Mac) of some of the displays we created, and there's [an insane amount of photography][fotos] and [video][videos] from Codebits for your perusal.

There was no rooting or custom firmware involved, and the only serious issue we had was the infamous [Android Webview memory leak bug][gc], which was heavily apparent if you used `canvas` for rendering assets (which we did, alas).

## Architecture

This app consists of an `InvisibleActivity` that provides a launcher icon and sets up a `PollingService` to run on boot. That in turn contacts the server, grabs playlist updates (basically a sequence of URLs) and hands them over to a `PlayerService` which actually asks a `FullScreenWebviewActivity` to render the signage displays.

All the signage displays we built were HTML5 pages that used the [Codebits API][api], residing on a [MEO Cloud][mc] shared folder, and none come with the client.

To play video, we just stick a `video` tag inside the WebView and run with it -- it was simpler than fiddling with a VideoView in the time we had, and has the additional benefit that we can place SVG overlays atop the `video` tag with program info. It does, however, have the drawback that we can't respond well to breaks in HLS streaming, etc.

## Stuff That Needs Improving

* [Better device identifiers](http://android-developers.blogspot.pt/2011/03/identifying-app-installations.html).
* Due to issues with passing some data in intent extras, we hand over some stuff between services using static class members. This is a hack, and needs to be expunged from the code.
* Besides exposing the device's MAC and IP addresses in the DOM, we need to provide a way for bi-directional communication between the app and the `video` tag for error handling (i.e., skip to the next playlist item if an HTTP live stream breaks or when a video finishes untimely).
* The network protocol we chose for 2014 (constantly retrieving a "live" playlist via HTTP polling) was designed in an attempt to do 'live' random playlists and dynamic insertion of MEO Kanal assets, and was, in retrospect, crap. Also, there wasn't any time to implement client status metrics, etc.

The original plan for this for was to do three things regarding the network protocol:

* Go back to the original 2012 design (only send playlists when changed, do playlist randomness on the client side, etc.)
* Implement a separate mode for "live" assets (even if it's an entirely separate player)
* Use MQTT instead of HTTP polling for real-time sync of multiple displays into a "signage wall".

Given the amount of time available, though, I'm going to be lucky to get this working with a saner HTTP format.

## Building

First off, edit `strings.xml.dist` with the appropriate endpoints and rename it to `strings.xml`.

Without an IDE, just set your PATH to the Android tools and use `ant`:

    export PATH=$PATH:$HOME/Developer/Android/sdk/tools
    # generate debug keystore
    ant debug
    ant release

This repo has been (somewhat regretfully) updated to use Android Studio, which is a dubious improvement. As it is, rebuilding this on another machine is subject to its vagaries, and best left to people with more patience than I currently have.

## Running

    # set up emulator
    android avd
    # start emulator
    emulator -avd ARM720 &
    ant release install
    # check logs
    monitor

## Recovering from the IDE

In case you wish to return to a saner environment:

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
[pc]: http://pixels.camp
[cb]: https://github.com/sapo/android-signage-client