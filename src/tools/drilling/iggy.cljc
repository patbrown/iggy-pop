(ns tools.drilling.iggy
  (:require [exoscale.interceptor :as ix]
            [integrant.core :as ig]
            [integrant.repl :as igr]
            [medley.core]
            [tools.drilling.inxs :refer [exec]]))

(defn expand-module [module]
  (let [result (exec module [:ensure-module-requires
                             :module-requires->ig-ref])]
    (dissoc result :exoscale.interceptor/queue :exoscale.interceptor/stack)))

(defn expand-system [system]
  (exec :expanded-system system [:expand-modules
                                 :event-groups->bob
                                 :expanded-system->ig-system]))

(defn setup-system! [system-id]
  (igr/set-prep! (fn [] (let [config (expand-system system-id)]
                        (ig/load-namespaces config)
                        (ig/prep config)))))
