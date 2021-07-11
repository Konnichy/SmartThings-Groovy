/**
 *  OpenWeather temperature and humidity
 *
 *  This Device Handler is a virtual temperature sensor which periodically fetches temperature from the OpenWeather API
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
    definition (name: "OpenWeather temperature and humidity", namespace: "konnichy", author: "Konnichy") {
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Momentary"
        capability "Health Check"
    }
    
    preferences {
        input("cityName", "text", title: "City name", required: true, displayDuringSetup: true)
        input("stateCode", "text", title: "State code", required: true, displayDuringSetup: true)
        input("units", "enum", title: "Units", required: true, displayDuringSetup: true, options: ["standard", "metric", "imperial"])
        input("refreshPeriod", "enum", title: "Update data every...", required: true, displayDuringSetup: true, options: ["5 minutes", "15 minutes", "30 minutes", "1 hour", "3 hours"])
        input("appid", "text", title: "API key", required: true, displayDuringSetup: true)
    }
}

def installed() {
    log.debug("installed()")
    
    // Make the device appear online
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
    sendEvent(name: "healthStatus", value: "online")
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)

    // The 'down_6x' attribute value is being used to seed the button attribute. This is something ST
    // seem to do in their handlers, but using 'pushed' seems a bit silly with so many otherwise
    // unused values available.
    def supportedbuttons = [ 'pushed', 'down_6x' ]

    sendEvent( name: 'supportedButtonValues', value: supportedbuttons.encodeAsJSON(), displayed: false                      )
    sendEvent( name: 'numberOfButtons',       value: 1,                               displayed: false                      )
    sendEvent( name: 'button',                value: 'down_6x',                       displayed: false, isStateChange: true )

    //updateWeatherData()
    sendEvent(name: "temperature", value: 0,   unit: 'C', displayed: false)
    sendEvent(name: "humidity",    value: 100, unit: "%", displayed: false)
}

def updated() {
    log.debug("updated()")
    
    switch(refreshPeriod) {
        case "5 minutes":
            runEvery5Minutes(updateWeatherData)
            log.info("OpenWeather servers will be fetched every ${refreshPeriod}")
            break
        case "15 minutes":
            runEvery15Minutes(updateWeatherData)
            log.info("OpenWeather servers will be fetched every ${refreshPeriod}")
            break
        case "30 minutes":
            runEvery30Minutes(updateWeatherData)
            log.info("OpenWeather servers will be fetched every ${refreshPeriod}")
            break
        case "1 hour":
            runEvery1Hour(updateWeatherData)
            log.info("OpenWeather servers will be fetched every ${refreshPeriod}")
            break
        case "3 hours":
            runEvery3Hours(updateWeatherData)
            log.info("OpenWeather servers will be fetched every ${refreshPeriod}")
            break
        default:
            log.error("OpenWeather servers will not be fetched periodically")
            break
    }
    
    updateWeatherData()
}

def setTemperature(value) {
    log.info("Setting temperature to ${value}")
    
    def unit = ""
    switch (units) {
        case "standard":
            unit = "K"
            break
        case "metric":
            unit = "C"
            break
        case "imperial":
            unit = "F"
            break
    }
    sendEvent(name: "temperature", value: value, unit: unit)
}

def setHumidity(value) {
    log.info("Setting humidity to ${value}")
    sendEvent(name: "humidity", value: value, unit: "%")
}

def updateWeatherData() {
    // Documentation:
    // - Synchronous HTTP requests: https://docs.smartthings.com/en/latest/smartapp-developers-guide/calling-web-services-in-smartapps.html
    // - (beta) Asynchronous HTTP requests: https://docs.smartthings.com/en/latest/smartapp-developers-guide/async-http.html
    
    log.debug("Requesting weather data...")
    def params = [
        uri:  'http://api.openweathermap.org/data/2.5/',
        path: 'weather',
        contentType: 'application/json',
        query: [q: "${cityName},${stateCode}", units: units, appid: appid]
    ]
    try {
        httpGet(params) {resp ->
            log.debug("Weather data received")
            setTemperature(resp.data.main.temp)
            setHumidity(resp.data.main.humidity)
        }
    } catch (e) {
        log.error("error: $e")
    }
}

def push() {
    updateWeatherData()
}

