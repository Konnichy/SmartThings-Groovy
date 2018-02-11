/**
 *  Video-projector monitor
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

import groovy.json.JsonSlurper

definition(
    name: "Video-projector monitor",
    namespace: "konnichy",
    author: "Konnichy",
    description: "Monitors when the video-projector is switched on or off, and changes the SmartThings hub mode to customize different lighting conditions.",
    iconUrl: "https://graph.api.smartthings.com/api/devices/icons/st.Entertainment.entertainment9-icn",
    iconX2Url: "https://graph.api.smartthings.com/api/devices/icons/st.Entertainment.entertainment9-icn"
)


preferences {
    section() {
        input "videoprojector_on_mode", "mode", title: "Choose the mode to switch to when the videoprojector is turned on?", required: true
        input "videoprojector_off_mode", "mode", title: "Choose the mode to switch to when the videoprojector is turned off?", required: true
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {
    // Source: https://community.smartthings.com/t/poll-or-subscribe-example-to-network-events/72862/15
    subscribe(location, null, handleLANEvent, [filterEvents:false])
}

def handleLANEvent(event)
{
    def message = parseLanMessage(event.value)
    
    // Get the HTTP path used on the first header line
    if (!message.header)
        return
    def path = message.header.split("\n")[0].split()[1]
    
    // Only handle the event if specifically directed to this application
    if (path == "/videoprojector") {
        // Source: https://community.smartthings.com/t/poll-or-subscribe-example-to-network-events/72862/15
        def slurper = new JsonSlurper();
        def json = slurper.parseText(message.body)
        switch (json.event) {
            case "VIDEOPROJECTOR-ON":
                log.info "video-projector is turned on"
                location.setMode(videoprojector_on_mode)
                break
            case "VIDEOPROJECTOR-OFF":
                log.info "video-projector is turned off"
                location.setMode(videoprojector_off_mode)
                break
        }
    }
}