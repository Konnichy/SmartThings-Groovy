/**
 *  ZoneMinder Server
 *
 *  This Device Handler allows controlling a locally-installed ZoneMinder server via its API.
 *
 *  Features:
 *    - Switch ZoneMinder to a user-defined state when this device is switched off
 *    - Switch ZoneMinder to user-defined states (up to 11 states supported)
 *    - States change instantly when device is on
 *    - State changes are deferred when device is off (change will occur when device is switched on)
 *
 *  Known bugs and limitations:
 *    - (Bug) Password is changed to actual stars each time the configuration screen is opened
 *    - (Bug) The device icon in the app stays gray even when the device is on
 *    - HTTPS in not supported (password and access tokens are sent in cleartext!)
 *    - (Limitation from ZoneMinder) Accentuated characters are not supported (from web gui neither)
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
    definition (name: "ZoneMinder Server", namespace: "konnichy", author: "Konnichy") {
        capability "Switch"
        capability "Switch Level"
    }

    preferences {
        input(type: "paragraph", title: "", description: "Server parameters", displayDuringSetup: true)
        input("ipAddress", "text", title: "IP Address", required: true, displayDuringSetup: true)
        input("port", "text", title: "Port", required: true, displayDuringSetup: true, defaultValue: "80")
        input("username", "text", title: "Username", required: true, displayDuringSetup: true)
        input("password", "password", title: "Password", required: true, displayDuringSetup: true)

        input(type: "paragraph", title: "", description: "ZoneMinder states")
        input("StateOff", "text", title: "State when the device is switched off", required: false)
        input("State1", "text", title: "State when the dimmer is set to 1 %", required: false)
        input("State10", "text", title: "State when the dimmer is set to 10 %", required: false)
        input("State20", "text", title: "State when the dimmer is set to 20 %", required: false)
        input("State30", "text", title: "State when the dimmer is set to 30 %", required: false)
        input("State40", "text", title: "State when the dimmer is set to 40 %", required: false)
        input("State50", "text", title: "State when the dimmer is set to 50 %", required: false)
        input("State60", "text", title: "State when the dimmer is set to 60 %", required: false)
        input("State70", "text", title: "State when the dimmer is set to 70 %", required: false)
        input("State80", "text", title: "State when the dimmer is set to 80 %", required: false)
        input("State90", "text", title: "State when the dimmer is set to 90 %", required: false)
        input("State100", "text", title: "State when the dimmer is set to 100 %", required: false)
    }
}

def installed() {
    // Initialize default values
    sendEvent(name: "level", value: 1, displayed: false)
}

private SendEmptyPOST(String path) {
    def hubAction = new physicalgraph.device.HubAction(
        method: "POST",
        path: "${path}?token=${state.access_token}",
        headers: [
            "Host" : "${ipAddress}:${port}"],
        null,
        [callback: handleResponse]
    )
    
    log.debug("Sending to ZoneMinder: ${hubAction.toString()}")
    sendHubCommand(hubAction);
}

def handleResponse(physicalgraph.device.HubResponse hubResponse) {
    if (hubResponse.status >= 200 && hubResponse.status < 300) {
        log.debug("Success (${hubResponse.status})")
    } else {
        log.debug("Error (${hubResponse.status}): ${hubResponse.body}")
    }
}

private ZM_LogIn(callback) {
    def hubAction = new physicalgraph.device.HubAction(
        method: "POST",
        path: "/zm/api/host/login.json",
        headers: [
            "Host" : "${ipAddress}:${port}",
            "Content-Type" : "application/x-www-form-urlencoded"],
        body: "user=${username}&pass=${password}",
        null,
        [callback: callback]
    )
    
    log.debug("Sending to ZoneMinder: ${hubAction.toString()}")
    sendHubCommand(hubAction);
}

private isLoggedIn(physicalgraph.device.HubResponse hubResponse) {
    // Login successful
    if (hubResponse.status >= 200 && hubResponse.status < 300) {
        log.debug("Successfully logged in (${hubResponse.status})")
        state.access_token = hubResponse.json.access_token
        return true
    }
    
    // Any other response
    log.debug("Failed to log in. Code ${hubResponse.status} received with: ${hubResponse.body}")
    return false
}

private ZM_ChangeState(String newState) {
    state.newState = newState
    ZM_LogIn(do_ChangeState)
}

def do_ChangeState(physicalgraph.device.HubResponse hubResponse) {
    // Return now if we are not logged in
    if (!isLoggedIn(hubResponse)) {
        return
    }

    log.info("Changing ZoneMinder state to '${state.newState}'...")
    def newState = URLEncoder.encode(state.newState.toString(), "UTF-8")
    SendEmptyPOST("/zm/api/states/change/${newState}.json")
}
    
/* Get the state corresponding to the level in argument
*/
private getState(level) {
    // Check if level is a valid value
    if ((level.intdiv(10) * 10 != level) && (level != 1)) {
        log.debug("Level ${level} is not supported")
        return null
    }
    
    // Get the state name for the rounded level
    def stateNames = [ State1, State10, State20, State30, State40, State50, State60, State70, State80, State90, State100 ]
    def state = "${stateNames[(int) (level / 10)]}"
    if (state == "null") {
        log.debug("No state is defined for level ${level}")
        return null
    }

    return "${state}"
}

def on() {
    log.debug("ZoneMinder is switched on")

    // Switch ZoneMinder to the state corresponding to the dimmer position
    def state = getState((int) device.currentValue("level"))
    if (state == null) {
        // stay off
        log.debug("Revert to off")
        sendEvent(name: "switch", value: "off", displayed: false)
        return null
    }

    // Change ZoneMinder state
    log.debug("Switching to state '${state}'...")
    ZM_ChangeState("${state}")
    sendEvent(name: "switch", value: "on", descriptionText: "Switched to on (state '${state}')")
}

def off() {
    log.debug("ZoneMinder is switched off")

    if (StateOff == null) {
        sendEvent(name: "switch", value: "off", descriptionText: "Switched to off")
        return
    }
    
    // Switch ZoneMinder to the state configured for the "off" position
    ZM_ChangeState("${StateOff}")
    sendEvent(name: "switch", value: "off", descriptionText: "Switched to off (state '${StateOff}')")
}

def setLevel(level) {
    log.debug("Dimmer changed to level ${level}")
    
    def state = getState(level)
    if (state == null) {
        // revert to the previous level
        def currentLevel = device.currentValue("level")
        log.debug("Revert to level ${currentLevel}")
        sendEvent(name: "level", value: currentLevel, displayed: false)
        return
    } else if (device.currentValue("switch") != "on") {
        // keep the new level, but don't switch state
        log.debug("Device is currently off, state will change to '${state}' when switched on")
        sendEvent(name: "level", value: level, displayed: false)
        return
    }
        
    // Change ZoneMinder state
    log.debug("Switching to state '${state}'...")
    ZM_ChangeState("${state}")
    sendEvent(name: "level", value: level, descriptionText: "State changed to '${state}'")
}

