(ns dojo.client
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:refer-clojure :exclude [chars])
  (:require
    [cljs.core.async :refer [<! >! put! close! timeout]] 
    [chord.client :refer [ws-ch]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

(enable-console-print!)

(defn save-message [e owner]
  (om/set-state! owner :message-to-send (.. e -target -value)))

(defn send-message [ws e owner]
  (put! ws (om/get-state owner :message-to-send))
  (om/set-state! owner :message-to-send "")
  false)

(defn the-sender [{:keys [ws]} owner]
  (reify
    om/IInitState
    (init-state [_] {:message-to-send ""})
    om/IRender
    (render [_]
      (let [msg (om/get-state owner :message-to-send)]
        (dom/form #js {:onSubmit #(send-message ws % owner)}
        (dom/h3 nil "Send message to server")
        (dom/input
            #js {:type "text"
                 :ref "text-field"
                 :value msg
                 :onChange #(save-message % owner)}))))))

(defn message [{:keys [message]} owner]
  (om/component
    (dom/li nil message)))

(defn message-list [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (dom/h3 nil "Messages received from server")
        (dom/ul nil
          (om/build-all message (data :messages)))))))

(defn bind-msgs [{:keys [ws messages]}]
  (go-loop []
    (when-let [msg (<! ws)]
      (om/update! messages conj msg)
      (recur))))

(go 
  (let [ws (<! (ws-ch "ws://localhost:3000/ws"))
        app-state (atom {:ws ws :messages [{:message "None Yet!"}]})]
        (om/root 
          app-state
          (fn [app owner]
            (reify
              om/IWillMount
              (will-mount [_] 
                (bind-msgs app))
              om/IRender
              (render [_]
                (dom/div nil
                  (om/build the-sender app)
                  (om/build message-list app)))))
          (.getElementById js/document "info"))))


