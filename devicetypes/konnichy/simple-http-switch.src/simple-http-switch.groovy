/**
 *  Simple HTTP Switch
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
        name: "Simple HTTP Switch",
        namespace: "konnichy",
        author: "Konnichy",
        description: "Allows the SmartThings hub to control a home device via an HTTP GET request.")
    {
        capability "Actuator"
        capability "Switch"
    }


    tiles(scale: 2) {
        // the main tile allows switching device states
        standardTile("switch", "device.switch", width: 6, height: 4, canChangeIcon: true) {
            state "off", label: '${currentValue}', action: "switch.on",
                  icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: '${currentValue}', action: "switch.off",
                  icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
        }
        // this tile switches the device on
        standardTile("switchOn", "device.switch", width: 3, height: 2, canChangeIcon: true) {
            state "off", label: 'ON', action: "switch.on",
                  icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: 'ON', action: "switch.on",
                  icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
        }
        // this tile switches the device off
        standardTile("switchOff", "device.switch", width: 3, height: 2, canChangeIcon: true) {
            state "off", label: 'OFF', action: "switch.off",
                  icon: "st.switches.switch.off", backgroundColor: "#00a0dc"
            state "on", label: 'OFF', action: "switch.off",
                  icon: "st.switches.switch.on", backgroundColor: "#ffffff"
        }

        // the "switch" tile will appear in the Things view
        main("switch")

        // all tiles will appear in the device details
        details("switch", "switchOn", "switchOff")
    }
    
    preferences {
        input "ipAddress", "text", title: "IP address", description: "What is the IP address of the device to control?", required: true, displayDuringSetup: true
        input "port", "number", title: "TCP port", description: "On which TCP port is the device listening?", required: true, range: "0..65535", displayDuringSetup: true
        input "onPath", "number", title: "Path to switch on", description: "What is the path of the HTTP request to switch the device on?", required: true, range: "0..65535", displayDuringSetup: true
        input "offPath", "number", title: "Path to switch off", description: "What is the path of the HTTP request to switch the device off?", required: true, range: "0..65535", displayDuringSetup: true
    }
}

def setDeviceState(String state) {
    def myHubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: (state == "on" ? "${onPath}" : "${offPath}"),
        headers: [
            HOST: "${ipAddress}:${port}",
        ])
    
    sendHubCommand(myHubAction)
}

def on() {
    log.debug "Executing 'on'"

    setDeviceState("on")
    sendEvent(name: "switch", value: "on") 
}

def off() {
    log.debug "Executing 'off'"

    setDeviceState("off")
    sendEvent(name: "switch", value: "off") 
}