(ns dojo.core
  (:require 
    [ring.util.response :refer [response]]
    [chord.http-kit :refer [with-channel]]
    [clojure.core.async :refer [<! >! put! close! go-loop]]
    [compojure.core :refer [defroutes GET routes]]
    [compojure.handler :refer [api]]
    [compojure.route :refer [resources]]
    [hiccup.page :refer [html5 include-js]]
    [hiccup.element :refer [javascript-tag]]
    ))

(defn index-page []
  (html5
   [:head
    [:title "London Clojure Dojo January 2014"]]
    [:body 
      [:div#app]
      (include-js "//fb.me/react-0.8.0.js") ; only required in dev build
      (include-js "/out/goog/base.js") ; only required in dev build
      (include-js "/js/dojo.js")
      (javascript-tag "goog.require('dojo.client');") ; only required in dev build
      ]))

(defn ws-handler [req]
  (with-channel req ws
    (println "Opened connection from" (:remote-addr req))
    (go-loop []
      (when-let [{:keys [message]} (<! ws)]
        (println "Message received:" message)
        (>! ws (format "You said: '%s' at %s." message (java.util.Date.)))
        (recur)))))

(defn app-routes []
  (routes
    (GET "/" [] (response (index-page)))
    (GET "/ws" [] ws-handler)
    (resources "/js" {:root "js"})
    (resources "/out" {:root "out"}) ; only required in dev build
    ))

(defn webapp [] 
  (-> (app-routes)
      api))
