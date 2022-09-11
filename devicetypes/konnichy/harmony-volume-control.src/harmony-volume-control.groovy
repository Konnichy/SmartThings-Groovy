/**
 *  Harmony volume control
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
metadata {
    definition (
        name: "Harmony volume control",
        namespace: "konnichy",
        author: "Konnichy",
        description: "Allows the SmartThings hub to control an audio device via a Logitech Harmony hub.")
    {
        capability "Audio Volume"
        capability "Audio Mute"
    }

    preferences {
        input "bridgeAddress", "text", title: "HTTP bridge IP address", description: "The IP address of the bridge server controlling the Harmony hub", required: true, displayDuringSetup: true
        input "bridgePort", "number", title: "HTTP bridge TCP port", description: "The TCP port on which the bridge server is listening", required: true, range: "0..65535", displayDuringSetup: true
        input "path", "text", title: "HTTP path", description: "The path of HTTP POST requests to send to the bridge", required: true, displayDuringSetup: true
        input "deviceId", "number", title: "Harmony device id", description: "The identifier of the Harmony device to control", required: true, range: "0..999999999", displayDuringSetup: true
    }
}

def private sendHTTPRequest(String harmonyCommand, repeat) {
	def myHubAction = new physicalgraph.device.HubAction(
		method: "POST",
		path: "$path",
		headers: [
			HOST: "$bridgeAddress:$bridgePort"
		],
		body: "{\"device\": $deviceId, \"command\": \"$harmonyCommand\", \"repeat_num\": $repeat, \"delay_sec\": 0}"
	)
    sendHubCommand(myHubAction)
}

def setMute(state) {
	log.debug "Switching to $state: unsupported command"
}

def mute() {
	log.debug "Muting"
    sendHTTPRequest('mute', 1)
    // requires an event to update the UI
    sendEvent(name: "mute", value: "unmuted")  // the device only supports switching state and cannnot be in sync with the UI, consider it always unmuted
}

def unmute() {
	log.debug "Unmuting"
    sendHTTPRequest('mute', 1)
    // requires an event to update the UI
    sendEvent(name: "mute", value: "unmuted")
}

def setVolume(volume) {
	log.debug "Setting volume to $volume"
    // requires an event to update the UI
    sendEvent(name: "volume", value: volume)  // the device doesn't support setting a specific volume value, update the UI but don't actually send a command
}

def volumeUp(repeat=1) {
	log.debug "Increasing volume by $repeat clic(s)"
    sendHTTPRequest('volumeUp', repeat)
}

def volumeDown(repeat=1) {
	log.debug "Decreasing volume by $repeat clic(s)"
    sendHTTPRequest('volumeDown', repeat)
}

