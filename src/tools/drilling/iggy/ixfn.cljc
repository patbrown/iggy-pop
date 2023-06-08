(ns tools.drilling.iggy.ixfn
  (:require [integrant.core :as ig]
            [tools.drilling.inxs.ixfn :refer [ixfn]]))

(ixfn ensure-module-requires
      (fn [{:as module :module/keys [requires env-blacklist?]}]
                 (let [ensured-requires (if env-blacklist?
                                          (if (nil? requires) [] requires)
                                          (into [:module/env] requires))]
                   (assoc module :module/requires ensured-requires))))

(ixfn module-require->ig-ref
      (fn [{:as module :module/keys [requires]}]
            (let [ig-ref-requires (if-not (nil? requires)
                                    (mapv (fn [ig-key] {ig-key (ig/ref ig-key)}) requires)
                                    requires)]
              (assoc module :module/requires ig-ref-requires))))

(ixfn expand-modules
      (fn [{:as sys :sys/keys [modules]}]
                 (assoc sys :sys/modules (mapv expand-module modules))))

(ixfn event-groups->bob
      (fn [{:as sys :sys/keys [modules]}]
                 (let [event-groups (remove empty? (map :module/event-group modules))
                       bulbo (apply (partial merge-with into) event-groups)]
                   (assoc sys :sys/bob bulbo))))

(ixfn expanded-system->ig-system
      (fn [{:as sys :sys/keys [modules]}]
                 (let [modules-mapped (apply merge
                                             (map (fn [{:as module :module/keys [requires]}]
                                                          (let [clean-module (dissoc module :module/requires)
                                                                requires-map (apply merge requires)]
                                                            {(:module/id module) (merge clean-module
                                                                                        requires-map)}))
                                                        modules))
                       module-env {:module/env (assoc (medley.core/filter-keys #(= "sys" (namespace %)) sys)
                                                      :sys/modules (vec (keys modules-mapped)))}]
                   (assoc sys :expanded-system (merge modules-mapped module-env)))))
