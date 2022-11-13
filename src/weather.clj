(ns weather
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [com.wsscode.pathom3.connect.indexes :as pci]
            [com.wsscode.pathom3.interface.eql :as p.eql]
            [com.wsscode.pathom3.connect.operation :as pco]))

(def base-url "http://dataservice.accuweather.com")
(def location-path "/locations/v1/cities/ipaddress")
(def weather-path "/forecasts/v1/daily/1day/")

(def APIKEY (System/getenv "APIKEY"))

(defn get-location [address-service api-key]
  (let [url (str address-service location-path)
        response (client/get url {:query-params {"apikey" api-key}})]
    (json/parse-string (:body response) true)))

(defn get-weather [address-service api-key location-key]
  (let [url (str address-service weather-path location-key)
        response (client/get url {:query-params {"apikey" api-key}})]
    (json/parse-string (:body response) true)))

(pco/defresolver location-key
  [{:keys [base-url api-key]} _]
  {::pco/output [:location-key]}
  (let [location (get-location base-url api-key)]
    {:location-key (get location :Key)}))

(pco/defresolver temperature
  [{:keys [base-url api-key]} {:keys [location-key]}]
  {::pco/input [:location-key]
   ::pco/output [:min :max]}
  (let [weather (get-weather base-url api-key location-key)
        forcasts (:DailyForecasts weather)
        forcast (first forcasts)]
    {:min (get-in forcast [:Temperature :Minimum :Value])
     :max (get-in forcast [:Temperature :Maximum :Value])}))

(comment
  (p.eql/process
   (pci/register {:base-url base-url
                  :api-key APIKEY} [weather/location-key
                                    weather/temperature])
   {}
   [:min :max]))
