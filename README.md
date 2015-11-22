# om-next-03

Simple example based around [om-next](https://github.com/omcljs/om/wiki/Quick-Start-%28om.next%29) 
 
A Drag and Drop list

initial xhr load of 'remote' data
computed event handlers
local state
read/mutate queries

(Works in Chrome, Safari and The Fox) 
 
[1.0.0-alpha23](https://clojars.org/org.omcljs/om)

## Overview

![om-next-03.gif](https://raw.githubusercontent.com/griffio/griffio.github.io/master/public/om-next-03.gif)

## Setup

Intellij - Cursive - REPL

![Figwheel Idea Cursive](https://raw.githubusercontent.com/griffio/griffio.github.io/master/public/figwheel-idea.png)

Open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

Copyright © 2015 

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.