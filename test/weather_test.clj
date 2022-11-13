(ns weather-test
  (:require
   [clojure.test :refer [deftest is]]
   [stub-http.core :as stubs]
   [cheshire.core :as json]
   [clj-http.lite.client :as client]
   [com.wsscode.pathom3.interface.eql :as p.eql]
   [com.wsscode.pathom3.connect.indexes :as pci]
   [weather :as weather]))

(require 'hashp.core)

(deftest get-location-key-test
  (stubs/with-routes!
    [location-key "location"
     api-key "key"]
    {weather/location-path
     {:method :get
      :status 200
      :content-type "application/json"
      :query-params {:apikey api-key}
      :body   (json/generate-string {:Key location-key})}}
    (let [response (weather/get-location uri api-key)]
      (is (= location-key (:Key response))))))

(deftest resolve-location-key-test
  (stubs/with-routes!
    [location-key "location"
     api-key "key"]
    {weather/location-path
     {:method :get
      :status 200
      :content-type "application/json"
      :query-params {:apikey api-key}
      :body   (json/generate-string {:Key location-key})}}
    (is (= location-key
           (:location-key
            (p.eql/process
             (pci/register {:base-url uri
                            :api-key api-key} [weather/location-key])
             {}
             [:location-key]))))))

(deftest get-weather-test
  (stubs/with-routes!
    [location-key "location"
     api-key "key"
     weather {:DailyForecasts
              [{:Date "2022-11-13T07:00:00+00:00"
                :Temperature {:Minimum {:Value 48.0 :Unit "F"}
                              :Maximum {:Value 63.0 :Unit "F"}}}]}]
    {(str weather/weather-path location-key)
     {:method :get
      :status 200
      :content-type "application/json"
      :query-params {:apikey api-key}
      :body   (json/generate-string weather)}}
    (let [response (weather/get-weather uri api-key location-key)]
      (is (= weather response)))))

(deftest resolve-get-weather-test
  (stubs/with-routes!
    [location-key "location"
     api-key "key"
     weather {:DailyForecasts
              [{:Date "2022-11-13T07:00:00+00:00"
                :Temperature {:Minimum {:Value 48.0 :Unit "F"}
                              :Maximum {:Value 63.0 :Unit "F"}}}]}]
    {weather/location-path
     {:method :get
      :status 200
      :content-type "application/json"
      :query-params {:apikey api-key}
      :body   (json/generate-string {:Key location-key})}
     (str weather/weather-path location-key)
     {:method :get
      :status 200
      :content-type "application/json"
      :query-params {:apikey api-key}
      :body   (json/generate-string weather)}}
    (is (= {:min 48.0
            :max 63.0}
           (p.eql/process
            (pci/register {:base-url uri
                           :api-key api-key} [weather/location-key
                                              weather/temperature])
            {}
            [:min :max])))))
