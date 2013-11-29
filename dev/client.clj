(ns client
  (:require [org.httpkit.client :as http]
            [clojure.tools.logging :refer [debug info error]]
            [gniazdo.core :as ws])
  (:gen-class))

(defn signup [username, password]
  @(http/post "http://localhost/signup"
              {:form-params {"username" username
                             "password" password} ; just like query-params, except sent in the body
               :keepalive 3000 ; Keep the TCP connection for 3000s
               :timeout 1000 ; connection timeout and reading timeout
               :insecure? true ; Need to contact a server with an untrusted SSL cert?
               }
              (fn [{:keys [status headers body error opts]}]
                (if error
                  (info "Failed, exception is " error)
                  (info "Async HTTP POST: " status))
                ;(info "status" (str status))
                ;(info "headers" (with-out-str (clojure.pprint/pprint headers)))
                ;(info "body" (str body))
                ;(info "error" (str error))
                ;(info "opts" (with-out-str (clojure.pprint/pprint opts)))
                )))

(def socket
  (delay (info "creating socket") (ws/connect
                                    "ws://localhost/wss"
                                    :on-receive #(info 'received %)
                                    :on-connect #(info "connected" %)
                                    :on-binary #(info "binary" % %2 %3)
                                    :on-error #(error "error" %)
                                    :on-close #(info "closed" % %2))))

(defn -main
  "Starts the client"
  [& args]
  (info "starting up client ...")
  (info "args: " args)
  (set! *warn-on-reflection* true)

  (signup (first args) (second args)))

;(-main "yosi2" "bosi2")
;(dotimes [n 5]
;  (ws/send-msg @socket "{\"type\":\"blabla\",\"a\":123,\"transactionKey\":\"ASDSADASDASDWEQWEWQRFDFASDSADASD123DFASDAS234ASDASDASD\"}"))
;(ws/close @socket)
