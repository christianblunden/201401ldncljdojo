(ns dojo.client
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:refer-clojure :exclude [chars])
  (:require
    [cljs.core.async :refer [<! >! put! close! timeout]] 
    [chord.client :refer [ws-ch]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

(enable-console-print!)

; debugging
; log to console
; (prn "test")
; trigger javascript breakpoint
; (js* "debugger;")

(defn save-message [e owner]
  (om/set-state! owner :message-to-send (.. e -target -value)))

(defn send-message [ws e owner]
  (put! ws (om/get-state owner :message-to-send))
  (om/set-state! owner :message-to-send "")
  (.preventDefault e))

(defn the-sender [{:keys [ws]} owner]
  (reify
    om/IInitState
    (init-state [_] {:message-to-send ""})
    om/IRender
    (render [_]
      (dom/form #js {:onSubmit #(send-message ws % owner)}
      (dom/h3 nil "Send message to server")
      (dom/input
          #js {:type "text"
               :ref "text-field"
               :value (om/get-state owner :message-to-send)
               :onChange #(save-message % owner)})))))

(defn message [{:keys [message]} owner]
  (om/component
    (dom/li nil message)))

(defn listen-for-mesages [{:keys [ws messages]}]
  (go-loop []
    (when-let [msg (<! ws)]
      (om/update! messages conj msg)
      (recur))))

(defn message-list [data owner]
  (reify
    om/IWillMount
    (will-mount [_] 
      (listen-for-mesages data))
    om/IRender
    (render [_]
      (dom/div nil
        (dom/h3 nil "Messages received from server")
        (dom/ul nil
          (om/build-all message (data :messages)))))))

(go 
  (let [ws (<! (ws-ch "ws://localhost:3000/ws")) ; establish web socket connection
        app-state (atom {:ws ws :messages [{:message "None Yet!"}]})] ; initialise global state of app
        (om/root 
          app-state
          (fn [app owner]
            (reify
              om/IRender
              (render [_]
                (dom/div nil
                  (om/build the-sender app)
                  (om/build message-list app)))))
          (.getElementById js/document "app"))))


